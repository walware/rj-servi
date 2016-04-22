/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ECommons;

import de.walware.rj.RjException;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;
import de.walware.rj.server.RjsComConfig;
import de.walware.rj.server.RjsStatus;
import de.walware.rj.server.Server;
import de.walware.rj.server.client.AbstractRJComClient;
import de.walware.rj.server.client.AbstractRJComClientGraphicActions;
import de.walware.rj.server.client.FunctionCallImpl;
import de.walware.rj.server.client.RClientGraphicFactory;
import de.walware.rj.server.client.RGraphicCreatorImpl;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RGraphicCreator;
import de.walware.rj.services.RPlatform;
import de.walware.rj.services.RService;


/**
 * Client side {@link RServi} handler
 */
public class RServiImpl implements RServi, Externalizable {
	
	
	private class RServiComClient extends AbstractRJComClient {
		
		
		public RServiComClient() {
		}
		
		
		@Override
		protected void initGraphicFactory() {
			final Object graphicFactory = RjsComConfig.getProperty("rj.servi.graphicFactory");
			final Object actionsFactory = RjsComConfig.getProperty("rj.servi.comClientGraphicActionsFactory");
			if (graphicFactory instanceof RClientGraphicFactory) {
				setGraphicFactory((RClientGraphicFactory) graphicFactory,
						(actionsFactory instanceof AbstractRJComClientGraphicActions.Factory) ?
								((AbstractRJComClientGraphicActions.Factory) actionsFactory).create(
										this, getRHandle() ) : null );
			}
		}
		
		@Override
		protected void handleServerStatus(final RjsStatus serverStatus, final IProgressMonitor monitor) throws CoreException {
			switch (serverStatus.getCode()) {
			case 0:
				return;
			case Server.S_DISCONNECTED:
			case Server.S_LOST:
			case Server.S_STOPPED:
				break;
			case RjsStatus.ERROR:
				throw new CoreException(new Status(IStatus.ERROR, RServiUtil.RJ_SERVI_ID, "Server or IO error."));
			default:
				throw new IllegalStateException();
			}
			
			if (!isClosed()) {
				setClosed(true);
				handleStatus(new Status(IStatus.INFO, RServiUtil.RJ_SERVI_ID, "RServi is disconnected."), monitor);
			}
			throw new CoreException(new Status(IStatus.ERROR, RServiUtil.RJ_SERVI_ID, "RServi is closed."));
		}
		
		@Override
		protected void handleStatus(final Status status, final IProgressMonitor monitor) {
			if (!status.isOK()) {
				log(status);
			}
		}
		
		@Override
		protected void log(final IStatus status) {
			ECommons.getEnv().log(status);
		}
		
	}
	
	
	public static interface PoolRef extends Remote {
		void returnObject(long accessId) throws RjException, RemoteException;
	}
	
	
	private long accessId;
	private PoolRef poolRef;
	private RServiBackend backend;
	
	private final AbstractRJComClient rjs = new RServiComClient();
	private int rjsId;
	
	private Object rHandle;
	
	
	public RServiImpl(final long accessId, final PoolRef ref, final RServiBackend backend) {
		this.accessId = accessId;
		this.poolRef = ref;
		this.backend = backend;
		this.rjs.setServer(this.backend, 1);
	}
	
	public RServiImpl() {
	}
	
	
	public void setRHandle(final Object rHandle) {
		this.rHandle = rHandle;
	}
	
	public Object getRHandle() {
		return this.rHandle;
	}
	
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		this.accessId = in.readLong();
		this.poolRef = (PoolRef) in.readObject();
		this.backend = (RServiBackend) in.readObject();
		this.rjs.setServer(this.backend, 1);
	}
	
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeLong(this.accessId);
		out.writeObject(this.poolRef);
		out.writeObject(this.backend);
	}
	
	
	private void init() throws CoreException {
		this.rjsId = RjsComConfig.registerClientComHandler(this.rjs);
		final Map<String, Object> properties = new HashMap<>();
		this.rjs.initClient(this.rHandle, this, properties, this.rjsId);
		this.rjs.setRjsProperties(properties);
	}
	
	@Override
	public boolean isClosed() {
		return this.rjs.isClosed();
	}
	
	@Override
	public synchronized void close() throws CoreException {
		if (this.rjs.isClosed()) {
			throw new CoreException(new Status(IStatus.ERROR, RServiUtil.RJ_SERVI_ID, 0,
					"RServi is already closed.", null));
		}
		try {
			this.rjs.setClosed(true);
			this.poolRef.returnObject(this.accessId);
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RServiUtil.RJ_SERVI_ID, 0,
					"An error occurred when closing RServi instance.", e));
		}
		finally {
			this.poolRef = null;
			this.backend = null;
			RjsComConfig.unregisterClientComHandler(this.rjsId);
			this.rjs.disposeAllGraphics();
		}
	}
	
	
	@Override
	public RPlatform getPlatform() {
		return this.rjs.getRPlatform();
	}
	
	@Override
	public void evalVoid(final String command, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		this.rjs.evalVoid(command, null, monitor);
	}
	
	@Override
	public RObject evalData(final String command, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		return this.rjs.evalData(command, null, null, 0, RService.DEPTH_INFINITE, monitor);
	}
	
	@Override
	public RObject evalData(final String command, final String factoryId, final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		return this.rjs.evalData(command, null, factoryId, options, depth, monitor);
	}
	
	@Override
	public RObject evalData(final RReference reference, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		return this.rjs.evalData(reference, null, 0, -1, monitor);
	}
	
	@Override
	public RObject evalData(final RReference reference, final String factoryId, final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		return this.rjs.evalData(reference, factoryId, options, depth, monitor);
	}
	
	@Override
	public void assignData(final String expression, final RObject data, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		this.rjs.assignData(expression, data, null, monitor);
	}
	
	@Override
	public void downloadFile(final OutputStream out, final String fileName, final int options, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		this.rjs.downloadFile(out, fileName, options, monitor);
	}
	
	@Override
	public byte[] downloadFile(final String fileName, final int options, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		return this.rjs.downloadFile(fileName, options, monitor);
	}
	
	@Override
	public void uploadFile(final InputStream in, final long length, final String fileName, final int options, final IProgressMonitor monitor) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		this.rjs.uploadFile(in, length, fileName, options, monitor);
	}
	
	@Override
	public FunctionCall createFunctionCall(final String name) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		return new FunctionCallImpl(this.rjs, name, RObjectFactoryImpl.INSTANCE);
	}
	
	@Override
	public RGraphicCreator createRGraphicCreator(final int options) throws CoreException {
		if (this.rjsId == 0) {
			init();
		}
		return new RGraphicCreatorImpl(this, this.rjs, options);
	}
	
}

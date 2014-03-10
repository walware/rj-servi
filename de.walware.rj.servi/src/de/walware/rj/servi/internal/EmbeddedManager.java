/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import java.rmi.RemoteException;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.net.RMIRegistry;

import de.walware.rj.RjException;
import de.walware.rj.RjInitFailedException;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.servi.pool.EmbeddedRServiManager;
import de.walware.rj.servi.pool.RServiNodeFactory;


public class EmbeddedManager implements EmbeddedRServiManager, IDisposable {
	
	
	private class EmbeddedObject extends NodeHandler implements RServiImpl.PoolRef {
		
		@Override
		public void returnObject(final long accessId) throws RjException, RemoteException {
			returnRServi(accessId);
		}
		
	}
	
	
	private final String id;
	
	private final RMIRegistry registry;
	
	private final LocalNodeFactory factory;
	
	private EmbeddedObject handler;
	
	private boolean inUse;
	private long accessId;
	
	
	public EmbeddedManager(final String id, final RMIRegistry registry, final RServiNodeFactory factory) {
		if (id == null || registry == null) {
			throw new NullPointerException();
		}
		this.id = id;
		this.registry = registry;
		this.factory = (LocalNodeFactory) factory;
		
		Utils.preLoad();
	}
	
	
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public RServiNodeFactory getFactory() {
		return this.factory;
	}
	
	@Override
	public void dispose() {
		stop();
	}
	
	@Override
	public synchronized void start() throws RjException {
		final EmbeddedObject poolObj = new EmbeddedObject();
		try {
			this.factory.createNode(poolObj);
			this.handler = poolObj;
			ECommons.getEnv().addStoppingListener(this);
		}
		catch (final Throwable e) {
			ECommons.getEnv().log(new Status(IStatus.ERROR, RServiUtil.RJ_SERVI_ID, Messages.StartNode_error_message, e));
			throw new RjInitFailedException(Messages.StartEmbedded_pub_error_message,
					(e instanceof RjException) ? e : null);
		}
	}
	
	@Override
	public synchronized void stop() {
		if (this.handler == null) {
			return;
		}
		ECommons.getEnv().removeStoppingListener(this);
		if (this.inUse) {
			returnRServi(this.accessId);
			if (this.handler == null) {
				return;
			}
		}
		this.factory.stopNode(this.handler);
		this.handler = null;
	}
	
	@Override
	public synchronized RServi getRServi(final String name) throws NoSuchElementException, RjException {
		if (this.handler == null) {
			start();
		}
		if (this.inUse) {
			throw new NoSuchElementException(Messages.GetRServi_NoInstance_pub_Embedded_message);
		}
		try {
			this.handler.bindClient(name, "embedded");
		}
		catch (final Throwable e) {
			ECommons.getEnv().log(new Status(IStatus.ERROR, RServiUtil.RJ_SERVI_ID, Messages.BindClient_error_message, e));
			throw new RjException(Messages.GetRServi_pub_error_message);
		}
		this.inUse = true;
		return new RServiImpl(this.accessId, this.handler, this.handler.clientHandler);
	}
	
	private synchronized void returnRServi(final long accessId) {
		if (this.handler == null) {
			return;
		}
		if (this.accessId != accessId) {
			throw new IllegalStateException("Access id no longer valid.");
		}
		this.inUse = false;
		this.accessId++;
		try {
			this.handler.unbindClient();
		}
		catch (final Throwable e) {
			ECommons.getEnv().log(new Status(IStatus.ERROR, RServiUtil.RJ_SERVI_ID, Messages.UnbindClient_error_message, e));
			stop();
		}
	}
	
}

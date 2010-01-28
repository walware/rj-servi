/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/RJ-Project (www.walware.de/goto/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.internal;

import static de.walware.rj.server.srvext.ServerUtil.MISSING_ANSWER_STATUS;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.rj.RjException;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RReference;
import de.walware.rj.server.BinExchange;
import de.walware.rj.server.DataCmdItem;
import de.walware.rj.server.MainCmdC2SList;
import de.walware.rj.server.MainCmdItem;
import de.walware.rj.server.MainCmdS2CList;
import de.walware.rj.server.RjsComObject;
import de.walware.rj.server.RjsStatus;
import de.walware.rj.servi.RServi;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RPlatform;


/**
 * Client side {@link RServi} handler
 **/
public class RServiImpl implements RServi, Serializable {
	
	
	private static final long serialVersionUID = -4070642116039259332L;
	
	
	public static interface PoolRef extends Remote {
		void returnObject(long accessId) throws RjException, RemoteException;
	}
	
	
	private final long accessId;
	private PoolRef poolRef;
	private RServiBackend backend;
	
	private boolean closed;
	
	private final ReentrantLock runMainLock = new ReentrantLock();
	private final MainCmdC2SList runMainC2SList = new MainCmdC2SList();
	
	private final RPlatform rPlatform;
	
	
	public RServiImpl(final long accessId, final PoolRef ref, final RServiBackend backend, final RPlatform rPlatform) {
		this.accessId = accessId;
		this.poolRef = ref;
		this.backend = backend;
		this.rPlatform = rPlatform;
	}
	
	
	public synchronized void close() throws CoreException {
		if (this.closed) {
			throw new CoreException(new Status(IStatus.ERROR, Utils.PLUGIN_ID, 0,
					"RServi is already closed.", null));
		}
		try {
			this.closed = true;
			this.poolRef.returnObject(this.accessId);
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Utils.PLUGIN_ID, 0,
					"An error occurred when closing RServi instance.", e));
		}
		finally {
			this.poolRef = null;
			this.backend = null;
		}
	}
	
	private DataCmdItem runMainLoop(RjsComObject sendCom, final MainCmdItem sendItem, final IProgressMonitor monitor) throws CoreException {
		if (this.closed) {
			throw new CoreException(new Status(IStatus.ERROR, Utils.PLUGIN_ID, 0,
					"RServi is closed.", null));
		}
		DataCmdItem answer = null;
		this.runMainLock.lock();
		try {
			this.runMainC2SList.setObjects(sendItem);
			sendCom = this.runMainC2SList;
			RjsComObject receivedCom = this.backend.runMainLoop(sendCom);
			sendCom = null;
			WAIT_FOR_ANSWER: while (true) {
				COM_TYPE: switch (receivedCom.getComType()) {
				case RjsComObject.T_PING:
					sendCom = RjsStatus.OK_STATUS;
					break COM_TYPE;
				case RjsComObject.T_STATUS:
					final RjsStatus status = (RjsStatus) receivedCom;
					switch (status.getSeverity()) {
					case RjsStatus.OK:
						break COM_TYPE;
					case RjsStatus.INFO:
						if (status.getCode() == RjsStatus.CANCEL) {
							break WAIT_FOR_ANSWER;
						}
						break COM_TYPE;
					default:
						throw new RjException(status.getMessage());
					}
				case RjsComObject.T_MAIN_LIST:
					final MainCmdS2CList list = (MainCmdS2CList) receivedCom;
					answer = (DataCmdItem) list.getItems();
					break COM_TYPE;
				}
				receivedCom = this.backend.runMainLoop(sendCom);
				sendCom = null;
			}
			this.runMainC2SList.clear();
			return answer;
		}
		catch (final Exception e) {
			if (this.closed) {
				throw new CoreException(new Status(IStatus.ERROR, Utils.PLUGIN_ID, 0,
						"RServi is closed.", null));
			}
			throw new CoreException(new Status(IStatus.ERROR, Utils.PLUGIN_ID, 0,
					"An error when executing RServi command.", e));
		}
		finally {
			this.runMainLock.unlock();
		}
	}
	
	private RjsComObject runAsync(final RjsComObject com) throws CoreException {
		if (this.closed) {
			throw new CoreException(new Status(IStatus.ERROR, Utils.PLUGIN_ID, 0,
					"RServi is closed.", null));
		}
		try {
			return this.backend.runAsync(com);
		}
		catch (final Exception e) {
			if (this.closed) {
				throw new CoreException(new Status(IStatus.ERROR, Utils.PLUGIN_ID, 0,
						"RServi is closed.", null));
			}
			throw new CoreException(new Status(IStatus.ERROR, Utils.PLUGIN_ID, 0,
					"An error when executing RServi command.", e));
		}
	}
	
	
	public RPlatform getPlatform() {
		return this.rPlatform;
	}
	
	public void evalVoid(final String command, final IProgressMonitor monitor) throws CoreException {
		final DataCmdItem answer = runMainLoop(null, new DataCmdItem(DataCmdItem.EVAL_VOID, 0, command), monitor);
		if (answer == null || !answer.isOK()) {
			final RjsStatus status = (answer != null) ? answer.getStatus() : MISSING_ANSWER_STATUS;
			if (status.getSeverity() == RjsStatus.CANCEL) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			else {
				throw new CoreException(new Status(status.getSeverity(), Utils.PLUGIN_ID, status.getCode(),
						"Evaluation failed: " + status.getMessage(), null));
			}
		}
	}
	
	public RObject evalData(final String command, final IProgressMonitor monitor) throws CoreException {
		return evalData(command, null, 0, -1, monitor);
	}
	
	public RObject evalData(final String command, final String factoryId, final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		final byte checkedDepth = (depth < Byte.MAX_VALUE) ? (byte) depth : Byte.MAX_VALUE;
		final DataCmdItem answer = runMainLoop(null, new DataCmdItem(((options & RObjectFactory.F_ONLY_STRUCT) == RObjectFactory.F_ONLY_STRUCT) ?
				DataCmdItem.EVAL_STRUCT : DataCmdItem.EVAL_DATA, 0, checkedDepth, command, factoryId), monitor);
		if (answer == null || !answer.isOK()) {
			final RjsStatus status = (answer != null) ? answer.getStatus() : MISSING_ANSWER_STATUS;
			if (status.getSeverity() == RjsStatus.CANCEL) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			else {
				throw new CoreException(new Status(status.getSeverity(), Utils.PLUGIN_ID, status.getCode(),
						"Evaluation failed: " + status.getMessage(), null));
			}
		}
		return answer.getData();
	}
	
	public RObject evalData(final RReference reference, final IProgressMonitor monitor) throws CoreException {
		return evalData(reference, null, 0, -1, monitor);
	}
	
	public RObject evalData(final RReference reference, final String factoryId, final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		final byte checkedDepth = (depth < Byte.MAX_VALUE) ? (byte) depth : Byte.MAX_VALUE;
		final long handle = reference.getHandle();
		final DataCmdItem answer = runMainLoop(null, new DataCmdItem(((options & RObjectFactory.F_ONLY_STRUCT) == RObjectFactory.F_ONLY_STRUCT) ?
				DataCmdItem.RESOLVE_STRUCT : DataCmdItem.RESOLVE_DATA, 0, checkedDepth, Long.toString(handle), factoryId), monitor);
		if (answer == null || !answer.isOK()) {
			final RjsStatus status = (answer != null) ? answer.getStatus() : MISSING_ANSWER_STATUS;
			if (status.getSeverity() == RjsStatus.CANCEL) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			else {
				throw new CoreException(new Status(status.getSeverity(), Utils.PLUGIN_ID, status.getCode(),
						"Evaluation failed: " + status.getMessage(), null));
			}
		}
		return answer.getData();
	}
	
	public void assignData(final String expression, final RObject data, final IProgressMonitor monitor) throws CoreException {
		final DataCmdItem answer = runMainLoop(null, new DataCmdItem(DataCmdItem.ASSIGN_DATA, 0, expression, data), monitor);
		if (answer == null || !answer.isOK()) {
			final RjsStatus status = (answer != null) ? answer.getStatus() : MISSING_ANSWER_STATUS;
			if (status.getSeverity() == RjsStatus.CANCEL) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			else {
				throw new CoreException(new Status(status.getSeverity(), Utils.PLUGIN_ID, status.getCode(),
						"Assignment failed: " + status.getMessage(), null));
			}
		}
	}
	
	public void downloadFile(final OutputStream out, final String fileName, final int options, final IProgressMonitor monitor) throws CoreException {
		final BinExchange request = new BinExchange(out, fileName, this.backend, options);
		final BinExchange answer;
		try {
			answer = (BinExchange) runAsync(request);
		}
		finally {
			request.clear();
		}
		if (answer == null || !answer.isOK()) {
			final RjsStatus status = (answer != null) ? answer.getStatus() : MISSING_ANSWER_STATUS;
			if (status.getSeverity() == RjsStatus.CANCEL) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			else {
				throw new CoreException(new Status(status.getSeverity(), Utils.PLUGIN_ID, status.getCode(),
						"Downloading file failed: " + status.getMessage(), null));
			}
		}
	}
	
	public byte[] downloadFile(final String fileName, final int options, final IProgressMonitor monitor) throws CoreException {
		final BinExchange request = new BinExchange(fileName, this.backend, options);
		final BinExchange answer;
		try {
			answer = (BinExchange) runAsync(request);
		}
		finally {
			request.clear();
		}
		if (answer == null || !answer.isOK()) {
			final RjsStatus status = (answer != null) ? answer.getStatus() : MISSING_ANSWER_STATUS;
			if (status.getSeverity() == RjsStatus.CANCEL) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			else {
				throw new CoreException(new Status(status.getSeverity(), Utils.PLUGIN_ID, status.getCode(),
						"Downloading file failed: " + status.getMessage(), null));
			}
		}
		return answer.getBytes();
	}
	
	public void uploadFile(final InputStream in, final long length, final String fileName, final int options, final IProgressMonitor monitor) throws CoreException {
		final BinExchange request = new BinExchange(in, length, fileName, this.backend, options);
		final BinExchange answer;
		try {
			answer = (BinExchange) runAsync(request);
		}
		finally {
			request.clear();
		}
		if (answer == null || !answer.isOK()) {
			final RjsStatus status = (answer != null) ? answer.getStatus() : MISSING_ANSWER_STATUS;
			if (status.getSeverity() == RjsStatus.CANCEL) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			else {
				throw new CoreException(new Status(status.getSeverity(), Utils.PLUGIN_ID, status.getCode(),
						"Uploading file failed: " + status.getMessage(), null));
			}
		}
	}
	
	public FunctionCall createFunctionCall(final String name) {
		return new FunctionCallImpl(this, name);
	}
	
}

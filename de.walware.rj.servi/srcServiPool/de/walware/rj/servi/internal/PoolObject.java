/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/RJ-Project (www.walware.de/goto/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.internal;

import java.rmi.Remote;
import java.rmi.server.Unreferenced;

import de.walware.rj.RjException;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;


public class PoolObject extends NodeHandler implements RServiImpl.PoolRef, Unreferenced {
	
	
	final ObjectPoolItem item;
	final Stats.NodeEntry stats = new Stats.NodeEntry();
	
	Remote thisRemote;
	
	
	public PoolObject(final ObjectPoolItem item) {
		this.item = item;
	}
	
	
	public long getAccessId() {
		final long clientId = this.item.getClientId();
		if (clientId == -1L) {
			throw new IllegalAccessError();
		}
		return clientId;
	}
	
	public void returnObject(final long accessId) throws RjException {
		try {
			synchronized(this.item) {
				if (this.item.getClientId() != accessId) {
					throw new IllegalStateException("Access id no longer valid.");
				}
				this.item.invalidateClient();
			}
			this.item.getPool().returnObject(this.item);
		}
		catch (final Exception e) {
			Utils.logError("An unexpected error occurred when returning RServi instance.", e);
			throw new RjException("An unexpected error occurred when closing RServi instance. See server log for detail.");
		}
	}
	
	public void unreferenced() {
		synchronized (this.item) {
			if (this.item.getState() != ObjectPoolItem.State.LENT
					|| this.item.getClientId() == -1L) {
				return;
			}
			this.item.invalidateClient();
		}
		Utils.logInfo("The RServi instance is lent and unreferenced. It will be returned now.");
		try {
			this.item.getPool().returnObject(this.item);
		}
		catch (final Exception e) {
			Utils.logError("An unexpected error occurred when returning RServi instance.", e);
		}
	}
	
}

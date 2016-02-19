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

import java.rmi.Remote;
import java.rmi.server.Unreferenced;

import de.walware.rj.RjException;
import de.walware.rj.servi.acommons.pool.ObjectPool;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;


public class PoolObject extends NodeHandler implements RServiImpl.PoolRef, Unreferenced {
	
	
	final ObjectPoolItem poolItemData;
	final Stats.NodeEntry stats = new Stats.NodeEntry();
	
	Remote thisRemote;
	
	
	public PoolObject(final ObjectPoolItem item) {
		this.poolItemData = item;
	}
	
	
	public ObjectPoolItem getPoolItemData() {
		return this.poolItemData;
	}
	
	public void evict(final long timeoutMillis) {
		final ObjectPool pool = this.poolItemData.getPool();
		pool.evictObject(this.poolItemData, timeoutMillis);
	}
	
	public long getAccessId() {
		final long clientId = this.poolItemData.getClientId();
		if (clientId == -1L) {
			throw new IllegalAccessError();
		}
		return clientId;
	}
	
	@Override
	public void returnObject(final long accessId) throws RjException {
		try {
			synchronized(this.poolItemData) {
				if (this.poolItemData.getClientId() != accessId) {
					throw new IllegalStateException("Access id no longer valid.");
				}
				this.poolItemData.invalidateClient();
			}
			this.poolItemData.getPool().returnObject(this.poolItemData);
		}
		catch (final Exception e) {
			Utils.logError("An unexpected error occurred when returning RServi instance.", e);
			throw new RjException("An unexpected error occurred when closing RServi instance. See server log for detail.");
		}
	}
	
	@Override
	public void unreferenced() {
		synchronized (this.poolItemData) {
			if (this.poolItemData.getState() != ObjectPoolItem.State.LENT
					|| this.poolItemData.getClientId() == -1L) {
				return;
			}
			this.poolItemData.invalidateClient();
		}
		Utils.logInfo("The RServi instance is lent and unreferenced. It will be returned now.");
		try {
			this.poolItemData.getPool().returnObject(this.poolItemData);
		}
		catch (final Exception e) {
			Utils.logError("An unexpected error occurred when returning RServi instance.", e);
		}
	}
	
}

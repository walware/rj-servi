/*******************************************************************************
 * Copyright (c) 2009-2013 Stephan Wahlbrink (WalWare.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.pool;


import de.walware.ecommons.net.RMIAddress;

import de.walware.rj.RjException;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.internal.PoolObject;


public class PoolItem {
	
	
	public static enum State {
		
		INITIALIZING,
		IDLING,
		LENT,
		EVICTING,
		
	}
	
	
	private long creationTime;
	
	private State state;
	private long stateTime;
	
	private long usageCount;
	private long usageDuration;
	
	private final PoolObject object;
	
	private String client;
	
	
	public PoolItem(final Object data, final long stamp) {
		final ObjectPoolItem item = (ObjectPoolItem) data;
		synchronized(item) {
			this.creationTime = item.getCreationTime();
			switch(item.getState()) {
			case INITIALIZING:
				this.state = State.INITIALIZING;
				break;
			case IDLING:
				this.state = State.IDLING;
				break;
			case LENT:
				this.state = State.LENT;
				break;
			case EVICTING:
			case EVICTED:
				this.state = State.EVICTING;
				break;
			}
			this.stateTime = item.getStateTime();
			this.usageCount = item.getLentCount();
			this.usageDuration = item.getLentDuration();
			if (this.state == State.LENT) {
				this.usageDuration += Math.min(stamp - item.getStateTime(), 0L);
			}
			this.client = item.getClientLabel();
		}
		this.object = (PoolObject) item.getObject();
	}
	
	
	public long getCreationTime() {
		return this.creationTime;
	}
	
	public State getState() {
		return this.state;
	}
	
	public long getStateTime() {
		return this.stateTime;
	}
	
	public String getCurrentClientId() {
		return this.client;
	}
	
	public long getUsageCount() {
		return this.usageCount;
	}
	
	public long getUsageDuration() {
		return this.usageDuration;
	}
	
	public boolean isConsoleEnabled() {
		return this.object != null && this.object.isConsoleEnabled();
	}
	
	public void enableConsole(final String authConfig) throws RjException {
		if (this.object != null) {
			this.object.enableConsole(authConfig);
		}
	}
	
	public void disableConsole() throws RjException {
		if (this.object != null) {
			this.object.disableConsole();
		}
	}
	
	/**
	 * Returns the RMI address of the node.
	 * 
	 * @return the address of the node if available, otherwise <code>null</code>
	 * 
	 * @since 2.0
	 */
	public RMIAddress getAddress() {
		if (this.object != null) {
			return this.object.getAddress();
		}
		return null;
	}
	
	/**
	 * Returns the item data of the represented item.
	 * 
	 * @return the item data
	 * 
	 * @since 2.0
	 */
	public Object getPoolItemData() {
		if (this.object != null) {
			return this.object.getPoolItemData();
		}
		return null;
	}
	
	/**
	 * Evicts the pool item.
	 * 
	 * The specified timeout is used when the node is in use.
	 * 
	 * @param timeoutMillis the timeout in milliseconds
	 * 
	 * @since 2.0
	 */
	public void evict(final long timeoutMillis) {
		if (this.object != null) {
			this.object.evict(timeoutMillis);
		}
	}
	
}

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

package de.walware.rj.servi.pool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A fixed pool status including note states.
 * 
 * @param <N> type of node state items, see {@link #createNodeState(PoolItem)}
 * 
 * @since 2.0
 */
public abstract class PoolStatus<N> {
	
	
	private final RServiPoolManager.Counter NO_MANAGER = new RServiPoolManager.Counter();
	
	
	protected final PoolServer server;
	
	private long stamp;
	private RServiPoolManager.Counter counter;
	
	private List<N> nodeStates;
	
	
	public PoolStatus(final PoolServer server) {
		if (server == null) {
			throw new NullPointerException("server");
		}
		this.server = server;
	}
	
	
	protected long getStatusStamp() {
		return this.stamp;
	}
	
	public synchronized int getNumInUse() {
		check();
		return this.counter.numInUse;
	}
	
	public synchronized int getNumIdling() {
		check();
		return this.counter.numIdling;
	}
	
	public synchronized int getNumTotal() {
		check();
		return this.counter.numTotal;
	}
	
	public synchronized int getMaxInUse() {
		check();
		return this.counter.maxInUse;
	}
	
	public synchronized int getMaxIdling() {
		check();
		return this.counter.maxIdling;
	}
	
	public synchronized int getMaxTotal() {
		check();
		return this.counter.maxTotal;
	}
	
	
	public synchronized List<N> getNodeStates() {
		check();
		return this.nodeStates;
	}
	
	
	protected void check() {
	}
	
	
	protected void refresh(final RServiPoolManager manager, final long stamp) {
		final List<N> list;
		RServiPoolManager.Counter counter;
		
		if (manager == null) {
			counter = this.NO_MANAGER;
			list = Collections.emptyList();
		}
		else {
			final Object[] itemDatas = manager.getPoolItemsData();
			counter = manager.getCounter();
			list = new ArrayList<>();
			counter.numIdling = 0;
			counter.numInUse = 0;
			counter.numTotal = 0;
			for (final Object itemData : itemDatas) {
				final PoolItem item = createPoolItem(itemData, stamp);
				final N nodeState = createNodeState(item);
				switch (item.getState()) {
				case LENT:
					counter.numInUse++;
					break;
				case IDLING:
					counter.numIdling++;
					break;
				default:
					break;
				}
				counter.numTotal++;
				list.add(nodeState);
			}
		}
		
		this.stamp = stamp;
		this.counter = counter;
		this.nodeStates = list;
	}
	
	protected PoolItem createPoolItem(final Object itemData, final long stamp) {
		return new PoolItem(itemData, stamp);
	}
	
	protected abstract N createNodeState(PoolItem item);
	
}

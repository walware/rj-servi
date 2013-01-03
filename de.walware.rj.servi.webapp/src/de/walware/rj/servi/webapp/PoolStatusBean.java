/*******************************************************************************
 * Copyright (c) 2009-2013 WalWare/RJ-Project (www.walware.de/goto/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.webapp;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import de.walware.rj.servi.pool.RServiPoolManager;


public class PoolStatusBean {
	
	
	private long stamp;
	private List<PoolItemBean> nodes;
	private int numInUse;
	private int numIdling;
	private RServiPoolManager.Counter counter;
	
	private boolean forceRefresh;
	private boolean autoRefresh;
	
	
	public PoolStatusBean() {
	}
	
	
	@PostConstruct
	public void init() {
		load();
	}
	
	private void load() {
		final RServiPoolManager poolManager = FacesUtils.getPoolManager();
		if (poolManager == null) {
			FacesUtils.addErrorMessage(null, "The pool is not available.");
			return;
		}
		int inUse = 0;
		int idling = 0;
		this.stamp = System.currentTimeMillis();
		final Object itemsData[] = poolManager.getPoolItemsData();
		final List<PoolItemBean> items = new ArrayList<PoolItemBean>(itemsData.length);
		for (final Object data : itemsData) {
			final PoolItemBean item = new PoolItemBean(data, this.stamp);
			items.add(item);
			switch(item.getState()) {
			case LENT:
				inUse++;
				break;
			case IDLING:
				idling++;
				break;
			}
		}
		this.nodes = items;
		this.numInUse = inUse;
		this.numIdling = idling;
		this.counter = poolManager.getCounter();
		this.forceRefresh = false;
	}
	
	public int getNumInUse() {
		check();
		return this.numInUse;
	}
	
	public int getNumIdling() {
		check();
		return this.numIdling;
	}
	
	public int getNumTotal() {
		check();
		return this.numIdling + this.numInUse;
	}
	
	public int getMaxInUse() {
		check();
		return this.counter.maxInUse;
	}
	
	public int getMaxIdling() {
		check();
		return this.counter.maxIdling;
	}
	
	public int getMaxTotal() {
		check();
		return this.counter.maxTotal;
	}
	
	public long getStamp() {
		check();
		return this.stamp;
	}
	
	public List<PoolItemBean> getNodes() {
		check();
		return this.nodes;
	}
	
	private void check() {
		if (this.forceRefresh) {
			load();
		}
	}
	
	public void forceRefresh() {
		this.forceRefresh = true;
	}
	
	
	public String actionRefresh() {
		return null;
	}
	
	public String actionEnableAutoRefresh() {
		this.autoRefresh = true;
		return null;
	}
	
	public String actionDisableAutoRefresh() {
		this.autoRefresh = false;
		return null;
	}
	
	public void setAutoRefreshEnabled(final boolean enabled) {
		this.autoRefresh = enabled;
	}
	
	public boolean isAutoRefreshEnabled() {
		return this.autoRefresh;
	}
	
}

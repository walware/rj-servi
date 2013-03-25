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

package de.walware.rj.servi.webapp;

import javax.annotation.PostConstruct;

import de.walware.rj.servi.pool.PoolItem;
import de.walware.rj.servi.pool.PoolStatus;
import de.walware.rj.servi.pool.RServiPoolManager;


public class PoolStatusBean extends PoolStatus<PoolItemBean> {
	
	
	private boolean forceRefresh;
	private boolean autoRefresh;
	
	
	public PoolStatusBean() {
		super(FacesUtils.getPoolServer());
	}
	
	
	@PostConstruct
	public void init() {
		load();
	}
	
	private void load() {
		final long stamp = System.currentTimeMillis();
		final RServiPoolManager poolManager = this.server.getManager();
		if (poolManager == null) {
			FacesUtils.addErrorMessage(null, "The pool is currently not available.");
		}
		refresh(poolManager, stamp);
	}
	
	@Override
	protected PoolItem createPoolItem(final Object itemData, final long stamp) {
		return new PoolItemBean(itemData, stamp);
	}
	
	@Override
	protected PoolItemBean createNodeState(final PoolItem item) {
		return (PoolItemBean) item;
	}
	
	
	public synchronized long getStamp() {
		check();
		return super.getStatusStamp();
	}
	
	@Override
	protected void check() {
		if (this.forceRefresh) {
			load();
		}
	}
	
	public synchronized void forceRefresh() {
		this.forceRefresh = true;
	}
	
	
	public String actionRefresh() {
		return null;
	}
	
	public synchronized String actionEnableAutoRefresh() {
		this.autoRefresh = true;
		return null;
	}
	
	public synchronized String actionDisableAutoRefresh() {
		this.autoRefresh = false;
		return null;
	}
	
	public synchronized void setAutoRefreshEnabled(final boolean enabled) {
		this.autoRefresh = enabled;
	}
	
	public synchronized boolean isAutoRefreshEnabled() {
		return this.autoRefresh;
	}
	
}

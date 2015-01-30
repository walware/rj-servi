/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import java.util.Date;

import de.walware.ecommons.net.RMIAddress;

import de.walware.rj.servi.jmx.NodeStateMX;
import de.walware.rj.servi.pool.PoolItem;
import de.walware.rj.servi.pool.PoolItem.State;


public class MXNodeState implements NodeStateMX {
	
	
	private final PoolItem item;
	
	private final Date creationTime;
	private final Date stateTime;
	
	
	public MXNodeState(final PoolItem item) {
		this.item = item;
		
		this.creationTime = new Date(this.item.getCreationTime());
		this.stateTime = new Date(this.item.getStateTime());
	}
	
	
	@Override
	public State getState() {
		return this.item.getState();
	}
	
	@Override
	public Date getStateBeginTime() {
		return this.stateTime;
	}
	
	@Override
	public String getCurrentClientId() {
		return this.item.getCurrentClientId();
	}
	
	
	@Override
	public Date getCreationTime() {
		return this.creationTime;
	}
	
	@Override
	public long getUsageCount() {
		return this.item.getUsageCount();
	}
	
	@Override
	public long getUsageDuration() {
		return this.item.getUsageDuration();
	}
	
	@Override
	public String getRMIAddress() {
		final RMIAddress address = this.item.getAddress();
		return (address != null) ? address.getAddress() : "";
	}
	
}

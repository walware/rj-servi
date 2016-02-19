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

package de.walware.rj.servi.webapp;

import de.walware.ecommons.net.RMIAddress;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.PoolItem;
import de.walware.rj.servi.pool.PoolServer;


public class PoolItemBean extends PoolItem {
	
	
	public PoolItemBean(final Object data, final long stamp) {
		super(data, stamp);
	}
	
	
	public String getRMIAddress() {
		final RMIAddress address = getAddress();
		return (address != null) ? address.getAddress() : null;
	}
	
	
	public String actionEnableConsole() {
		try {
			super.enableConsole("none");
		}
		catch (final RjException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
		}
		return null;
	}
	
	public String actionDisableConsole() {
		try {
			super.disableConsole();
		}
		catch (final RjException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
		}
		return null;
	}
	
	public void actionStop() {
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		final PoolConfig config = new PoolConfig();
		poolServer.getPoolConfig(config);
		
		evict(config.getEvictionTimeout());
	}
	
	public void actionKill() {
		evict(0);
	}
	
}

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

import javax.annotation.PostConstruct;
import javax.management.OperationsException;
import javax.naming.directory.InvalidAttributeValueException;

import de.walware.rj.servi.pool.NetConfig;
import de.walware.rj.servi.pool.PoolServer;


public class NetConfigBean extends NetConfig {
	
	
	public NetConfigBean() {
	}
	
	
	@PostConstruct
	public void init() {
		FacesUtils.loadFromFile(this, false);
	}
	
	
	public String getEffectivePoolAddress() {
		final String address = getPoolAddress(this, FacesUtils.getPoolId());
		return (address != null) ? address : "";
	}
	
	
	public String actionLoadCurrent() {
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		synchronized (this) {
			poolServer.getNetConfig(this);
			FacesUtils.validate(this);
		}
		
		return RJWeb.NETCONFIG_NAV;
	}
	
	public String actionLoadDefaults() throws InvalidAttributeValueException {
		synchronized (this) {
			load(new NetConfigBean());
			FacesUtils.validate(this);
		}
		
		return RJWeb.NETCONFIG_NAV;
	}
	
	public String actionRestart() {
		final NetConfig config = new NetConfig(this);
		
		if (!FacesUtils.validate(config)) {
			return null;
		}
		
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		poolServer.setNetConfig(config);
		try {
			poolServer.restart();
		}
		catch (final OperationsException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
		}
		return null;
	}
	
	public String actionSave() {
		FacesUtils.saveToFile(this);
		return null;
	}
	
}

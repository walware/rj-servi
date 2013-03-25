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
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.PoolServer;


public class PoolConfigBean extends PoolConfig {
	
	
	public PoolConfigBean() {
	}
	
	
	@PostConstruct
	public void init() {
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		poolServer.getPoolConfig(this);
	}
	
	
	public String actionLoadCurrent() {
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		synchronized (this) {
			poolServer.getPoolConfig(this);
			FacesUtils.validate(this);
		}
		
		return RJWeb.POOLCONFIG_NAV;
	}
	
	public String actionLoadDefaults() {
		synchronized (this) {
			load(new PoolConfig());
			FacesUtils.validate(this);
		}
		
		return RJWeb.POOLCONFIG_NAV;
	}
	
	public String actionApply() {
		final PoolConfig config = new PoolConfig(this);
		
		if (!FacesUtils.validate(config)) {
			return null;
		}
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		poolServer.setPoolConfig(config);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration applied.", null));
		return null;
	}
	
	public String actionSaveAndApply() {
		final PoolConfig config = new PoolConfig(this);
		
		if (!FacesUtils.validate(config)) {
			return null;
		}
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		poolServer.setPoolConfig(config);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration applied.", null));
		
		FacesUtils.saveToFile(config);
		return null;
	}
	
}

/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
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
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.PoolServer;
import de.walware.rj.servi.pool.RServiNodeConfig;


public class NodeConfigBean extends RServiNodeConfig {
	
	
	public NodeConfigBean() {
	}
	
	
	@PostConstruct
	public void init() {
		actionLoadCurrent();
	}
	
	
	public synchronized String getRLibsVariable() {
		return getEnvironmentVariables().get("R_LIBS");
	}
	
	public synchronized void setRLibsVariable(final String value) {
		if (value != null && value.length() > 0) {
			getEnvironmentVariables().put("R_LIBS", value);
		}
		else {
			getEnvironmentVariables().remove("R_LIBS");
		}
	}
	
	
	public String actionLoadCurrent() {
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		synchronized (this) {
			poolServer.getNodeConfig(this);
			FacesUtils.validate(this);
		}
		
		return RJWeb.RCONFIG_NAV;
	}
	
	public String actionLoadDefaults() {
		synchronized (this) {
			load(new RServiNodeConfig());
			FacesUtils.validate(this);
		}
		
		return RJWeb.RCONFIG_NAV;
	}
	
	public String actionApply() {
		final RServiNodeConfig config = new RServiNodeConfig(this);
		
		if (!FacesUtils.validate(config)) {
			return null;
		}
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		try {
			poolServer.setNodeConfig(config);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration applied.", null));
		}
		catch (final RjException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
		}
		return null;
	}
	
	public synchronized String actionSaveAndApply() {
		final RServiNodeConfig config = new RServiNodeConfig(this);
		
		if (!FacesUtils.validate(config)) {
			return null;
		}
		final PoolServer poolServer = FacesUtils.getPoolServer();
		
		try {
			poolServer.setNodeConfig(config);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration applied.", null));
		}
		catch (final RjException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
			return null;
		}
		FacesUtils.saveToFile(config);
		return null;
	}
	
}

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

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.RServiPoolManager;


public class PoolConfigBean extends PoolConfig {
	
	
	private static final String FORM_UI = "pool_config";
	
	private static final String MAX_TOTAL_COUNT_UI = FORM_UI + ':' + MAX_TOTAL_COUNT_ID.replace('.', '_');
	private static final String MIN_IDLE_COUNT_UI = FORM_UI + ':' + MIN_IDLE_COUNT_ID.replace('.', '_');
	private static final String MAX_IDLE_COUNT_UI = FORM_UI + ':' + MAX_IDLE_COUNT_ID.replace('.', '_');
	private static final String MIN_IDLE_TIME_UI = FORM_UI + ':' + MIN_IDLE_TIME_ID.replace('.', '_');
	private static final String MAX_WAIT_TIME_UI = FORM_UI + ':' + MAX_WAIT_TIME_ID.replace('.', '_');
	private static final String MAX_USAGE_COUNT_UI = FORM_UI + ':' + MAX_USAGE_COUNT_ID.replace('.', '_');
	
	
	public PoolConfigBean() {
	}
	
	
	@PostConstruct
	public void init() {
		final RServiPoolManager poolManager = FacesUtils.getPoolManager();
		load(poolManager.getConfig());
	}
	
	
	public boolean validate() {
		boolean valid = true;
		if (getMinIdleCount() < 0) {
			FacesUtils.addErrorMessage(MIN_IDLE_COUNT_UI, "Value must be >= 0");
			valid = false;
		}
		if (getMaxTotalCount() < 1) {
			FacesUtils.addErrorMessage(MAX_TOTAL_COUNT_UI, "Value must be > 0.");
			valid = false;
		}
		if (getMaxIdleCount() < 0) {
			FacesUtils.addErrorMessage(MAX_IDLE_COUNT_UI, "Value must be >= 0.");
			valid = false;
		}
		if (getMinIdleCount() >= 0 && getMaxIdleCount() >= 0 && getMaxIdleCount() < getMinIdleCount()) {
			FacesUtils.addErrorMessage(MAX_IDLE_COUNT_UI, (new StringBuilder("Value must be >= ")).append(FacesUtils.getLabel(MIN_IDLE_COUNT_UI)).append(".").toString());
			valid = false;
		}
		if (getMinIdleTime() < 0L) {
			FacesUtils.addErrorMessage(MIN_IDLE_TIME_UI, "Value must be >= 0");
			valid = false;
		}
		if (getMaxWaitTime() < 0L && getMaxUsageCount() != -1) {
			FacesUtils.addErrorMessage(MAX_WAIT_TIME_UI, "Value must be >= 0 or == -1 (infinite)");
			valid = false;
		}
		if (getMaxUsageCount() < 1 && getMaxUsageCount() != -1) {
			FacesUtils.addErrorMessage(MAX_USAGE_COUNT_UI, "Value must be > 0 or == -1 (disable)");
			valid = false;
		}
		return valid;
	}
	
	public String actionLoadCurrent() {
		// init
		return RJWeb.POOLCONFIG_NAV;
	}
	
	public String actionLoadDefaults() {
		load(new PoolConfig());
		return RJWeb.POOLCONFIG_NAV;
	}
	
	public String actionApply() {
		if (!validate()) {
			return null;
		}
		final RServiPoolManager poolManager = FacesUtils.getPoolManager();
		poolManager.setConfig(this);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration applied.", null));
		return null;
	}
	
	public String actionSaveAndApply() {
		if (!validate()) {
			return null;
		}
		final RServiPoolManager poolManager = FacesUtils.getPoolManager();
		poolManager.setConfig(this);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration applied.", null));
		FacesUtils.saveToFile(this);
		return null;
	}
	
}

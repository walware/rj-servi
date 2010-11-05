/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/RJ-Project (www.walware.de/goto/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.webapp;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.RServiNodeConfig;
import de.walware.rj.servi.pool.RServiNodeFactory;
import de.walware.rj.servi.pool.RServiPoolManager;


public class NodeConfigBean extends RServiNodeConfig {
	
	
	private static final String FORM_UI = "r_config";
	
	private static final String R_HOME_UI = FORM_UI + ':' + R_HOME_ID.replace('.', '_');
	private static final String R_ARCH_UI = FORM_UI + ':' + R_ARCH_ID.replace('.', '_');
	private static final String JAVA_HOME_UI = FORM_UI + ':' + JAVA_HOME_ID.replace('.', '_');
	private static final String BASE_WD_UI = FORM_UI + ':' + BASE_WD_ID.replace('.', '_');
	
	private static final List<SelectItem> BITS_ITEMS = Arrays.asList(new SelectItem[] {
		new SelectItem(Integer.valueOf(32)), new SelectItem(Integer.valueOf(64))
	});
	
	
	public NodeConfigBean() {
	}
	
	
	@PostConstruct
	public void init() {
		actionLoadCurrent();
	}
	
	
	public List<SelectItem> getBitsItems() {
		return BITS_ITEMS;
	}
	
	public String getRLibsVariable() {
		return getEnvironmentVariables().get("R_LIBS");
	}
	
	public void setRLibsVariable(String value) {
		if (value != null && value.length() > 0) {
			getEnvironmentVariables().put("R_LIBS", value);
		}
		else {
			getEnvironmentVariables().remove("R_LIBS");
		}
	}
	
	public boolean validate() {
		boolean valid = true;
		final String rHome = getRHome();
		if (rHome != null && !new File(rHome).exists()) {
			FacesUtils.addErrorMessage(R_HOME_UI, "The directory does not exist.");
			valid = false;
		}
		final String javaHome = getJavaHome();
		if (javaHome != null && !new File(javaHome).exists()) {
			FacesUtils.addErrorMessage(JAVA_HOME_UI, "The directory does not exist.");
			valid = false;
		}
		final String baseWd = getBaseWorkingDirectory();
		if (baseWd != null && !new File(baseWd).exists()) {
			FacesUtils.addErrorMessage(BASE_WD_UI, "The directory does not exist.");
			valid = false;
		}
		return valid;
	}
	
	
	public String actionLoadCurrent() {
		final RServiPoolManager poolManager = FacesUtils.getPoolManager();
		if (poolManager == null) {
			FacesUtils.addErrorMessage(null, "The pool is not available.");
			return RJWeb.RCONFIG_NAV;
		}
		final RServiNodeFactory factory = poolManager.getFactories();
		load(factory.getConfig());
		return RJWeb.RCONFIG_NAV;
	}
	
	public String actionLoadDefaults() {
		load(new RServiNodeConfig());
		return RJWeb.RCONFIG_NAV;
	}
	
	public String actionApply() {
		if (!validate()) {
			return null;
		}
		final RServiPoolManager poolManager = FacesUtils.getPoolManager();
		final RServiNodeFactory factory = poolManager.getFactories();
		try {
			factory.setConfig(this);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration applied.", null));
		}
		catch (final RjException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
		}
		return null;
	}
	
	public String actionSaveAndApply() {
		if (!validate()) {
			return null;
		}
		final RServiPoolManager poolManager = FacesUtils.getPoolManager();
		final RServiNodeFactory factory = poolManager.getFactories();
		try {
			factory.setConfig(this);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration applied.", null));
		}
		catch (final RjException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
			return null;
		}
		FacesUtils.saveToFile(this);
		return null;
	}
	
}

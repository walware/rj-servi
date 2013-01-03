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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import de.walware.ecommons.net.RMIAddress;

import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.PropertiesBean;


public class OtherConfigBean implements PropertiesBean {
	
	public static final String HOSTADDRESS_ID = "host.address";
	public static final String REGISTRY_PORT_ID = "rmi_registry.address.port";
	public static final String REGISTRY_EMBED_ID = "rmi_registry.embed.enabled";
	
	private static final String FORM_UI = "other_config";
	
	private static final String HOSTADDRESS_UI = FORM_UI + ':' + HOSTADDRESS_ID.replace('.', '_');
	
	
	private String hostAddress;
	private String effectiveHostaddress;
	private int registryPort;
	private int effectiveRegsitryPort;
	private boolean registryEmbed;
	
	
	public OtherConfigBean() {
		setHostAddress(null);
		setRegistryPort(-1);
		setRegistryEmbed(true);
	}
	
	
	public String getBeanId() {
		return "other";
	}
	
	
	public void load(final OtherConfigBean templ) {
		setHostAddress(templ.hostAddress);
		setRegistryPort(templ.registryPort);
		setRegistryEmbed(templ.registryEmbed);
	}
	
	public void load(final Properties map) {
		String tmp = map.getProperty(HOSTADDRESS_ID);
		setHostAddress(tmp);
		tmp = map.getProperty(REGISTRY_PORT_ID);
		setRegistryPort((tmp == null || tmp.length() <= 0) ? -1 : Integer.parseInt(tmp));
		tmp = map.getProperty(REGISTRY_EMBED_ID);
		setRegistryEmbed((tmp == null || tmp.length() <= 0) ? false : Boolean.parseBoolean(tmp));
	}
	
	public void save(final Properties map) {
		map.put(HOSTADDRESS_ID, (this.hostAddress == null) ? "" : this.hostAddress);
		map.put(REGISTRY_PORT_ID, (this.registryPort <= 0) ? "" : Integer.toString(this.registryPort));
		map.put(REGISTRY_EMBED_ID, Boolean.toString(this.registryEmbed));
	}
	
	
	public String getHostAddress() {
		return this.hostAddress;
	}
	
	public void setHostAddress(final String address) {
		this.hostAddress = address;
		String effective = address;
		if (effective == null || effective.length() == 0) {
			effective = System.getProperty("java.rmi.server.hostname");
		}
		try {
			final InetAddress inet = (effective == null || effective.length() <= 0) ? InetAddress.getLocalHost() : InetAddress.getByName(effective);
			this.effectiveHostaddress = inet.getHostAddress();
		}
		catch (final Exception e) {
			this.effectiveHostaddress = null;
			String msg = e.getMessage();
			if (e instanceof UnknownHostException) {
				msg = "Unknown Host";
			}
			FacesUtils.addErrorMessage(HOSTADDRESS_UI, msg);
		}
	}
	
	public String getEffectiveHostaddress() {
		return this.effectiveHostaddress;
	}
	
	public int getRegistryPort() {
		return this.registryPort;
	}
	
	public void setRegistryPort(final int port) {
		this.registryPort = port;
		this.effectiveRegsitryPort = (port <= 0) ? 1099 : port;
	}
	
	public int getEffectiveRegistryPort() {
		return this.effectiveRegsitryPort;
	}
	
	public boolean getRegistryEmbed() {
		return this.registryEmbed;
	}
	
	public void setRegistryEmbed(final boolean embed) {
		this.registryEmbed = embed;
	}
	
	public String getEffectivePoolAddress() {
		if (validate()) {
			try {
				return new RMIAddress(this.effectiveHostaddress, this.effectiveRegsitryPort,
						PoolConfig.getPoolName(FacesUtils.getPoolId())).toString();
			}
			catch (UnknownHostException e) {}
			catch (MalformedURLException e) {
				// TODO 
			}
		}
		return "";
	}
	
	private boolean validate() {
		return (this.effectiveHostaddress != null && this.effectiveHostaddress.length() > 0);
	}
	
	
	@PostConstruct
	public void init() {
		FacesUtils.loadFromFile(this, false);
	}
	
	public String actionLoadCurrent() {
		FacesUtils.loadFromFile(this);
		return RJWeb.OTHERCONFIG_NAV;
	}
	
	public String actionLoadDefaults() {
		load(new OtherConfigBean());
		return RJWeb.OTHERCONFIG_NAV;
	}
	
	public String actionSave() {
		if (!validate()) {
			return null;
		}
		FacesUtils.saveToFile(this);
		return null;
	}
	
}

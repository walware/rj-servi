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

package de.walware.rj.servi.pool;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Properties;

import de.walware.ecommons.net.RMIAddress;


public class NetConfig implements PropertiesBean {
	
	
	public static final String BEAN_ID = "netconfig";
	
	
	public static String getPoolAddress(final NetConfig config, final String poolId) {
		final String hostAddress;
		final int registryPort;
		synchronized (config) {
			if (!config.validate(null)) {
				return null;
			}
			hostAddress = config.effectiveHostaddress;
			registryPort = config.effectiveRegistryPort;
		}
		return getPoolAddress(hostAddress, registryPort, poolId);
	}
	
	public static String getPoolAddress(final String hostAddress, final int registryPort,
			final String poolId) {
		try {
			return new RMIAddress(hostAddress, registryPort, PoolConfig.getPoolName(poolId))
					.toString();
		}
		catch (final UnknownHostException e) {}
		catch (final MalformedURLException e) {
		}
		return null; 
	}
	
	
	public static final String HOSTADDRESS_ID = "host.address";
	public static final String REGISTRY_PORT_ID = "rmi_registry.address.port";
	public static final String REGISTRY_EMBED_ID = "rmi_registry.embed.enabled";
	
	/**
	 * Property id if SSL is enabled
	 * 
	 * @see #setSSLEnabled(boolean)
	 * @since 2.0
	 */
	public static final String SSL_ENABLED_ID = "ssl.enabled";
	
	private static final String REGISTRY_HOST_DEFAULT = null;
	private static final int REGISTRY_PORT_DEFAULT = -1;
	private static final boolean REGISTRY_EMBED_DEFAULT = true;
	private static final boolean SSL_ENABLED_DEFAULT = false;
	
	
	private String hostAddress;
	private String effectiveHostaddress;
	private int registryPort;
	private int effectiveRegistryPort;
	
	private boolean registryEmbed;
	
	private boolean isSSLEnabled;
	
	
	public NetConfig() {
		loadDefaults();
	}
	
	public NetConfig(final NetConfig config) {
		load(config);
	}
	
	
	@Override
	public String getBeanId() {
		return BEAN_ID;
	}
	
	
	public synchronized void loadDefaults() {
		setHostAddress(REGISTRY_HOST_DEFAULT);
		setRegistryPort(REGISTRY_PORT_DEFAULT);
		setRegistryEmbed(REGISTRY_EMBED_DEFAULT);
		setSSLEnabled(SSL_ENABLED_DEFAULT);
	}
	
	public synchronized void load(final NetConfig templ) {
		setHostAddress(templ.hostAddress);
		setRegistryPort(templ.registryPort);
		setRegistryEmbed(templ.registryEmbed);
		setSSLEnabled(templ.isSSLEnabled);
	}
	
	@Override
	public synchronized void load(final Properties map) {
		loadDefaults();
		
		if (map != null) {
			{	final String s = map.getProperty(HOSTADDRESS_ID);
				if (s != null && !s.isEmpty()) {
					setHostAddress(s);
				}
			}
			{	final String s = map.getProperty(REGISTRY_PORT_ID);
				setRegistryPort((s != null && !s.isEmpty()) ?
						Integer.parseInt(s) : REGISTRY_PORT_DEFAULT );
			}
			{	final String s = map.getProperty(REGISTRY_EMBED_ID);
				setRegistryEmbed((s != null && !s.isEmpty()) ?
						Boolean.parseBoolean(s) : REGISTRY_EMBED_DEFAULT);
			}
			{	final String s = map.getProperty(SSL_ENABLED_ID);
				setSSLEnabled((s != null && !s.isEmpty()) ?
						Boolean.parseBoolean(s) : SSL_ENABLED_DEFAULT);
			}
		}
	}
	
	@Override
	public synchronized void save(final Properties map) {
		map.put(HOSTADDRESS_ID, (this.hostAddress == null) ? "" : this.hostAddress);
		map.put(REGISTRY_PORT_ID, (this.registryPort <= 0) ? "" : Integer.toString(this.registryPort));
		map.put(REGISTRY_EMBED_ID, Boolean.toString(this.registryEmbed));
		map.put(SSL_ENABLED_ID, Boolean.toString(this.isSSLEnabled));
	}
	
	
	public synchronized String getHostAddress() {
		return this.hostAddress;
	}
	
	public synchronized void setHostAddress(final String address) {
		this.hostAddress = address;
		this.effectiveHostaddress = null;
	}
	
	public synchronized String getEffectiveHostaddress() {
		return this.effectiveHostaddress;
	}
	
	public synchronized int getRegistryPort() {
		return this.registryPort;
	}
	
	public synchronized void setRegistryPort(final int port) {
		this.registryPort = port;
		this.effectiveRegistryPort = (port <= 0) ? 1099 : port;
	}
	
	public synchronized int getEffectiveRegistryPort() {
		return this.effectiveRegistryPort;
	}
	
	public synchronized boolean getRegistryEmbed() {
		return this.registryEmbed;
	}
	
	public synchronized void setRegistryEmbed(final boolean embed) {
		this.registryEmbed = embed;
	}
	
	/**
	 * Returns if SSL is enabled
	 * 
	 * @return <code>true</code> if SSL is enabled, otherwise <code>false</code>
	 */
	public synchronized boolean isSSLEnabled() {
		return this.isSSLEnabled;
	}
	
	/**
	 * Sets if SSL is enabled.
	 * 
	 * @param enable
	 * 
	 * @since 2.0
	 */
	public synchronized void setSSLEnabled(final boolean enable) {
		this.isSSLEnabled = enable;
	}
	
	
	@Override
	public synchronized boolean validate(final Collection<ValidationMessage> messages) {
		boolean valid = true;
		
		String effective = this.hostAddress;
		if (effective == null || effective.length() == 0) {
			effective = System.getProperty("java.rmi.server.hostname");
		}
		try {
			InetAddress inet = null;
			if (effective != null && !effective.isEmpty()) {
				inet = InetAddress.getByName(effective);
			}
			else {
				try {
					inet = InetAddress.getLocalHost();
				}
				catch (final UnknownHostException e) {}
				catch (final ArrayIndexOutOfBoundsException e) { /* JVM bug */ }
				if (inet == null) {
					inet = RMIAddress.LOOPBACK;
				}
			}
			this.effectiveHostaddress = inet.getHostAddress();
		}
		catch (final Exception e) {
			this.effectiveHostaddress = null;
			String msg = e.getMessage();
			if (e instanceof UnknownHostException) {
				msg = "Unknown Host";
			}
			if (messages != null) {
				messages.add(new ValidationMessage(HOSTADDRESS_ID, msg));
			}
			valid = false;
		}
		
		return valid;
	}
	
}

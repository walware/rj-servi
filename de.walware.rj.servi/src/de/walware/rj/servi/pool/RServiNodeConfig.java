/*******************************************************************************
 * Copyright (c) 2009 WalWare/RJ-Project (www.walware.de/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.pool;

import java.util.Properties;


public class RServiNodeConfig implements PropertiesBean {
	
	
	public static final String R_HOME_ID = "r_home.path";
	public static final String BITS_ID = "bits.num";
	public static final String JAVA_HOME_ID = "java_home.path";
	public static final String JAVA_ARGS_ID = "java_args.path";
	public static final String BASE_WD_ID = "base_wd.path";
	public static final String CONSOLE_ENABLED_ID = "debug_console.enabled";
	public static final String VERBOSE_ENABLED_ID = "debug_verbose.enabled";
	
	
	private String rHome;
	private int bits;
	
	private String javaHome;
	private String javaArgs;
	
	private String baseWd;
	
	private boolean enableConsole;
	private boolean enableVerbose;
	
	
	public RServiNodeConfig() {
		this.rHome = System.getenv("R_HOME");
		this.bits = 64;
		this.javaArgs = "-server";
	}
	
	
	public String getBeanId() {
		return "rconfig";
	}
	
	protected void load(final RServiNodeConfig templ) {
		this.rHome = templ.rHome;
		this.bits = templ.bits;
		this.javaHome = templ.javaHome;
		this.javaArgs = templ.javaArgs;
		this.baseWd = templ.baseWd;
		this.enableConsole = templ.enableConsole;
		this.enableVerbose = templ.enableVerbose;
	}
	
	public void load(final Properties map) {
		setRHome(map.getProperty(R_HOME_ID));
		setBits(Integer.parseInt(map.getProperty(BITS_ID, "64")));
		setJavaHome(map.getProperty(JAVA_HOME_ID));
		setJavaArgs(map.getProperty(JAVA_ARGS_ID));
		setBaseWorkingDirectory(map.getProperty(BASE_WD_ID));
		setEnableConsole(Boolean.parseBoolean(map.getProperty(CONSOLE_ENABLED_ID)));
		setEnableVerbose(Boolean.parseBoolean(map.getProperty(VERBOSE_ENABLED_ID)));
	}
	
	public void save(final Properties map) {
		map.setProperty(R_HOME_ID, this.rHome);
		map.setProperty(BITS_ID, Integer.toString(this.bits));
		map.setProperty(JAVA_HOME_ID, (this.javaHome != null) ? this.javaHome : "");
		map.setProperty(JAVA_ARGS_ID, this.javaArgs);
		map.setProperty(BASE_WD_ID, (this.baseWd != null) ? this.baseWd : "");
		map.setProperty(CONSOLE_ENABLED_ID, Boolean.toString(this.enableConsole));
		map.setProperty(VERBOSE_ENABLED_ID, Boolean.toString(this.enableVerbose));
	}
	
	public void setRHome(final String path) {
		this.rHome = path;
	}
	
	public String getRHome() {
		return this.rHome;
	}
	
	public void setBaseWorkingDirectory(final String path) {
		this.baseWd = (path != null && path.trim().length() > 0) ? path : null;
	}
	
	public String getBaseWorkingDirectory() {
		return this.baseWd;
	}
	
	public void setBits(final int bits) {
		this.bits = bits;
	}
	
	public int getBits() {
		return this.bits;
	}
	
	public String getJavaHome() {
		return this.javaHome;
	}
	
	public void setJavaHome(final String javaHome) {
		this.javaHome = (javaHome != null && javaHome.trim().length() > 0) ? javaHome : null;
	}
	
	public String getJavaArgs() {
		return this.javaArgs;
	}
	
	public void setJavaArgs(final String args) {
		this.javaArgs = (args != null) ? args : "";
	}
	
	public boolean getEnableConsole() {
		return this.enableConsole;
	}
	
	public void setEnableConsole(final boolean enable) {
		this.enableConsole = enable;
	}
	
	public boolean getEnableVerbose() {
		return this.enableVerbose;
	}
	
	public void setEnableVerbose(final boolean enable) {
		this.enableVerbose = enable;
	}
	
}

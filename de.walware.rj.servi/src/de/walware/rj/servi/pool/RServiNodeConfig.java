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

package de.walware.rj.servi.pool;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import de.walware.rj.servi.internal.Utils;


/**
 * Configuration for an R node (of pool or embedded).
 */
public class RServiNodeConfig implements PropertiesBean {
	
	
	public static final String BEAN_ID = "rconfig";
	
	public static final String R_HOME_ID = "r_home.path";
	
	public static final String R_ARCH_ID = "r_arch.code";
	
	public static final String JAVA_HOME_ID = "java_home.path";
	
	public static final String JAVA_ARGS_ID = "java_cmd.args";
	private static final String JAVA_ARGS_OLD_ID = "java_args.path";
	
	public static final String NODE_ENVIRONMENT_VARIABLES_PREFIX = "node_environment.variables.";
	public static final String NODE_ARGS_ID = "node_cmd.args";
	
	public static final String BASE_WD_ID = "base_wd.path";
	
	/**
	 * Property id for R startup snippet
	 * 
	 * @see #setRStartupSnippet(String)
	 * @since 0.5
	 */
	public static final String R_STARTUP_SNIPPET_ID = "r_startup.snippet";
	
	public static final String CONSOLE_ENABLED_ID = "debug_console.enabled";
	
	public static final String VERBOSE_ENABLED_ID = "debug_verbose.enabled";
	
	/**
	 * Property id for timeout of start/stop of nodes
	 * 
	 * @see #setStartStopTimeout(long)
	 * @since 2.0
	 */
	public static final String STARTSTOP_TIMEOUT__ID = "startstop_timeout.millis";
	
	private static final long STARTSTOP_TIMEOUT_DEFAULT = 30 * 1000;
	
	
	private String rHome;
	private String rArch;
	
	private String javaHome;
	private String javaArgs;
	
	private final Map<String, String> environmentVariables = new HashMap<String, String>();
	private String nodeArgs;
	
	private String baseWd;
	
	private String rStartupSnippet;
	
	private boolean enableConsole;
	private boolean enableVerbose;
	
	private long startStopTimeout;
	
	
	public RServiNodeConfig() {
		this.rHome = System.getenv("R_HOME");
		this.rArch = System.getenv("R_ARCH");
		this.javaArgs = "-server";
		this.nodeArgs = "";
		this.rStartupSnippet = "";
		this.startStopTimeout = STARTSTOP_TIMEOUT_DEFAULT;
	}
	
	public RServiNodeConfig(final RServiNodeConfig config) {
		this();
		synchronized (config) {
			load(config);
		}
	}
	
	
	@Override
	public String getBeanId() {
		return BEAN_ID;
	}
	
	public synchronized void load(final RServiNodeConfig templ) {
		this.rHome = templ.rHome;
		this.rArch = templ.rArch;
		this.javaHome = templ.javaHome;
		this.javaArgs = templ.javaArgs;
		this.environmentVariables.clear();
		this.environmentVariables.putAll(templ.environmentVariables);
		this.nodeArgs = templ.nodeArgs;
		this.baseWd = templ.baseWd;
		this.rStartupSnippet = templ.rStartupSnippet;
		this.enableConsole = templ.enableConsole;
		this.enableVerbose = templ.enableVerbose;
		this.startStopTimeout = templ.startStopTimeout;
	}
	
	@Override
	public synchronized void load(final Properties map) {
		setRHome(map.getProperty(R_HOME_ID));
		setRArch(map.getProperty(R_ARCH_ID));
		setJavaHome(map.getProperty(JAVA_HOME_ID));
		setJavaArgs(map.getProperty(JAVA_ARGS_ID));
		if (this.javaArgs.length() == 0) {
			setJavaArgs(map.getProperty(JAVA_ARGS_OLD_ID));
		}
		this.environmentVariables.clear();
		final int prefixLength = NODE_ENVIRONMENT_VARIABLES_PREFIX.length();
		for (final Entry<Object, Object> p : map.entrySet()) {
			final String name = (String) p.getKey();
			if (name != null && name.length() > prefixLength
					&& name.startsWith(NODE_ENVIRONMENT_VARIABLES_PREFIX)
					&& p.getValue() instanceof String) {
				this.environmentVariables.put(name.substring(prefixLength), (String) p.getValue());
			}
		}
		setNodeArgs(map.getProperty(NODE_ARGS_ID));
		setBaseWorkingDirectory(map.getProperty(BASE_WD_ID));
		setRStartupSnippet(map.getProperty(R_STARTUP_SNIPPET_ID));
		setEnableConsole(Boolean.parseBoolean(map.getProperty(CONSOLE_ENABLED_ID)));
		setEnableVerbose(Boolean.parseBoolean(map.getProperty(VERBOSE_ENABLED_ID)));
		{	final String s = map.getProperty(STARTSTOP_TIMEOUT__ID);
			this.startStopTimeout = ((s != null) ? Long.parseLong(s) : STARTSTOP_TIMEOUT_DEFAULT);
		}
	}
	
	@Override
	public synchronized void save(final Properties map) {
		Utils.setProperty(map, R_HOME_ID, this.rHome);
		Utils.setProperty(map, R_ARCH_ID, this.rArch);
		Utils.setProperty(map, JAVA_HOME_ID, this.javaHome);
		Utils.setProperty(map, JAVA_ARGS_ID, this.javaArgs);
		for (final Entry<String, String> variable : this.environmentVariables.entrySet()) {
			map.setProperty(NODE_ENVIRONMENT_VARIABLES_PREFIX + variable.getKey(), variable.getValue());
		}
		Utils.setProperty(map, NODE_ARGS_ID, this.nodeArgs);
		Utils.setProperty(map, BASE_WD_ID, this.baseWd);
		Utils.setProperty(map, R_STARTUP_SNIPPET_ID, this.rStartupSnippet);
		Utils.setProperty(map, CONSOLE_ENABLED_ID, Boolean.toString(this.enableConsole));
		Utils.setProperty(map, VERBOSE_ENABLED_ID, Boolean.toString(this.enableVerbose));
		Utils.setProperty(map, STARTSTOP_TIMEOUT__ID, Long.toString(this.startStopTimeout));
	}
	
	public synchronized void setRHome(final String path) {
		this.rHome = path;
	}
	
	public synchronized String getRHome() {
		return this.rHome;
	}
	
	public synchronized void setRArch(final String code) {
		this.rArch = code;
	}
	
	public synchronized String getRArch() {
		return this.rArch;
	}
	
	public synchronized String getJavaHome() {
		return this.javaHome;
	}
	
	public synchronized void setJavaHome(final String javaHome) {
		this.javaHome = (javaHome != null && javaHome.trim().length() > 0) ? javaHome : null;
	}
	
	public synchronized String getJavaArgs() {
		return this.javaArgs;
	}
	
	public synchronized void setJavaArgs(final String args) {
		this.javaArgs = (args != null) ? args : "";
	}
	
	/**
	 * Additional environment variables for the R process.
	 * 
	 * @return a name - value map of the environment variables
	 */
	public synchronized Map<String, String> getEnvironmentVariables() {
		return this.environmentVariables;
	}
	
	public synchronized void addToClasspath(final String entry) {
		String cp = this.environmentVariables.get("CLASSPATH");
		if (cp != null) {
			cp += File.pathSeparatorChar + entry;
		}
		else {
			cp = entry;
		}
		this.environmentVariables.put("CLASSPATH", cp);
	}
	
	public synchronized String getNodeArgs() {
		return this.nodeArgs;
	}
	
	public synchronized void setNodeArgs(final String args) {
		this.nodeArgs = (args != null) ? args : "";
	}
	
	public synchronized void setBaseWorkingDirectory(final String path) {
		this.baseWd = (path != null && path.trim().length() > 0) ? path : null;
	}
	
	public synchronized String getBaseWorkingDirectory() {
		return this.baseWd;
	}
	
	/**
	 * Returns the R code snippet to run at startup of a node.
	 * 
	 * @return the code
	 * 
	 * @see #setRStartupSnippet(String)
	 * @since 0.5
	 */
	public synchronized String getRStartupSnippet() {
		return this.rStartupSnippet;
	}
	
	/**
	 * Sets the R code snippet to run at startup of a node.
	 * <p>
	 * Typical use case is to load required R packages. The default is an empty snippet.
	 * If the execution of the code throws an error, the startup of the node is canceled.</p>
	 * 
	 * @param code the R code to run
	 * 
	 * @since 0.5
	 */
	public synchronized void setRStartupSnippet(final String code) {
		this.rStartupSnippet = (code != null) ? code : "";
	}
	
	public synchronized boolean getEnableConsole() {
		return this.enableConsole;
	}
	
	public synchronized void setEnableConsole(final boolean enable) {
		this.enableConsole = enable;
	}
	
	public synchronized boolean getEnableVerbose() {
		return this.enableVerbose;
	}
	
	public synchronized void setEnableVerbose(final boolean enable) {
		this.enableVerbose = enable;
	}
	
	/**
	 * Returns the timeout of start/stop of nodes
	 * 
	 * @return the timeout in milliseconds
	 * 
	 * @since 2.0
	 */
	public long getStartStopTimeout() {
		return this.startStopTimeout;
	}
	
	/**
	 * Sets the timeout of start/stop of nodes
	 * 
	 * @param milliseconds the timeout in milliseconds
	 * 
	 * @since 2.0
	 */
	public void setStartStopTimeout(final long milliseconds) {
		this.startStopTimeout = milliseconds;
	}
	
	
	@Override
	public synchronized boolean validate(final Collection<ValidationMessage> messages) {
		boolean valid = true;
		
		if (this.rHome != null && !new File(this.rHome).exists()) {
			if (messages != null) {
				messages.add(new ValidationMessage(R_HOME_ID, "The directory does not exist."));
			}
			valid = false;
		}
		if (this.javaHome != null && !new File(this.javaHome).exists()) {
			if (messages != null) {
				messages.add(new ValidationMessage(JAVA_HOME_ID, "The directory does not exist."));
			}
			valid = false;
		}
		if (this.baseWd != null && !new File(this.baseWd).exists()) {
			if (messages != null) {
				messages.add(new ValidationMessage(BASE_WD_ID, "The directory does not exist."));
			}
			valid = false;
		}
		
		if (this.startStopTimeout != -1 && this.startStopTimeout < 0) {
			if (messages != null) {
				messages.add(new ValidationMessage(STARTSTOP_TIMEOUT__ID, "Value must be > 0 or -1 (infinite)."));
			}
			valid = false;
		}
		
		return valid;
	}
	
}

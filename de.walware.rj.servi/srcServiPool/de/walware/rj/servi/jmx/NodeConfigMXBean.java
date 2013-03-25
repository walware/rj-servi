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

package de.walware.rj.servi.jmx;

import java.util.Map;

import javax.management.OperationsException;


public interface NodeConfigMXBean {
	
	
	@DisplayName("Java home (path; empty \u21d2 same as the server)")
	String getJavaHome();
	void setJavaHome(String javaHome);
	
	@DisplayName("Java arguments")
	String getJavaArgs();
	void setJavaArgs(String args);
	
	@DisplayName("R home / R_HOME (path)")
	String getRHome();
	void setRHome(String path);
	
	@DisplayName("Architecture of binaries / R_ARCH (empty \u21d2 autodetection)")
	String getRArch();
	void setRArch(String code);
	
	@DisplayName("Environment variables (like R_LIBS)")
	Map<String, String> getEnvironmentVariables();
	@DisplayName("Add/Set/Remove an environment variable (like R_LIBS)")
	void setEnvironmentVariable(String name, String value);
	
	@DisplayName("Working directory (path; empty \u21d2 default temp dir)")
	String getBaseWorkingDirectory();
	void setBaseWorkingDirectory(String path);
	
	@DisplayName("R startup snippet (complete R command per line)")
	String getRStartupSnippet();
	void setRStartupSnippet(String code);
	
	@DisplayName("Timeout when starting/stopping node (millis)")
	long getStartStopTimeout();
	void setStartStopTimeout(long milliseconds);
	
	@DisplayName("Enable debug console")
	boolean getEnableConsole();
	void setEnableConsole(boolean enable);
	
	@DisplayName("Enable verbose logging")
	boolean getEnableVerbose();
	void setEnableVerbose(boolean enable);
	
	
	@DisplayName("Apply the current configuration")
	void apply() throws OperationsException;
	
	@DisplayName("Reset current changes / load actual configuration")
	void loadActual() throws OperationsException;
	@DisplayName("Load the default configuration")
	void loadDefault() throws OperationsException;
	@DisplayName("Load the saved configuration")
	void loadSaved() throws OperationsException;
	@DisplayName("Save the current configuration")
	void save() throws OperationsException;
	
}

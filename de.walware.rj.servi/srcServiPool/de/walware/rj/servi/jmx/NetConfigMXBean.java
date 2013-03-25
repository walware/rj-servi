/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink (WalWare.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.jmx;

import javax.management.OperationsException;


public interface NetConfigMXBean {
	
	
	@DisplayName("Host address (IP/name)")
	String getHostAddress();
	void setHostAddress(String address);
	
	@DisplayName("RMI registry port (-1 \u21d2 default)")
	int getRegistryPort();
	void setRegistryPort(int port);
	
	@DisplayName("Start embedded registry")
	boolean getRegistryEmbed();
	void setRegistryEmbed(boolean embed);
	
	@DisplayName("Enable SSL")
	boolean isSSLEnabled();
	void setSSLEnabled(boolean enable);
	
	
	@DisplayName("Load the default configuration")
	void loadDefault() throws OperationsException;
	@DisplayName("Load the saved configuration")
	void loadSaved() throws OperationsException;
	@DisplayName("Save the current configuration")
	void save() throws OperationsException;
	
}

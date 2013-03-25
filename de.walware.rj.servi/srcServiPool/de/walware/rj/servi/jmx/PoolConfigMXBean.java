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

import javax.management.OperationsException;


public interface PoolConfigMXBean {
	
	
	@DisplayName("Max total nodes (count)")
	int getMaxUsageCount();
	void setMaxUsageCount(int count);
	
	@DisplayName("Min idle nodes (count)")
	long getMaxWaitTime();
	void setMaxWaitTime(long milliseconds);
	
	@DisplayName("Max idle nodes (count)")
	long getMinIdleTime();
	void setMinIdleTime(long milliseconds);
	
	@DisplayName("Min node idle time (millisec)")
	int getMaxIdleCount();
	void setMaxIdleCount(int count);
	
	@DisplayName("Max wait time (millisec)")
	int getMinIdleCount();
	void setMinIdleCount(int count);
	
	@DisplayName("Max node reuse (count)")
	int getMaxTotalCount();
	void setMaxTotalCount(int count);
	
	@DisplayName("Timeout when evicting node in use (millisec)")
	long getEvictionTimeout();
	void setEvictionTimeout(long milliseconds);
	
	
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

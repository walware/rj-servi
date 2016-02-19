/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.jmx;

import javax.management.OperationsException;


public interface PoolServerMXBean {
	
	
	String getId();
	
	String getPoolAddress();
	
	PoolStatusMX getPoolStatus();
	
	boolean isPoolNodeManagementEnabled();
	void setPoolNodeManagementEnabled(boolean enable);
	
	
	void start() throws OperationsException;
	void stop() throws OperationsException;
	void restart() throws OperationsException;
	
}

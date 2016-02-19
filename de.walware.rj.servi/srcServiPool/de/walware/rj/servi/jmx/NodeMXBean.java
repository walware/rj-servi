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

import java.util.Date;

import javax.management.OperationsException;


public interface NodeMXBean {
	
	
	String getId();
	Date getCreationTime();
	
	NodeStateMX getState();
	
	boolean isConsoleEnabled();
	void setConsoleEnabled(boolean enable) throws OperationsException;
	
	
	@DisplayName("Stop using the default timeout")
	void stop() throws OperationsException;
	@DisplayName("Stop using given timeout")
	void stop(long timeoutMillis) throws OperationsException;
	@DisplayName("Stop using timeout 0")
	void kill() throws OperationsException;
	
}

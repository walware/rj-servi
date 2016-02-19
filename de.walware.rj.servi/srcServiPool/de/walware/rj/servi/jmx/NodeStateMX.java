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

import de.walware.rj.servi.pool.PoolItem.State;


public interface NodeStateMX {
	
	
	Date getCreationTime();
	
	State getState();
	
	Date getStateBeginTime();
	
	String getCurrentClientId();
	
	long getUsageCount();
	
	long getUsageDuration();
	
	String getRMIAddress();
	
}

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
import java.util.List;


public interface PoolStatusMX {
	
	
	Date getStatusTime();
	
	int getNumInUse();
	int getNumIdling();
	int getNumTotal();
	
	int getMaxInUse();
	int getMaxIdling();
	int getMaxTotal();
	
	List<NodeStateMX> getNodeStates();
	
}

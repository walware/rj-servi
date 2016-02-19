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

import de.walware.ecommons.net.RMIRegistry;

import de.walware.rj.RjInvalidConfigurationException;


public interface RServiNodeFactory {
	
	
	void setRegistry(RMIRegistry rmiRegistry);
	
	RServiNodeConfig getConfig();
	void setConfig(RServiNodeConfig config) throws RjInvalidConfigurationException;
	
}

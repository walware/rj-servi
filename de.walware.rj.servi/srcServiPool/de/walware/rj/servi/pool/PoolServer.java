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

package de.walware.rj.servi.pool;

import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.RJContext;
import de.walware.rj.servi.jmx.PoolServerMXBean;


/**
 * @since 2.0
 */
public interface PoolServer extends PoolServerMXBean {
	
	
	RJContext getRJContext();
	String getJMBaseName();
	
	void getNetConfig(NetConfig config);
	void setNetConfig(NetConfig config);
	
	void getPoolConfig(PoolConfig config);
	void setPoolConfig(PoolConfig config);
	
	void getNodeConfig(RServiNodeConfig config);
	void setNodeConfig(RServiNodeConfig config) throws RjInvalidConfigurationException;
	
	RServiPoolManager getManager();
	
	
}

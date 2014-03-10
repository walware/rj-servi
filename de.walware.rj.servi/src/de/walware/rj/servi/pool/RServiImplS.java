/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.pool;

import static de.walware.rj.server.srvext.ServerUtil.RJ_DATA_ID;
import static de.walware.rj.server.srvext.ServerUtil.RJ_SERVER_ID;
import static de.walware.rj.servi.RServiUtil.RJ_CLIENT_ID;
import static de.walware.rj.servi.RServiUtil.RJ_SERVI_ID;

import de.walware.ecommons.net.RMIRegistry;

import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.RJContext;
import de.walware.rj.servi.internal.EmbeddedManager;
import de.walware.rj.servi.internal.LocalNodeFactory;
import de.walware.rj.servi.internal.PoolManager;


/**
 * Factory for RServi objects with minimal requirements and libraries in a given libPath (Server, SWT only)
 */
public class RServiImplS {
	
	/**
	 * Creates a node factory establishing RServi nodes on the local system.
	 * 
	 * This method is intended for RServi pools when utilizing the default bundles.
	 * 
	 * @param poolId the id of the pool or application
	 * @param libDirPath the absolute path where the libraries are located in
	 * @return a node factory
	 * @throws RjInvalidConfigurationException
	 */
	public static RServiNodeFactory createLocalNodeFactory(final String poolId, final RJContext context)
			throws RjInvalidConfigurationException {
		final String[] libIds = new String[] {
				RJ_DATA_ID, RJ_SERVER_ID, RJ_SERVI_ID, RJ_CLIENT_ID, "de.walware.rj.services.eruntime" };
		context.searchRJLibs(libIds);
		
		return new LocalNodeFactory(poolId, context, libIds);
	}
	
	/**
	 * Creates an RServi pool.
	 * 
	 * @param poolId the id of the pool
	 * @param registry a handler for the RMI registry to use
	 * @return the pool manager
	 */
	public static RServiPoolManager createPool(final String poolId, final RMIRegistry registry) {
		return new PoolManager(poolId, registry);
	}
	
	/**
	 * Creates an embedded RServi.
	 * 
	 * @param id the id (like the poolId)
	 * @param registry a handler for the RMI registry to use
	 * @param factory the node factory to use to establish the node
	 * @return the manager for the RServi instance
	 */
	public static EmbeddedRServiManager createEmbeddedRServi(final String id, final RMIRegistry registry, final RServiNodeFactory factory) {
		return new EmbeddedManager(id, registry, factory);
	}
	
}

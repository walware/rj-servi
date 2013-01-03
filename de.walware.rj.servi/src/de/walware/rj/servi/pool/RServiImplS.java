/*******************************************************************************
 * Copyright (c) 2009-2013 WalWare/RJ-Project (www.walware.de/goto/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.pool;

import static de.walware.rj.server.srvext.ServerUtil.RJ_DATA_ID;
import static de.walware.rj.server.srvext.ServerUtil.RJ_SERVER_ID;
import static de.walware.rj.servi.RServiUtil.RJ_CLIENT_ID;
import static de.walware.rj.servi.RServiUtil.RJ_SERVI_ID;

import java.io.File;

import de.walware.ecommons.net.RMIRegistry;

import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.ServerUtil;
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
	 * @param registry a handler for the RMI registry to use
	 * @param libDirPath the absolute path where the libraries are located in
	 * @return a node factory
	 * @throws RjInvalidConfigurationException
	 */
	public static RServiNodeFactory createLocalNodeFactory(final String poolId, final RMIRegistry registry, final String libDirPath)
			throws RjInvalidConfigurationException {
		final String[] libIds = new String[] {
				RJ_DATA_ID, RJ_SERVER_ID, RJ_SERVI_ID, RJ_CLIENT_ID, "de.walware.rj.services.eruntime" };
		ServerUtil.searchRJLibs(libDirPath, libIds);
		
		return new LocalNodeFactory(poolId, registry, libIds) {
			@Override
			protected String[] getRJLibs(final String[] libIds) throws RjInvalidConfigurationException {
				return ServerUtil.searchRJLibs(libDirPath, libIds);
			}
			@Override
			protected String getPolicyFile() throws RjInvalidConfigurationException {
				return RServiImplS.getPolicyFile(libDirPath);
			}
		};
	}
	
	/**
	 * Creates a node factory establishing RServi nodes on the local system for the local host.
	 * 
	 * This method is intended for embedded RServi instances when utilizing the default bundles.
	 * 
	 * @param poolId the id of the pool or application
	 * @param registry a handler for the RMI registry to use
	 * @param libDirPath the absolute path where the libraries are located in
	 * @return a node factory
	 * @throws RjInvalidConfigurationException
	 */
	public static RServiNodeFactory createLocalhostNodeFactory(final String poolId, final RMIRegistry registry, final String libDirPath)
			throws RjInvalidConfigurationException {
		final String[] libIds = new String[] {
				RJ_DATA_ID, RJ_SERVER_ID, RJ_SERVI_ID, RJ_CLIENT_ID, "de.walware.rj.services.eruntime" };
		ServerUtil.searchRJLibs(libDirPath, libIds);
		
		return new LocalNodeFactory(poolId, registry, libIds) {
			@Override
			protected String[] getRJLibs(final String[] libIds) throws RjInvalidConfigurationException {
				return ServerUtil.searchRJLibs(libDirPath, libIds);
			}
			@Override
			protected String getPolicyFile() throws RjInvalidConfigurationException {
				return RServiImplS.getLocalhostPolicyFile(libDirPath);
			}
		};
	}
	
	/**
	 * Creates a node factory establishing RServi nodes on the local system.
	 * 
	 * This method is intended for RServi pools when utilizing customized libraries.
	 * 
	 * @param poolId the id of the pool or application
	 * @param registry a handler for the RMI registry to use
	 * @param libsPaths an array with absolute paths of all required libraries
	 * @param policyPath the absolute path of the policy file
	 * @return a node factory
	 * @throws RjInvalidConfigurationException
	 */
	public static RServiNodeFactory createLocalNodeFactory(final String poolId, final RMIRegistry registry,
			final String[] libsPaths, final String policyPath) throws RjInvalidConfigurationException {
		return new LocalNodeFactory(poolId, registry, null) {
			@Override
			protected String[] getRJLibs(final String[] libIds) throws RjInvalidConfigurationException {
				return libsPaths;
			}
			@Override
			protected String getPolicyFile() throws RjInvalidConfigurationException {
				return policyPath;
			}
		};
	}
	
	
	private static final String getPolicyFile(final String libDirPath) throws RjInvalidConfigurationException {
		if (libDirPath == null) {
			throw new NullPointerException();
		}
		final int length = libDirPath.length();
		if (length > 0 && libDirPath.charAt(length-1) == '/'
			|| libDirPath.charAt(length-1) == File.separatorChar) {
			return libDirPath + "security.policy";
		}
		return libDirPath + File.separatorChar + "security.policy";
	}
	
	public static final String getLocalhostPolicyFile(final String libDirPath) throws RjInvalidConfigurationException {
		if (libDirPath == null) {
			throw new NullPointerException();
		}
		String serverLib = ServerUtil.searchRJLibs(libDirPath, new String[] { RJ_SERVER_ID })[0];
		serverLib = new File(serverLib).toURI().toString();
		return (serverLib.charAt(serverLib.length()-1) == '/') ? // directory or jar file
				serverLib + "localhost.policy" :
				"jar:" + serverLib + "!/localhost.policy";
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

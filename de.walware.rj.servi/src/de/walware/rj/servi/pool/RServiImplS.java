/*******************************************************************************
 * Copyright (c) 2009 WalWare/RJ-Project (www.walware.de/opensource).
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
import static de.walware.rj.server.srvext.ServerUtil.RJ_SERVI_ID;

import java.io.File;

import de.walware.ecommons.net.RMIRegistry;

import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.ServerUtil;
import de.walware.rj.servi.internal.LocalNodeFactory;
import de.walware.rj.servi.internal.PoolManager;


/**
 * Factory for RServi objects with minimal requirements and libraries in a given libPath (Server, SWT only)
 */
public class RServiImplS {
	
	
	public static RServiNodeFactory createLocalNodeFactory(final String poolId, final RMIRegistry registry, final String libPath)
			throws RjInvalidConfigurationException {
		final String[] libIds = new String[] {
				RJ_DATA_ID, RJ_SERVER_ID, RJ_SERVI_ID, "de.walware.rj.services.eruntime" };
		ServerUtil.searchRJLibs(libPath, libIds);
		
		return new LocalNodeFactory(poolId, registry, libIds) {
			@Override
			protected String[] getRJLibs(String[] libIds) throws RjInvalidConfigurationException {
				return ServerUtil.searchRJLibs(libPath, libIds);
			}
			@Override
			protected String getPolicyFile() throws RjInvalidConfigurationException {
				return RServiImplS.getPolicyFile(libPath);
			}
		};
	}
	
	private static final String getPolicyFile(String libPath) throws RjInvalidConfigurationException {
		if (libPath == null) {
			throw new NullPointerException();
		}
		int length = libPath.length();
		if (length > 0 && libPath.charAt(length-1) == '/'
			|| libPath.charAt(length-1) == File.separatorChar) {
			return libPath + "security.policy";
		}
		return libPath + File.separatorChar + "security.policy";
	}
	
	public static RServiPoolManager createPool(final String poolId, final RMIRegistry registry) {
		return new PoolManager(poolId, registry);
	}
	
}

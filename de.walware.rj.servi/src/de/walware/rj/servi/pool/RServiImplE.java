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

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import de.walware.ecommons.net.RMIRegistry;

import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.EServerUtil;
import de.walware.rj.servi.internal.EmbeddedManager;
import de.walware.rj.servi.internal.LocalNodeFactory;


/**
 * Factory for RServi objects on the Eclipse Platform
 */
public class RServiImplE {
	
	
	public static RServiNodeFactory createLocalNodeFactory(final String poolId, final RMIRegistry registry) 
			throws RjInvalidConfigurationException {
		final String[] libIds = new String[] {
				RJ_DATA_ID, RJ_SERVER_ID, RJ_SERVI_ID, "org.eclipse.equinox.common", "org.eclipse.osgi" };
		
		return new LocalNodeFactory(poolId, registry, libIds) {
			@Override
			protected String[] getRJLibs(String[] libIds) throws RjInvalidConfigurationException {
				return EServerUtil.searchRJLibsInPlatform(libIds, (getConfig().getBits() == 64));
			}
			@Override
			protected String getPolicyFile() throws RjInvalidConfigurationException {
				return getLocalhostPolicyFile();
			}
		};
	}
	
	public static final String getLocalhostPolicyFile() throws RjInvalidConfigurationException {
		try {
			final Bundle bundle = Platform.getBundle(RJ_SERVER_ID);
			if (bundle == null) {
				throw new RjInvalidConfigurationException("RJ Server bundle ('"+RJ_SERVER_ID+"') is missing.");
			}
			final URL intern = bundle.getEntry("/localhost.policy"); 
			URL java = FileLocator.resolve(intern);
			String path = java.toExternalForm();
			return path;
		}
		catch (IOException e) {
			throw new RjInvalidConfigurationException("Failed to resolve path to 'localhost.policy'.", e);
		}
	}
	
	public static EmbeddedRServiManager createEmbeddedRServi(final String poolId, final RMIRegistry registry, final RServiNodeFactory factory) {
		return new EmbeddedManager(poolId, registry, factory);
	}
	
}

/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/RJ-Project (www.walware.de/goto/opensource).
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
	
	
	/**
	 * @deprecated use {@link #createLocalhostNodeFactory(String, RMIRegistry)}
	 */
	@Deprecated
	public static RServiNodeFactory createLocalNodeFactory(final String poolId, final RMIRegistry registry) {
		return createLocalNodeFactory(poolId, registry);
	}
	
	/**
	 * Creates a node factory establishing RServi nodes on the local system for the local host.
	 * 
	 * @param poolId the id of the pool or application
	 * @param registry a handler for the RMI registry to use
	 * @return a node factory
	 * @throws RjInvalidConfigurationException
	 */
	public static RServiNodeFactory createLocalhostNodeFactory(final String poolId, final RMIRegistry registry) 
			throws RjInvalidConfigurationException {
		final String[] libIds = new String[] {
				RJ_DATA_ID, RJ_SERVER_ID, RJ_SERVI_ID, RJ_CLIENT_ID, "org.eclipse.equinox.common", "org.eclipse.osgi" };
		
		return new LocalNodeFactory(poolId, registry, libIds) {
			@Override
			protected String[] getRJLibs(final String[] libIds) throws RjInvalidConfigurationException {
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
			final URL java = FileLocator.resolve(intern);
			final String path = java.toExternalForm();
			return path;
		}
		catch (final IOException e) {
			throw new RjInvalidConfigurationException("Failed to resolve path to 'localhost.policy'.", e);
		}
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

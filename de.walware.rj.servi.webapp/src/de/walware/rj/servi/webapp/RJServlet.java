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

package de.walware.rj.servi.webapp;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;

import de.walware.rj.RjException;
import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.ServerUtil;
import de.walware.rj.servi.internal.PoolManager;
import de.walware.rj.servi.pool.RServiImplS;
import de.walware.rj.servi.pool.RServiNodeConfig;
import de.walware.rj.servi.pool.RServiNodeFactory;
import de.walware.rj.servi.pool.RServiPoolManager;


public class RJServlet extends HttpServlet {
	
	
	private RServiPoolManager manager;
	
	
	public RJServlet(){
	}
	
	
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		try {
			String id = getServletContext().getContextPath();
			if (id.startsWith("/")) {
				id = id.substring(1);
			}
			getServletContext().setAttribute(RJWeb.POOLID_KEY, id);
			
			final String libPath = getServletContext().getRealPath("WEB-INF/lib");
			
			final String property = System.getProperty("java.rmi.server.codebase");
			if (property == null) {
				final String[] libs = ServerUtil.searchRJLibs(libPath,
						new String[] { ServerUtil.RJ_SERVER_ID, ServerUtil.RJ_SERVI_ID });
				System.setProperty("java.rmi.server.codebase", ServerUtil.concatCodebase(libs));
			}
			
			final OtherConfigBean rmiConfig = (OtherConfigBean) Utils.loadFromFile(getServletContext(), new OtherConfigBean());
			if (System.getProperty("java.rmi.server.hostname") == null) {
				System.setProperty("java.rmi.server.hostname", rmiConfig.getEffectiveHostaddress());
			}
			final RMIAddress rmiRegistryAddress = new RMIAddress(rmiConfig.getEffectiveHostaddress(), rmiConfig.getEffectiveRegistryPort(), null);
			Registry registry = LocateRegistry.getRegistry(rmiRegistryAddress.getPortNum());
			RMIRegistry rmiRegistry = null;
			if (rmiConfig.getRegistryEmbed()) {
				try {
					rmiRegistry = new RMIRegistry(rmiRegistryAddress, registry, true);
				}
				catch (final RemoteException e) {
					ECommons.getEnv().log(new Status(IStatus.INFO, RJWeb.PLUGIN_ID, 0, "Failed to connect to registry - will try to start embedded.", e));
					RMIUtil.INSTANCE.setEmbeddedPrivatePort(rmiConfig.getEffectiveRegistryPort());
					rmiRegistry = RMIUtil.INSTANCE.getEmbeddedPrivateRegistry();
				}
			}
			if (rmiRegistry == null) {
				rmiRegistry = new RMIRegistry(rmiRegistryAddress, registry, true);
			}
			
			final RServiPoolManager manager = new PoolManager(id, rmiRegistry);
			
			final PoolConfigBean poolConfig = (PoolConfigBean) Utils.loadFromFile(getServletContext(), new PoolConfigBean());
			manager.setConfig(poolConfig);
			
			final RServiNodeFactory nodeFactory = RServiImplS.createLocalNodeFactory(id, rmiRegistry, libPath);
			final RServiNodeConfig factoryConfig = (RServiNodeConfig) Utils.loadFromFile(getServletContext(), new RServiNodeConfig());
			try {
				nodeFactory.setConfig(factoryConfig);
			}
			catch (final RjInvalidConfigurationException e) {}
			manager.addNodeFactory(nodeFactory);
			
			manager.init();
			this.manager = manager;
			getServletContext().setAttribute("pool.manager", manager);
		}
		catch (final Exception e) {
			throw new ServletException("Failed to initialized RServi Server.", e);
		}
	}
	
	@Override
	public void destroy() {
		if (this.manager != null) {
			try {
				getServletContext().removeAttribute(RJWeb.POOLMANAGER_KEY);
				this.manager.stop(1);
			}
			catch (final RjException e) {
				log("An error occurred when closing the pool.", e);
			}
		}
	}
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		if (this.manager != null) {
			response.setStatus(200);
		}
		else {
			response.setStatus(503);
		}
	}
	
}

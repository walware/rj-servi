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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;

import de.walware.rj.RjException;
import de.walware.rj.RjInitFailedException;
import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.ServerUtil;
import de.walware.rj.servi.RServiUtil;
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
			
			// RMI registry setup
			final OtherConfigBean rmiConfig = (OtherConfigBean) Utils.loadFromFile(getServletContext(), new OtherConfigBean());
			
			final String property = System.getProperty("java.rmi.server.codebase");
			if (property == null) {
				final String[] libs = ServerUtil.searchRJLibs(libPath,
						new String[] { ServerUtil.RJ_SERVER_ID, RServiUtil.RJ_SERVI_ID });
				System.setProperty("java.rmi.server.codebase", ServerUtil.concatCodebase(libs));
			}
			
			if (System.getProperty("java.rmi.server.hostname") == null) {
				System.setProperty("java.rmi.server.hostname", rmiConfig.getEffectiveHostaddress());
			}
			
			final int registryPort = rmiConfig.getEffectiveRegistryPort();
			final RMIAddress rmiRegistryAddress = new RMIAddress(rmiConfig.getEffectiveHostaddress(), registryPort, null);
			Registry registry = LocateRegistry.getRegistry(rmiRegistryAddress.getPortNum());
			RMIRegistry rmiRegistry;
			if (rmiConfig.getRegistryEmbed()) {
				try {
					rmiRegistry = new RMIRegistry(rmiRegistryAddress, registry, true);
					ECommons.getEnv().log(new Status(IStatus.WARNING, RJWeb.PLUGIN_ID, 0,
							"Found running RMI registry at port "+registryPort+", embedded RMI registry will not be started.", null));
				}
				catch (final RemoteException e) {
					RMIUtil.INSTANCE.setEmbeddedPrivateMode(false);
					RMIUtil.INSTANCE.setEmbeddedPrivatePort(registryPort);
					rmiRegistry = RMIUtil.INSTANCE.getEmbeddedPrivateRegistry(new NullProgressMonitor());
					if (rmiRegistry != null) {
						ECommons.getEnv().log(new Status(IStatus.INFO, RJWeb.PLUGIN_ID, 0,
								"Embedded RMI registry at port "+registryPort+" started.", null));
					}
					else {
						ECommons.getEnv().log(new Status(IStatus.INFO, RJWeb.PLUGIN_ID, 0,
								"Failed to connect to running RMI registry at port "+registryPort+".", e));
						ECommons.getEnv().log(new Status(IStatus.ERROR, RJWeb.PLUGIN_ID, 0,
								"Failed to start embedded RMI registry at port "+registryPort+".", null));
						throw new RjInitFailedException("Initalization of RMI registry setup failed.");
					}
				}
			}
			else {
				try {
					rmiRegistry = new RMIRegistry(rmiRegistryAddress, registry, true);
					ECommons.getEnv().log(new Status(IStatus.INFO, RJWeb.PLUGIN_ID, 0,
							"Found running RMI registry at port "+registryPort+".", null));
				}
				catch (final RemoteException e) {
					ECommons.getEnv().log(new Status(IStatus.ERROR, RJWeb.PLUGIN_ID, 0,
							"Failed to connect to running RMI registry at port "+registryPort+".", e));
					throw new RjInitFailedException("Initalization of RMI registry setup failed.");
				}
			}
			
			// Pool manager setup
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
			getServletContext().setAttribute(RJWeb.POOLMANAGER_KEY, manager);
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

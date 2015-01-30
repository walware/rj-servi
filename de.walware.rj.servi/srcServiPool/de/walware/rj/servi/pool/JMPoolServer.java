/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.pool;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;

import de.walware.rj.RjException;
import de.walware.rj.RjInitFailedException;
import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.RJContext;
import de.walware.rj.server.srvext.ServerUtil;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.servi.internal.MXNetConfig;
import de.walware.rj.servi.internal.MXNodeConfig;
import de.walware.rj.servi.internal.MXNodeManager;
import de.walware.rj.servi.internal.MXPoolConfig;
import de.walware.rj.servi.internal.MXPoolStatus;
import de.walware.rj.servi.internal.MXUtil;
import de.walware.rj.servi.internal.PoolManager;
import de.walware.rj.servi.internal.Utils;
import de.walware.rj.servi.jmx.PoolServerMXBean;
import de.walware.rj.servi.jmx.PoolStatusMX;


public class JMPoolServer implements PoolServer, PoolServerMXBean {
	
	
	private final String id;
	private final RJContext context;
	
	private final String jmBaseName;
	private ObjectName jmxName;
	
	private RMIRegistry rmiRegistry;
	private Set<Integer> rmiEmbeddedPorts;
	private boolean rmiHostnameSet;
	
	private String poolAddress;
	
	private final MXNetConfig currentNetConfig;
	private volatile PoolConfig currentPoolConfig;
	private volatile RServiNodeConfig currentNodeConfig;
	
	private final MXNetConfig jmNetConfig;
	private final MXPoolConfig jmPoolConfig;
	private final MXNodeConfig jmNodeConfig;
	
	private volatile boolean jmIsNodeManagementEnabled;
	private MXNodeManager jmNodeManager;
	
	private final RServiNodeFactory nodeFactory;
	
	private PoolManager poolManager;
	
	
	public JMPoolServer(final String id, final RJContext context) throws RjInitFailedException {
		this(id, context, true);
	}
	
	public JMPoolServer(final String id, final RJContext context, final boolean enableJM) throws RjInitFailedException {
		this.id = id;
		this.context = context;
		this.jmBaseName = "RServi:rservi.id=" + getId() + ",";
		
		this.currentNetConfig = (MXNetConfig) MXUtil.loadInit(new MXNetConfig(this), this.context);
		this.currentPoolConfig = (PoolConfig) MXUtil.loadInit(new PoolConfig(), this.context);
		this.currentNodeConfig = (RServiNodeConfig) MXUtil.loadInit(new RServiNodeConfig(), this.context);
		
		try {
			this.nodeFactory = RServiImplS.createLocalNodeFactory(this.id, this.context);
		}
		catch (final RjInvalidConfigurationException e) {
			throw new RjInitFailedException("Creating local R node factory failed.", e);
		}
		try {
			if (enableJM) {
				this.jmxName = new ObjectName(this.jmBaseName + "type=Server");
				ManagementFactory.getPlatformMBeanServer().registerMBean(this, this.jmxName);
			}
			
			this.jmNetConfig = this.currentNetConfig;
			if (enableJM) {
				this.jmNetConfig.initJM();
			}
			
			this.jmPoolConfig = new MXPoolConfig(this);
			this.jmPoolConfig.load(this.currentPoolConfig);
			if (enableJM) {
				this.jmPoolConfig.initJM();
			}
			
			this.jmNodeConfig = new MXNodeConfig(this);
			this.jmNodeConfig.load(this.currentNodeConfig);
			if (enableJM) {
				this.jmNodeConfig.initJM();
			}
		}
		catch (final Exception e) {
			try {
				shutdown();
			}
			catch (final Exception e2) {}
			throw new RjInitFailedException("Initializing JMX for pool server failed.", e);
		}
		try {
			this.nodeFactory.setConfig(this.currentNodeConfig);
		}
		catch (final RjInvalidConfigurationException e) {
			Utils.logWarning(e.getMessage());
		}
	}
	
	
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public RJContext getRJContext() {
		return this.context;
	}
	
	@Override
	public String getJMBaseName() {
		return this.jmBaseName;
	}
	
	@Override
	public void getNetConfig(final NetConfig config) {
		config.load(this.currentNetConfig);
	}
	
	@Override
	public void setNetConfig(NetConfig config) {
		config = new NetConfig(config);
		
		if (!config.validate(null)) {
			throw new IllegalArgumentException();
		}
		this.currentNetConfig.load(config);
	}
	
	@Override
	public void getPoolConfig(final PoolConfig config) {
		config.load(this.currentPoolConfig);
	}
	
	@Override
	public void setPoolConfig(PoolConfig config) {
		config = new PoolConfig(config); // intern
		
		if (!config.validate(null)) {
			throw new IllegalArgumentException();
		}
		synchronized (this.jmPoolConfig) {
			final PoolManager manager = this.poolManager;
			if (manager != null) {
				manager.setConfig(config);
			}
			
			this.currentPoolConfig = config;
			this.jmPoolConfig.load(config);
		}
	}
	
	@Override
	public void getNodeConfig(final RServiNodeConfig config) {
		config.load(this.currentNodeConfig);
	}
	
	@Override
	public void setNodeConfig(RServiNodeConfig config) throws RjInvalidConfigurationException {
		config = new RServiNodeConfig(config); // intern
		
		if (!config.validate(null)) {
			throw new IllegalArgumentException();
		}
		synchronized (this.jmPoolConfig) {
			this.nodeFactory.setConfig(config);
			
			this.currentNodeConfig = config;
			this.jmNodeConfig.load(config);
		}
	}
	
	
	private void initRMI() throws RjException, OperationsException {
		final String hostAddress;
		final int registryPort;
		final boolean embed;
		final boolean ssl;
		synchronized (this.currentNetConfig) {
			if (!MXUtil.validate(this.currentNetConfig)) {
				return;
			}
			hostAddress = this.currentNetConfig.getEffectiveHostaddress();
			registryPort = this.currentNetConfig.getEffectiveRegistryPort();
			embed = this.currentNetConfig.getRegistryEmbed();
			ssl = this.currentNetConfig.isSSLEnabled();
		}
		
		this.rmiRegistry = null;
		this.nodeFactory.setRegistry(null);
		this.poolAddress = null;
		
		// RMI registry setup
		final String property = System.getProperty("java.rmi.server.codebase");
		if (property == null) {
			final String[] libs = this.context.searchRJLibs(
					new String[] { ServerUtil.RJ_SERVER_ID, RServiUtil.RJ_SERVI_ID });
			System.setProperty("java.rmi.server.codebase", ServerUtil.concatCodebase(libs));
		}
		
		if (this.rmiHostnameSet || System.getProperty("java.rmi.server.hostname") == null) {
			System.setProperty("java.rmi.server.hostname", hostAddress);
			this.rmiHostnameSet = true;
		}
		
		RMIAddress rmiRegistryAddress;
		Registry registry;
		try {
			rmiRegistryAddress = new RMIAddress(hostAddress, registryPort, null);
			final RMIClientSocketFactory csf = (ssl) ? new SslRMIClientSocketFactory() : null;
			registry = LocateRegistry.getRegistry(null, registryPort, csf);
		}
		catch (final UnknownHostException e) {
			throw new RjInvalidConfigurationException("Invalid RMI address.", e);
		}
		catch (final MalformedURLException e) {
			throw new RjInvalidConfigurationException("Invalid RMI address.", e);
		}
		catch (final RemoteException e) {
			throw new RjInitFailedException("Failed to reference local registry.", e);
		}
		RMIRegistry rmiRegistry = null;
		if (embed) {
			if (this.rmiEmbeddedPorts == null) {
				this.rmiEmbeddedPorts = new HashSet<Integer>();
			}
			try {
				rmiRegistry = new RMIRegistry(rmiRegistryAddress, registry, true);
				if (this.rmiEmbeddedPorts.add(registryPort)) {
					Utils.logWarning("Found running RMI registry at port "+registryPort+", embedded RMI registry will not be started.");
				}
			}
			catch (final RemoteException e) {
				RMIUtil.INSTANCE.setEmbeddedPrivateMode(false, ssl);
				RMIUtil.INSTANCE.setEmbeddedPrivatePort(registryPort);
				try {
					rmiRegistry = RMIUtil.INSTANCE.getEmbeddedPrivateRegistry(new NullProgressMonitor());
					if (rmiRegistry != null) {
						Utils.logInfo("Embedded RMI registry at port "+registryPort+" started.");
					}
					else {
						Utils.logInfo("Failed to connect to running RMI registry at port "+registryPort+".", e);
						Utils.logError("Failed to start embedded RMI registry at port "+registryPort+".");
						throw new RjInitFailedException("Initalization of RMI registry setup failed.");
					}
				}
				catch (final CoreException ee) {
					Utils.logError("Failed to start embedded RMI registry at port "+registryPort+".", ee);
				}
			}
		}
		else {
			try {
				rmiRegistry = new RMIRegistry(rmiRegistryAddress, registry, true);
				Utils.logInfo("Found running RMI registry at port "+registryPort+".");
			}
			catch (final RemoteException e) {
				Utils.logError("Failed to connect to running RMI registry at port "+registryPort+".", e);
				throw new RjInitFailedException("Initalization of RMI registry setup failed.");
			}
		}
		
		this.rmiRegistry = rmiRegistry;
		this.nodeFactory.setRegistry(rmiRegistry);
		this.poolAddress = NetConfig.getPoolAddress(hostAddress, registryPort, this.id);
	}
	
	private void startManager() throws RjException {
		final PoolManager manager = new PoolManager(this.id, this.rmiRegistry);
		synchronized (this.jmPoolConfig) {
			
			manager.setConfig(this.currentPoolConfig);
			
			manager.addNodeFactory(this.nodeFactory);
			
			manager.init();
			
			this.poolManager = manager;
		}
		
		if (this.jmIsNodeManagementEnabled) {
			this.jmNodeManager = new MXNodeManager(this, manager);
			this.jmNodeManager.activate();
		}
	}
	
	
	private void stopManager() {
		final PoolManager manager = this.poolManager;
		this.poolManager = null;
		if (manager != null && manager.isInitialized()) {
			try {
				manager.stop(0);
			}
			catch (final RjException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@Override
	public PoolManager getManager() {
		return this.poolManager;
	}
	
	@Override
	public String getPoolAddress() {
		return this.poolAddress;
	}
	
	@Override
	public PoolStatusMX getPoolStatus() {
		return new MXPoolStatus(this);
	}
	
	@Override
	public boolean isPoolNodeManagementEnabled() {
		return this.jmIsNodeManagementEnabled;
	}
	
	@Override
	public synchronized void setPoolNodeManagementEnabled(final boolean enable) {
		if (this.jmIsNodeManagementEnabled == enable) {
			return;
		}
		this.jmIsNodeManagementEnabled = enable;
		if (enable) {
			if (this.jmNodeManager == null) {
				this.jmNodeManager = new MXNodeManager(this, this.poolManager);
			}
			this.jmNodeManager.activate();
		}
		else {
			if (this.jmNodeManager != null) {
				this.jmNodeManager.deactivate();
			}
		}
	}
	
	
	@Override
	public synchronized void start() throws OperationsException {
		try {
			final PoolManager manager = this.poolManager;
			if (manager != null) {
				return;
			}
			
			initRMI();
			
			startManager();
		}
		catch (final RjException e) {
			Utils.logError("Failed to start RServi pool server.", e);
			throw new OperationsException("Failed to start RServi pool server: " + e.getMessage());
		}
	}
	
	@Override
	public synchronized void stop() throws OperationsException {
		stopManager();
	}
	
	@Override
	public synchronized void restart() throws OperationsException {
		stop();
		start();
	}
	
	public synchronized void shutdown() {
		stopManager();
		try {
			if (this.jmPoolConfig != null) {
				this.jmPoolConfig.disposeJM();
			}
			if (this.jmNetConfig != null) {
				this.jmNetConfig.disposeJM();
			}
			if (this.jmNodeConfig != null) {
				this.jmNodeConfig.disposeJM();
			}
			
			if (this.jmxName != null) {
				ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.jmxName);
				this.jmxName = null;
			}
		}
		catch (final Exception e) {
			Utils.logError("An error occured when disposing JMX for pool server.", e);
		}
	}
	
}

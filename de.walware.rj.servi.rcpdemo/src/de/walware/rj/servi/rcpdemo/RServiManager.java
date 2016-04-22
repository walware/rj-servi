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

package de.walware.rj.servi.rcpdemo;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;

import de.walware.rj.RjException;
import de.walware.rj.eclient.graphics.comclient.ERClientGraphicActionsFactory;
import de.walware.rj.rsetups.RSetup;
import de.walware.rj.rsetups.RSetupUtil;
import de.walware.rj.server.RjsComConfig;
import de.walware.rj.server.client.RClientGraphicFactory;
import de.walware.rj.server.srvext.ERJContext;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.servi.internal.rcpdemo.Activator;
import de.walware.rj.servi.pool.EmbeddedRServiManager;
import de.walware.rj.servi.pool.RServiImplE;
import de.walware.rj.servi.pool.RServiNodeConfig;
import de.walware.rj.servi.pool.RServiNodeFactory;

public class RServiManager {
	
	
	private static final int EMBEDDED = 1;
	private static final int POOL = 2;
	private static final int RSETUP = 3;
	
	private static class Config {
		
		private int mode;
		private String address;
		
	}
	
	
	private final String name;
	
	private Config config = new Config();
	
	private EmbeddedRServiManager embeddedR;
	
	private final ISchedulingRule schedulingRule = new ISchedulingRule() {
		@Override
		public boolean contains(final ISchedulingRule rule) {
			return (rule == this);
		}
		@Override
		public boolean isConflicting(final ISchedulingRule rule) {
			// if concurrent remote instances are desired, return false here
			return (rule == this);
		}
	};
	
	
	public RServiManager(final String appId, final RClientGraphicFactory graphicFactory) {
		this.name = appId;
		
		RjsComConfig.setProperty("rj.servi.graphicFactory", graphicFactory);
		RjsComConfig.setProperty("rj.servi.comClientGraphicActionsFactory",
				new ERClientGraphicActionsFactory() );
	}
	
	
	public ISchedulingRule getSchedulingRule() {
		return this.schedulingRule;
	}
	
	public void setEmbedded(final String rHome) throws CoreException {
		final Config config = new Config();
		config.mode = EMBEDDED;
		config.address = rHome;
		this.config = config;
		
		final RServiNodeConfig rConfig = new RServiNodeConfig();
		rConfig.setRHome(rHome);
		rConfig.setEnableVerbose(true);
		
		startEmbedded(rConfig);
	}
	
	public void setPool(final String poolAddress) {
		final Config config = new Config();
		config.mode = POOL;
		config.address = poolAddress;
		this.config = config;
	}
	
	public void setRSetup(final String setupId) throws CoreException {
		final Config config = new Config();
		config.mode = RSETUP;
		config.address = setupId;
		this.config = config;
		
		final RSetup setup = RSetupUtil.loadSetup(setupId, null);
		if (setup == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "No R setup with specified id found."));
		}
		
		final RServiNodeConfig rConfig = new RServiNodeConfig();
		rConfig.setRHome(setup.getRHome());
		setLibs(setup.getRLibsSite(), rConfig, "R_LIBS_SITE");
		setLibs(setup.getRLibs(), rConfig, "R_LIBS");
		setLibs(setup.getRLibsUser(), rConfig, "R_LIBS_USER");
		rConfig.setEnableVerbose(true);
		
		startEmbedded(rConfig);
	}
	
	private void setLibs(final List<String> locations, final RServiNodeConfig rConfig, final String varName) {
		if (locations != null && locations.size() > 0) {
			final StringBuilder sb = new StringBuilder(locations.get(0));
			for (int i = 0; i < locations.size(); i++) {
				sb.append(File.pathSeparatorChar);
				sb.append(locations.get(i));
			}
			rConfig.getEnvironmentVariables().put(varName, sb.toString());
		}
	}
	
	private void startEmbedded(final RServiNodeConfig rConfig) throws CoreException {
		startEmbedded(rConfig, new NullProgressMonitor()); // TODO real monitor, e.g. in a Job
	}
	
	private void startEmbedded(final RServiNodeConfig rConfig,
			final IProgressMonitor monitor) throws CoreException {
		if (rConfig == null) {
			throw new NullPointerException("rConfig");
		}
		if (monitor == null) {
			throw new NullPointerException("monitor");
		}
		try {
			final ERJContext context = new ERJContext();
			if (System.getSecurityManager() == null) {
				if (System.getProperty("java.security.policy") == null) {
					final String policyFile = context.getServerPolicyFilePath();
					System.setProperty("java.security.policy", policyFile);
				}
				System.setSecurityManager(new SecurityManager());
			}
			
			final RMIRegistry registry = RMIUtil.INSTANCE.getEmbeddedPrivateRegistry(monitor);
			
			rConfig.setNodeArgs(rConfig.getNodeArgs() + " -embedded");
			
			final RServiNodeFactory nodeFactory = RServiImplE.createLocalNodeFactory(this.name, context);
			nodeFactory.setRegistry(registry);
			nodeFactory.setConfig(rConfig);
			
			final EmbeddedRServiManager newEmbeddedR = RServiImplE.createEmbeddedRServi(this.name, registry, nodeFactory);
			newEmbeddedR.start();
			if (this.embeddedR != null) {
				this.embeddedR.stop();
				this.embeddedR = null;
			}
			this.embeddedR = newEmbeddedR;
		}
		catch (final RjException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Embedded R instance could not created.", e));
		}
	}
	
	
	public RServi getRServi(final String task) throws CoreException {
		final Config config = this.config;
		final String key = this.name + "-" + task;
		
		try {
			switch (config.mode) {
			case EMBEDDED:
			case RSETUP:
				return RServiUtil.getRServi(this.embeddedR, key);
			case POOL:
				return RServiUtil.getRServi(config.address, key);
			}
		}
		catch (final CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "R not available, please check the configuration.", e));
		}
		catch (final LoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "R not available, please check the configuration.", e));
		}
		catch (final NoSuchElementException e) {
			throw new CoreException(new Status(IStatus.INFO, Activator.PLUGIN_ID, "R currently not available, please try again later.", e));
		}
		throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "R is not configured, please check the configuration."));
	}
	
}

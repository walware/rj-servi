package de.walware.rj.servi.rcpdemo;

import java.util.NoSuchElementException;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;
import de.walware.rj.RjException;
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
	
	private static class Config {
		
		private int mode;
		private String address;
		
	}
	
	
	private String name;
	
	private Config config = new Config();
	
	private EmbeddedRServiManager embeddedR;
	
	private ISchedulingRule schedulingRule = new ISchedulingRule() {
		public boolean contains(ISchedulingRule rule) {
			return (rule == this);
		}
		public boolean isConflicting(ISchedulingRule rule) {
			// if concurrent remote instances are desired, return false here
			return (rule == this);
		}
	};
	
	
	public RServiManager(final String appId) {
		this.name = appId;
	}
	
	
	public ISchedulingRule getSchedulingRule() {
		return schedulingRule;
	}
	
	public void setEmbedded(final String rHome) throws CoreException {
		final Config config = new Config();
		config.mode = EMBEDDED;
		config.address = rHome;
		this.config = config;
		
		try {
			if (System.getSecurityManager() == null) {
				if (System.getProperty("java.security.policy") == null) {
					String policyFile = RServiImplE.getLocalhostPolicyFile();
					System.setProperty("java.security.policy", policyFile);
				}
				System.setSecurityManager(new SecurityManager());
			}
			
			RMIUtil.INSTANCE.setEmbeddedPrivateMode(true);
			final RMIRegistry registry = RMIUtil.INSTANCE.getEmbeddedPrivateRegistry();
			final RServiNodeFactory nodeFactory = RServiImplE.createLocalhostNodeFactory(this.name, registry);
			final RServiNodeConfig rConfig = new RServiNodeConfig();
			rConfig.setRHome(rHome);
			rConfig.setEnableVerbose(true);
			nodeFactory.setConfig(rConfig);
			
			EmbeddedRServiManager newEmbeddedR = RServiImplE.createEmbeddedRServi(this.name, registry, nodeFactory);
			newEmbeddedR.start();
			if (embeddedR != null) {
				embeddedR.stop();
				embeddedR = null;
			}
			embeddedR = newEmbeddedR;
		}
		catch (RjException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Embedded R instance could not created.", e));
		}
	}
	
	public void setPool(final String poolAddress) {
		final Config config = new Config();
		config.mode = POOL;
		config.address = poolAddress;
		this.config = config;
	}
	
	
	public RServi getRServi(String task) throws CoreException {
		final Config config = this.config;
		String key = name + "-" + task;
		
		try {
			switch (config.mode) {
			case EMBEDDED:
				return RServiUtil.getRServi(embeddedR, key);
			case POOL:
				return RServiUtil.getRServi(config.address, key);
			}
		}
		catch (CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "R not available, please check the configuration.", e));
		}
		catch (LoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "R not available, please check the configuration.", e));
		}
		catch (NoSuchElementException e) {
			throw new CoreException(new Status(IStatus.INFO, Activator.PLUGIN_ID, "R currently not available, please try again later.", e));
		}
		throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "R is not configured, please check the configuration."));
	}
	
}

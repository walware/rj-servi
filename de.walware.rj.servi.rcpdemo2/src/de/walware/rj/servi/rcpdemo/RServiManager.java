package de.walware.rj.servi.rcpdemo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.IDisposable;
import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;

import de.walware.rj.RjException;
import de.walware.rj.eclient.graphics.comclient.ERClientGraphicActionsFactory;
import de.walware.rj.rsetups.RSetup;
import de.walware.rj.rsetups.RSetupUtil;
import de.walware.rj.server.RjsComConfig;
import de.walware.rj.server.client.RClientGraphicFactory;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.servi.internal.rcpdemo.Activator;
import de.walware.rj.servi.pool.EmbeddedRServiManager;
import de.walware.rj.servi.pool.RServiImplE;
import de.walware.rj.servi.pool.RServiNodeConfig;
import de.walware.rj.servi.pool.RServiNodeFactory;


public class RServiManager implements IDisposable {
	
	
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
	
	private RServiSession currentSession;
	private final List<RServiSession> runningSessions = new ArrayList<RServiSession>();
	
	
	public RServiManager(final String appId, final RClientGraphicFactory graphicFactory) {
		this.name = appId;
		
		RjsComConfig.setProperty("rj.servi.graphicFactory", graphicFactory);
		RjsComConfig.setProperty("rj.servi.comClientGraphicActionsFactory",
				new ERClientGraphicActionsFactory() );
	}
	
	
	public void setEmbedded(final String rHome) throws CoreException {
		closeRServiSession();
		
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
		closeRServiSession();
		
		final Config config = new Config();
		config.mode = POOL;
		config.address = poolAddress;
		this.config = config;
	}
	
	public void setRSetup(final String setupId) throws CoreException {
		closeRServiSession();
		
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
		try {
			if (System.getSecurityManager() == null) {
				if (System.getProperty("java.security.policy") == null) {
					final String policyFile = RServiImplE.getLocalhostPolicyFile();
					System.setProperty("java.security.policy", policyFile);
				}
				System.setSecurityManager(new SecurityManager());
			}
			
			RMIUtil.INSTANCE.setEmbeddedPrivateMode(true);
			final RMIRegistry registry = RMIUtil.INSTANCE.getEmbeddedPrivateRegistry();
			final RServiNodeFactory nodeFactory = RServiImplE.createLocalhostNodeFactory(this.name, registry);
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
	
	
	public synchronized ITool getRServiSession() throws CoreException {
		if (this.currentSession == null) {
			final RServi servi = getRServi("session");
			this.currentSession = new RServiSession(servi) {
				@Override
				protected void terminated() {
					synchronized (RServiManager.this.runningSessions) {
						RServiManager.this.runningSessions.remove(this);
					}
				}
			};
			synchronized (this.runningSessions) {
				this.runningSessions.add(this.currentSession);
			}
		}
		return this.currentSession;
	}
	
	private void closeRServiSession() {
		if (this.currentSession != null) {
			this.currentSession.close(false);
			this.currentSession = null;
		}
	}
	
	
	public void schedule(final IToolRunnable runnable) throws CoreException {
		final ITool session = getRServiSession();
		final IStatus status;
		if (session != null) {
			status = session.getQueue().add(runnable);
		}
		else {
			status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"R engine not available.");
		}
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}
	
	public void scheduleDemo(final IToolRunnable runnable) {
		try {
			schedule(runnable);
		}
		catch (final CoreException e) {
			final Status status = new Status(e.getStatus().getSeverity(), Activator.PLUGIN_ID,
					"Cannot schedule '" + runnable.getLabel() + "'", e);
			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
		}
	}
	
	public void dispose() {
		this.config = new Config();
		final RServiSession[] sessions;
		synchronized (this.runningSessions) {
			sessions = this.runningSessions.toArray(new RServiSession[this.runningSessions.size()]);
		}
		for (final RServiSession session : sessions) {
			session.close(true);
		}
	}
	
	private RServi getRServi(final String task) throws CoreException {
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

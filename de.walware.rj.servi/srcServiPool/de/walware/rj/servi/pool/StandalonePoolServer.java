/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.pool;

import java.io.File;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.OperationsException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.IDisposable;

import de.walware.rj.RjInitFailedException;
import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.RJContext;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.servi.jmx.StandalonePoolServerMXBean;


/**
 * Standalone version of pool server managed by JMX
 * 
 * <p>Required bundles (libraries): <code>de.walware.rj.data</code>,
 * <code>de.walware.rj.server</code>, <code>de.walware.rj.client</code>,
 * <code>de.walware.rj.servi</code>, <code>de.walware.rj.services.eruntime</code>
 * </p>
 * <p>The configuration is loaded from / saved to the current directory.
 * </p>
 * <p>To start the server use e.g.:
 * <pre>
 * java -cp "*" de.walware.rj.servi.pool.StandalonePoolServer &lt;id&gt;
 * </pre></p>
 * <p>By default the libraries are expected in the current directory. If they are located
 * in another directory, it must be specified in the java property <code>de.walware.rj.path</code>:
 * <pre>
 * java -cp "/path/to/rjlibs/*" -Dde.walware.rj.path=/path/to/rjlibs/ de.walware.rj.servi.pool.StandalonePoolServer &lt;id&gt;
 * </pre></p>
 * </p>
 */
public class StandalonePoolServer extends JMPoolServer implements StandalonePoolServerMXBean {
	
	
	private static class EAppEnv implements ECommons.IAppEnvironment {
		
		
		private final CopyOnWriteArraySet<IDisposable> stopListeners = new CopyOnWriteArraySet<>();
		
		
		public EAppEnv() {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					for (final IDisposable listener : EAppEnv.this.stopListeners) {
						listener.dispose();
					}
				}
			});
		}
		
		
		@Override
		public void log(final IStatus status) {
			System.out.println(status.toString());
		}
		
		@Override
		public void addStoppingListener(final IDisposable listener) {
			this.stopListeners.add(listener);
		}
		
		@Override
		public void removeStoppingListener(final IDisposable listener) {
			this.stopListeners.add(listener);
		}
		
	}
	
	
	private final AtomicBoolean running = new AtomicBoolean(true);
	
	
	protected StandalonePoolServer(final String id, final RJContext context) throws RjInitFailedException {
		super(id, context);
		
		new Thread("KeepAlive") {
			{
				setDaemon(false);
			}
			@Override
			public void run() {
				while (StandalonePoolServer.this.running.get()) {
					synchronized (StandalonePoolServer.this.running) {
						try {
							StandalonePoolServer.this.running.wait();
						}
						catch (final InterruptedException e) {
							Thread.interrupted();
						}
					}
				}
			}
		}.start();
	}
	
	
	@Override
	public synchronized void shutdown() {
		try {
			super.shutdown();
		}
		finally {
			this.running.set(false);
			synchronized (this.running) {
				this.running.notifyAll();
			}
		}
	}
	
	
	public static void main(final String[] args) throws RjInitFailedException {
		final StandalonePoolServer server = initServer(args);
		
		try {
			server.start();
		}
		catch (final OperationsException e) {
			ECommons.getEnv().log(new Status(IStatus.WARNING, RServiUtil.RJ_SERVI_ID,
					"The server is started, but the pool could not be started.", e));
		}
	}
	
	static StandalonePoolServer initServer(final String[] args) throws RjInitFailedException {
		final String id = (args.length > 0) ? args[0] : null;
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("No pool id specified.");
		}
		
		ECommons.init(RServiUtil.RJ_SERVI_ID, new EAppEnv());
		
		final RJContext context = new RJContext(System.getProperty("user.dir")) {
			@Override
			public String getServerPolicyFilePath() throws RjInvalidConfigurationException {
				final String path = getPropertiesDirPath() + "security.policy";
				if (new File(path).exists()) {
					return path;
				}
				return super.getServerPolicyFilePath();
			}
		};
		
		return new StandalonePoolServer(id, context);
	}
	
	
}

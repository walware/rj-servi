/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.pool;


/**
 * This class allows to run the {@link StandalonePoolServer} as daemon using JSVC of Apache
 * Commons-Daemon (http://commons.apache.org/proper/commons-daemon/).
 * 
 * Not tested, feedback welcome.
 */
public class PoolServerDaemon {
	
	
	private StandalonePoolServer poolServer;
	
	
	public PoolServerDaemon() {
	}
	
	
	public void init(final String[] args) throws Exception {
		if (this.poolServer != null) {
			throw new IllegalStateException();
		}
		this.poolServer = StandalonePoolServer.initServer(args);
	}
	
	public void start() throws Exception {
		this.poolServer.start();
	}
	
	public void stop() throws Exception {
		this.poolServer.stop();
	}
	
	public void destroy() {
		if (this.poolServer != null) {
			this.poolServer.shutdown();
			this.poolServer = null;
		}
	}
	
}

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

package de.walware.rj.servi.internal;

import java.io.File;
import java.rmi.RemoteException;

import de.walware.ecommons.net.RMIAddress;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.RServiNode;


public abstract class NodeHandler {
	
	
	RServiNode node;
	
	RMIAddress address;
	File dir;
	Process process;
	
	RServiBackend clientHandler;
	
	boolean isConsoleEnabled;
	
	
	public NodeHandler() {
	}
	
	
	public boolean isConsoleEnabled() {
		return this.isConsoleEnabled;
	}
	
	public void enableConsole(final String authConfig) throws RjException {
		try {
			this.isConsoleEnabled = this.node.setConsole(authConfig);
		}
		catch (final Exception e) {
			Utils.logError("An error occurred when configuring the debug console.", e);
			throw new RjException("An error occurred when configuring the debug console. See server log for detail.");
		}
	}
	
	public void disableConsole() throws RjException {
		enableConsole(null);
	}
	
	public RMIAddress getAddress() {
		return this.address;
	}
	
	String bindClient(final String name, final String host) throws RemoteException {
		final StringBuilder sb = new StringBuilder(80);
		if (name != null) {
			sb.append(name);
		}
		sb.append('@');
		sb.append(host);
		final String client = sb.toString();
		this.clientHandler = this.node.bindClient(client);
		return client;
	}
	
	void unbindClient() throws RemoteException {
		this.clientHandler = null;
		this.node.unbindClient();
	}
	
	void shutdown() throws RemoteException {
		this.clientHandler = null;
		final RServiNode node = this.node;
		this.node = null;
		if (node != null) {
			node.shutdown();
		}
	}
	
}

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

package de.walware.rj.servi.pool;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RMIRegistry {
	
	
	public static String getItemAddress(final String host, final int port, final String name) {
		final StringBuilder sb = new StringBuilder();
		sb.append("rmi://");
		sb.append(host);
		if (port != Registry.REGISTRY_PORT) {
			sb.append(":");
			sb.append(port);
		}
		sb.append("/");
		sb.append(name);
		return sb.toString();
	}
	
	
	public final Registry registry;
	
	public final String host;
	public final int port;
	
	
	public RMIRegistry(final String host, final int port, final boolean validate) throws RemoteException {
		this.host = host;
		this.port = (port >= 0) ? port : Registry.REGISTRY_PORT;
		this.registry = LocateRegistry.getRegistry(host, port);
		if (validate) {
			this.registry.list();
		}
	}
	
	public RMIRegistry(final String host, final int port, final Registry registry) throws RemoteException {
		this.host = host;
		this.port = port;
		this.registry = registry;
		this.registry.list();
	}
	
	public String getItemAddress(final String name) {
		return getItemAddress(this.host, this.port, name);
	}
	
	
}

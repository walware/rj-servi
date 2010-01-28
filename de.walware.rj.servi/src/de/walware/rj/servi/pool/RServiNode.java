/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/RJ-Project (www.walware.de/goto/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.pool;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.walware.rj.RjException;
import de.walware.rj.servi.internal.RServiBackend;
import de.walware.rj.services.RPlatform;


public interface RServiNode extends Remote {
	
	
	boolean setConsole(String authConfig) throws RjException, RemoteException;
	
	void ping() throws RemoteException;
	
	RPlatform getPlatform() throws RemoteException;
	String getPoolHost() throws RemoteException;
	RServiBackend bindClient(String client) throws RemoteException;
	void unbindClient() throws RemoteException;
	
	void shutdown() throws RemoteException;
	
	int getEvalTime() throws RemoteException;
	
}

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

package de.walware.rj.servi.pool;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.walware.rj.RjException;
import de.walware.rj.servi.internal.RServiBackend;


public interface RServiNode extends Remote {
	
	
	void ping() throws RemoteException;
	
	String getPoolHost() throws RemoteException;
	RServiBackend bindClient(String client) throws RemoteException;
	void unbindClient() throws RemoteException;
	
	void shutdown() throws RemoteException;
	
	int getEvalTime() throws RemoteException;
	
	boolean setConsole(String authConfig) throws RjException, RemoteException;
	
	/**
	 * Runs the given code in R
	 * 
	 * @param code the R code
	 * @throws RjException if an R error occurred when running the snippet
	 * @throws RemoteException if an RMI/communication error occurred
	 */
	void runSnippet(String code) throws RjException, RemoteException;
	
}

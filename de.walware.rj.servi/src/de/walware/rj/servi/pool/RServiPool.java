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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.NoSuchElementException;

import javax.security.auth.login.LoginException;

import de.walware.rj.RjException;
import de.walware.rj.server.ServerLogin;
import de.walware.rj.servi.RServi;


/**
 * Generic interface to a pool providing RServi instances.
 */
public interface RServiPool extends Remote {
	
	
	/**
	 * Requests a {@link RServi} instance from this pool.
	 * 
	 * <p>The R services returned by this method are available for exclusive usage
	 * by the caller (consumer). The consumer is responsible to return it to the pool
	 * by {@link RServi#close() closing} the RServi.
	 * 
	 * @param name a name which can be used to identify the client
	 * @param login not yet used
	 * @return an R services node
	 * 
	 * @throws NoSuchElementException if there is currently no free RServi
	 *     instance available. A later call with the same configuration 
	 *     can be successfully.
	 * @throws RjException when an server error occurs (retry not promising)
	 * @throws LoginException when the login failed
	 * @throws RemoteException when communication or runtime error occurs
	 */
	RServi getRServi(String name, ServerLogin login) throws NoSuchElementException, RjException, LoginException, RemoteException;
	
}

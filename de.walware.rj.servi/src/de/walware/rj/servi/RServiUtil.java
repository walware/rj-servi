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

package de.walware.rj.servi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.NoSuchElementException;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.EmbeddedRServiManager;
import de.walware.rj.servi.pool.RServiPool;


/**
 * The class provides utility methods for the work with {@link RServi}.
 * <p>
 * It is not intend to instance the class.</p>
 */
public class RServiUtil {
	
	
	public static final String RJ_SERVI_ID = "de.walware.rj.servi";
	public static final String RJ_CLIENT_ID = "de.walware.rj.client";
	
	
	/**
	 * Requests a {@link RServi} instance from a pool. The pool must be accessible
	 * via RMI under the given address.
	 * <p>
	 * The R services returned by this method are available for exclusive usage
	 * by the caller (consumer). The consumer is responsible to return it to the pool
	 * by {@link RServi#close() closing} the RServi.
	 * 
	 * @param address the RMI address of the pool
	 * @param name a name which can be used to identify the client
	 * @return a reference to the RServi instance
	 * @throws CoreException if the operation was failed; the status
	 *     of the exception contains detail about the cause
	 * @throws NoSuchElementException if there is currently no free RServi
	 *     instance available. A later call with the same configuration 
	 *     can be successfully.
	 * @throws LoginException if the RServi request requires authentication
	 */
	public static RServi getRServi(final String address, final String name) throws CoreException, NoSuchElementException, LoginException {
		RServiPool pool;
		try {
			pool = (RServiPool) Naming.lookup(address);
		}
		catch (final MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, RJ_SERVI_ID, 0,
					"Invalid address for the RServi pool.", e));
		}
		catch (final NotBoundException e) {
			throw new CoreException(new Status(IStatus.ERROR, RJ_SERVI_ID, 0,
					"The address does not point to a valid RServi pool.", e));
		}
		catch (final ClassCastException e) {
			throw new CoreException(new Status(IStatus.ERROR, RJ_SERVI_ID, 0,
					"The address does not point to a valid/compatible RServi pool.", e));
		}
		catch (final RemoteException e) {
			throw new CoreException(new Status(IStatus.ERROR, RJ_SERVI_ID, 0,
					"Failed looking for RServi pool in the RMI registry.", e));
		}
		try {
			return pool.getRServi(name, null);
		}
		catch (final RjException e) {
			throw new CoreException(new Status(IStatus.ERROR, RJ_SERVI_ID, 0,
					"Failed getting an RServi instance from the RServi pool.", e));
		}
		catch (final RemoteException e) {
			throw new CoreException(new Status(IStatus.ERROR, RJ_SERVI_ID, 0,
					"Failed looking for RServi pool in the RMI registry.", e));
		}
	}
	
	/**
	 * Requests a {@link RServi} instance from the given manager. The manager must be
	 * configured and started.
	 * <p>
	 * The R services returned by this method are available for exclusive usage
	 * by the caller (consumer). The consumer is responsible to return it to the manager
	 * by {@link RServi#close() closing} the RServi.
	 * 
	 * @param manager manager for embedded RServi
	 * @param name a name which can be used to identify the client
	 * @return a reference to the RServi instance
	 * @throws CoreException if the operation was failed; the status
	 *     of the exception contains detail about the cause
	 * @throws NoSuchElementException if there is currently no free RServi
	 *     instance available. A later call with the same configuration 
	 *     can be successfully.
	 */
	public static RServi getRServi(final EmbeddedRServiManager manager, final String name) throws CoreException, NoSuchElementException {
		if (manager == null) {
			throw new CoreException(new Status(IStatus.ERROR, RJ_SERVI_ID, 0,
					"Embedded RServi instance is not available.", null));
		}
		try {
			return manager.getRServi(name);
		}
		catch (final RjException e) {
			throw new CoreException(new Status(IStatus.ERROR, RJ_SERVI_ID, 0,
					"Failed getting an embedded RServi instance.", e));
		}
	}
	
	
	private RServiUtil() {
	}
	
}

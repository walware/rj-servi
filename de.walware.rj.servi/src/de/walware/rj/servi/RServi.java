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

package de.walware.rj.servi;

import org.eclipse.core.runtime.CoreException;

import de.walware.rj.services.RService;


/**
 * A RServi provides {@link RService R services} as background computing engine
 * without an interactive R console. Usually a RServi is connected to
 * a server side R engine.
 * <p>
 * How to receive an RServi instance depends on the application.
 * The RServi framework includes a server side RServi 
 * {@link de.walware.rj.servi.pool.RServiPool pool}
 * which can be used to provide RServi instances. To get a RServi instance
 * from such a pool which made available via RMI {@link de.walware.rj.servi.pool.RServiUtil RServiUtil}
 * provides helper methods to request a RServi instance.</p>
 * <p>
 * In many applications it is sufficient to use an RServi for a direct 
 * sequence of executions, so that request, evaluations and closing can be
 * performed one after another in a single thread. If it is required to use
 * the RServi multiple times (e.g. a large data set should be reused),
 * the application must prevent concurrent access.</p>
 * <p>
 * A RServi instance must be closed if it is no longer used,
 * so that the R service consumer doesn't block the resources.
 * When using a RServi pool closing means that the RServi is returned
 * to the pool and can can be used by other consumers.</p>
 * <p>
 * After closing the RServi all resources created by it, R data objects and
 * files on the R host system are cleaned up. Client side R data objects and
 * files are not affected by the clean up. After calling this method,
 * the RServi instance can no longer be used; further function call will 
 * throw an exception.</p>
 * <p>
 * All common guidelines of {@link RService} should be taken into account.</p>
 */
public interface RServi extends RService {
	
	/**
	 * Closes this RServi instance. An cleanup of resources created by this RServi
	 * is automatically performed.
	 * <p>
	 * See {@link RServi class comment} for detail.</p>
	 * <p>
	 * After calling the method the RServi instance is closed even if the method
	 * thrown an exception.</p>
	 * 
	 * @throws CoreException if the operation failed; the status
	 *     of the exception contains detail about the cause
	 */
	public void close() throws CoreException;
	
}

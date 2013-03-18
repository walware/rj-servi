/*******************************************************************************
 * Copyright (c) 2009-2013 Stephan Wahlbrink (WalWare.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.pool;

import java.util.NoSuchElementException;

import de.walware.rj.RjException;
import de.walware.rj.servi.RServi;


/**
 * Manager for an embedded RServi instances.
 * 
 * @see RServiImplS
 * @see RServiImplE
 */
public interface EmbeddedRServiManager {
	
	String getId();
	
	/**
	 * Returns the R factory passed during creation.
	 * 
	 * @return the factory
	 */
	RServiNodeFactory getFactory();
	
	/**
	 * Starts the RServi respectively R instance.
	 * Does nothing if already started.
	 * 
	 * @throws RjException if the node could not be started
	 */
	void start() throws RjException;
	
	/**
	 * Stops the RServi respectively R instance.
	 * Does nothing if not started.
	 */
	void stop();
	
	/**
	 * Returns the RServi instance.
	 * 
	 * @param name
	 * @return the RServi instance
	 * @throws NoSuchElementException if the RServi instance is already in use
	 * @throws RjException if an error occurred
	 */
	RServi getRServi(final String name) throws NoSuchElementException, RjException;
	
}

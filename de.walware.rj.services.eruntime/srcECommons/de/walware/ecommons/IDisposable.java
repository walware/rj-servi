/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons;


/**
 * The interface indicates that the object (service) which implements that
 * interface must be disposed if it is no longer required.
 * 
 * This is a common interface and the responsibility calling the 
 * {@link #dispose() dispose method} must be documented in the more concrete
 * context.
 */
public interface IDisposable {
	
	
	/**
	 * Disposes of this object.
	 * 
	 * All resources must be freed.  All listeners must be detached.  All
	 * states must be saved. Dispose will only be called once during the life
	 * cycle of a service.
	 */
	public void dispose();
	
}

/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import de.walware.rj.servi.acommons.pool.ObjectPoolItem;


public interface PoolListener {
	
	
	void initializing(ObjectPoolItem poolObj);
	void initialized(ObjectPoolItem poolObj);
	
	void evicting(ObjectPoolItem poolObj);
	void evicted(ObjectPoolItem poolObj);
	
}

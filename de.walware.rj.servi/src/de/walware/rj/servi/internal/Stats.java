/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
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


public class Stats implements PoolListener {
	
	
	public final static int MAX_USAGE = 1;
	public final static int VALIDATION_FAILED = 2;
	
	public final static int EXHAUSTED_FAILED = 3;
	public final static int UNEXPECTED_FAILED = 4;
	
	
	static class NodeEntry {
		int shutdownReason;
	}
	
	
//	private final ArrayList<NodeEntry> fTempList = new ArrayList<NodeEntry>();
	
	
	@Override
	public void initializing(final ObjectPoolItem poolObj) {
	}
	
	@Override
	public void initialized(final ObjectPoolItem poolObj) {
//		this.fTempList.add(poolObj.stats);
	}
	
	@Override
	public void evicting(final ObjectPoolItem poolObj) {
	}
	
	@Override
	public void evicted(final ObjectPoolItem poolObj) {
	}
	
	public void logServUsage(final int borrowTime, final int evalTime) {
	}
	
	public void logServRequestFailed(final int reason) {
	}
	
}

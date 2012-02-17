/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/RJ-Project (www.walware.de/goto/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.pool;

import de.walware.rj.RjException;


public interface RServiPoolManager {
	
	
	String POOLCONFIG_NAME = "poolconfig";
	String RCONFIG_NAME = "rconfig";
	
	
	class Counter {
		public int numIdling;
		public int numInUse;
		public int numTotal;
		public int maxIdling;
		public int maxInUse;
		public int maxTotal;
		
		public Counter() {}
	}
	
	
	String getId();
	
	void setConfig(PoolConfig config);
	PoolConfig getConfig();
	void addNodeFactory(RServiNodeFactory nodeFactory);
	RServiNodeFactory getFactories();
	
	void init() throws RjException;
	void stop(int mode) throws RjException;
	
	RServiPoolManager.Counter getCounter();
	Object[] getPoolItemsData();
	
}

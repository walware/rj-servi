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

package de.walware.rj.servi.internal;

import java.util.ArrayList;


public class Stats {
	
	
	public final static int MAX_USAGE = 1;
	public final static int VALIDATION_FAILED = 2;
	
	public final static int EXHAUSTED_FAILED = 3;
	public final static int UNEXPECTED_FAILED = 4;
	
	
	static class NodeEntry {
		int shutdownReason;
	}
	
	
	private final ArrayList<NodeEntry> fTempList = new ArrayList<NodeEntry>();
	
	
	public void logServUsage(final int borrowTime, final int evalTime) {
	}
	
	public void logNodeUsageBegin(final NodeHandler poolObj) {
//		this.fTempList.add(poolObj.stats);
	}
	public void logNodeUsageEnd(final NodeHandler poolObj) {
	}
	
	public void logServRequestFailed(final int reason) {
	}
	
}

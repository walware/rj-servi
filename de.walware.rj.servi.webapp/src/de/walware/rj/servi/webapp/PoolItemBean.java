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

package de.walware.rj.servi.webapp;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.PoolItem;


public class PoolItemBean extends PoolItem {
	
	
	public PoolItemBean(final Object data, final long stamp) {
		super(data, stamp);
	}
	
	
	public String actionEnableConsole() {
		try {
			super.enableConsole("none");
		}
		catch (final RjException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
		}
		return null;
	}
	
	public String actionDisableConsole() {
		try {
			super.disableConsole();
		}
		catch (final RjException e) {
			FacesUtils.addErrorMessage(null, e.getMessage());
		}
		return null;
	}
	
}

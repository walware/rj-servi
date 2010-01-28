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

package de.walware.rj.servi.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.walware.rj.server.RjsComObject;


public interface RServiBackend extends Remote {
	
	RjsComObject runMainLoop(RjsComObject com) throws RemoteException;
	RjsComObject runAsync(RjsComObject com) throws RemoteException;
	
}

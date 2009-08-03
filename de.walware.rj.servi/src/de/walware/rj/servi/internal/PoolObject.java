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

package de.walware.rj.servi.internal;

import static org.apache.commons.pool.ObjectPoolItem.State.LENT;

import java.io.File;
import java.rmi.server.Unreferenced;
import java.util.logging.Level;

import org.apache.commons.pool.ObjectPoolItem;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.RServiNode;
import de.walware.rj.services.RPlatform;


public class PoolObject implements RServiImpl.PoolRef, Unreferenced {
	
	final ObjectPoolItem item;
	final Stats.NodeEntry stats = new Stats.NodeEntry();
	
	RServiNode node;
	
	String address;
	File dir;
	
	RServiBackend clientHandler;
	RPlatform rInfo;
	
	boolean isConsoleEnabled;
	
	
	public PoolObject(final ObjectPoolItem item) {
		this.item = item;
	}
	
	public long getAccessId() {
		final long clientId = this.item.getClientId();
		if (clientId == -1L) {
			throw new IllegalAccessError();
		}
		return clientId;
	}
	
	public void returnObject(final long accessId) throws RjException {
		try {
			synchronized(this.item) {
				if (this.item.getClientId() != accessId) {
					throw new IllegalStateException("Access id no longer valid.");
				}
				this.item.invalidateClient();
			}
			this.item.getPool().returnObject(this.item);
		}
		catch (final Exception e) {
			PoolManager.LOGGER.log(Level.SEVERE, "An unexpected error occurred when returning RServi instance.", e);
			throw new RjException("An unexpected error occurred when closing RServi instance. See server log for detail.");
		}
	}
	
	public void unreferenced() {
		synchronized (this.item) {
			if (this.item.getState() != LENT
					|| this.item.getClientId() == -1L) {
				return;
			}
			this.item.invalidateClient();
		}
		PoolManager.LOGGER.log(Level.INFO, "The RServi instance is lent and unreferenced. It will returnted now.");
		try {
			this.item.getPool().returnObject(this.item);
		}
		catch (final Exception e) {
			PoolManager.LOGGER.log(Level.SEVERE, "An unexpected error occurred when returning RServi instance.", e);
		}
	}
	
	public boolean isConsoleEnabled() {
		return this.isConsoleEnabled;
	}
	
	public void enableConsole(final String authConfig) throws RjException {
		try {
			this.isConsoleEnabled = this.node.setConsole(authConfig);
		}
		catch (final Exception e) {
			PoolManager.LOGGER.log(Level.SEVERE, "An error occurred when configuring the debug console.", e);
			throw new RjException("An error occurred when configuring the debug console. See server log for detail.");
		}
	}
	
	public void disableConsole() throws RjException {
		enableConsole(null);
	}
	
	public String getAddress() {
		return this.address;
	}
	
}

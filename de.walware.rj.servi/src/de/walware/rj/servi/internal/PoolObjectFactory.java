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

import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;

import org.apache.commons.pool.ObjectPoolItem;
import org.apache.commons.pool.PoolableObjectFactory;

import de.walware.rj.servi.pool.PoolConfig;


public class PoolObjectFactory implements PoolableObjectFactory {
	
	
	private static final Integer MAX_USAGE = Integer.valueOf(Stats.MAX_USAGE);
	private static final Integer VALIDATION_FAILED = Integer.valueOf(Stats.VALIDATION_FAILED);
	
	
	private final NodeFactory nodeFactory;
	
	private int maxUsageCount;
	
	private final Stats stats;
	
	
	public PoolObjectFactory(final NodeFactory factory, final Stats stats, final PoolConfig config) {
		this.nodeFactory = factory;
		this.stats = stats;
		setConfig(config);
	}
	
	
	public void setConfig(final PoolConfig config) {
		this.maxUsageCount = config.getMaxUsageCount();
	}
	
	
	public void makeObject(final ObjectPoolItem item) throws Exception {
		// start
		final PoolObject poolObj = new PoolObject(item);
		this.nodeFactory.createNode(poolObj);
		UnicastRemoteObject.exportObject(poolObj, 0);
		this.stats.logNodeUsageBegin(poolObj);
		item.setObject(poolObj);
	}
	
	
	public String activateObject(final ObjectPoolItem item, final Object arg) throws Exception {
		final PoolObject poolObj = (PoolObject) item.getObject();
		final StringBuilder sb = (arg != null) ? new StringBuilder((String) arg) : new StringBuilder();
		String clientHost;
		try {
			clientHost = RemoteServer.getClientHost();
		}
		catch (final ServerNotActiveException e) {
			clientHost = poolObj.node.getPoolHost();
		}
		sb.append('@');
		sb.append(clientHost);
		final String label = sb.toString();
		poolObj.clientHandler = poolObj.node.bindClient(label);
		return label;
	}
	
	public void passivateObject(final ObjectPoolItem item) throws Exception {
		final PoolObject poolObj = (PoolObject) item.getObject();
		poolObj.node.unbindClient();
		poolObj.clientHandler = null;
	}
	
	public void destroyObject(final ObjectPoolItem item) throws Exception {
		final PoolObject poolObj = (PoolObject) item.getObject();
		this.stats.logNodeUsageEnd(poolObj);
		if (poolObj.node != null) {
			try {
				poolObj.node.shutdown();
			}
			catch (final Throwable e) {
				PoolManager.LOGGER.log(Level.WARNING, "An exception was thrown when trying to shutdown the node", e);
			}
		}
		try {
			UnicastRemoteObject.unexportObject(poolObj, true);
		}
		catch (final Throwable e) {
			PoolManager.LOGGER.log(Level.WARNING, "An exception was thrown when trying to unexport the node", e);
		}
		this.nodeFactory.cleanupNode(poolObj);
		poolObj.node = null;
		poolObj.clientHandler = null;
	}
	
	public boolean validateObject(final ObjectPoolItem item) {
		final PoolObject poolObj = (PoolObject) item.getObject();
		if (this.maxUsageCount > 0 && item.getLentCount() > this.maxUsageCount) {
			poolObj.stats.shutdownReason = MAX_USAGE;
			return false;
		}
		return true;
	}
	
}

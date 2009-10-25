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
		String clientHost;
		try {
			clientHost = RemoteServer.getClientHost();
		}
		catch (final ServerNotActiveException e) {
			clientHost = poolObj.node.getPoolHost();
		}
		return poolObj.bindClient((String) arg, clientHost);
	}
	
	public void passivateObject(final ObjectPoolItem item) throws Exception {
		final PoolObject poolObj = (PoolObject) item.getObject();
		poolObj.unbindClient();
	}
	
	public void destroyObject(final ObjectPoolItem item) throws Exception {
		final PoolObject poolObj = (PoolObject) item.getObject();
		this.stats.logNodeUsageEnd(poolObj);
		try {
			poolObj.shutdown();
		}
		catch (final Throwable e) {
			PoolManager.LOGGER.log(Level.WARNING, Messages.ShutdownNode_error_message, e);
		}
		try {
			UnicastRemoteObject.unexportObject(poolObj, true);
		}
		catch (final Throwable e) {
			PoolManager.LOGGER.log(Level.WARNING, Messages.RmiUnexportNode_error_message, e);
		}
		this.nodeFactory.cleanupNode(poolObj);
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

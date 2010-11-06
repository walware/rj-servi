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
import java.rmi.server.UnicastRemoteObject;
import java.util.NoSuchElementException;

import org.apache.commons.pool.ObjectPoolItem;
import org.apache.commons.pool.impl.ExtGenericObjectPool;
import org.apache.commons.pool.impl.ExtGenericObjectPool.Config;

import de.walware.ecommons.net.RMIRegistry;

import de.walware.rj.RjException;
import de.walware.rj.RjInitFailedException;
import de.walware.rj.server.ServerLogin;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.RServiNodeFactory;
import de.walware.rj.servi.pool.RServiPool;
import de.walware.rj.servi.pool.RServiPoolManager;


public class PoolManager implements RServiPool, RServiPoolManager {
	
	
	private final String id;
	
	private final RMIRegistry registry;
	
	private Remote thisRemote;
	
	private ExtGenericObjectPool pool;
	private PoolObjectFactory poolFactory;
	private PoolConfig poolConfig;
	
	
	private NodeFactory nodeFactory;
	
	private final Stats stats;
	
	
	public PoolManager(final String id, final RMIRegistry registry) {
		if (id == null || registry == null) {
			throw new NullPointerException();
		}
		this.id = id;
		this.registry = registry;
		this.stats = new Stats();
		this.poolConfig = new PoolConfig();
		
		Utils.preLoad();
	}
	
	
	public String getId() {
		return this.id;
	}
	
	public NodeFactory getFactories() {
		return this.nodeFactory;
	}
	
	public synchronized void addNodeFactory(final RServiNodeFactory factory) {
		this.nodeFactory = (NodeFactory) factory;
	}
	
	public synchronized void setConfig(PoolConfig config) {
		config = new PoolConfig(config);
		if (this.pool != null) {
			this.pool.setConfig(createConfig(config));
			this.poolFactory.setConfig(config);
		}
		this.poolConfig = config;
	}
	
	public PoolConfig getConfig() {
		final PoolConfig config = this.poolConfig;
		if (config != null) {
			return new PoolConfig(config);
		}
		return null;
	}
	
	public synchronized void init() throws RjException {
		this.poolFactory = new PoolObjectFactory(this.nodeFactory, this.stats, this.poolConfig);
		this.pool = new ExtGenericObjectPool(this.poolFactory, createConfig(this.poolConfig));
		
		Utils.logInfo("Publishing pool in registry...");
		if (this.registry != null) {
			try {
				this.thisRemote = UnicastRemoteObject.exportObject(this, 0);
				this.registry.getRegistry().rebind(PoolConfig.getPoolName(this.id), this.thisRemote);
			}
			catch (final Exception e) {
				try {
					stop(8);
				}
				catch (final Exception ignore) {}
				Utils.logError("An error occurred when binding the pool in the registry.", e);
				throw new RjInitFailedException("An error occurred when publishing the pool in the registry.");
			}
		}
	}
	
	public Object[] getPoolItemsData() {
		return this.pool.getItems();
	}
	
	public synchronized void stop(final int mode) throws RjException {
		Utils.logInfo("Unpublishing pool...");
		if (this.registry != null) {
			try {
				this.registry.getRegistry().unbind(PoolConfig.getPoolName(this.id));
			}
			catch (final Exception e) {
				if (mode != 8) {
					Utils.logError("An error occurred when unbinding the pool from the registry.", e);
				}
			}
		}
		if (this.thisRemote != null) {
			try {
				this.thisRemote = null;
				UnicastRemoteObject.unexportObject(this, true);
			}
			catch (final Exception e) {
				if (mode != 8) {
					Utils.logError("An error occurred when unexport the pool.", e);
				}
			}
		}
		
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
		}
		if (PoolManager.this.pool != null) {
			Utils.logInfo("Closing R nodes...");
			try {
				PoolManager.this.pool.close();
			}
			catch (final Exception e) {
				Utils.logError("An error occurred when closing the pool.", e);
			}
			finally {
				Runtime.getRuntime().gc();
			}
		}
	}
	
	private Config createConfig(final PoolConfig config) {
		final Config poolConfig = new Config();
		poolConfig.lifo = true;
		poolConfig.testOnReturn = true;
		poolConfig.testWhileIdle = false;
		poolConfig.testOnBorrow = false;
		poolConfig.whenExhaustedAction = 1;
		poolConfig.maxActive = config.getMaxTotalCount();
		poolConfig.maxWait = config.getMaxWaitTime();
		poolConfig.minIdle = config.getMinIdleCount();
		poolConfig.maxIdle = config.getMaxIdleCount();
		poolConfig.minEvictableIdleTimeMillis = 0L;
		poolConfig.softMinEvictableIdleTimeMillis = config.getMinIdleTime();
		poolConfig.timeBetweenEvictionRunsMillis = 7500L;
		poolConfig.numTestsPerEvictionRun = -3;
		return poolConfig;
	}
	
	public RServi getRServi(final String name, final ServerLogin login) throws NoSuchElementException, RjException {
		final PoolObject poolObject = getPoolObject(name);
		return new RServiImpl(poolObject.getAccessId(), poolObject, poolObject.clientHandler);
	}
	
	public PoolObject getPoolObject(final String client) throws NoSuchElementException, RjException {
		try {
			final ObjectPoolItem item = this.pool.borrowObject(client);
			final PoolObject poolObj = (PoolObject)item.getObject();
			return poolObj;
		}
		catch (final NoSuchElementException e) {
			this.stats.logServRequestFailed(3);
			throw new NoSuchElementException(Messages.GetRServi_NoInstance_pub_Pool_message);
		}
		catch (final Exception e) {
			this.stats.logServRequestFailed(4);
			Utils.logError(Messages.BindClient_error_message, e);
			throw new RjException(Messages.GetRServi_pub_error_message);
		}
	}
	
	public RServiPoolManager.Counter getCounter() {
		final RServiPoolManager.Counter counter = new RServiPoolManager.Counter();
		synchronized (this.pool) {
			counter.numIdling = this.pool.getNumIdle();
			counter.numInUse = this.pool.getNumActive();
			counter.maxIdling = this.pool.getStatMaxIdle();
			counter.maxInUse = this.pool.getStatMaxActive();
			counter.maxTotal = this.pool.getStatMaxTotal();
		}
		counter.numTotal = counter.numIdling + counter.numTotal;
		return counter;
	}
	
}

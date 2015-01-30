/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import de.walware.ecommons.FastList;

import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.acommons.pool.PoolableObjectFactory;


public class PoolObjectFactory implements PoolableObjectFactory {
	
	
	private final NodeFactory nodeFactory;
	
	private int maxUsageCount;
	
	private final FastList<PoolListener> poolListeners;
	
	private RMIClientSocketFactory sslClientSocketFactory;
	private RMIServerSocketFactory sslServerSocketFactory;
	
	
	public PoolObjectFactory(final NodeFactory factory, final FastList<PoolListener> poolListeners) {
		this.nodeFactory = factory;
		this.poolListeners = poolListeners;
	}
	
	
	public void setMaxUsageCount(final int count) {
		this.maxUsageCount = count;
	}
	
	
	@Override
	public void makeObject(final ObjectPoolItem item) throws Exception {
		{	final PoolListener[] listeners = this.poolListeners.toArray();
			for (int i = 0; i < listeners.length; i++) {
				try {
					listeners[i].initializing(item);
				}
				catch (final Exception e) {
					e.printStackTrace(); // TODO
				}
			}
		}
		
		// start
		final PoolObject poolObj = new PoolObject(item);
		this.nodeFactory.createNode(poolObj);
		
		{	RMIClientSocketFactory clientSocketFactory = null;
			RMIServerSocketFactory serverSocketFactory = null;
			if (poolObj.address.isSSL()) {
				synchronized (this) {
					if (this.sslClientSocketFactory == null) {
						this.sslClientSocketFactory = new SslRMIClientSocketFactory();
						this.sslServerSocketFactory = new SslRMIServerSocketFactory(null, null, true);
					}
					clientSocketFactory = this.sslClientSocketFactory;
					serverSocketFactory = this.sslServerSocketFactory;
				}
			}
			poolObj.thisRemote = UnicastRemoteObject.exportObject(poolObj, 0,
					clientSocketFactory, serverSocketFactory );
		}
		item.setObject(poolObj);
		
		{	final PoolListener[] listeners = this.poolListeners.toArray();
			for (int i = 0; i < listeners.length; i++) {
				try {
					listeners[i].initialized(item);
				}
				catch (final Exception e) {
					e.printStackTrace(); // TODO
				}
			}
		}
	}
	
	
	@Override
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
	
	@Override
	public void passivateObject(final ObjectPoolItem item) throws Exception {
		final PoolObject poolObj = (PoolObject) item.getObject();
		poolObj.unbindClient();
	}
	
	@Override
	public void destroyObject(final ObjectPoolItem item) throws Exception {
		final PoolObject poolObj = (PoolObject) item.getObject();
		
		{	final PoolListener[] listeners = this.poolListeners.toArray();
			for (int i = 0; i < listeners.length; i++) {
				try {
					listeners[i].evicting(item);
				}
				catch (final Exception e) {
					e.printStackTrace(); // TODO
				}
			}
		}
		
		if (poolObj.thisRemote != null) {
			try {
				poolObj.thisRemote = null;
				UnicastRemoteObject.unexportObject(poolObj, true);
			}
			catch (final Throwable e) {
				Utils.logWarning(Messages.RmiUnexportNode_error_message, e);
			}
		}
		this.nodeFactory.stopNode(poolObj);
		
		{	final PoolListener[] listeners = this.poolListeners.toArray();
			for (int i = 0; i < listeners.length; i++) {
				try {
					listeners[i].evicted(item);
				}
				catch (final Exception e) {
					e.printStackTrace(); // TODO
				}
			}
		}
	}
	
	@Override
	public boolean validateObject(final ObjectPoolItem item) {
		final PoolObject poolObj = (PoolObject) item.getObject();
		if (this.maxUsageCount > 0 && item.getLentCount() > this.maxUsageCount) {
			poolObj.stats.shutdownReason = Stats.MAX_USAGE;
			return false;
		}
		return true;
	}
	
}

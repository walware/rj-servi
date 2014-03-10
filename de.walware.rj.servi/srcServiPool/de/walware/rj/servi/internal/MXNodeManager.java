/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.JMException;

import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem.State;
import de.walware.rj.servi.pool.PoolServer;


public class MXNodeManager implements PoolListener {
	
	
	private final PoolServer server;
	
	private final Map<ObjectPoolItem, MXNode> nodes = new HashMap<ObjectPoolItem, MXNode>();
	
	private final PoolManager poolManager;
	
	
	public MXNodeManager(final PoolServer server, final PoolManager poolManager) {
		this.server = server;
		this.poolManager = poolManager;
	}
	
	
	@Override
	public void initializing(final ObjectPoolItem poolItem) {
	}
	
	@Override
	public void initialized(final ObjectPoolItem poolItem) {
		final MXNode node;
		synchronized (this) {
			if (this.nodes.containsKey(poolItem)) {
				return;
			}
			node = new MXNode(this.server, poolItem);
			this.nodes.put(poolItem, node);
		}
		try {
			node.initJM();
		}
		catch (final JMException e) {
			Utils.logError("An error occurred when initializing JMX for node '" + node.getId() + "'.", e);
		}
	}
	
	@Override
	public void evicting(final ObjectPoolItem poolItem) {
	}
	
	@Override
	public void evicted(final ObjectPoolItem poolItem) {
		final MXNode node = this.nodes.remove(poolItem);
		if (node != null) {
			dispose(node);
		}
	}
	
	private void dispose(final MXNode node) {
		try {
			node.disposeJM();
		}
		catch (final JMException e) {
			Utils.logError("An error occurred when disposing JMX for node '" + node.getId() + "'.", e);
		}
	}
	
	public void activate() {
		this.poolManager.addPoolListener(this);
		
		final ObjectPoolItem[] itemsData = this.poolManager.getPoolItemsData();
		synchronized (this) {
			for (final ObjectPoolItem poolItem : itemsData) {
				final Object object = poolItem.getObject();
				if (object == null || poolItem.getState() == State.EVICTING) {
					continue;
				}
				initialized(poolItem);
			}
		}
	}
	
	public void deactivate() {
		this.poolManager.removePoolListener(this);
		
		synchronized (this) {
			try {
				for (final Entry<ObjectPoolItem, MXNode> entry : this.nodes.entrySet()) {
					dispose(entry.getValue());
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
			finally {
				this.nodes.clear();
			}
		}
	}
	
}

/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import java.lang.management.ManagementFactory;
import java.util.Date;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.OperationsException;

import de.walware.ecommons.net.RMIAddress;

import de.walware.rj.RjException;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.jmx.NodeMXBean;
import de.walware.rj.servi.jmx.NodeStateMX;
import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.PoolItem;
import de.walware.rj.servi.pool.PoolServer;


public class MXNode implements NodeMXBean {
	
	
	private final PoolServer server;
	private final ObjectPoolItem itemData;
	private final PoolObject handler;
	private final RMIAddress address;
	private final Date creationTime;
	
	private ObjectName jmName;
	
	
	public MXNode(final PoolServer server, final ObjectPoolItem itemData) {
		this.server = server;
		this.itemData = itemData;
		this.handler = (PoolObject) itemData.getObject();
		this.address = this.handler.getAddress();
		if (this.address == null) {
			throw new NullPointerException("address");
		}
		this.creationTime = new Date(this.itemData.getCreationTime());
	}
	
	
	public void initJM() throws JMException {
		this.jmName = new ObjectName(this.server.getJMBaseName() + "type=Server.Node,rservi.nodeId=" + this.address.getName());
		ManagementFactory.getPlatformMBeanServer().registerMBean(this, this.jmName);
	}
	
	public void disposeJM() throws JMException {
		if (this.jmName != null) {
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.jmName);
			this.jmName = null;
		}
	}
	
	
	@Override
	public String getId() {
		return this.address.getName();
	}
	
	@Override
	public Date getCreationTime() {
		return this.creationTime;
	}
	
	@Override
	public NodeStateMX getState() {
		final PoolItem item = new PoolItem(this.itemData, System.currentTimeMillis());
		return new MXNodeState(item);
	}
	
	@Override
	public boolean isConsoleEnabled() {
		return this.handler.isConsoleEnabled();
	}
	
	@Override
	public synchronized void setConsoleEnabled(final boolean enable) throws OperationsException {
		try {
			if (enable) {
				this.handler.enableConsole("none");
			}
			else {
				this.handler.disableConsole();
			}
		}
		catch (final RjException e) {
			throw new OperationsException(e.getMessage());
		}
	}
	
	@Override
	public void stop() throws OperationsException {
		final PoolConfig config = new PoolConfig();
		this.server.getPoolConfig(config);
		this.handler.evict(config.getEvictionTimeout());
	}
	
	@Override
	public void stop(final long timeoutMillis) throws OperationsException {
		if (timeoutMillis < 0) {
			throw new OperationsException("Invalid parameter 'timeoutMillis' >= 0.");
		}
		this.handler.evict(timeoutMillis);
	}
	
	@Override
	public void kill() throws OperationsException {
		this.handler.evict(0);
	}
	
}

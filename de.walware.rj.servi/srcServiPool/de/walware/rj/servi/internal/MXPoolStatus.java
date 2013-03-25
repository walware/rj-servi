/*******************************************************************************
 * Copyright (c) 2009-2013 Stephan Wahlbrink (WalWare.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.internal;

import java.lang.management.ManagementFactory;
import java.util.Date;

import javax.management.JMException;
import javax.management.ObjectName;

import de.walware.rj.servi.jmx.NodeStateMX;
import de.walware.rj.servi.jmx.PoolStatusMX;
import de.walware.rj.servi.pool.PoolItem;
import de.walware.rj.servi.pool.PoolServer;
import de.walware.rj.servi.pool.PoolStatus;


public class MXPoolStatus extends PoolStatus<NodeStateMX> implements PoolStatusMX {
	
	
	private ObjectName jmName;
	
	private Date time;
	
	
	public MXPoolStatus(final PoolServer server) {
		super(server);
		
		refresh();
	}
	
	
	public void initJM() throws JMException {
		this.jmName = new ObjectName(this.server.getJMBaseName() + "type=Server.PoolStatus");
		ManagementFactory.getPlatformMBeanServer().registerMBean(this, this.jmName);
	}
	
	public void disposeJM() throws JMException {
		if (this.jmName != null) {
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.jmName);
			this.jmName = null;
		}
	}
	
	protected synchronized void refresh() {
		final long stamp = System.currentTimeMillis();
		refresh(this.server.getManager(), stamp);
		this.time = new Date(stamp);
	}
	
	
	@Override
	protected NodeStateMX createNodeState(final PoolItem item) {
		return new MXNodeState(item);
	}
	
	
	@Override
	public synchronized Date getStatusTime() {
		return this.time;
	}
	
	
}

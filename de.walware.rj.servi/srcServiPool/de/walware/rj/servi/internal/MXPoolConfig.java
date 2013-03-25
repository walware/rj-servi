/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink (WalWare.de) and others.
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

import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.OperationsException;

import de.walware.rj.servi.jmx.PoolConfigMXBean;
import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.PoolServer;


public class MXPoolConfig extends PoolConfig implements PoolConfigMXBean {
	
	
	private final PoolServer server;
	
	private ObjectName jmName;
	
	
	public MXPoolConfig(final PoolServer server) throws JMException {
		if (server == null) {
			throw new NullPointerException("server");
		}
		this.server = server;
	}
	
	
	public void initJM() throws JMException {
		this.jmName = new ObjectName(this.server.getJMBaseName() + "type=Server.PoolConfig");
		ManagementFactory.getPlatformMBeanServer().registerMBean(this, this.jmName);
	}
	
	public void disposeJM() throws JMException {
		if (this.jmName != null) {
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.jmName);
			this.jmName = null;
		}
	}
	
	
	@Override
	public void apply() throws OperationsException {
		synchronized (this) {
			MXUtil.validate(this);
			
			this.server.setPoolConfig(this);
		}
	}
	
	@Override
	public void loadActual() throws OperationsException {
		this.server.getPoolConfig(this);
	}
	
	@Override
	public void loadDefault() throws OperationsException {
		load(new PoolConfig());
	}
	
	@Override
	public void loadSaved() throws OperationsException {
		MXUtil.load(this, this.server.getRJContext());
	}
	
	@Override
	public void save() throws OperationsException {
		MXUtil.save(this, this.server.getRJContext());
	}
	
}

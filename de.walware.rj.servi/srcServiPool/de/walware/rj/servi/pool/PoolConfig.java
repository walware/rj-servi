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

package de.walware.rj.servi.pool;

import java.util.Properties;


public class PoolConfig implements PropertiesBean {
	
	public static String getPoolName(final String id) {
		return (new StringBuilder(String.valueOf(id))).append("-pool").toString();
	}
	
	
	public static final long MINUTES = 60000L;
	public static final long SECONDS = 1000L;
	
	public static final String MAX_TOTAL_COUNT_ID = "max_total.count";
	public static final String MIN_IDLE_COUNT_ID = "min_idle.count";
	public static final String MAX_IDLE_COUNT_ID = "max_idle.count";
	public static final String MIN_IDLE_TIME_ID = "min_idle.time";
	public static final String MAX_WAIT_TIME_ID = "max_wait.time";
	public static final String MAX_USAGE_COUNT_ID = "max_usage.count";
	
	
	private int maxTotalCount;
	private int minIdleCount;
	private int maxIdleCount;
	private long minIdleTime;
	private long maxWaitTime;
	private int maxUsageCount;
	
	
	public PoolConfig() {
		this.maxTotalCount = 20;
		this.minIdleCount = 1;
		this.maxIdleCount = 10;
		this.minIdleTime = 10*MINUTES;
		this.maxWaitTime = 3*SECONDS;
		this.maxUsageCount = 1000;
	}
	
	public PoolConfig(final PoolConfig templ) {
		this();
		load(templ);
	}
	
	
	public String getBeanId() {
		return RServiPoolManager.POOLCONFIG_NAME;
	}
	
	protected void load(final PoolConfig templ) {
		this.maxTotalCount = templ.maxTotalCount;
		this.minIdleCount = templ.minIdleCount;
		this.maxIdleCount = templ.maxIdleCount;
		this.minIdleTime = templ.minIdleTime;
		this.maxWaitTime = templ.maxWaitTime;
		this.maxUsageCount = templ.maxUsageCount;
	}
	
	public void load(final Properties map) {
		this.maxTotalCount = Integer.parseInt(map.getProperty(MAX_TOTAL_COUNT_ID));
		this.minIdleCount = Integer.parseInt(map.getProperty(MIN_IDLE_COUNT_ID));
		this.maxIdleCount = Integer.parseInt(map.getProperty(MAX_IDLE_COUNT_ID));
		this.minIdleTime = Long.parseLong(map.getProperty(MIN_IDLE_TIME_ID));
		this.maxWaitTime = Long.parseLong(map.getProperty(MAX_WAIT_TIME_ID));
		this.maxUsageCount = Integer.parseInt(map.getProperty(MAX_USAGE_COUNT_ID));
	}
	
	public void save(final Properties map) {
		map.setProperty(MAX_TOTAL_COUNT_ID, Integer.toString(this.maxTotalCount));
		map.setProperty(MIN_IDLE_COUNT_ID, Integer.toString(this.minIdleCount));
		map.setProperty(MAX_IDLE_COUNT_ID, Integer.toString(this.maxIdleCount));
		map.setProperty(MIN_IDLE_TIME_ID, Long.toString(this.minIdleTime));
		map.setProperty(MAX_WAIT_TIME_ID, Long.toString(this.maxWaitTime));
		map.setProperty(MAX_USAGE_COUNT_ID, Integer.toString(this.maxUsageCount));
	}
	
	public int getMaxTotalCount() {
		return this.maxTotalCount;
	}
	
	public void setMaxTotalCount(final int count) {
		this.maxTotalCount = count;
	}
	
	public int getMinIdleCount() {
		return this.minIdleCount;
	}
	
	public void setMinIdleCount(final int count) {
		this.minIdleCount = count;
	}
	
	public int getMaxIdleCount() {
		return this.maxIdleCount;
	}
	
	public void setMaxIdleCount(final int count) {
		this.maxIdleCount = count;
	}
	
	public long getMinIdleTime() {
		return this.minIdleTime;
	}
	
	public void setMinIdleTime(final long milliseconds) {
		this.minIdleTime = milliseconds;
	}
	
	public void setMaxWaitTime(final long milliseconds) {
		this.maxWaitTime = milliseconds;
	}
	
	public long getMaxWaitTime() {
		return this.maxWaitTime;
	}
	
	public void setMaxUsageCount(final int count) {
		this.maxUsageCount = count;
	}
	
	public int getMaxUsageCount() {
		return this.maxUsageCount;
	}
	
}

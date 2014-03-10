/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.pool;

import java.util.Collection;
import java.util.Properties;

import de.walware.rj.servi.internal.Utils;


public class PoolConfig implements PropertiesBean {
	
	
	public static final String BEAN_ID = "poolconfig";
	
	
	public static String getPoolName(final String id) {
		return (new StringBuilder(String.valueOf(id))).append("-pool").toString();
	}
	
	
	public static final long MINUTES = 60000L;
	public static final long SECONDS = 1000L;
	
	public static final String MAX_TOTAL_COUNT_ID = "max_total.count";
	public static final String MIN_IDLE_COUNT_ID = "min_idle.count";
	public static final String MAX_IDLE_COUNT_ID = "max_idle.count";
	public static final String MIN_IDLE_MILLIS_ID = "min_idle.millis";
	@Deprecated
	public static final String MIN_IDLE_TIME_ID = "min_idle.time";
	public static final String MAX_WAIT_MILLIS_ID = "max_wait.millis";
	@Deprecated
	public static final String MAX_WAIT_TIME_ID = "max_wait.time";
	public static final String MAX_USAGE_COUNT_ID = "max_usage.count";
	
	/**
	 * Id of timeout when evicing lent pool items
	 * 
	 * @since 2.0
	 */
	public static final String EVICT_TIMEOUT_ID = "eviction_timeout.millis";
	
	private static final long EVICT_TIMEOUT_DEFAULT = 30*MINUTES;
	
	
	private int maxTotalCount;
	private int minIdleCount;
	private int maxIdleCount;
	private long minIdleTime;
	private long maxWaitTime;
	private int maxUsageCount;
	
	private long evictTimeout;
	
	
	public PoolConfig() {
		this.maxTotalCount = 20;
		this.minIdleCount = 1;
		this.maxIdleCount = 10;
		this.minIdleTime = 10*MINUTES;
		this.maxWaitTime = 3*SECONDS;
		this.maxUsageCount = 1000;
		
		this.evictTimeout = EVICT_TIMEOUT_DEFAULT;
	}
	
	public PoolConfig(final PoolConfig config) {
		this();
		synchronized (config) {
			load(config);
		}
	}
	
	
	@Override
	public String getBeanId() {
		return BEAN_ID;
	}
	
	public synchronized void load(final PoolConfig templ) {
		this.maxTotalCount = templ.maxTotalCount;
		this.minIdleCount = templ.minIdleCount;
		this.maxIdleCount = templ.maxIdleCount;
		this.minIdleTime = templ.minIdleTime;
		this.maxWaitTime = templ.maxWaitTime;
		this.maxUsageCount = templ.maxUsageCount;
		this.evictTimeout = templ.evictTimeout;
	}
	
	@Override
	public synchronized void load(final Properties map) {
		this.maxTotalCount = Integer.parseInt(map.getProperty(MAX_TOTAL_COUNT_ID));
		this.minIdleCount = Integer.parseInt(map.getProperty(MIN_IDLE_COUNT_ID));
		this.maxIdleCount = Integer.parseInt(map.getProperty(MAX_IDLE_COUNT_ID));
		this.minIdleTime = Long.parseLong(Utils.getProperty(map, MIN_IDLE_MILLIS_ID, MIN_IDLE_TIME_ID));
		this.maxWaitTime = Long.parseLong(Utils.getProperty(map, MAX_WAIT_MILLIS_ID, MAX_WAIT_TIME_ID));
		this.maxUsageCount = Integer.parseInt(map.getProperty(MAX_USAGE_COUNT_ID));
		{	final String s = map.getProperty(EVICT_TIMEOUT_ID);
			this.evictTimeout = (s != null) ? Long.parseLong(s) : EVICT_TIMEOUT_DEFAULT;
		}
	}
	
	@Override
	public synchronized void save(final Properties map) {
		map.setProperty(MAX_TOTAL_COUNT_ID, Integer.toString(this.maxTotalCount));
		map.setProperty(MIN_IDLE_COUNT_ID, Integer.toString(this.minIdleCount));
		map.setProperty(MAX_IDLE_COUNT_ID, Integer.toString(this.maxIdleCount));
		map.setProperty(MIN_IDLE_MILLIS_ID, Long.toString(this.minIdleTime));
		map.setProperty(MAX_WAIT_MILLIS_ID, Long.toString(this.maxWaitTime));
		map.setProperty(MAX_USAGE_COUNT_ID, Integer.toString(this.maxUsageCount));
		map.setProperty(EVICT_TIMEOUT_ID, Long.toString(this.evictTimeout));
	}
	
	public synchronized int getMaxTotalCount() {
		return this.maxTotalCount;
	}
	
	public synchronized void setMaxTotalCount(final int count) {
		this.maxTotalCount = count;
	}
	
	public synchronized int getMinIdleCount() {
		return this.minIdleCount;
	}
	
	public synchronized void setMinIdleCount(final int count) {
		this.minIdleCount = count;
	}
	
	public synchronized int getMaxIdleCount() {
		return this.maxIdleCount;
	}
	
	public synchronized void setMaxIdleCount(final int count) {
		this.maxIdleCount = count;
	}
	
	public synchronized long getMinIdleTime() {
		return this.minIdleTime;
	}
	
	public synchronized void setMinIdleTime(final long milliseconds) {
		this.minIdleTime = milliseconds;
	}
	
	public synchronized void setMaxWaitTime(final long milliseconds) {
		this.maxWaitTime = milliseconds;
	}
	
	public synchronized long getMaxWaitTime() {
		return this.maxWaitTime;
	}
	
	public synchronized void setMaxUsageCount(final int count) {
		this.maxUsageCount = count;
	}
	
	public synchronized int getMaxUsageCount() {
		return this.maxUsageCount;
	}
	
	
	/**
	 * Returns the timeout when evicing lent pool items
	 * 
	 * @return the timeout in milliseconds
	 * 
	 * @since 2.0
	 */
	public synchronized long getEvictionTimeout() {
		return this.evictTimeout;
	}
	
	/**
	 * Sets the timeout when evicing lent pool items
	 * 
	 * @param milliseconds the timeout in milliseconds
	 * 
	 * @since 2.0
	 */
	
	public synchronized void setEvictionTimeout(final long milliseconds) {
		this.evictTimeout = milliseconds;
	}
	
	
	@Override
	public synchronized boolean validate(final Collection<ValidationMessage> messages) {
		boolean valid = true;
		
		if (this.minIdleCount < 0) {
			if (messages != null) {
				messages.add(new ValidationMessage(MIN_IDLE_COUNT_ID, "Value must be >= 0"));
			}
			valid = false;
		}
		if (this.maxTotalCount < 1) {
			if (messages != null) {
				messages.add(new ValidationMessage(MAX_TOTAL_COUNT_ID, "Value must be > 0."));
			}
			valid = false;
		}
		if (this.maxIdleCount < 0) {
			if (messages != null) {
				messages.add(new ValidationMessage(MAX_IDLE_COUNT_ID, "Value must be >= 0."));
			}
			valid = false;
		}
		if (this.minIdleCount >= 0 && this.maxIdleCount >= 0 && this.maxIdleCount < this.minIdleCount) {
			if (messages != null) {
				messages.add(new ValidationMessage(MAX_IDLE_COUNT_ID, "Value must be >= {" + MIN_IDLE_COUNT_ID + "}."));
			}
			valid = false;
		}
		if (this.minIdleTime < 0L) {
			if (messages != null) {
				messages.add(new ValidationMessage(MIN_IDLE_MILLIS_ID, "Value must be >= 0"));
			}
			valid = false;
		}
		if (this.maxWaitTime < 0L && this.maxUsageCount != -1) {
			if (messages != null) {
				messages.add(new ValidationMessage(MAX_WAIT_MILLIS_ID, "Value must be >= 0 or == -1 (infinite)"));
			}
			valid = false;
		}
		if (this.maxUsageCount < 1 && this.maxUsageCount != -1) {
			if (messages != null) {
				messages.add(new ValidationMessage(MAX_USAGE_COUNT_ID, "Value must be > 0 or == -1 (disable)"));
			}
			valid = false;
		}
		
		if (this.evictTimeout < 0) {
			if (messages != null) {
				messages.add(new ValidationMessage(EVICT_TIMEOUT_ID, "Value must be >= 0"));
			}
			valid = false;
		}
		return valid;
	}
	
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.walware.rj.servi.acommons.pool.impl;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * Provides a shared idle object eviction timer for all pools. This class wraps
 * the standard {@link ScheduledExecutorService} and keeps track of how many pools are using it.
 * If no pools are using the timer, it is canceled. This prevents a thread
 * being left running which, in application server environments, can lead to
 * memory leads and/or prevent applications from shutting down or reloading
 * cleanly.
 * </p>
 * <p>
 * This class has package scope to prevent its inclusion in the pool public API.
 * The class declaration below should *not* be changed to public.
 * </p> 
 */
class DaemonTimer {
	
	
	static final DaemonTimer DEFAULT = new DaemonTimer();
	
	
	private int usageCount;
	
	private ScheduledExecutorService executor;
	
	public DaemonTimer() {
	}
	
	
	public synchronized void register() {
		this.usageCount++;
		if (this.executor == null) {
			this.executor = Executors.newSingleThreadScheduledExecutor();
		}
	}
	
	public synchronized void unregister() {
		this.usageCount--;
		if (this.usageCount == 0 && this.executor != null) {
			this.executor.shutdown();
			this.executor = null;
		}
	}
	
	/**
	 * Add the specified eviction task to the timer. Tasks that are added with a
	 * call to this method *must* call {@link #cancel(TimerTask)} to cancel the
	 * task to prevent memory and/or thread leaks in application server
	 * environments.
	 * @param task      Task to be scheduled
	 * @param delay     Delay in milliseconds before task is executed
	 * @param period    Time in milliseconds between executions
	 * @return 
	 */
	public synchronized ScheduledFuture<?> schedule(Runnable task, long delay, long period) {
		return this.executor.scheduleWithFixedDelay(task, delay, period, TimeUnit.MILLISECONDS);
	}
	
}

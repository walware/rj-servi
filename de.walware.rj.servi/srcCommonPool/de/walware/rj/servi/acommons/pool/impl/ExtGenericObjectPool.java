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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.walware.rj.servi.acommons.pool.ObjectPool;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem.State;
import de.walware.rj.servi.acommons.pool.PoolableObjectFactory;


/**
 * A configurable {@link ObjectPool} implementation.
 * <p>
 * When coupled with the appropriate {@link PoolableObjectFactory},
 * <tt>GenericObjectPool</tt> provides robust pooling functionality for
 * arbitrary objects.
 * <p>
 * A <tt>GenericObjectPool</tt> provides a number of configurable parameters:
 * <ul>
 *  <li>
 *    {@link #setMaxActive <i>maxActive</i>} controls the maximum number of
 *    objects that can be borrowed from the pool at one time.  When
 *    non-positive, there is no limit to the number of objects that may be
 *    active at one time. When {@link #setMaxActive <i>maxActive</i>} is
 *    exceeded, the pool is said to be exhausted. The default setting for this
 *    parameter is 8.
 *  </li>
 *  <li>
 *    {@link #setMaxIdle <i>maxIdle</i>} controls the maximum number of objects
 *    that can sit idle in the pool at any time.  When negative, there is no
 *    limit to the number of objects that may be idle at one time. The default
 *    setting for this parameter is 8.
 *  </li>
 *  <li>
 *    {@link #setWhenExhaustedAction <i>whenExhaustedAction</i>} specifies the
 *    behavior of the {@link #borrowObject} method when the pool is exhausted:
 *    <ul>
 *    <li>
 *      When {@link #setWhenExhaustedAction <i>whenExhaustedAction</i>} is
 *      {@link #WHEN_EXHAUSTED_FAIL}, {@link #borrowObject} will throw
 *      a {@link NoSuchElementException}
 *    </li>
 *    <li>
 *      When {@link #setWhenExhaustedAction <i>whenExhaustedAction</i>} is
 *      {@link #WHEN_EXHAUSTED_GROW}, {@link #borrowObject} will create a new
 *      object and return it(essentially making {@link #setMaxActive <i>maxActive</i>}
 *      meaningless.)
 *    </li>
 *    <li>
 *      When {@link #setWhenExhaustedAction <i>whenExhaustedAction</i>}
 *      is {@link #WHEN_EXHAUSTED_BLOCK}, {@link #borrowObject} will block
 *      (invoke {@link Object#wait()} until a new or idle object is available.
 *      If a positive {@link #setMaxWait <i>maxWait</i>}
 *      value is supplied, the {@link #borrowObject} will block for at
 *      most that many milliseconds, after which a {@link NoSuchElementException}
 *      will be thrown.  If {@link #setMaxWait <i>maxWait</i>} is non-positive,
 *      the {@link #borrowObject} method will block indefinitely.
 *    </li>
 *    </ul>
 *    The default <code>whenExhaustedAction</code> setting is
 *    {@link #WHEN_EXHAUSTED_BLOCK} and the default <code>maxWait</code>
 *    setting is -1. By default, therefore, <code>borrowObject</code> will
 *    block indefinitely until an idle instance becomes available.
 *  </li>
 *  <li>
 *    When {@link #setTestOnBorrow <i>testOnBorrow</i>} is set, the pool will
 *    attempt to validate each object before it is returned from the
 *    {@link #borrowObject} method. (Using the provided factory's
 *    {@link PoolableObjectFactory#validateObject} method.)  Objects that fail
 *    to validate will be dropped from the pool, and a different object will
 *    be borrowed. The default setting for this parameter is
 *    <code>false.</code>
 *  </li>
 *  <li>
 *    When {@link #setTestOnReturn <i>testOnReturn</i>} is set, the pool will
 *    attempt to validate each object before it is returned to the pool in the
 *    {@link #returnObject} method. (Using the provided factory's
 *    {@link PoolableObjectFactory#validateObject}
 *    method.)  Objects that fail to validate will be dropped from the pool.
 *    The default setting for this parameter is <code>false.</code>
 *  </li>
 * </ul>
 * <p>
 * Optionally, one may configure the pool to examine and possibly evict objects
 * as they sit idle in the pool and to ensure that a minimum number of idle
 * objects are available. This is performed by an "idle object eviction"
 * thread, which runs asynchronously. Caution should be used when configuring
 * this optional feature. Eviction runs require an exclusive synchronization
 * lock on the pool, so if they run too frequently and / or incur excessive
 * latency when creating, destroying or validating object instances,
 * performance issues may result.  The idle object eviction thread may be
 * configured using the following attributes:
 * <ul>
 *  <li>
 *   {@link #setTimeBetweenEvictionRunsMillis <i>timeBetweenEvictionRunsMillis</i>}
 *   indicates how long the eviction thread should sleep before "runs" of examining
 *   idle objects.  When non-positive, no eviction thread will be launched. The
 *   default setting for this parameter is -1 (i.e., idle object eviction is
 *   disabled by default).
 *  </li>
 *  <li>
 *   {@link #setMinEvictableIdleTimeMillis <i>minEvictableIdleTimeMillis</i>}
 *   specifies the minimum amount of time that an object may sit idle in the pool
 *   before it is eligible for eviction due to idle time.  When non-positive, no object
 *   will be dropped from the pool due to idle time alone. This setting has no
 *   effect unless <code>timeBetweenEvictionRunsMillis > 0.</code> The default
 *   setting for this parameter is 30 minutes.
 *  </li>
 *  <li>
 *   {@link #setTestWhileIdle <i>testWhileIdle</i>} indicates whether or not idle
 *   objects should be validated using the factory's
 *   {@link PoolableObjectFactory#validateObject} method. Objects that fail to
 *   validate will be dropped from the pool. This setting has no effect unless 
 *   <code>timeBetweenEvictionRunsMillis > 0.</code>  The default setting for
 *   this parameter is <code>false.</code>
 *  </li>
 *  <li>
 *   {@link #setSoftMinEvictableIdleTimeMillis <i>softMinEvictableIdleTimeMillis</i>} 
 *   specifies the minimum amount of time an object may sit idle in the pool
 *   before it is eligible for eviction by the idle object evictor
 *   (if any), with the extra condition that at least "minIdle" amount of object 
 *   remain in the pool.  When non-positive, no objects will be evicted from the pool
 *   due to idle time alone. This setting has no effect unless
 *   <code>timeBetweenEvictionRunsMillis > 0.</code>  The default setting for
 *   this parameter is -1 (disabled).
 *  </li>
 *  <li>
 *   {@link #setNumTestsPerEvictionRun <i>numTestsPerEvictionRun</i>}
 *   determines the number of objects examined in each run of the idle object
 *   evictor. This setting has no effect unless 
 *   <code>timeBetweenEvictionRunsMillis > 0.</code>  The default setting for
 *   this parameter is 3.  
 *  </li>
 * </ul>
 * <p>
 * <p>
 * The pool can be configured to behave as a LIFO queue with respect to idle
 * objects - always returning the most recently used object from the pool,
 * or as a FIFO queue, where borrowObject always returns the oldest object
 * in the idle object pool.
 * <ul>
 *  <li>
 *   {@link #setLifo <i>lifo</i>}
 *   determines whether or not the pool returns idle objects in 
 *   last-in-first-out order. The default setting for this parameter is
 *   <code>true.</code>
 *  </li>
 * </ul>
 * <p>
 * GenericObjectPool is not usable without a {@link PoolableObjectFactory}.  A
 * non-<code>null</code> factory must be provided either as a constructor argument
 * or via a call to {@link #setFactory} before the pool is used.
 *
 * @see GenericKeyedObjectPool
 * @author Rodney Waldhoff
 * @author Dirk Verbeeck
 * @author Sandy McArthur
 * @version $Revision: 609487 $ $Date: 2008-01-06 19:36:42 -0700 (Sun, 06 Jan 2008) $
 * @since Pool 1.0
 */
public class ExtGenericObjectPool implements ObjectPool {
	
	//--- public constants -------------------------------------------
	
	/**
	 * A "when exhausted action" type indicating that when the pool is
	 * exhausted (i.e., the maximum number of active objects has
	 * been reached), the {@link #borrowObject}
	 * method should fail, throwing a {@link NoSuchElementException}.
	 * @see #WHEN_EXHAUSTED_BLOCK
	 * @see #WHEN_EXHAUSTED_GROW
	 * @see #setWhenExhaustedAction
	 */
	public static final byte WHEN_EXHAUSTED_FAIL   = 0;
	
	/**
	 * A "when exhausted action" type indicating that when the pool
	 * is exhausted (i.e., the maximum number
	 * of active objects has been reached), the {@link #borrowObject}
	 * method should block until a new object is available, or the
	 * {@link #getMaxWait maximum wait time} has been reached.
	 * @see #WHEN_EXHAUSTED_FAIL
	 * @see #WHEN_EXHAUSTED_GROW
	 * @see #setMaxWait
	 * @see #getMaxWait
	 * @see #setWhenExhaustedAction
	 */
	public static final byte WHEN_EXHAUSTED_BLOCK  = 1;
	
	/**
	 * A "when exhausted action" type indicating that when the pool is
	 * exhausted (i.e., the maximum number
	 * of active objects has been reached), the {@link #borrowObject}
	 * method should simply create a new object anyway.
	 * @see #WHEN_EXHAUSTED_FAIL
	 * @see #WHEN_EXHAUSTED_GROW
	 * @see #setWhenExhaustedAction
	 */
	public static final byte WHEN_EXHAUSTED_GROW   = 2;
	
	/**
	 * The default cap on the number of "sleeping" instances in the pool.
	 * @see #getMaxIdle
	 * @see #setMaxIdle
	 */
	public static final int DEFAULT_MAX_IDLE  = 8;
	
	/**
	 * The default minimum number of "sleeping" instances in the pool
	 * before before the evictor thread (if active) spawns new objects.
	 * @see #getMinIdle
	 * @see #setMinIdle
	 */
	public static final int DEFAULT_MIN_IDLE = 0;
	
	/**
	 * The default cap on the total number of active instances from the pool.
	 * @see #getMaxActive
	 */
	public static final int DEFAULT_MAX_ACTIVE  = 8;
	
	/**
	 * The default "when exhausted action" for the pool.
	 * @see #WHEN_EXHAUSTED_BLOCK
	 * @see #WHEN_EXHAUSTED_FAIL
	 * @see #WHEN_EXHAUSTED_GROW
	 * @see #setWhenExhaustedAction
	 */
	public static final byte DEFAULT_WHEN_EXHAUSTED_ACTION = WHEN_EXHAUSTED_BLOCK;
	
	/**
	 * The default LIFO status. True means that borrowObject returns the
	 * most recently used ("last in") idle object in the pool (if there are
	 * idle instances available).  False means that the pool behaves as a FIFO
	 * queue - objects are taken from the idle object pool in the order that
	 * they are returned to the pool.
	 * @see #setLifo
	 * @since 1.4
	 */
	public static final boolean DEFAULT_LIFO = true;
	
	/**
	 * The default maximum amount of time (in milliseconds) the
	 * {@link #borrowObject} method should block before throwing
	 * an exception when the pool is exhausted and the
	 * {@link #getWhenExhaustedAction "when exhausted" action} is
	 * {@link #WHEN_EXHAUSTED_BLOCK}.
	 * @see #getMaxWait
	 * @see #setMaxWait
	 */
	public static final long DEFAULT_MAX_WAIT = -1L;
	
	/**
	 * The default "test on borrow" value.
	 * @see #getTestOnBorrow
	 * @see #setTestOnBorrow
	 */
	public static final boolean DEFAULT_TEST_ON_BORROW = false;
	
	/**
	 * The default "test on return" value.
	 * @see #getTestOnReturn
	 * @see #setTestOnReturn
	 */
	public static final boolean DEFAULT_TEST_ON_RETURN = false;
	
	/**
	 * The default "test while idle" value.
	 * @see #getTestWhileIdle
	 * @see #setTestWhileIdle
	 * @see #getTimeBetweenEvictionRunsMillis
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public static final boolean DEFAULT_TEST_WHILE_IDLE = false;
	
	/**
	 * The default "time between eviction runs" value.
	 * @see #getTimeBetweenEvictionRunsMillis
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public static final long DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 10000L;
	
	/**
	 * The default number of objects to examine per run in the
	 * idle object evictor.
	 * @see #getNumTestsPerEvictionRun
	 * @see #setNumTestsPerEvictionRun
	 * @see #getTimeBetweenEvictionRunsMillis
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public static final int DEFAULT_NUM_TESTS_PER_EVICTION_RUN = 3;
	
	/**
	 * The default value for {@link #getMinEvictableIdleTimeMillis}.
	 * @see #getMinEvictableIdleTimeMillis
	 * @see #setMinEvictableIdleTimeMillis
	 */
	public static final long DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS = 1000L * 60L * 30L;
	
	/**
	 * The default value for {@link #getSoftMinEvictableIdleTimeMillis}.
	 * @see #getSoftMinEvictableIdleTimeMillis
	 * @see #setSoftMinEvictableIdleTimeMillis
	 */
	public static final long DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS = -1;
	
	
	//--- static internal --------------------------------------------
	
	
	private static final int CLOSE_DAEMON = 1;
	private static final int CLOSE_FINAL = 3;
	
	/**
	 * A simple "struct" encapsulating the
	 * configuration information for a {@link ExtGenericObjectPool}.
	 * @see ExtGenericObjectPool#GenericObjectPool(de.walware.rj.servi.acommons.pool.PoolableObjectFactory,de.walware.rj.servi.acommons.pool.impl.ExtGenericObjectPool.Config)
	 * @see ExtGenericObjectPool#setConfig
	 */
	public static class Config {
		/**
		 * @see ExtGenericObjectPool#setMaxIdle
		 */
		public int maxIdle = ExtGenericObjectPool.DEFAULT_MAX_IDLE;
		/**
		 * @see ExtGenericObjectPool#setMinIdle
		 */
		public int minIdle = ExtGenericObjectPool.DEFAULT_MIN_IDLE;
		/**
		 * @see ExtGenericObjectPool#setMaxActive
		 */
		public int maxActive = ExtGenericObjectPool.DEFAULT_MAX_ACTIVE;
		/**
		 * @see ExtGenericObjectPool#setMaxWait
		 */
		public long maxWait = ExtGenericObjectPool.DEFAULT_MAX_WAIT;
		/**
		 * @see ExtGenericObjectPool#setWhenExhaustedAction
		 */
		public byte whenExhaustedAction = ExtGenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
		/**
		 * @see ExtGenericObjectPool#setTestOnBorrow
		 */
		public boolean testOnBorrow = ExtGenericObjectPool.DEFAULT_TEST_ON_BORROW;
		/**
		 * @see ExtGenericObjectPool#setTestOnReturn
		 */
		public boolean testOnReturn = ExtGenericObjectPool.DEFAULT_TEST_ON_RETURN;
		/**
		 * @see ExtGenericObjectPool#setTestWhileIdle
		 */
		public boolean testWhileIdle = ExtGenericObjectPool.DEFAULT_TEST_WHILE_IDLE;
		/**
		 * @see ExtGenericObjectPool#setTimeBetweenEvictionRunsMillis
		 */
		public long timeBetweenEvictionRunsMillis = ExtGenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
		/**
		 * @see ExtGenericObjectPool#setNumTestsPerEvictionRun
		 */
		public int numTestsPerEvictionRun =  ExtGenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
		/**
		 * @see ExtGenericObjectPool#setMinEvictableIdleTimeMillis
		 */
		public long minEvictableIdleTimeMillis = ExtGenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
		/**
		 * @see ExtGenericObjectPool#setSoftMinEvictableIdleTimeMillis
		 */
		public long softMinEvictableIdleTimeMillis = ExtGenericObjectPool.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
		/**
		 * @see ExtGenericObjectPool#setLifo
		 */
		public boolean lifo = ExtGenericObjectPool.DEFAULT_LIFO;
	
	}
	
	/**
	 * A simple "struct" encapsulating an object instance and a timestamp.
	 *
	 * Implements Comparable, objects are sorted from old to new.
	 *
	 * This is also used by {@link ExtGenericObjectPool}.
	 */
	class Item implements Comparable<Item>, ObjectPoolItem {
		
		private State state;
		
		private long createStamp;
		private long destroyStamp = -1;
		private long stateStamp;
		private long lentCount;
		private long lentDuration;
		
		private long clientId;
		private String clientLabel;
		
		private Object value;
		
		private long evict;
		
		
		Item() {
			this.createStamp = this.stateStamp = System.currentTimeMillis();
			this.state = State.INITIALIZING;
		}
		
		
		@Override
		public long getClientId() {
			return this.clientId;
		}
		
		@Override
		public String getClientLabel() {
			return this.clientLabel;
		}
		
		@Override
		public ObjectPool getPool() {
			return ExtGenericObjectPool.this;
		}
		
		@Override
		public State getState() {
			return this.state;
		}
		
		synchronized void setState(State state, String label) {
			if (this.state != state) {
				long stamp = System.currentTimeMillis();
				if (this.state == State.LENT) {
					this.lentDuration += stamp-this.stateStamp;
					this.clientId = -1L;
					this.clientLabel = null;
				}
				else if (state == State.LENT) {
					this.lentCount++;
					this.clientId = this.lentCount;
					this.clientLabel = label;
				}
				this.state = state;
				this.stateStamp = stamp;
			}
		}
		
		@Override
		public synchronized void invalidateClient() {
			this.clientId = -1L;
			this.clientLabel = null;
		}
		
		@Override
		public long getCreationTime() {
			return this.createStamp;
		}
		
		@Override
		public long getDestrutionTime() {
			return this.destroyStamp;
		}
		
		@Override
		public long getStateTime() {
			return this.stateStamp;
		}
		
		@Override
		public long getLentCount() {
			return this.lentCount;
		}
		
		@Override
		public long getLentDuration() {
			return this.lentDuration;
		}
		
		@Override
		public String toString() {
			return this.value + ";" + this.stateStamp;
		}
		
		@Override
		public int compareTo(Item other) {
			final long tstampdiff = this.stateStamp - other.stateStamp;
			if (tstampdiff == 0) {
				// make sure the natural ordering is consistent with equals
				// see java.lang.Comparable Javadocs
				return System.identityHashCode(this) - System.identityHashCode(other);
			} else {
				// handle int overflow
				return (int)Math.min(Math.max(tstampdiff, Integer.MIN_VALUE), Integer.MAX_VALUE);
			}
		}
		
		@Override
		public Object getObject() {
			return this.value;
		}
		
		@Override
		public void setObject(Object obj) {
			this.value = obj;
		}
	}
	
	
	/**
	 * The cap on the number of idle instances in the pool.
	 * @see #setMaxIdle
	 * @see #getMaxIdle
	 */
	private int _maxIdle = DEFAULT_MAX_IDLE;
	
	/**
	* The cap on the minimum number of idle instances in the pool.
	* @see #setMinIdle
	* @see #getMinIdle
	*/
	private int _minIdle = DEFAULT_MIN_IDLE;
	
	/**
	 * The cap on the total number of active instances from the pool.
	 * @see #setMaxActive
	 * @see #getMaxActive
	 */
	private int _maxActive = DEFAULT_MAX_ACTIVE;
	
	/**
	 * The maximum amount of time (in millis) the
	 * {@link #borrowObject} method should block before throwing
	 * an exception when the pool is exhausted and the
	 * {@link #getWhenExhaustedAction "when exhausted" action} is
	 * {@link #WHEN_EXHAUSTED_BLOCK}.
	 *
	 * When less than or equal to 0, the {@link #borrowObject} method
	 * may block indefinitely.
	 *
	 * @see #setMaxWait
	 * @see #getMaxWait
	 * @see #WHEN_EXHAUSTED_BLOCK
	 * @see #setWhenExhaustedAction
	 * @see #getWhenExhaustedAction
	 */
	private long _maxWait = DEFAULT_MAX_WAIT;
	
	/**
	 * The action to take when the {@link #borrowObject} method
	 * is invoked when the pool is exhausted (the maximum number
	 * of "active" objects has been reached).
	 *
	 * @see #WHEN_EXHAUSTED_BLOCK
	 * @see #WHEN_EXHAUSTED_FAIL
	 * @see #WHEN_EXHAUSTED_GROW
	 * @see #DEFAULT_WHEN_EXHAUSTED_ACTION
	 * @see #setWhenExhaustedAction
	 * @see #getWhenExhaustedAction
	 */
	private byte _whenExhaustedAction = DEFAULT_WHEN_EXHAUSTED_ACTION;
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * before being returned by the {@link #borrowObject}
	 * method.  If the object fails to validate,
	 * it will be dropped from the pool, and we will attempt
	 * to borrow another.
	 *
	 * @see #setTestOnBorrow
	 * @see #getTestOnBorrow
	 */
	private volatile boolean _testOnBorrow = DEFAULT_TEST_ON_BORROW;
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * before being returned to the pool within the
	 * {@link #returnObject}.
	 *
	 * @see #getTestOnReturn
	 * @see #setTestOnReturn
	 */
	private volatile boolean _testOnReturn = DEFAULT_TEST_ON_RETURN;
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * by the idle object evictor (if any).  If an object
	 * fails to validate, it will be dropped from the pool.
	 *
	 * @see #setTestWhileIdle
	 * @see #getTestWhileIdle
	 * @see #getTimeBetweenEvictionRunsMillis
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	private boolean _testWhileIdle = DEFAULT_TEST_WHILE_IDLE;
	
	/**
	 * The number of milliseconds to sleep between runs of the
	 * idle object evictor thread.
	 * When non-positive, no idle object evictor thread will be
	 * run.
	 *
	 * @see #setTimeBetweenEvictionRunsMillis
	 * @see #getTimeBetweenEvictionRunsMillis
	 */
	private long _timeBetweenEvictionRunsMillis = DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
	
	/**
	 * The max number of objects to examine during each run of the
	 * idle object evictor thread (if any).
	 * <p>
	 * When a negative value is supplied, <tt>ceil({@link #getNumIdle})/abs({@link #getNumTestsPerEvictionRun})</tt>
	 * tests will be run.  I.e., when the value is <i>-n</i>, roughly one <i>n</i>th of the
	 * idle objects will be tested per run.
	 *
	 * @see #setNumTestsPerEvictionRun
	 * @see #getNumTestsPerEvictionRun
	 * @see #getTimeBetweenEvictionRunsMillis
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	private int _numTestsPerEvictionRun =  DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
	
	/**
	 * The minimum amount of time an object may sit idle in the pool
	 * before it is eligible for eviction by the idle object evictor
	 * (if any).
	 * When non-positive, no objects will be evicted from the pool
	 * due to idle time alone.
	 *
	 * @see #setMinEvictableIdleTimeMillis
	 * @see #getMinEvictableIdleTimeMillis
	 * @see #getTimeBetweenEvictionRunsMillis
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	private long _minEvictableIdleTimeMillis = DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
	
	/**
	 * The minimum amount of time an object may sit idle in the pool
	 * before it is eligible for eviction by the idle object evictor
	 * (if any), with the extra condition that at least
	 * "minIdle" amount of object remain in the pool.
	 * When non-positive, no objects will be evicted from the pool
	 * due to idle time alone.
	 *
	 * @see #setSoftMinEvictableIdleTimeMillis
	 * @see #getSoftMinEvictableIdleTimeMillis
	 */
	private long _softMinEvictableIdleTimeMillis = DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
	
	/** Whether or not the pool behaves as a LIFO queue (last in first out) */
	private boolean _lifo = DEFAULT_LIFO;
	
	//-- data --
	
	private final ArrayList<Item> _completePool = new ArrayList<>();
	
	
	/** My pool. */
	private CursorableLinkedList<Item> _idlePool = null;
	
	private final ArrayList<Item> _evictionCheckList = new ArrayList<>();
	
	/** Eviction cursor - keeps track of idle object evictor position */
	private CursorableLinkedList<Item>.Cursor _evictionCursor = null;
	
	/** My {@link PoolableObjectFactory}. */
	private PoolableObjectFactory _factory = null;
	
	/**
	 * The number of objects {@link #borrowObject} borrowed
	 * from the pool, but not yet returned.
	 */
	private int _numActive = 0;
	
	private DaemonTimer timer = DaemonTimer.DEFAULT;
	private Evictor evictor = null;
	private ScheduledFuture<?> evictorFuture;
	
	private List<Item> _toDestroy = new ArrayList<>();
	
	private Lock _clearLock = new ReentrantLock();
	
	private volatile int _closed = 0;
	
	
	private int _statMaxActive;
	private int _statMaxIdle;
	private int _statMaxTotal;
	
	
	//--- constructors -----------------------------------------------
	
	/**
	 * Create a new <tt>GenericObjectPool</tt>.
	 */
	public ExtGenericObjectPool() {
		this(null,DEFAULT_MAX_ACTIVE,DEFAULT_WHEN_EXHAUSTED_ACTION,DEFAULT_MAX_WAIT,DEFAULT_MAX_IDLE,DEFAULT_MIN_IDLE,DEFAULT_TEST_ON_BORROW,DEFAULT_TEST_ON_RETURN,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_LIFO);
	}
	
	/**
	 * Create a new <tt>GenericObjectPool</tt> using the specified values.
	 * @param factory the (possibly <tt>null</tt>)PoolableObjectFactory to use to create, validate and destroy objects
	 */
	public ExtGenericObjectPool(PoolableObjectFactory factory) {
		this(factory,DEFAULT_MAX_ACTIVE,DEFAULT_WHEN_EXHAUSTED_ACTION,DEFAULT_MAX_WAIT,DEFAULT_MAX_IDLE,DEFAULT_MIN_IDLE,DEFAULT_TEST_ON_BORROW,DEFAULT_TEST_ON_RETURN,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_LIFO);
	}
	
	/**
	 * Create a new <tt>GenericObjectPool</tt> using the specified values.
	 * @param factory the (possibly <tt>null</tt>)PoolableObjectFactory to use to create, validate and destroy objects
	 * @param config a non-<tt>null</tt> {@link ExtGenericObjectPool.Config} describing my configuration
	 */
	public ExtGenericObjectPool(PoolableObjectFactory factory, ExtGenericObjectPool.Config config) {
		this(factory,config.maxActive,config.whenExhaustedAction,config.maxWait,config.maxIdle,config.minIdle,config.testOnBorrow,config.testOnReturn,config.timeBetweenEvictionRunsMillis,config.numTestsPerEvictionRun,config.minEvictableIdleTimeMillis,config.testWhileIdle,config.softMinEvictableIdleTimeMillis, config.lifo);
	}
	
	/**
	 * Create a new <tt>GenericObjectPool</tt> using the specified values.
	 * @param factory the (possibly <tt>null</tt>)PoolableObjectFactory to use to create, validate and destroy objects
	 * @param maxActive the maximum number of objects that can be borrowed from me at one time (see {@link #setMaxActive})
	 * @param whenExhaustedAction the action to take when the pool is exhausted (see {@link #setWhenExhaustedAction})
	 * @param maxWait the maximum amount of time to wait for an idle object when the pool is exhausted an and <i>whenExhaustedAction</i> is {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see {@link #setMaxWait})
	 * @param maxIdle the maximum number of idle objects in my pool (see {@link #setMaxIdle})
	 * @param minIdle the minimum number of idle objects in my pool (see {@link #setMinIdle})
	 * @param testOnBorrow whether or not to validate objects before they are returned by the {@link #borrowObject} method (see {@link #setTestOnBorrow})
	 * @param testOnReturn whether or not to validate objects after they are returned to the {@link #returnObject} method (see {@link #setTestOnReturn})
	 * @param timeBetweenEvictionRunsMillis the amount of time (in milliseconds) to sleep between examining idle objects for eviction (see {@link #setTimeBetweenEvictionRunsMillis})
	 * @param numTestsPerEvictionRun the number of idle objects to examine per run within the idle object eviction thread (if any) (see {@link #setNumTestsPerEvictionRun})
	 * @param minEvictableIdleTimeMillis the minimum number of milliseconds an object can sit idle in the pool before it is eligible for eviction (see {@link #setMinEvictableIdleTimeMillis})
	 * @param testWhileIdle whether or not to validate objects in the idle object eviction thread, if any (see {@link #setTestWhileIdle})
	 * @param softMinEvictableIdleTimeMillis the minimum number of milliseconds an object can sit idle in the pool before it is eligible for eviction with the extra condition that at least "minIdle" amount of object remain in the pool. (see {@link #setSoftMinEvictableIdleTimeMillis})
	 * @param lifo whether or not objects are returned in last-in-first-out order from the idle object pool (see {@link #setLifo})
	 * @since Pool 1.4
	 */
	public ExtGenericObjectPool(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle, int minIdle, boolean testOnBorrow, boolean testOnReturn, long timeBetweenEvictionRunsMillis, int numTestsPerEvictionRun, long minEvictableIdleTimeMillis, boolean testWhileIdle, long softMinEvictableIdleTimeMillis, boolean lifo) {
		this._factory = factory;
		this._maxActive = maxActive;
		this._lifo = lifo;
		switch(whenExhaustedAction) {
		case WHEN_EXHAUSTED_BLOCK:
		case WHEN_EXHAUSTED_FAIL:
		case WHEN_EXHAUSTED_GROW:
			this._whenExhaustedAction = whenExhaustedAction;
			break;
		default:
			throw new IllegalArgumentException("whenExhaustedAction " + whenExhaustedAction + " not recognized.");
		}
		this._maxWait = maxWait;
		this._maxIdle = maxIdle;
		this._minIdle = minIdle;
		this._testOnBorrow = testOnBorrow;
		this._testOnReturn = testOnReturn;
		this._timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
		this._numTestsPerEvictionRun = numTestsPerEvictionRun;
		this._minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		this._softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
		this._testWhileIdle = testWhileIdle;
		
		this._idlePool = new CursorableLinkedList<>();
		
		this.timer.register();
		startEvictor(this._timeBetweenEvictionRunsMillis);
	}
	
	//--- public methods ---------------------------------------------
	
	//--- configuration methods --------------------------------------
	
	/**
	 * Returns the cap on the total number of active instances from the pool.
	 * @return the cap on the total number of active instances from the pool.
	 * @see #setMaxActive
	 */
	public synchronized int getMaxActive() {
		return this._maxActive;
	}
	
	/**
	 * Sets the cap on the total number of active instances from the pool.
	 * @param maxActive The cap on the total number of active instances from the pool.
	 * Use a negative value for no limit.
	 * @see #getMaxActive
	 */
	public synchronized void setMaxActive(int maxActive) {
		this._maxActive = maxActive;
		notifyAll();
		synchronized (this._completePool) {
			this._completePool.ensureCapacity((int) (this._maxActive*1.25));
		}
	}
	
	/**
	 * Returns the action to take when the {@link #borrowObject} method
	 * is invoked when the pool is exhausted (the maximum number
	 * of "active" objects has been reached).
	 *
	 * @return one of {@link #WHEN_EXHAUSTED_BLOCK}, {@link #WHEN_EXHAUSTED_FAIL} or {@link #WHEN_EXHAUSTED_GROW}
	 * @see #setWhenExhaustedAction
	 */
	public synchronized byte getWhenExhaustedAction() {
		return this._whenExhaustedAction;
	}
	
	/**
	 * Sets the action to take when the {@link #borrowObject} method
	 * is invoked when the pool is exhausted (the maximum number
	 * of "active" objects has been reached).
	 *
	 * @param whenExhaustedAction the action code, which must be one of
	 *        {@link #WHEN_EXHAUSTED_BLOCK}, {@link #WHEN_EXHAUSTED_FAIL},
	 *        or {@link #WHEN_EXHAUSTED_GROW}
	 * @see #getWhenExhaustedAction
	 */
	public synchronized void setWhenExhaustedAction(byte whenExhaustedAction) {
		switch(whenExhaustedAction) {
		case WHEN_EXHAUSTED_BLOCK:
		case WHEN_EXHAUSTED_FAIL:
		case WHEN_EXHAUSTED_GROW:
			this._whenExhaustedAction = whenExhaustedAction;
			notifyAll();
			break;
		default:
			throw new IllegalArgumentException("whenExhaustedAction " + whenExhaustedAction + " not recognized.");
		}
	}
	
	
	/**
	 * Returns the maximum amount of time (in milliseconds) the
	 * {@link #borrowObject} method should block before throwing
	 * an exception when the pool is exhausted and the
	 * {@link #setWhenExhaustedAction "when exhausted" action} is
	 * {@link #WHEN_EXHAUSTED_BLOCK}.
	 * 
	 * When less than or equal to 0, the {@link #borrowObject} method
	 * may block indefinitely.
	 * 
	 * @return maximum number of milliseconds to block when borrowing an object.
	 * @see #setMaxWait
	 * @see #setWhenExhaustedAction
	 * @see #WHEN_EXHAUSTED_BLOCK
	 */
	public synchronized long getMaxWait() {
		return this._maxWait;
	}
	
	/**
	 * Sets the maximum amount of time (in milliseconds) the
	 * {@link #borrowObject} method should block before throwing
	 * an exception when the pool is exhausted and the
	 * {@link #setWhenExhaustedAction "when exhausted" action} is
	 * {@link #WHEN_EXHAUSTED_BLOCK}.
	 *
	 * When less than or equal to 0, the {@link #borrowObject} method
	 * may block indefinitely.
	 *
	 * @param maxWait maximum number of milliseconds to block when borrowing an object.
	 * @see #getMaxWait
	 * @see #setWhenExhaustedAction
	 * @see #WHEN_EXHAUSTED_BLOCK
	 */
	public synchronized void setMaxWait(long maxWait) {
		this._maxWait = maxWait;
		notifyAll();
	}
	
	/**
	 * Returns the cap on the number of "idle" instances in the pool.
	 * @return the cap on the number of "idle" instances in the pool.
	 * @see #setMaxIdle
	 */
	public synchronized int getMaxIdle() {
		return this._maxIdle;
	}
	
	/**
	 * Sets the cap on the number of "idle" instances in the pool.
	 * @param maxIdle The cap on the number of "idle" instances in the pool.
	 * Use a negative value to indicate an unlimited number of idle instances.
	 * @see #getMaxIdle
	 */
	public synchronized void setMaxIdle(int maxIdle) {
		this._maxIdle = maxIdle;
		notifyAll();
	}
	
	/**
	 * Sets the minimum number of objects allowed in the pool
	 * before the evictor thread (if active) spawns new objects.
	 * Note that no objects are created when 
	 * <code>numActive + numIdle >= maxActive.</code>
	 * This setting has no effect if the idle object evictor is disabled
	 * (i.e. if <code>timeBetweenEvictionRunsMillis <= 0</code>).
	 *
	 * @param minIdle The minimum number of objects.
	 * @see #getMinIdle
	 * @see #getTimeBetweenEvictionRunsMillis()
	 */
	public synchronized void setMinIdle(int minIdle) {
		this._minIdle = minIdle;
		notifyAll();
	}
	
	/**
	 * Returns the minimum number of objects allowed in the pool
	 * before the evictor thread (if active) spawns new objects.
	 * (Note no objects are created when: numActive + numIdle >= maxActive)
	 *
	 * @return The minimum number of objects.
	 * @see #setMinIdle
	 */
	public synchronized int getMinIdle() {
		return this._minIdle;
	}
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * before being returned by the {@link #borrowObject}
	 * method.  If the object fails to validate,
	 * it will be dropped from the pool, and we will attempt
	 * to borrow another.
	 *
	 * @return <code>true</code> if objects are validated before being borrowed.
	 * @see #setTestOnBorrow
	 */
	public boolean getTestOnBorrow() {
		return this._testOnBorrow;
	}
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * before being returned by the {@link #borrowObject}
	 * method.  If the object fails to validate,
	 * it will be dropped from the pool, and we will attempt
	 * to borrow another.
	 *
	 * @param testOnBorrow <code>true</code> if objects should be validated before being borrowed.
	 * @see #getTestOnBorrow
	 */
	public void setTestOnBorrow(boolean testOnBorrow) {
		this._testOnBorrow = testOnBorrow;
	}
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * before being returned to the pool within the
	 * {@link #returnObject}.
	 *
	 * @return <code>true</code> when objects will be validated after returned to {@link #returnObject}.
	 * @see #setTestOnReturn
	 */
	public boolean getTestOnReturn() {
		return this._testOnReturn;
	}
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * before being returned to the pool within the
	 * {@link #returnObject}.
	 *
	 * @param testOnReturn <code>true</code> so objects will be validated after returned to {@link #returnObject}.
	 * @see #getTestOnReturn
	 */
	public void setTestOnReturn(boolean testOnReturn) {
		this._testOnReturn = testOnReturn;
	}
	
	/**
	 * Returns the number of milliseconds to sleep between runs of the
	 * idle object evictor thread.
	 * When non-positive, no idle object evictor thread will be
	 * run.
	 *
	 * @return number of milliseconds to sleep between evictor runs.
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public synchronized long getTimeBetweenEvictionRunsMillis() {
		return this._timeBetweenEvictionRunsMillis;
	}
	
	/**
	 * Sets the number of milliseconds to sleep between runs of the
	 * idle object evictor thread.
	 * When non-positive, no idle object evictor thread will be
	 * run.
	 *
	 * @param timeBetweenEvictionRunsMillis number of milliseconds to sleep between evictor runs.
	 * @see #getTimeBetweenEvictionRunsMillis
	 */
	public synchronized void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this._timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
		startEvictor(this._timeBetweenEvictionRunsMillis);
	}
	
	/**
	 * Returns the max number of objects to examine during each run of the
	 * idle object evictor thread (if any).
	 *
	 * @return max number of objects to examine during each evictor run.
	 * @see #setNumTestsPerEvictionRun
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public synchronized int getNumTestsPerEvictionRun() {
		return this._numTestsPerEvictionRun;
	}
	
	/**
	 * Sets the max number of objects to examine during each run of the
	 * idle object evictor thread (if any).
	 * <p>
	 * When a negative value is supplied, <tt>ceil({@link #getNumIdle})/abs({@link #getNumTestsPerEvictionRun})</tt>
	 * tests will be run.  I.e., when the value is <i>-n</i>, roughly one <i>n</i>th of the
	 * idle objects will be tested per run.
	 *
	 * @param numTestsPerEvictionRun max number of objects to examine during each evictor run.
	 * @see #getNumTestsPerEvictionRun
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public synchronized void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		this._numTestsPerEvictionRun = numTestsPerEvictionRun;
	}
	
	/**
	 * Returns the minimum amount of time an object may sit idle in the pool
	 * before it is eligible for eviction by the idle object evictor
	 * (if any).
	 *
	 * @return minimum amount of time an object may sit idle in the pool before it is eligible for eviction.
	 * @see #setMinEvictableIdleTimeMillis
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public synchronized long getMinEvictableIdleTimeMillis() {
		return this._minEvictableIdleTimeMillis;
	}
	
	/**
	 * Sets the minimum amount of time an object may sit idle in the pool
	 * before it is eligible for eviction by the idle object evictor
	 * (if any).
	 * When non-positive, no objects will be evicted from the pool
	 * due to idle time alone.
	 * @param minEvictableIdleTimeMillis minimum amount of time an object may sit idle in the pool before it is eligible for eviction.
	 * @see #getMinEvictableIdleTimeMillis
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public synchronized void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this._minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}
	
	/**
	 * Returns the minimum amount of time an object may sit idle in the pool
	 * before it is eligible for eviction by the idle object evictor
	 * (if any), with the extra condition that at least
	 * "minIdle" amount of object remain in the pool.
	 *
	 * @return minimum amount of time an object may sit idle in the pool before it is eligible for eviction.
	 * @since Pool 1.3
	 * @see #setSoftMinEvictableIdleTimeMillis
	 */
	public synchronized long getSoftMinEvictableIdleTimeMillis() {
		return this._softMinEvictableIdleTimeMillis;
	}
	
	/**
	 * Sets the minimum amount of time an object may sit idle in the pool
	 * before it is eligible for eviction by the idle object evictor
	 * (if any), with the extra condition that at least
	 * "minIdle" amount of object remain in the pool.
	 * When non-positive, no objects will be evicted from the pool
	 * due to idle time alone.
	 *
	 * @param softMinEvictableIdleTimeMillis minimum amount of time an object may sit idle in the pool before it is eligible for eviction.
	 * @since Pool 1.3
	 * @see #getSoftMinEvictableIdleTimeMillis
	 */
	public synchronized void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
		this._softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
	}
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * by the idle object evictor (if any).  If an object
	 * fails to validate, it will be dropped from the pool.
	 *
	 * @return <code>true</code> when objects will be validated by the evictor.
	 * @see #setTestWhileIdle
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public synchronized boolean getTestWhileIdle() {
		return this._testWhileIdle;
	}
	
	/**
	 * When <tt>true</tt>, objects will be
	 * {@link PoolableObjectFactory#validateObject validated}
	 * by the idle object evictor (if any).  If an object
	 * fails to validate, it will be dropped from the pool.
	 *
	 * @param testWhileIdle <code>true</code> so objects will be validated by the evictor.
	 * @see #getTestWhileIdle
	 * @see #setTimeBetweenEvictionRunsMillis
	 */
	public synchronized void setTestWhileIdle(boolean testWhileIdle) {
		this._testWhileIdle = testWhileIdle;
	}
	
	/**
	 * Whether or not the idle object pool acts as a LIFO queue. True means
	 * that borrowObject returns the most recently used ("last in") idle object
	 * in the pool (if there are idle instances available).  False means that
	 * the pool behaves as a FIFO queue - objects are taken from the idle object
	 * pool in the order that they are returned to the pool.
	 * 
	 * @return <code>true</true> if the pool is configured to act as a LIFO queue
	 * @since 1.4
	 */
	public synchronized boolean getLifo() {
		return this._lifo;
	}
	
	/**
	 * Sets the LIFO property of the pool. True means that borrowObject returns
	 * the most recently used ("last in") idle object in the pool (if there are
	 * idle instances available).  False means that the pool behaves as a FIFO
	 * queue - objects are taken from the idle object pool in the order that
	 * they are returned to the pool.
	 * 
	 * @param lifo the new value for the LIFO property
	 * @since 1.4
	 */
	public synchronized void setLifo(boolean lifo) {
		this._lifo = lifo;
	}
	
	/**
	 * Sets my configuration.
	 *
	 * @param conf configuration to use.
	 * @see ExtGenericObjectPool.Config
	 */
	public synchronized void setConfig(ExtGenericObjectPool.Config conf) {
		setMaxIdle(conf.maxIdle);
		setMinIdle(conf.minIdle);
		setMaxActive(conf.maxActive);
		setMaxWait(conf.maxWait);
		setWhenExhaustedAction(conf.whenExhaustedAction);
		setTestOnBorrow(conf.testOnBorrow);
		setTestOnReturn(conf.testOnReturn);
		setTestWhileIdle(conf.testWhileIdle);
		setNumTestsPerEvictionRun(conf.numTestsPerEvictionRun);
		setMinEvictableIdleTimeMillis(conf.minEvictableIdleTimeMillis);
		setTimeBetweenEvictionRunsMillis(conf.timeBetweenEvictionRunsMillis);
		setSoftMinEvictableIdleTimeMillis(conf.softMinEvictableIdleTimeMillis);
		setLifo(conf.lifo);
		notifyAll();
	}
	
	/**
	 * Sets the {@link PoolableObjectFactory factory} this pool uses
	 * to create new instances. Trying to change
	 * the <code>factory</code> while there are borrowed objects will
	 * throw an {@link IllegalStateException}.
	 *
	 * @param factory the {@link PoolableObjectFactory} used to create new instances.
	 * @throws IllegalStateException when the factory cannot be set at this time
	 */
	public synchronized void setFactory(PoolableObjectFactory factory) throws IllegalStateException {
		if (factory == null) {
			throw new NullPointerException();
		}
		assertOpen();
		if(0 < getNumActive()) {
			throw new IllegalStateException("Objects are already active");
		} else {
			clear();
			this._factory = factory;
		}
	}
	
	//-- ObjectPool methods ------------------------------------------
	
	/**
	 * Return the number of instances currently borrowed from this pool.
	 *
	 * @return the number of instances currently borrowed from this pool
	 */
	@Override
	public synchronized int getNumActive() {
		return this._numActive;
	}
	
	/**
	 * Return the number of instances currently idle in this pool.
	 *
	 * @return the number of instances currently idle in this pool
	 */
	@Override
	public synchronized int getNumIdle() {
		return this._idlePool.size();
	}
	
	public int getStatMaxTotal() {
		return this._statMaxTotal;
	}
	
	public int getStatMaxActive() {
		return this._statMaxActive;
	}
	
	public int getStatMaxIdle() {
		return this._statMaxIdle;
	}
	
	/**
	 * Create an object, and place it into the pool.
	 * addObject() is useful for "pre-loading" a pool with idle objects.
	 */
	@Override
	public void addObject() throws Exception {
		assertOpen();
		Item item = null;
		try {
			item = uDoCreate();
			uAddToIdlePool(item, false);
		}
		catch (Throwable e) {
			if (item != null) {
				uDoDestroy(new Item[] { item });
			}
			throw new Exception("Failed to create additional item.", e);
		}
	}
	
	@Override
	public ObjectPoolItem borrowObject(Object arg) throws Exception {
		long starttime = System.currentTimeMillis();
		for(;;) {
			Item item = null;
			
			synchronized (this) {
				assertOpen();
				// if there are any sleeping, just grab one of those
				try {
					item = (Item)(this._idlePool.removeFirst());
				} catch(NoSuchElementException e) {
					; /* ignored */
				}
				
				// otherwise
				if(null == item) {
					// check if we can create one
					// (note we know that the num sleeping is 0, else we wouldn't be here)
					if(this._maxActive < 0 || this._numActive < this._maxActive) {
						// allow new object to be created
					} else {
						// the pool is exhausted
						switch(this._whenExhaustedAction) {
							case WHEN_EXHAUSTED_GROW:
								// allow new object to be created
								break;
							case WHEN_EXHAUSTED_FAIL:
								throw new NoSuchElementException("Pool exhausted");
							case WHEN_EXHAUSTED_BLOCK:
								try {
									if(this._maxWait <= 0) {
										wait();
									} else {
										// this code may be executed again after a notify then continue cycle
										// so, need to calculate the amount of time to wait
										final long elapsed = (System.currentTimeMillis() - starttime);
										final long waitTime = this._maxWait - elapsed;
										if (waitTime > 0)
										{
											wait(waitTime);
										}
									}
								} catch(InterruptedException e) {
									Thread.currentThread().interrupt();
									throw e; 
								}
								if(this._maxWait > 0 && ((System.currentTimeMillis() - starttime) >= this._maxWait)) {
									throw new NoSuchElementException("Timeout waiting for idle object");
								} else {
									continue; // keep looping
								}
							default:
								throw new IllegalArgumentException("WhenExhaustedAction property " + this._whenExhaustedAction + " not recognized.");
						}
					}
				}
				this._numActive++;
				this._statMaxActive = Math.max(this._numActive, this._statMaxActive);
				this._statMaxTotal = Math.max(this._numActive+this._idlePool.size(), this._statMaxTotal);
			}
			
			boolean newlyCreated = false;
			try {
				// create new object when needed
				if (null == item) {
					newlyCreated = true;
					item = uDoCreate();
				}
				
				// activate & validate the object
				String label = this._factory.activateObject(item, arg);
				if(this._testOnBorrow && !this._factory.validateObject(item)) {
					throw new Exception("ValidateObject failed");
				}
				item.setState(State.LENT, label);
				return item;
			}
			catch (Throwable e) {
				synchronized (this) {
					if (item != null) {
						sPrepareDestroy(item);
					}
					this._numActive--;
					notifyAll();
				}
				if (newlyCreated) {
					if (e instanceof Exception) {
						throw (Exception) e;
					}
					throw new RuntimeException(e);
				}
				else {
					continue;
				}
			}
		}
	}
	
	@Override
	public void evictObject(ObjectPoolItem obj, long timeoutMillis) {
		if (obj == null) {
			throw new NullPointerException();
		}
		long evict = checkEvict(timeoutMillis, System.nanoTime());
		Item item = (Item) obj;
		synchronized (this) {
			sPrepareEvict(item, evict);
		}
	}
	
	@Override
	public void invalidateObject(ObjectPoolItem obj) throws Exception {
		if (obj == null) {
			throw new NullPointerException();
		}
		Item item = (Item) obj;
		try {
			uDoDestroy(new Item[] { item });
		}
		finally {
			synchronized (this) {
				this._numActive--;
				notifyAll(); // _numActive has changed
			}
		}
	}
	
	/**
	 * Clears any objects sitting idle in the pool.
	 */
	@Override
	public void clear() {
		this._clearLock.lock();
		try {
			Item[] toDestroy;
			synchronized (this) {
				this._evictionCheckList.clear();
				for(Iterator<Item> it = this._idlePool.iterator(); it.hasNext(); ) {
					sPrepareDestroyEvicting(it.next());
					it.remove();
				}
				this._idlePool.clear();
				notifyAll(); // num sleeping has changed
				toDestroy = this._toDestroy.toArray(new Item[this._toDestroy.size()]);
			}
			uDoDestroy(toDestroy);
		}
		finally {
			this._clearLock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Note: </strong> There is no guard to prevent an object
	 * being returned to the pool multiple times. Clients are expected to
	 * discard references to returned objects and ensure that an object is not
	 * returned to the pool multiple times in sequence (i.e., without being
	 * borrowed again between returns). Violating this contract will result in
	 * the same object appearing multiple times in the pool and pool counters 
	 * (numActive, numIdle) returning incorrect values.</p>
	 */
	@Override
	public void returnObject(ObjectPoolItem obj) throws Exception {
		if (obj == null) {
			throw new NullPointerException();
		}
		Item item = (Item) obj;
		if (item.getState() == State.IDLING) {
			throw new IllegalStateException();
		}
		try {
			uAddToIdlePool(item, true);
		}
		catch (Throwable e) {
			uDoDestroy(new Item[] { item });
			// TODO: Correctness here depends on control in addObjectToPool.
			// These two methods should be refactored, removing the 
			// "behavior flag",decrementNumActive, from addObjectToPool.
			synchronized(this) {
				this._numActive--;
				notifyAll();
			}
		}
	}
	
	@Override
	public synchronized void close(final long timeoutMillis) throws Exception {
		long evict = checkEvict(timeoutMillis, System.nanoTime());
		startEvictor(-1);
		this._closed = CLOSE_DAEMON;
		for (int i = 0; i < this._completePool.size(); i++) {
			Item item = this._completePool.get(i);
			sPrepareEvict(item, evict);
		}
		startEvictor(100);
	}
	
	public ObjectPoolItem[] getItems() {
		synchronized (this._completePool) {
			return this._completePool.toArray(new ObjectPoolItem[this._completePool.size()]);
		}
	}
	
	/**
	 * <p>Perform <code>numTests</code> idle object eviction tests, evicting
	 * examined objects that meet the criteria for eviction. If 
	 * <code>testWhileIdle</code> is true, examined objects are validated
	 * when visited (and removed if invalid); otherwise only objects that
	 * have been idle for more than <code>minEvicableIdletimeMillis</code>
	 * are removed.</p>
	 * 
	 * <p>Successive activations of this method examine objects in 
	 * in sequence, cycling through objects in oldest-to-youngest order.</p>
	 *
	 * @throws Exception if the pool is closed or eviction fails.
	 */
	public void evict() throws Exception {
		Item[] toDestroy;
		synchronized (this) {
			if (this._closed >= CLOSE_FINAL) {
				return;
			}
			if (!this._evictionCheckList.isEmpty()) {
				long t = System.nanoTime();
				for (int i = 0; i < this._evictionCheckList.size(); ) {
					Item item = this._evictionCheckList.get(i);
					if (item.evict == 1 || t - item.evict > 0) {
						this._evictionCheckList.remove(i);
						sPrepareDestroyEvicting(item);
						continue;
					}
					i++;
				}
			}
			if(!this._idlePool.isEmpty()) {
				if (null == this._evictionCursor) {
					this._evictionCursor = (this._idlePool.cursor(this._lifo ? this._idlePool.size() : 0));
				}
				int m = this._numTestsPerEvictionRun;
				if (m < 0) {
					m = this._idlePool.size() / -m;
				}
				if (m <= 0) {
					m = 1;
				}
				m = Math.min(m, this._idlePool.size());
				for (int i=0; i<m; i++) {
					if ((this._lifo && !this._evictionCursor.hasPrevious()) || 
							!this._lifo && !this._evictionCursor.hasNext()) {
						this._evictionCursor.close();
						this._evictionCursor = this._idlePool.cursor(this._lifo ? this._idlePool.size() : 0);
					}
					boolean removeObject = false;
					final Item item = this._lifo ? 
							(Item) this._evictionCursor.previous() : 
							(Item) this._evictionCursor.next();
					final long idleTimeMilis = System.currentTimeMillis() - item.stateStamp;
					if ((this._maxIdle >= 0) && (this._idlePool.size() >= this._maxIdle)) {
						removeObject = true;
					} else if ((this._minEvictableIdleTimeMillis > 0)
							&& (idleTimeMilis > this._minEvictableIdleTimeMillis)) {
						removeObject = true;
					} else if ((this._softMinEvictableIdleTimeMillis > 0)
							&& (idleTimeMilis > this._softMinEvictableIdleTimeMillis)
							&& (getNumIdle() > getMinIdle())) {
						removeObject = true;
					}
					if(this._testWhileIdle && !removeObject) {
						boolean active = false;
						try {
							this._factory.activateObject(item, null);
							active = true;
						}
						catch(Exception e) {
							removeObject=true;
						}
						if (active) {
							if(!this._factory.validateObject(item)) {
								removeObject=true;
							}
							else {
								try {
									this._factory.passivateObject(item);
								}
								catch (Exception e) {
									removeObject=true;
								}
							}
						}
					}
					if(removeObject) {
						this._evictionCursor.remove();
						sPrepareDestroy(item);
					}
				}
			} // if !empty
			toDestroy = this._toDestroy.toArray(new Item[this._toDestroy.size()]);
			this._toDestroy.clear();
			
			if (this._closed != 0 && toDestroy.length == 0 && this._completePool.isEmpty()) {
				this._closed = CLOSE_FINAL;
				startEvictor(-1);
				this.timer.unregister();
			}
		}
		uDoDestroy(toDestroy);
	}
	
	/**
	 * Check to see if we are below our minimum number of objects
	 * if so enough to bring us back to our minimum.
	 * 
	 * @throws Exception when {@link #addObject()} fails.
	 */
	private void ensureMinIdle() throws Exception {
		// this method isn't synchronized so the
		// calculateDeficit is done at the beginning
		// as a loop limit and a second time inside the loop
		// to stop when another thread already returned the
		// needed objects
		int m = this._numTestsPerEvictionRun;
		if (m < 0) {
			m = this._minIdle / (-m);
		}
		if (m <= 0) {
			m = 1;
		}
		for (int i = 0; ; ) {
			synchronized (this) {
				if (this._closed > 0 || sCalculateDeficit() <= 0) {
					return;
				}
			}
			Item item = null;
			try {
				item = uDoCreate();
				uAddToIdlePool(item, false);
				if (++i >= m) {
					return;
				}
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				Thread.interrupted();
			}
			catch (Throwable e) {
				if (item != null) {
					uDoDestroy(new Item[] { item });
				}
				throw new Exception("Failed to create additional item.", e);
			}
		}
	}
	
	private int sCalculateDeficit() {
		int objectDeficit = getMinIdle() - getNumIdle();
		if (this._maxActive > 0) {
			int growLimit = Math.max(0, getMaxActive() - getNumActive() - getNumIdle());
			objectDeficit = Math.min(objectDeficit, growLimit);
		}
		return objectDeficit;
	}
	
	
	private Item uDoCreate() throws Throwable {
		Item item = new Item();
		try {
			synchronized (this._completePool) {
				this._completePool.add(item);
			}
			this._factory.makeObject(item);
			return item;
		}
		catch (Throwable e) {
			synchronized (this._completePool) {
				this._completePool.remove(item);
			}
			throw e;
		}
	}
	
	private static long checkEvict(long timeoutMillis, long stamp) {
		if (timeoutMillis == 0) {
			return 1;
		}
		long t = System.nanoTime() + timeoutMillis * 1000000L;
		return (t != 0 && t != 1) ? t : 2;
	}
	
	private void sPrepareEvict(final Item item, final long evict) {
		State state = item.getState();
		switch (state) {
		case EVICTING:
		case EVICTED:
			return;
		default:
			break;
		}
		
		if (item.evict == 0 || item.evict - evict > 0) {
			item.evict = evict;
		}
		if (this._idlePool.remove(item) || evict == 1) { // order important!
			sPrepareDestroy(item);
			return;
		}
		else {
			this._evictionCheckList.add(item);
		}
	}
	
	private void sPrepareDestroy(final Item item) {
		if (item.evict != 0) {
			this._evictionCheckList.remove(item);
		}
		item.setState(State.EVICTING, null);
		this._toDestroy.add(item);
	}
	
	private void sPrepareDestroyEvicting(final Item item) {
		item.setState(State.EVICTING, null);
		this._toDestroy.add(item);
	}
	
	private void uDoDestroy(Item[] toDestroy) {
		if (toDestroy != null) {
			for (Item item : toDestroy) {
				item.setState(State.EVICTING, null);
				try {
					this._factory.destroyObject(item);
				}
				catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				item.destroyStamp = System.currentTimeMillis();
				synchronized (this._completePool) {
					this._completePool.remove(item);
					item.setState(State.EVICTED, null);
				}
			}
		}
	}
	
	//--- non-public methods ----------------------------------------
	
	/**
	 * Start the eviction thread or service, or when
	 * <i>delay</i> is non-positive, stop it
	 * if it is already running.
	 *
	 * @param delay milliseconds between evictor runs.
	 */
	private synchronized void startEvictor(long delay) {
		if (this.evictorFuture != null) {
			this.evictorFuture.cancel(false);
		}
		if (delay >= 0 && this._closed <= CLOSE_DAEMON) {
			if (this.evictor == null) {
				this.evictor = new Evictor();
			}
			this.evictorFuture = this.timer.schedule(this.evictor, delay/2, delay);
		}
	}
	
	synchronized String debugInfo() {
		StringBuffer buf = new StringBuffer();
		buf.append("Active: ").append(getNumActive()).append("\n");
		buf.append("Idle: ").append(getNumIdle()).append("\n");
		buf.append("Idle Objects:\n");
		Iterator<Item> it = this._idlePool.iterator();
		long time = System.currentTimeMillis();
		while(it.hasNext()) {
			Item pair = it.next();
			buf.append("\t").append(pair.value).append("\t").append(time - pair.stateStamp).append("\n");
		}
		return buf.toString();
	}
	
	/**
	 * Throws an <code>IllegalStateException</code> when this pool has been closed.
	 * @throws IllegalStateException when this pool has been closed.
	 * @see #isClosed()
	 */
	private void assertOpen() throws IllegalStateException {
		if(this._closed != 0) {
			throw new IllegalStateException("Pool not open");
		}
	}
	
	private void uAddToIdlePool(Item item, boolean decrementNumActive) throws Exception {
		boolean success = true;
		if((this._testOnReturn && !(this._factory.validateObject(item))) || item.evict != 0) {
			success = false;
		}
		else {
			this._factory.passivateObject(item);
		}
		
		// Add instance to pool if there is room and it has passed validation
		// (if testOnreturn is set)
		synchronized (this) {
			switch (item.getState()) {
			case EVICTING:
			case EVICTED:
				return;
			default:
				break;
			}
			final int numActive = (decrementNumActive) ? (this._numActive - 1) : this._numActive;
			final int numIdle = this._idlePool.size() + 1;
			if (success && this._closed == 0 && numIdle <= this._maxIdle && item.evict == 0) {
				// borrowObject always takes the first element from the queue,
				// so for LIFO, push on top, FIFO add to end
				item.setState(State.IDLING, null);
				if (this._lifo) {
					this._idlePool.addFirst(item);
				}
				else {
					this._idlePool.addLast(item);
				}
				this._statMaxIdle = Math.max(numIdle, this._statMaxIdle);
				this._statMaxTotal = Math.max(numActive+numIdle, this._statMaxTotal);
			}
			else {
				sPrepareDestroy(item);
			}
			
			if (decrementNumActive) {
				this._numActive = numActive;
				notifyAll();
			}
		}
	}
	
	//--- inner classes ----------------------------------------------
	
	/**
	 * The idle object evictor {@link TimerTask}.
	 * @see ExtGenericObjectPool#setTimeBetweenEvictionRunsMillis
	 */
	private class Evictor implements Runnable {
		
		@Override
		public void run() {
			ExtGenericObjectPool.this._clearLock.lock();
			try {
				evict();
			}
			catch(Throwable e) {
				e.printStackTrace();
			}
			finally {
				ExtGenericObjectPool.this._clearLock.unlock();
			}
			
			ExtGenericObjectPool.this._clearLock.lock();
			try {
				ensureMinIdle();
			} catch(Throwable e) {
				e.printStackTrace();
			}
			finally {
				ExtGenericObjectPool.this._clearLock.unlock();
			}
		}
	}
	
}

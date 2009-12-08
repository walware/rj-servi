/*******************************************************************************
 * Copyright (c) 2009 WalWare/RJ-Project (www.walware.de/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.internal;

import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.IDisposable;


public class Activator implements BundleActivator, ECommons.IAppEnvironment {
	
	
	private static Activator plugin;
	
	public static Activator getDefault() {
		return plugin;
	}
	
	
	private BundleContext context;
	
	private final CopyOnWriteArraySet<IDisposable> stopListeners = new CopyOnWriteArraySet<IDisposable>();
	
	
	public void start(final BundleContext context) throws Exception {
		Activator.plugin = this;
		this.context = context;
		
		ECommons.init("de.walware.rj.servi", this);
	}
	
	public void stop(final BundleContext context) throws Exception {
		try {
			for (final IDisposable listener : this.stopListeners) {
				listener.dispose();
			}
		}
		finally {
			this.stopListeners.clear();
			this.context = null;
			Activator.plugin = null;
		}
	}
	
	
	public void log(final IStatus status) {
		Platform.getLog(this.context.getBundle()).log(status);
	}
	
	public void addStoppingListener(final IDisposable listener) {
		this.stopListeners.add(listener);
	}
	
	public void removeStoppingListener(final IDisposable listener) {
		this.stopListeners.remove(listener);
	}
	
}

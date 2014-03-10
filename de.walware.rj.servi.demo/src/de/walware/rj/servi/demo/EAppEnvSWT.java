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

package de.walware.rj.servi.demo;

import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.IDisposable;


/** Replaces missing Eclipse environment */
public class EAppEnvSWT implements ECommons.IAppEnvironment, DisposeListener {
	
	
	private final CopyOnWriteArraySet<IDisposable> stopListeners = new CopyOnWriteArraySet<IDisposable>();
	
	
	public EAppEnvSWT() {
		ECommons.init("de.walware.rj.services.eruntime", this);
	}
	
	
	@Override
	public void addStoppingListener(IDisposable listener) {
		stopListeners.add(listener);
	}
	
	@Override
	public void removeStoppingListener(IDisposable listener) {
		stopListeners.add(listener);
	}
	
	@Override
	public void log(IStatus status) {
		System.out.println(status.toString());
	}
	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		try {
			for (final IDisposable listener : stopListeners) {
				listener.dispose();
			}
		}
		finally {
			stopListeners.clear();
		}
	}
	
}

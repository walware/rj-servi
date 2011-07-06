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
	
	
	public void addStoppingListener(IDisposable listener) {
		stopListeners.add(listener);
	}
	
	public void removeStoppingListener(IDisposable listener) {
		stopListeners.add(listener);
	}
	
	public void log(IStatus status) {
		System.out.println(status.toString());
	}
	
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

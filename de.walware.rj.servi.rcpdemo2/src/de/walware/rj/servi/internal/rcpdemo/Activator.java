package de.walware.rj.servi.internal.rcpdemo;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.IDisposable;

import de.walware.rj.eclient.graphics.comclient.ERGraphicFactory;
import de.walware.rj.servi.rcpdemo.RServiManager;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID = "de.walware.rj.servi.rcpdemo2";
	
	
	private static Activator plugin;
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	
	private RServiManager rserviManager;
	
	private ERGraphicFactory graphicFactory;
	
	
	/**
	 * The constructor
	 */
	public Activator() {
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		IDisposable disposable;
		synchronized (this) {
			disposable = this.rserviManager;
			this.rserviManager = null;
		}
		if (disposable != null) {
			disposable.dispose();
		}
		
		plugin = null;
		super.stop(context);
	}
	
	
	public synchronized RServiManager getRServiManager() {
		if (this.rserviManager == null) {
			this.rserviManager = new RServiManager("RCPDemo", getRGraphicFactory());
		}
		return this.rserviManager;
	}
	
	public synchronized ERGraphicFactory getRGraphicFactory() {
		if (this.graphicFactory == null) {
			this.graphicFactory = new ERGraphicFactory();
		}
		return this.graphicFactory;
	}
	
}

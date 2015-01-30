/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal.rcpdemo;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.rj.eclient.graphics.comclient.ERGraphicFactory;
import de.walware.rj.servi.rcpdemo.RServiManager;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID = "de.walware.rj.servi.rcpdemo";
	
	
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

/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.webapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.el.ELContext;
import javax.faces.context.FacesContext;

import de.walware.rj.servi.RServi;
import de.walware.rj.servi.pool.PoolServer;
import de.walware.rj.servi.pool.RServiPool;
import de.walware.rj.servi.pool.RServiPoolManager;


public class DebugBean {
	
	
	private final List<RServi> nodes = new ArrayList<RServi>();
	
	
	public DebugBean() {
	}
	
	
	@PreDestroy
	public void destroy() {
		actionCloseAllNodes();
	}
	
	
	public String actionNewNode() {
		final PoolServer poolServer = FacesUtils.getPoolServer();
		final RServiPoolManager poolManager = poolServer.getManager();
		
		if (poolManager == null) {
			FacesUtils.addErrorMessage(null, "The pool is currently not available.");
			return null;
		}
		try {
			final RServi rservi = ((RServiPool) poolManager).getRServi("control-web-app", null);
			synchronized(this) {
				this.nodes.add(rservi);
			}
		}
		catch (final Exception e) {
			FacesUtils.addErrorMessage(null, "An error occurred getting a node: " + e.getMessage());
		}
		refreshPoolStatus();
		return null;
	}
	
	public String actionCloseAllNodes() {
		synchronized (this) {
			for (final Iterator<RServi> iter = this.nodes.iterator(); iter.hasNext(); ) {
				final RServi rservi = iter.next();
				iter.remove();
				try {
					rservi.close();
				}
				catch (final Exception e) {
					FacesUtils.addErrorMessage(null, "An error occurred closing a node: " +e.getMessage());
				}
			}
		}
		
		refreshPoolStatus();
		return RJWeb.POOLSTATUS_NAV;
	}
	
	private void refreshPoolStatus() {
		final FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			try {
				final ELContext elContext = context.getELContext();
				final PoolStatusBean poolStatus = (PoolStatusBean) elContext.getELResolver().getValue(elContext, null, "poolStatus");
				if (poolStatus != null) {
					poolStatus.forceRefresh();
				}
			}
			catch (final Exception exception) {}
		}
	}
	
}

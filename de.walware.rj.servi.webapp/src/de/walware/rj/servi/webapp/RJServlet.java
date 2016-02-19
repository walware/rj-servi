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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.walware.rj.servi.pool.JMPoolServer;


public class RJServlet extends HttpServlet {
	
	
	private JMPoolServer server;
	
	
	public RJServlet(){
	}
	
	
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		try {
			String id = getServletContext().getContextPath();
			if (id.startsWith("/")) {
				id = id.substring(1);
			}
			getServletContext().setAttribute(RJWeb.POOLID_KEY, id);
			
			final ServletRJContext rjContext = new ServletRJContext(getServletContext());
			getServletContext().setAttribute(RJWeb.RJCONTEXT_KEY, rjContext);
			
			this.server = new JMPoolServer(id, rjContext);
			this.server.start();
			
			getServletContext().setAttribute(RJWeb.RJ_POOLSERVER_KEY, this.server);
		}
		catch (final Exception e) {
			if (this.server != null) {
				this.server.shutdown();
				this.server = null;
			}
			throw new ServletException("Failed to initialized RServi Server.", e);
		}
	}
	
	@Override
	public void destroy() {
		if (this.server != null) {
			getServletContext().removeAttribute(RJWeb.RJ_POOLSERVER_KEY);
			
			this.server.shutdown();
		}
	}
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		if (this.server != null) {
			response.setStatus(HttpServletResponse.SC_OK);
		}
		else {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}
	
}

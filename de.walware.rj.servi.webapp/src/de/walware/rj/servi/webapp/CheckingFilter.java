/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/RJ-Project (www.walware.de/goto/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Checks http request:
 *  - session timeout (to avoid JSF message)
 */
public class CheckingFilter implements Filter {
	
	
	public CheckingFilter() {
	}
	
	
	public void init(FilterConfig fConfig) throws ServletException {
	}
	
	public void destroy() {
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpSession session = httpRequest.getSession(false);
		if (!httpRequest.getParameterMap().isEmpty()
				&& httpRequest.getRequestedSessionId() != null
				&& ((session == null) || session.isNew() || !session.getId().equals(httpRequest.getRequestedSessionId()))
				) {
			httpResponse.sendRedirect(httpRequest.getContextPath()+"/faces/resources/sessionexpired.jsp");
			return;
		}
		chain.doFilter(request, response);
		return;
	}
	
}

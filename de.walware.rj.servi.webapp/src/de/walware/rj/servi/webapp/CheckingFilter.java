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
	
	
	@Override
	public void init(final FilterConfig fConfig) throws ServletException {
	}
	
	@Override
	public void destroy() {
	}
	
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		final HttpSession session = httpRequest.getSession(false);
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

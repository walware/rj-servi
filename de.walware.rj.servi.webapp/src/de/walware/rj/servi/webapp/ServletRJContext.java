/*******************************************************************************
 * Copyright (c) 2009-2013 Stephan Wahlbrink (WalWare.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.RJContext;


public class ServletRJContext extends RJContext {
	
	
	private final ServletContext servletContext;
	
	
	public ServletRJContext(final ServletContext context) {
		this.servletContext = context;
	}
	
	
	@Override
	protected String[] getLibDirPaths() throws RjInvalidConfigurationException {
		return new String[] { this.servletContext.getRealPath("WEB-INF/lib") };
	}
	
	
	@Override
	public String getServerPolicyFilePath() throws RjInvalidConfigurationException {
		String path = this.servletContext.getRealPath("WEB-INF/lib");
		final int length = path.length();
		if (length == 0 || (path.charAt(length-1) != '/' && path.charAt(length-1) != File.separatorChar)) {
			path += File.separatorChar;
		}
		return path + "security.policy";
	}
	
	
	@Override
	protected String getPropertiesDirPath() {
		return "/WEB-INF/";
	}
	
	@Override
	protected InputStream getInputStream(final String path) throws IOException {
		return this.servletContext.getResourceAsStream(path);
	}
	
	@Override
	protected OutputStream getOutputStream(final String path) throws IOException {
		final String realPath = this.servletContext.getRealPath(path);
		if (realPath == null) {
			throw new IOException("Writing to '" + path + "' not supported.");
		}
		final File file = new File(realPath);
		if (!file.exists()) {
			file.createNewFile();
		}
		return new FileOutputStream(file, false);
	}
	
}

/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/RJ-Project (www.walware.de/goto/opensource).
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
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;

import javax.servlet.ServletContext;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.PropertiesBean;


public class Utils {
	
	
	public static InputStream getPropertiesFileInput(final ServletContext servletContext, final String name) throws IOException {
		final String path = "/WEB-INF/" + name + ".properties";
		final URL resource = servletContext.getResource(path);
		if (resource != null) {
			return resource.openStream();
		}
		return null;
	}
	
	public static OutputStream getPropertiesFileOutput(final ServletContext servletContext, final String name) throws IOException {
		final String path = "/WEB-INF/" + name + ".properties";
		final String realPath = servletContext.getRealPath(path);
		if (realPath != null) {
			final File file = new File(realPath);
			if (!file.exists()) {
				file.createNewFile();
			}
			return new FileOutputStream(file, false);
		}
		return null;
	}
	
	public static PropertiesBean loadFromFile(final ServletContext context, final PropertiesBean bean) throws RjException {
		InputStream in = null;
		try {
			in = getPropertiesFileInput(context, bean.getBeanId());
			if (in == null) {
				context.log(
						MessageFormat.format("The configuration file ''{0}'' could not be found. Default values are used.", new Object[] { bean.getBeanId() }));
				return bean;
			}
			
			final Properties properties = new Properties();
			properties.load(in);
			bean.load(properties);
			return bean;
		}
		catch (final Exception e) {
			throw new RjException(
					MessageFormat.format("Failed to load the configuration from file ''{0}''.", new Object[] { bean.getBeanId() }),
					e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (final IOException ioexception) {}
			}
		}
	}
	
	
	private Utils() {}
	
}

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import de.walware.rj.servi.pool.PropertiesBean;
import de.walware.rj.servi.pool.RServiPoolManager;


public class FacesUtils {
	
	
	public static void addErrorMessage(final String id, String message) {
		final String label = getLabel(id);
		if (label != null) {
			message = label + ": " + message;
		}
		FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
	}
	
	public static String getLabel(final String componentId) {
		if (componentId != null) {
			final UIComponent component = FacesContext.getCurrentInstance().getViewRoot().findComponent(componentId);
			if (component != null) {
				return (String)component.getAttributes().get("label");
			}
		}
		return null;
	}
	
	public static RServiPoolManager getPoolManager() {
		final Object externalContext = FacesContext.getCurrentInstance().getExternalContext().getContext();
		if (externalContext instanceof ServletContext) {
			return (RServiPoolManager) ((ServletContext) externalContext).getAttribute(RJWeb.POOLMANAGER_KEY);
		}
		throw new IllegalThreadStateException();
	}
	
	public static String getPoolId() {
		final Object externalContext = FacesContext.getCurrentInstance().getExternalContext().getContext();
		if (externalContext instanceof ServletContext) {
			return (String) ((ServletContext) externalContext).getAttribute(RJWeb.POOLID_KEY);
		}
		throw new IllegalThreadStateException();
	}
	
	public static InputStream getPropertiesFileInput(final String name) throws IOException {
		final Object externalContext = FacesContext.getCurrentInstance().getExternalContext().getContext();
		if (externalContext instanceof ServletContext) {
			return Utils.getPropertiesFileInput((ServletContext)externalContext, name);
		}
		throw new IllegalThreadStateException();
	}
	
	public static OutputStream getPropertiesFileOutput(final String name) throws IOException {
		final Object externalContext = FacesContext.getCurrentInstance().getExternalContext().getContext();
		if (externalContext instanceof ServletContext) {
			return Utils.getPropertiesFileOutput((ServletContext)externalContext, name);
		}
		throw new IllegalThreadStateException();
	}
	
	public static boolean saveToFile(final PropertiesBean bean) {
		OutputStream out = null;
		try {
			out = getPropertiesFileOutput(bean.getBeanId());
			final Properties properties = new Properties();
			bean.save(properties);
			properties.store(out, null);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration saved.", null));
			return true;
		}
		catch (final Exception e) {
			FacesContext.getCurrentInstance().getExternalContext().log(
					MessageFormat.format("Failed to save to the configuration file ''{0}''.", new Object[] { bean.getBeanId() }), e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to save the configuration: " + e.getMessage(), null));
			return false;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (final IOException e) {}
			}
		}
	}
	
	public static boolean loadFromFile(final PropertiesBean bean) {
		return loadFromFile(bean, true);
	}
	
	public static boolean loadFromFile(final PropertiesBean bean, final boolean report) {
		InputStream in = null;
		try {
			in = getPropertiesFileInput(bean.getBeanId());
			if (in == null) {
				if (report) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							MessageFormat.format("The configuration file ''{0}'' could not be found. Default values are used.", new Object[] { bean.getBeanId() }), null));
				}
				return false;
			}
			final Properties properties = new Properties();
			properties.load(in);
			bean.load(properties);
			return true;
		}
		catch (final Exception e) {
			if (report) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						MessageFormat.format("Failed to load the configuration from file ''{0}''.", new Object[] { bean.getBeanId() }), null));
			}
			return false;
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
	
	
	private FacesUtils() {}
	
}

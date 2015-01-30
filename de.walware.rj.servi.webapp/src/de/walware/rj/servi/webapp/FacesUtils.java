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

package de.walware.rj.servi.webapp;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import de.walware.rj.server.srvext.RJContext;
import de.walware.rj.servi.pool.NetConfig;
import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.PoolServer;
import de.walware.rj.servi.pool.PropertiesBean;
import de.walware.rj.servi.pool.PropertiesBean.ValidationMessage;
import de.walware.rj.servi.pool.RServiNodeConfig;


public class FacesUtils {
	
	
	public static String toUI(final String formId, final String propertyId) {
		if (formId == null || propertyId == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder(formId.length() + propertyId.length() + 1);
		sb.append(formId);
		sb.append(':');
		for (int i = 0; i < propertyId.length(); i++) {
			final char c = propertyId.charAt(i);
			if (c == '.') {
				sb.append('_');
			}
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String toFormId(final String beanId) {
		if (beanId == NetConfig.BEAN_ID) {
			return "net_config";
		}
		if (beanId == RServiNodeConfig.BEAN_ID) {
			return "r_config";
		}
		if (beanId == PoolConfig.BEAN_ID) {
			return "pool_config";
		}
		return null;
	}
	
	public static boolean validate(final PropertiesBean bean) {
		final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
		if (bean.validate(messages)) {
			return true;
		}
		final String formId = toFormId(bean.getBeanId()) + "_config";
		for (final ValidationMessage message : messages) {
			addErrorMessage(toUI(formId, message.getPropertyId()), message.getMessage());
		}
		return false;
	}
	
	public static void addErrorMessage(final String ui, String message) {
		{	int end = -1;
			while (true) {
				final int begin = message.indexOf('{', end + 1);
				if (begin < 0) {
					break;
				}
				end = message.indexOf('}', begin + 1);
				if (end >= 0) {
					final String label = getLabel(message.substring(begin + 1, end));
					if (label != null && !label.isEmpty()) {
						message = message.substring(0, begin) + label + message.substring(end + 1);
					}
				}
				else {
					end = begin;
				}
			}
		}
		
		final String label = getLabel(ui);
		if (label != null) {
			message = label + ": " + message;
		}
		
		FacesContext.getCurrentInstance().addMessage(ui, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
	}
	
	public static String getLabel(final String componentId) {
		if (componentId != null) {
			final UIComponent component = FacesContext.getCurrentInstance().getViewRoot().findComponent(componentId);
			if (component != null) {
				return (String) component.getAttributes().get("label");
			}
		}
		return null;
	}
	
	public static PoolServer getPoolServer() {
		final Object externalContext = FacesContext.getCurrentInstance().getExternalContext().getContext();
		if (externalContext instanceof ServletContext) {
			return (PoolServer) ((ServletContext) externalContext).getAttribute(RJWeb.RJ_POOLSERVER_KEY);
		}
		throw new IllegalStateException();
	}
	
	private static RJContext getRJContext() {
		final Object externalContext = FacesContext.getCurrentInstance().getExternalContext().getContext();
		if (externalContext instanceof ServletContext) {
			return (RJContext) ((ServletContext) externalContext).getAttribute(RJWeb.RJCONTEXT_KEY);
		}
		throw new IllegalStateException();
	}
	
	public static String getPoolId() {
		final Object externalContext = FacesContext.getCurrentInstance().getExternalContext().getContext();
		if (externalContext instanceof ServletContext) {
			return (String) ((ServletContext) externalContext).getAttribute(RJWeb.POOLID_KEY);
		}
		throw new IllegalStateException();
	}
	
	
	public static boolean saveToFile(final PropertiesBean bean) {
		try {
			final Properties properties = new Properties();
			
			synchronized (bean) {
				if (!validate(bean)) {
					return false;
				}
				bean.save(properties);
			}
			
			getRJContext().saveProperties(bean.getBeanId(), properties);
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuration saved.", null));
			return true;
		}
		catch (final Exception e) {
			FacesContext.getCurrentInstance().getExternalContext().log(
					MessageFormat.format("Failed to save to the configuration file ''{0}''.", new Object[] { bean.getBeanId() }), e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to save the configuration: " + e.getMessage(), null));
			return false;
		}
	}
	
	public static boolean loadFromFile(final PropertiesBean bean, final boolean report) {
		try {
			final Properties properties = getRJContext().loadProperties(bean.getBeanId());
			if (properties == null) {
				if (report) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							MessageFormat.format("The configuration file ''{0}'' could not be found. Default values are used.", new Object[] { bean.getBeanId() }), null));
				}
				return false;
			}
			
			synchronized(bean) {
				bean.load(properties);
				
				validate(bean);
			}
			return true;
		}
		catch (final Exception e) {
			if (report) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						MessageFormat.format("Failed to load the configuration from file ''{0}''.", new Object[] { bean.getBeanId() }), null));
			}
			return false;
		}
	}
	
	
	private FacesUtils() {}
	
}

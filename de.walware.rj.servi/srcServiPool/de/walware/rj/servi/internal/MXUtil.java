/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.OperationsException;

import de.walware.rj.RjInitFailedException;
import de.walware.rj.server.srvext.RJContext;
import de.walware.rj.servi.pool.PropertiesBean;
import de.walware.rj.servi.pool.PropertiesBean.ValidationMessage;


public class MXUtil {
	
	public static boolean validate(final PropertiesBean bean) throws OperationsException {
		final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
		if (bean.validate(messages)) {
			return true;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(MessageFormat.format("The configuration ''{0}'' has invalid values:\n", bean.getBeanId()));
		for (final ValidationMessage message : messages) {
			if (message.getPropertyId() != null) {
				sb.append("{");
				sb.append(message.getPropertyId());
				sb.append("}: ");
			}
			sb.append(message);
			sb.append('\n');
		}
		sb.deleteCharAt(sb.length() - 1);
		throw new OperationsException(sb.toString());
	}
	
	public static PropertiesBean loadInit(final PropertiesBean bean, final RJContext context) throws RjInitFailedException {
		try {
			final Properties map = context.loadProperties(bean.getBeanId());
			if (map == null) {
				Utils.logInfo(MessageFormat.format("The configuration ''{0}'' could not be found. Default values are used.", bean.getBeanId()));
			}
			else {
				bean.load(map);
				bean.validate(null);
			}
			
			return bean;
		}
		catch (final IOException e) {
			throw new RjInitFailedException(MessageFormat.format("Failed to load configuration ''{0}''.", bean.getBeanId()), e);
		}
	}
	
	
	public static PropertiesBean load(final PropertiesBean bean, final RJContext context) throws OperationsException {
		try {
			final Properties map = context.loadProperties(bean.getBeanId());
			
			synchronized (bean) {
				bean.load(map);
				validate(bean);
			}
			
			return bean;
		}
		catch (final IOException e) {
			throw new OperationsException(MessageFormat.format("Failed to load configuration ''{0}'': {1}", bean.getBeanId(), e.getMessage()));
		}
	}
	
	public static void save(final PropertiesBean bean, final RJContext context) throws OperationsException {
		try {
			final Properties map = new Properties();
			
			synchronized (bean) {
				if (!validate(bean)) {
					return;
				}
				bean.save(map);
			}
			
			context.saveProperties(bean.getBeanId(), map);
			
			return;
		}
		catch (final IOException e) {
			throw new OperationsException(MessageFormat.format("Failed to save configuration ''{0}'': {1}", bean.getBeanId(), e.getMessage()));
		}
	}
	
}

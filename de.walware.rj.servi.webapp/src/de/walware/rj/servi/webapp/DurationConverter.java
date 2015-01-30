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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;


public class DurationConverter implements Converter {
	
	
	public DurationConverter() {
	}
	
	
	@Override
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		return null;
	}
	
	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		if (value instanceof Long) {
			long time1 = ((Long)value).longValue();
			if (time1 < 0L) {
				time1 = -time1;
			}
			time1 /= 1000L;
			final long sec = time1 % 60L;
			time1 /= 60L;
			final long min = time1 % 60L;
			time1 /= 60L;
			final long hour = time1 % 24L;
			time1 /= 24L;
			final StringBuilder sb = new StringBuilder();
			if (time1 > 0L) {
				sb.append(time1);
				sb.append(time1 != 1L ? " days " : " day ");
			}
			if (hour < 10L) {
				sb.append('0');
			}
			sb.append(Long.toString(hour));
			sb.append(':');
			if (min < 10L) {
				sb.append('0');
			}
			sb.append(Long.toString(min));
			sb.append(':');
			if (sec < 10L) {
				sb.append('0');
			}
			sb.append(Long.toString(sec));
			return sb.toString();
		}
		return null;
	}
	
}

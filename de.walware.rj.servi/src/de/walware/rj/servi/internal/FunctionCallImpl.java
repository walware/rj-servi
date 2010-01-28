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

package de.walware.rj.servi.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RComplexStore;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RNumericStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RComplexDataBImpl;
import de.walware.rj.data.defaultImpl.RIntegerDataImpl;
import de.walware.rj.data.defaultImpl.RNumericDataBImpl;
import de.walware.rj.data.defaultImpl.RVectorImpl;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RService;


public class FunctionCallImpl implements FunctionCall {
	
	
	private final String name;
	
	private final List<String> argNames = new ArrayList<String>();
	private final List<Object> argValues = new ArrayList<Object>();
	
	private final RService service;
	
	
	public FunctionCallImpl(final RService service, final String name) {
		this.service = service;
		this.name = name;
	}
	
	
	public FunctionCall add(final String arg, final String expression) {
		if (expression == null) {
			throw new NullPointerException();
		}
		this.argNames.add(arg);
		this.argValues.add(expression);
		return this;
	}
	
	public FunctionCall add(final String expression) {
		return this.add(null, expression);
	}
	
	public FunctionCall add(final String arg, final RObject data) {
		if (data == null) {
			throw new NullPointerException();
		}
		this.argNames.add(arg);
		this.argValues.add(data);
		return this;
	}
	
	public FunctionCall add(final RObject data) {
		return this.add(null, data);
	}
	
	public FunctionCall addLogi(final String arg, final boolean logical) {
		this.argNames.add(arg);
		this.argValues.add(logical ? "TRUE" : "FALSE");
		return this;
	}
	
	public FunctionCall addLogi(final boolean logical) {
		return addLogi(null, logical);
	}
	
	public FunctionCall addInt(final String arg, final int integer) {
		final RVectorImpl<RIntegerStore> data = new RVectorImpl<RIntegerStore>(
				new RIntegerDataImpl(new int[] { integer }, null));
		this.argNames.add(arg);
		this.argValues.add(data);
		return this;
	}
	
	public FunctionCall addInt(final int integer) {
		return addInt(null, integer);
	}
	
	public FunctionCall addNum(final String arg, final double numeric) {
		final RVectorImpl<RNumericStore> data = new RVectorImpl<RNumericStore>(
				new RNumericDataBImpl(new double[] { numeric }, null));
		this.argNames.add(arg);
		this.argValues.add(data);
		return this;
	}
	
	public FunctionCall addNum(final double numeric) {
		return this.addNum(null, numeric);
	}
	
	public FunctionCall addChar(final String arg, final String character) {
		final RVectorImpl<RCharacterStore> data = new RVectorImpl<RCharacterStore>(
				new RCharacterDataImpl(new String[] { character }));
		this.argNames.add(arg);
		this.argValues.add(data);
		return this;
	}
	
	public FunctionCall addChar(final String character) {
		return this.addChar(null, character);
	}
	
	public FunctionCall addCplx(final String arg, final double real, final double imaginary) {
		final RVectorImpl<RComplexStore> data = new RVectorImpl<RComplexStore>(
				new RComplexDataBImpl(new double[] { real }, new double[] {imaginary }, null) );
		this.argNames.add(arg);
		this.argValues.add(data);
		return this;
	}
	
	public FunctionCall addCplx(final double real, final double imaginary) {
		return addCplx(null, real, imaginary);
	}
	
	public FunctionCall addNull(final String arg) {
		this.argNames.add(arg);
		this.argValues.add("NULL");
		return this;
	}
	
	public FunctionCall addNull() {
		return this.addNull(null);
	}
	
	
	private String prepare(final IProgressMonitor monitor) throws CoreException {
		final StringBuilder call = new StringBuilder(this.name.length() + this.argNames.size()*10);
		call.append(this.name);
		call.append('(');
		if (this.argNames.size() > 0) {
			int i = 0;
			try {
				for (i = 0; i < this.argNames.size(); i++) {
					final String argName = this.argNames.get(i);
					if (argName != null) {
						call.append(argName);
						call.append('=');
					}
					final Object value = this.argValues.get(i);
					if (value instanceof String) {
						call.append((String) value);
					}
					else if (value instanceof RObject) {
						final String tmp = "rjfctmp"+i;
						this.service.assignData(tmp, (RObject) value, monitor);
						call.append(".rj.getTmp(\"");
						call.append(tmp);
						call.append("\")");
					}
					call.append(',');
				}
				call.setLength(call.length()-1);
			}
			catch (final CoreException e) {
				if (e.getStatus().getSeverity() == IStatus.CANCEL) {
					throw e;
				}
				final StringBuilder message = new StringBuilder("Failed to prepare argument ");
				message.append(i);
				final String argName = this.argNames.get(i);
				if (argName != null) {
					message.append(" (");
					message.append(argName);
					message.append(")");
				}
				message.append('.');
				throw new CoreException(new Status(IStatus.ERROR, RServiUtil.RJ_SERVI_ID, -1, message.toString(), e));
			}
		}
		call.append(')');
		return call.toString();
	}
	
	public void evalVoid(final IProgressMonitor monitor) throws CoreException {
		final String call = prepare(null);
		this.service.evalVoid(call, monitor);
	}
	
	public RObject evalData(final IProgressMonitor monitor) throws CoreException {
		final String call = prepare(null);
		return this.service.evalData(call, monitor);
	}
	
	public RObject evalData(final String factoryId, final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		final String call = prepare(null);
		return this.service.evalData(call, factoryId, options, depth, monitor);
	}
	
	@Override
	public String toString() {
		final StringBuilder call = new StringBuilder();
		call.append(this.name);
		call.append('(');
		if (this.argNames.size() > 0) {
			for (int i = 0; i < this.argNames.size(); i++) {
				final String argName = this.argNames.get(i);
				if (argName != null) {
					call.append('\n');
					call.append(argName);
					call.append(" = ");
				}
				final Object value = this.argValues.get(i);
				if (value instanceof String) {
					call.append((String) value);
				}
				else if (value instanceof RObject) {
					call.append("\n<DATA>\n");
					call.append(value.toString());
					call.append("\n</DATA>");
				}
			}
			call.append("\n");
		}
		call.append(')');
		return call.toString();
	}
	
}

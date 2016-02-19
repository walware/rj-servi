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

package de.walware.rj.servi.rcpdemo;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.rj.servi.RServi;
import de.walware.rj.servi.internal.rcpdemo.Activator;
import de.walware.rj.services.RService;


public abstract class RJob extends Job {
	
	
	private final RServiManager rServiManager;
	
	
	public RJob(final String name) {
		super(name);
		this.rServiManager = Activator.getDefault().getRServiManager();
		setRule(this.rServiManager.getSchedulingRule());
	}
	
	
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		RServi servi = null;
		try {
			servi = this.rServiManager.getRServi(getName());
			runRTask(servi, monitor);
		}
		catch (final CoreException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"An error occurred when running " + getName() + ".", e);
		}
		finally {
			if (servi != null) {
				try {
					servi.close();
				} catch (final CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public boolean belongsTo(final Object family) {
		return this.rServiManager == family;
	}
	
	protected abstract void runRTask(RService r, IProgressMonitor monitor) throws CoreException;
	
}

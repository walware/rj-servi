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
	
	
	private RServiManager rServiManager;
	
	
	public RJob(String name) {
		super(name);
		rServiManager = Activator.getDefault().getRServiManager();
		setRule(rServiManager.getSchedulingRule());
	}
	
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		RServi servi = null;
		try {
			servi = rServiManager.getRServi(getName());
			runRTask(servi, monitor);
		}
		catch (CoreException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"An error occurred when running " + getName() + ".", e);
		}
		finally {
			if (servi != null) {
				try {
					servi.close();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public boolean belongsTo(Object family) {
		return rServiManager == family;
	}
	
	protected abstract void runRTask(RService r, IProgressMonitor monitor) throws CoreException;
	
}

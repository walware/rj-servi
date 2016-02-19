/*=============================================================================#
 # Copyright (c) 2011-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.rcpdemo;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ts.IQueue;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.internal.RServiImpl;
import de.walware.rj.servi.internal.rcpdemo.Activator;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RGraphicCreator;
import de.walware.rj.services.RPlatform;
import de.walware.rj.services.RService;


/**
 * Implementations of ECommons Tool Service and Scheduling interfaces (de.walware.ecommons.ts) for 
 * RServi using Eclipse jobs.
 */
public class RServiSession extends PlatformObject implements ITool {
	
	
	private class Queue implements IQueue {
		
		public IStatus add(final IToolRunnable runnable) {
			synchronized (RServiSession.this.jobs) {
				if (isTerminated()) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"The R session is terminated.");
				}
				if (!runnable.changed(IToolRunnable.ADDING_TO, RServiSession.this)) {
					return Status.CANCEL_STATUS;
				}
				final RunnableJob job = new RunnableJob(runnable);
				RServiSession.this.jobs.add(job);
				job.addJobChangeListener(RServiSession.this.jobListener);
				job.schedule();
				return Status.OK_STATUS;
			}
		}
		
		public void remove(final IToolRunnable runnable) {
			RunnableJob removed = null;
			synchronized (RServiSession.this.jobs) {
				for (int i = 0; i < RServiSession.this.jobs.size(); i++) {
					final RunnableJob job = RServiSession.this.jobs.get(i);
					if (job.runnable == runnable) {
						if (job.runnable.changed(IToolRunnable.REMOVING_FROM, RServiSession.this)) {
							removed = job;
							RServiSession.this.jobs.remove(i);
							break;
						}
						return;
					}
				}
			}
			if (removed != null) {
				removed.cancel();
			}
		}
		
		public boolean isHotSupported() {
			return false;
		}
		
		public IStatus addHot(final IToolRunnable runnable) {
			return add(runnable);
		}
		
		public void removeHot(final IToolRunnable runnable) {
			remove(runnable);
		}
		
	}
	
	private class RServiService implements IRToolService, RService, IToolService {
		
		public ITool getTool() {
			return RServiSession.this;
		}
		
		public RPlatform getPlatform() {
			return RServiSession.this.servi.getPlatform();
		}
		
		public void evalVoid(final String expression,
				final IProgressMonitor monitor) throws CoreException {
			RServiSession.this.servi.evalVoid(expression, monitor);
		}
		
		public RObject evalData(final String expression,
				final IProgressMonitor monitor) throws CoreException {
			return RServiSession.this.servi.evalData(expression, monitor);
		}
		
		public RObject evalData(final String expression,
				final String factoryId, final int options, final int depth,
				final IProgressMonitor monitor) throws CoreException {
			return RServiSession.this.servi.evalData(expression, factoryId, options, depth, monitor);
		}
		
		public RObject evalData(final RReference reference,
				final IProgressMonitor monitor) throws CoreException {
			return RServiSession.this.servi.evalData(reference, monitor);
		}
		
		public RObject evalData(final RReference reference,
				final String factoryId, final int options, final int depth,
				final IProgressMonitor monitor) throws CoreException {
			return RServiSession.this.servi.evalData(reference, factoryId, options, depth, monitor);
		}
		
		public void assignData(final String expression, final RObject data,
				final IProgressMonitor monitor) throws CoreException {
			RServiSession.this.servi.assignData(expression, data, monitor);
		}
		
		public void uploadFile(final InputStream in, final long length, final String fileName,
				final int options, final IProgressMonitor monitor) throws CoreException {
			RServiSession.this.servi.uploadFile(in, length, fileName, options, monitor);
		}
		
		public void downloadFile(final OutputStream out, final String fileName, final int options,
				final IProgressMonitor monitor) throws CoreException {
			RServiSession.this.servi.downloadFile(fileName, options, monitor);
		}
		
		public byte[] downloadFile(final String fileName, final int options,
				final IProgressMonitor monitor) throws CoreException {
			return RServiSession.this.servi.downloadFile(fileName, options, monitor);
		}
		
		public FunctionCall createFunctionCall(final String name) throws CoreException {
			return RServiSession.this.servi.createFunctionCall(name);
		}
		
		public RGraphicCreator createRGraphicCreator(final int options) throws CoreException {
			return RServiSession.this.servi.createRGraphicCreator(options);
		}
		
	}
	
	private class RunnableJob extends Job {
		
		private final IToolRunnable runnable;
		
		public RunnableJob(final IToolRunnable runnable) {
			super(runnable.getLabel());
			this.runnable = runnable;
			setRule(RServiSession.this.schedulingRule);
			if (runnable instanceof ISystemRunnable) {
				setSystem(true);
			}
		}
		
		@Override
		public boolean belongsTo(final Object family) {
			return (family == RServiSession.this);
		}
		
		@Override
		public boolean shouldRun() {
			synchronized (RServiSession.this.jobs) {
				return RServiSession.this.jobs.remove(this);
			}
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				this.runnable.run(RServiSession.this.service, monitor);
				this.runnable.changed(IToolRunnable.FINISHING_OK, RServiSession.this);
				return Status.OK_STATUS;
			}
			catch (final CoreException e) {
				if (e.getStatus() != null && e.getStatus().getSeverity() == IStatus.CANCEL) {
					this.runnable.changed(IToolRunnable.FINISHING_CANCEL, RServiSession.this);
					return e.getStatus();
				}
				final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"An error occurred when running " + getName() + ".", e);
				StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
				this.runnable.changed(IToolRunnable.FINISHING_ERROR, RServiSession.this);
				return status;
			}
		}
		
	}
	
	private class JobListener implements IJobChangeListener {
		
		public void aboutToRun(final IJobChangeEvent event) {
		}
		
		public void awake(final IJobChangeEvent event) {
		}
		
		public void done(final IJobChangeEvent event) {
			if (event.getResult() == Status.CANCEL_STATUS) {
				synchronized (RServiSession.this.jobs) {
					if (RServiSession.this.jobs.remove(event.getJob())) {
						((RunnableJob) event.getJob()).runnable.changed(IToolRunnable.BEING_ABANDONED, RServiSession.this);
					}
				}
			}
		}
		
		public void running(final IJobChangeEvent event) {
		}
		
		public void scheduled(final IJobChangeEvent event) {
		}
		
		public void sleeping(final IJobChangeEvent event) {
		}
		
	}
	
	
	private final Queue queue = new Queue();
	private final RServiService service = new RServiService();
	private final String label;
	
	private final ISchedulingRule schedulingRule;
	private int state;
	private RServi servi;
	
	private final List<RunnableJob> jobs = new ArrayList<RServiSession.RunnableJob>();
	private final IJobChangeListener jobListener = new JobListener();
	
	
	public RServiSession(final RServi servi) {
		this("R engine", servi, new ISchedulingRule() {
			public boolean contains(final ISchedulingRule rule) {
				return (rule == this);
			}
			public boolean isConflicting(final ISchedulingRule rule) {
				return (rule == this);
			}
		});
	}
	
	public RServiSession(final String label,
			final RServi servi, final ISchedulingRule schedulingRule) {
		this.label = label;
		this.servi = servi;
		this.schedulingRule = schedulingRule;
		
		doStart();
	}
	
	
	public String getMainType() {
		return "R";
	}
	
	public boolean isProvidingFeatureSet(final String featureSetId) {
		return "de.walware.rj.services.RService".equals(featureSetId); //$NON-NLS-1$
	}
	
	public IQueue getQueue() {
		return this.queue;
	}
	
	public boolean isTerminated() {
		return (this.state < 0);
	}
	
	private void doStart() {
		if (this.servi != null) {
			((RServiImpl) this.servi).setRHandle(this);
			this.state = 1;
		}
		else {
			doTerminate();
		}
	}
	
	private void doTerminate() {
		if (this.servi != null) {
			try {
				this.servi.close();
			}
			catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.servi = null;
		}
		if (this.state != -2) {
			this.state = -2;
			terminated();
		}
	}
	
	protected void terminated() {
	}
	
	public String getLabel(final int config) {
		return this.label;
	}
	
	public void close(final boolean immediately) {
		synchronized (this.jobs) {
			if (this.state < 0) {
				return;
			}
			if (immediately) {
				Job.getJobManager().cancel(this);
				for (int i = 0; i < this.jobs.size(); i++) {
					this.jobs.get(i).runnable.changed(IToolRunnable.BEING_ABANDONED, RServiSession.this);
				}
				this.jobs.clear();
			}
			this.queue.add(new ISystemRunnable() {
				public String getTypeId() {
					return "r/session/close";
				}
				public String getLabel() {
					return "Close R Session";
				}
				public boolean isRunnableIn(final ITool tool) {
					return (tool == RServiSession.this);
				}
				public boolean changed(final int event, final ITool tool) {
					return true;
				}
				public void run(final IToolService service,
						final IProgressMonitor monitor) throws CoreException {
					doTerminate();
				}
			});
			this.state = -1;
		}
	}
	
}

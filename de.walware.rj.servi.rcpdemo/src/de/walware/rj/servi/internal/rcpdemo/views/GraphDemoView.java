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

package de.walware.rj.servi.internal.rcpdemo.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.walware.rj.eclient.graphics.IERGraphic;
import de.walware.rj.eclient.graphics.RGraphicComposite;
import de.walware.rj.graphic.RGraphic;
import de.walware.rj.servi.rcpdemo.RJob;
import de.walware.rj.services.RGraphicCreator;
import de.walware.rj.services.RService;


public class GraphDemoView extends ViewPart {
	
	
	public static final String VIEW_ID = "de.walware.rj.servi.rcpdemo.views.GraphDemo";
	
	
	private Text commandControl;
	
	private RGraphicComposite imageControl;
	
	private IERGraphic currentPlot;
	
	
	public GraphDemoView() {
	}
	
	
	@Override
	public void createPartControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		
		final Label label = new Label(composite, SWT.LEFT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText("Graphic &command:");
		
		this.commandControl = new Text(composite, SWT.BORDER);
		this.commandControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Button button = new Button(composite, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		button.setText("Run");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				run();
			}
		});
		
		this.commandControl.setText("hist(rnorm(1e+07))");
		
		this.imageControl = new RGraphicComposite(composite, null);
		this.imageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
	}
	
	protected void setGraphic(final IERGraphic graphic) {
		this.imageControl.setGraphic(graphic);
		
		if (this.currentPlot != null) {
			this.currentPlot.close();
			this.currentPlot = null;
		}
		this.currentPlot = graphic;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (this.currentPlot != null) {
			this.currentPlot.close();
			this.currentPlot = null;
		}
	}
	
	@Override
	public void setFocus() {
		this.commandControl.setFocus();
	}
	
	private void run() {
		final Point size = this.imageControl.getSize();
		final String command = this.commandControl.getText();
		final RJob job = new RJob("GraphDemo") {
			@Override
			protected void runRTask(final RService r, final IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("Creating graphic in R...", 100);
				
				final RGraphicCreator rGraphicCreator = r.createRGraphicCreator(0);
				rGraphicCreator.setSize(size.x, size.y);
				final RGraphic plot = rGraphicCreator.create(command, monitor);
				
				monitor.worked(90);
				
				if (plot instanceof IERGraphic) {
					final IERGraphic erPlot = (IERGraphic) plot;
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (GraphDemoView.this.imageControl.isDisposed()) {
								erPlot.close();
								return;
							}
							setGraphic(erPlot);
						}
					});
				}
				return;
			}
		};
		job.schedule();
	}
	
}

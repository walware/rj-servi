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

package de.walware.rj.servi.internal.rcpdemo.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.rj.servi.internal.rcpdemo.Activator;
import de.walware.rj.servi.rcpdemo.RServiManager;

public class RServiConfigView extends ViewPart {
	
	
	public static final String VIEW_ID = "de.walware.rj.servi.rcpdemo.views.RServiConfig";
	
	
	private Button remoteSelectControl;
	private Text remoteAddressControl;
	private Button embeddedSelectControl;
	private Text embeddedRhomeControl;
	private Button rsetupSelectControl;
	private Text rsetupIdControl;
	
	
	public RServiConfigView() {
	}
	
	
	@Override
	public void createPartControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		this.remoteSelectControl = new Button(composite, SWT.RADIO);
		this.remoteSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.remoteSelectControl.setText("Remote/Pool - RMI pool address:");
		
		{	this.remoteAddressControl = new Text(composite, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalIndent = 10; 
			this.remoteAddressControl.setLayoutData(gd);
		}
		
		this.embeddedSelectControl = new Button(composite, SWT.RADIO);
		this.embeddedSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.embeddedSelectControl.setText("Embedded - R_HOME:");
		
		{	this.embeddedRhomeControl = new Text(composite, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalIndent = 10; 
			this.embeddedRhomeControl.setLayoutData(gd);
		}
		
		{	final Button button = new Button(composite, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			button.setText("Select...");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					final DirectoryDialog dialog = new DirectoryDialog(button.getShell());
					dialog.setMessage("Select R_HOME directory:");
					final String path = dialog.open();
					if (path != null) {
						RServiConfigView.this.embeddedRhomeControl.setText(path);
					}
				}
			});
		}
		
		this.rsetupSelectControl = new Button(composite, SWT.RADIO);
		this.rsetupSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.rsetupSelectControl.setText("Predefined R Setup - Id:");
		
		{	this.rsetupIdControl = new Text(composite, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalIndent = 10; 
			this.rsetupIdControl.setLayoutData(gd);
		}
		
		final Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Button applyControl = new Button(composite, SWT.PUSH);
		applyControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		applyControl.setText("Apply");
		applyControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				applyConfig();
			}
		});
		
		this.remoteSelectControl.setSelection(true);
		this.remoteAddressControl.setText("rmi://localhost/rservi-pool");
		final String rhome = System.getenv("R_HOME");
		this.embeddedRhomeControl.setText((rhome != null) ? rhome : "");
		this.rsetupIdControl.setText("org.rproject.r.DefaultSetup");
	}
	
	@Override
	public void setFocus() {
	}
	
	private void applyConfig() {
		try {
			final RServiManager manager = Activator.getDefault().getRServiManager();
			if (this.remoteSelectControl.getSelection()) {
				manager.setPool(this.remoteAddressControl.getText());
				return;
			}
			if (this.embeddedSelectControl.getSelection()) {
				manager.setEmbedded(this.embeddedRhomeControl.getText());
				return;
			}
			if (this.rsetupSelectControl.getSelection()) {
				manager.setRSetup(this.rsetupIdControl.getText());
				return;
			}
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Could not apply RServi configuration.", e),
					StatusManager.SHOW | StatusManager.LOG);
		}
		return;
	}
	
}

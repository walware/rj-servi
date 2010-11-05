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
		
		remoteSelectControl = new Button(composite, SWT.RADIO);
		remoteSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		remoteSelectControl.setText("Remote/Pool - RMI pool address:");
		
		{	remoteAddressControl = new Text(composite, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalIndent = 10; 
			remoteAddressControl.setLayoutData(gd);
		}
		
		embeddedSelectControl = new Button(composite, SWT.RADIO);
		embeddedSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		embeddedSelectControl.setText("Embedded - R_HOME:");
		
		{	embeddedRhomeControl = new Text(composite, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalIndent = 10; 
			embeddedRhomeControl.setLayoutData(gd);
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
						embeddedRhomeControl.setText(path);
					}
				}
			});
		}
		
		rsetupSelectControl = new Button(composite, SWT.RADIO);
		rsetupSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		rsetupSelectControl.setText("Predefined R Setup - Id:");
		
		{	rsetupIdControl = new Text(composite, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalIndent = 10; 
			rsetupIdControl.setLayoutData(gd);
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
		
		remoteSelectControl.setSelection(true);
		remoteAddressControl.setText("rmi://localhost/rservi-pool");
		embeddedRhomeControl.setText("D:\\R\\R-2.12.0");
		rsetupIdControl.setText("org.rproject.r.DefaultSetup");
	}
	
	@Override
	public void setFocus() {
	}
	
	private void applyConfig() {
		try {
			final RServiManager manager = Activator.getDefault().getRServiManager();
			if (remoteSelectControl.getSelection()) {
				manager.setPool(remoteAddressControl.getText());
				return;
			}
			if (embeddedSelectControl.getSelection()) {
				manager.setEmbedded(embeddedRhomeControl.getText());
				return;
			}
			if (rsetupSelectControl.getSelection()) {
				manager.setRSetup(rsetupIdControl.getText());
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

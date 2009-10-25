package de.walware.rj.servi.internal.rcpdemo.views;

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.walware.rj.servi.rcpdemo.RJob;
import de.walware.rj.services.RService;
import de.walware.rj.services.utils.Graphic;
import de.walware.rj.services.utils.PngGraphic;


public class GraphDemoView extends ViewPart {
	
	
	public static final String VIEW_ID = "de.walware.rj.servi.rcpdemo.views.GraphDemo";
	
	
	private Text commandControl;
	
	private Label imageControl;
	
	private Image currentImage;
	
	
	public GraphDemoView() {
	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		
		Label label = new Label(composite, SWT.LEFT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText("Graphic &command:");
		
		commandControl = new Text(composite, SWT.BORDER);
		commandControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Button button = new Button(composite, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		button.setText("Run");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				run();
			}
		});
		
		commandControl.setText("hist(rnorm(1e+07))");
		
		imageControl = new Label(composite, SWT.NONE);
		imageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
	}
	
	@Override
	public void dispose() {
		if (currentImage != null) {
			currentImage.dispose();
			currentImage = null;
		}
	}
	
	@Override
	public void setFocus() {
		commandControl.setFocus();
	}
	
	private void run() {
		final Point size = imageControl.getSize();
		final String command = commandControl.getText();
		RJob job = new RJob("GraphDemo") {
			@Override
			protected void runRTask(RService r, IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("Creating graphic in R...", 100);
				
				final PngGraphic pngGraphic = new PngGraphic();
				pngGraphic.setSize(size.x, size.y, Graphic.UNIT_PX);
				final byte[] plot = pngGraphic.create(command, r, monitor);
				
				monitor.worked(90);
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (imageControl.isDisposed()) {
							return;
						}
						
						final Image image = new Image(Display.getCurrent(), new ByteArrayInputStream(plot));
						imageControl.setImage(image);
						
						if (currentImage != null) {
							currentImage.dispose();
							currentImage = null;
						}
						currentImage = image;
					}
				});
				return;
			}
		};
		job.schedule();
	}
	
}

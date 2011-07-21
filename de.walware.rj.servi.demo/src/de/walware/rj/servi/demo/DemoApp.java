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

package de.walware.rj.servi.demo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.walware.rj.data.RObject;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.utils.Graphic;
import de.walware.rj.services.utils.PngGraphic;


public class DemoApp {
	
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("(RJ) - RServi Demo/Test");
		shell.setLayout(new FillLayout());
		
		new DemoApp(shell);
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) { 
				display.sleep();
			}
		}
		display.dispose();
	}
	
	
	private Text fInitRemoteText;
	private Button fInitRemoteButton;
	private Text fInitEmbeddedText;
	private Button fInitEmbeddedButton;
	private Button fCloseButton;
	private Text fLogText;
	
	private Text fEvalText;
	private Button fEvalVoidButton;
	private Button fEvalDataButton;
	private Button fAssignDataButton;
	
	private Text fRemoteFileText;
	private Button fUploadButton;
	private Button fDownloadButton;
	private Button fOpenButton;
	
	private FunctionCall fFunctionBuilder;
	private Text fFunctionNameText;
	private Button fFunctionNewButton;
	private Text fFunctionArgumentText;
	private Button fFunctionAddDataButton;
	private Text fFunctionAddExpressionText;
	private Button fFunctionAddExpressionButton;
	private Button fFunctionEvalData;
	
	private Button fCombinedHistButton;
	
	private RServi fRServi;
	
	private RObject fData;
	private File fFile;
	
	
	public DemoApp(Composite parent) {
		EAppEnvSWT eAppEnv = new EAppEnvSWT();
		Control content = createContent(parent);
		
		parent.getShell().addDisposeListener(eAppEnv);
		parent.getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				if (fRServi != null) {
					close();
				}
			}
		});
		checkedEnabled();
	}
	
	
	private Control createContent(Composite parent) {
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		
		Composite composite = new Composite(form, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		{	Composite log = new Composite(form, SWT.NONE);
			GridLayout layout = new GridLayout(1, false);
			layout.marginRight += layout.marginWidth;
			layout.marginWidth = 0;
			log.setLayout(layout);
			
			fLogText = new Text(log, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 20);
			gd.widthHint = 300;
			gd.heightHint = fLogText.getLineHeight()*10;
			fLogText.setLayoutData(gd);
		}
		{	addLabel(composite, "Pool:");
			fInitRemoteText = new Text(composite, SWT.SINGLE | SWT.BORDER);
			fInitRemoteText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			addDummy(composite);
			fInitRemoteButton = new Button(composite, SWT.PUSH);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.widthHint = 300;
			fInitRemoteButton.setLayoutData(gd);
			fInitRemoteButton.setText("Start - Connect Remote");
			fInitRemoteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					initRemote();
				}
			});
			
//			addLabel(composite, "R_HOME:");
//			fInitEmbeddedText = new Text(composite, SWT.SINGLE | SWT.BORDER);
//			fInitEmbeddedText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//			
//			addDummy(composite);
//			fInitEmbeddedButton = new Button(composite, SWT.PUSH);
//			fInitEmbeddedButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//			fInitEmbeddedButton.setText("Start - Embedded");
//			fInitEmbeddedButton.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					initEmbedded();
//				}
//			});
			addDummy(composite);
			fCloseButton = new Button(composite, SWT.PUSH);
			fCloseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fCloseButton.setText("Close");
			fCloseButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					close();
				}
			});
		}
		
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		{	addLabel(composite, "Expression:");
			fEvalText = new Text(composite, SWT.BORDER);
			fEvalText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			addDummy(composite);
			fEvalVoidButton = new Button(composite, SWT.PUSH);
			fEvalVoidButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fEvalVoidButton.setText("Execute #evalVoid");
			fEvalVoidButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					evalVoid();
				}
			});
			
			addDummy(composite);
			fEvalDataButton = new Button(composite, SWT.PUSH);
			fEvalDataButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fEvalDataButton.setText("Execute #evalData");
			fEvalDataButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					evalData();
				}
			});
			
			addDummy(composite);
			fAssignDataButton = new Button(composite, SWT.PUSH);
			fAssignDataButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fAssignDataButton.setText("Execute #assignData");
			fAssignDataButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					assignData();
				}
			});
		}
		
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		{	addLabel(composite, "Remote file:");
			fRemoteFileText = new Text(composite, SWT.BORDER);
			fRemoteFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			addDummy(composite);
			fUploadButton = new Button(composite, SWT.PUSH);
			fUploadButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fUploadButton.setText("Upload File...");
			fUploadButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					uploadFile();
				}
			});
			
			fOpenButton = new Button(composite, SWT.PUSH);
			fOpenButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
			fOpenButton.setText("Open File <-");
			fOpenButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					openFile();
				}
			});
			fDownloadButton = new Button(composite, SWT.PUSH);
			fDownloadButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fDownloadButton.setText("Download File...");
			fDownloadButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					downloadFile();
				}
			});
			
		}
		
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		{	addLabel(composite, "Function:");
			fFunctionNameText = new Text(composite, SWT.BORDER);
			fFunctionNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			addDummy(composite);
			fFunctionNewButton = new Button(composite, SWT.PUSH);
			fFunctionNewButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fFunctionNewButton.setText("New (Restart builder)");
			fFunctionNewButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						fFunctionBuilder = fRServi.createFunctionCall(fFunctionNameText.getText());
					}
					catch (Exception e) {
						logError(e);
					}
					logFunction();
					checkedEnabled();
				}
			});
			
			addLabel(composite, "Argument:");
			fFunctionArgumentText = new Text(composite, SWT.BORDER);
			fFunctionArgumentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			addLabel(composite, "Expression:");
			fFunctionAddExpressionText = new Text(composite, SWT.BORDER);
			fFunctionAddExpressionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			addDummy(composite);
			fFunctionAddDataButton = new Button(composite, SWT.PUSH);
			fFunctionAddDataButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fFunctionAddDataButton.setText("Add data argument");
			fFunctionAddDataButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String arg = fFunctionArgumentText.getText();
					fFunctionBuilder.add(arg.length() > 0 ? arg : null, fData);
					logFunction();
				}
			});
			
			addDummy(composite);
			fFunctionAddExpressionButton = new Button(composite, SWT.PUSH);
			fFunctionAddExpressionButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fFunctionAddExpressionButton.setText("Add expression argument");
			fFunctionAddExpressionButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String arg = fFunctionArgumentText.getText();
					fFunctionBuilder.add(arg.length() > 0 ? arg : null, fFunctionAddExpressionText.getText());
					logFunction();
				}
			});
			
			addDummy(composite);
			fFunctionEvalData = new Button(composite, SWT.PUSH);
			fFunctionEvalData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fFunctionEvalData.setText("Execute #evalData");
			fFunctionEvalData.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					evalFunctionData();
				}
			});
		}
		
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		{	fCombinedHistButton = new Button(composite, SWT.PUSH);
			fCombinedHistButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			fCombinedHistButton.setText("Create and Show 'hist(x)'");
			fCombinedHistButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					showHist();
				}
			});
		}
			
		form.setWeights(new int[] { 2, 3 });
		
		fInitRemoteText.setText("rmi://localhost/rservi-pool");
		String rHome = System.getenv("R_HOME");
		if (rHome != null) {
			fInitEmbeddedText.setText(rHome);
		}
		
		return composite;
	}
	
	private Label addLabel(Composite composite, String text) {
		final Label label = new Label(composite, SWT.NONE);
		label.setText(text);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		return label;
	}
	
	private void addDummy(Composite composite) {
		final Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
	}
	
	private void logOK() {
		fLogText.append(" OK\n");
	}
	
	private void logCancelled() {
		fLogText.append(" CANCELLED\n");
	}
	
	private void logFailed() {
		fLogText.append(" FAILED\n");
	}
	
	private void logFunction() {
		fLogText.append("Current Function:\n");
		fLogText.append(fFunctionBuilder.toString());
		fLogText.append("\n");
	}
	
	private void logError(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		fLogText.append(" FAILED\n");
		fLogText.append(sw.toString());
		fLogText.append("\n");
	}
	
	private void checkedEnabled() {
		boolean ok = (fRServi != null);
		fInitRemoteButton.setEnabled(!ok);
		if (fInitEmbeddedButton != null) {
			fInitEmbeddedButton.setEnabled(!ok);
		}
		fCloseButton.setEnabled(ok);
		
		fEvalVoidButton.setEnabled(ok);
		fEvalDataButton.setEnabled(ok);
		fAssignDataButton.setEnabled(ok && fData != null);
		
		fUploadButton.setEnabled(ok);
		fDownloadButton.setEnabled(ok);
		fOpenButton.setEnabled(fFile != null);
		
		fCombinedHistButton.setEnabled(ok);
		
		fFunctionNewButton.setEnabled(ok);
		ok = ok && (fFunctionBuilder != null);
		fFunctionAddDataButton.setEnabled(ok && fData != null);
		fFunctionAddExpressionButton.setEnabled(ok);
		fFunctionEvalData.setEnabled(ok);
	}
	
	private void initRemote() {
		fLogText.setText("");
		fLogText.append("Requesting RServi instance...");
		try {
			fRServi = RServiUtil.getRServi(fInitRemoteText.getText(), "demo/test");
			logOK();
		}
		catch (Exception e) {
			fRServi = null;
			logError(e);
		}
		checkedEnabled();
	}
	
//	private void initEmbedded() {
//		fLogText.setText("");
//		fLogText.append("Requesting RServi instance...");
//		try {
//			TODO
//			logOK();
//		}
//		catch (Exception e) {
//			fRServi = null;
//			logError(e);
//		}
//		checkedEnabled();
//	}
	
	private void close() {
		fLogText.append("Closing RServi instance...");
		try {
			fFunctionBuilder = null;
			fRServi.close();
			fRServi = null;
			logOK();
		}
		catch (Exception e) {
			fRServi = null;
			logError(e);
		}
		checkedEnabled();
	}
	
	private void evalVoid() {
		fLogText.append("Executing #evalVoid:\n\t");
		String command = fEvalText.getText();
		fLogText.append(command);
		try {
			fRServi.evalVoid(command, null);
			fLogText.append("\n----\n");
		}
		catch (Exception e) {
			logError(e);
		}
		checkedEnabled();
	}
	
	private void evalData() {
		fLogText.append("Executing #evalData:\n\t");
		final String command = fEvalText.getText();
		fLogText.append(command);
		try {
			RObject data = fRServi.evalData(command, null);
			fLogText.append("\n");
			fLogText.append(data.toString());
			fLogText.append("\n----\n");
			fData = data;
		}
		catch (Exception e) {
			logError(e);
		}
		checkedEnabled();
	}
	
	private void evalFunctionData() {
		fLogText.append("Executing FunctionCall#evalData:\n\t");
		final String command = fEvalText.getText();
		fLogText.append(command);
		try {
			RObject data = fFunctionBuilder.evalData(null);
			fLogText.append("\n");
			fLogText.append(data.toString());
			fLogText.append("\n----\n");
			fData = data;
		}
		catch (Exception e) {
			logError(e);
		}
		checkedEnabled();
	}
	
	private void assignData() {
		fLogText.append("Executing #assignData:\n\t");
		final String command = fEvalText.getText();
		fLogText.append(command);
		try {
			fRServi.assignData(command, fData, null);
			logOK();
		}
		catch (Exception e) {
			logError(e);
		}
		checkedEnabled();
	}
	
	private void uploadFile() {
		fLogText.append("Uploading file...");
		final FileDialog dialog = new FileDialog(fLogText.getShell(), SWT.OPEN);
		final String local = dialog.open();
		if (local == null) {
			logCancelled();
			return;
		}
		
		final String remote = fRemoteFileText.getText();
		fLogText.append("\n\t");
		fLogText.append(local);
		fLogText.append(" -> ");
		fLogText.append(remote);
		fLogText.append("\n");
		
		FileInputStream in = null;
		try {
			File file = new File(local);
			in = new FileInputStream(file);
			fRServi.uploadFile(in, file.length(), remote, 0, null);
			logOK();
		}
		catch (Exception e) {
			logError(e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {}
			}
		}
		checkedEnabled();
	}
	
	private void downloadFile() {
		fLogText.append("Downloading file...");
		final FileDialog dialog = new FileDialog(fLogText.getShell(), SWT.SAVE);
		final String local = dialog.open();
		if (local == null) {
			logCancelled();
			return;
		}
		
		File file = new File(local);
		if (file.exists()) {
			MessageBox box = new MessageBox(fLogText.getShell(), SWT.YES | SWT.NO);
			box.setText("Downloading File");
			box.setMessage("Overwrite existing file '"+file.getName()+"'?");
			if (box.open() != SWT.YES) {
				logCancelled();
				return;
			}
		}
		final String remote = fRemoteFileText.getText();
		fLogText.append("\n\t");
		fLogText.append(local);
		fLogText.append(" <- ");
		fLogText.append(remote);
		fLogText.append("\n");
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			fRServi.downloadFile(out, remote, 0, null);
			logOK();
			fFile = file;
		}
		catch (Exception e) {
			fFile = null;
			logError(e);
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (IOException e) {}
			}
		}
		checkedEnabled();
	}
	
	private void openFile() {
		String local = fFile.getPath();
		fLogText.append("Opening file...\n\t");
		fLogText.append(local);
		fLogText.append("\n");
		if (Program.launch(local)) {
			logOK();
		}
		else {
			logFailed();
		}
	}
	
	private void showHist() {
		fLogText.append("Create and Show 'hist(x)'...");
		
		try {
			final PngGraphic pngGraphic = new PngGraphic();
			pngGraphic.setSize(400, 500, Graphic.UNIT_PX);
			
			final FunctionCall hist = fRServi.createFunctionCall("hist");
			hist.add("x");
			
			final byte[] plot = pngGraphic.create(hist, fRServi, null);
			final Shell shell = new Shell(fLogText.getShell(), SWT.DIALOG_TRIM | SWT.RESIZE);
			shell.setSize(500, 600);
			shell.setText("PNG Graphic");
			shell.setLayout(new GridLayout());
			
			final Image image = new Image(Display.getCurrent(), new ByteArrayInputStream(plot));
			shell.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					image.dispose();
				}
			});
			final Label label = new Label(shell, SWT.NONE);
			label.setImage(image);
			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			
			shell.open();
			logOK();
		}
		catch (Exception e) {
			logError(e);
		}
		checkedEnabled();
	}
	
}

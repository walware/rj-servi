/*******************************************************************************
 * Copyright (c) 2009 WalWare/RJ-Project (www.walware.de/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v 1.0
 * which accompanies this distribution, and is available at
 * http://eclipse.org/org/documents/edl-v10.html
 * 
 * Contributors:
 *     Tobias Verbeke - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.examples;

import java.io.ByteArrayInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.walware.rj.data.RObject;
import de.walware.rj.servi.RServi;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.utils.Graphic;
import de.walware.rj.services.utils.PngGraphic;


public class CorrelationPlotter {
	
	
	Display display = new Display();
	Shell shell = new Shell(display);
	
	Label nPointsLabel;
	Text nPointsText;
	Label correlationLabel;
	Label correlationValueLabel; 
	Scale correlationScale;
	Label pngLabel;
	Label messageLabel;
	
	RServi fRservi;
	
	RObject muObj;
	RObject nObj;
	RObject rObj;
	double rValue = 1001; // corresponds to zero correlation
	int nValue = 100; // initial number of points
	
	
	public CorrelationPlotter(){
		shell.setText("Correlation Plotter"); // set window title
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		shell.setLayout(gridLayout);
		
		nPointsLabel = new Label(shell, SWT.NULL);
		nPointsLabel.setText("Number of points: ");
		nPointsLabel.setVisible(true);
		
		nPointsText = new Text(shell, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
		GridData gridDataNPointsText = new GridData(GridData.FILL_HORIZONTAL);
		gridDataNPointsText.horizontalSpan = 1;
		nPointsText.setLayoutData(gridDataNPointsText);
		nPointsText.setSelection(nValue);
		nPointsText.insert("" + nValue);
		nPointsText.setVisible(true);
		
		correlationLabel = new Label(shell, SWT.NULL);
		correlationLabel.setText("Correlation value: ");
		GridData gridDataCorrelationLabel = new GridData(GridData.FILL_HORIZONTAL);
		gridDataCorrelationLabel.horizontalSpan = 1;
		correlationLabel.setLayoutData(gridDataCorrelationLabel);
		correlationLabel.setVisible(true);
		
		correlationValueLabel = new Label(shell, SWT.NULL);
		double labelValue = (double) ((int) ((rValue-1001) * 100.0))/100;
		correlationValueLabel.setText("" + labelValue); // TODO remove hard-coded value
		GridData gridDataCorrelationValueLabel = new GridData(GridData.FILL_HORIZONTAL);
		gridDataCorrelationValueLabel.horizontalSpan = 1;
		correlationValueLabel.setLayoutData(gridDataCorrelationValueLabel);
		correlationValueLabel.setAlignment(SWT.RIGHT);
		correlationValueLabel.setVisible(true);
		
		correlationScale = new Scale(shell, SWT.HORIZONTAL);
		GridData gridDataCorrelationScale = new GridData(GridData.FILL_HORIZONTAL);
		gridDataCorrelationScale.horizontalSpan = 2;
		correlationScale.setLayoutData(gridDataCorrelationScale);
		correlationScale.setMinimum(0);
		correlationScale.setMaximum(2001);
		correlationScale.setPageIncrement(10);
		correlationScale.setSelection((int) rValue); // default value
		correlationScale.setVisible(true);
		
		pngLabel = new Label(shell, SWT.IMAGE_PNG);
		pngLabel.setSize(300, 300);
		GridData gridDataPngLabel = new GridData(GridData.FILL_BOTH);
		gridDataPngLabel.horizontalSpan = 2;
		pngLabel.setLayoutData(gridDataPngLabel);
		pngLabel.setToolTipText("Click to sample again");
		pngLabel.setVisible(true);
		
		messageLabel = new Label(shell, SWT.BORDER);
		GridData gridDataMessageLabel = new GridData(GridData.FILL_HORIZONTAL);
		gridDataMessageLabel.horizontalSpan = 2;
		messageLabel.setLayoutData(gridDataMessageLabel);
		messageLabel.setVisible(true);
		
		ModifyListener nPointsListener = new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				nPointsChanged();
			}
		};
		
		SelectionListener correlationListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				correlationValueChanged();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing is done
				return;
			}
		};
		
		MouseListener clickListener = new MouseListener() {
			public void mouseUp(MouseEvent e) {
				return;
			}
			public void mouseDown(MouseEvent e) {
				try {
					makePlot();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				return;
			}
		};
		
		nPointsText.addModifyListener(nPointsListener);
		correlationScale.addSelectionListener(correlationListener);
		pngLabel.addMouseListener(clickListener);
		
		String URL = "rmi://127.0.0.1/rservi-pool";
		try {
			fRservi = RServiUtil.getRServi(URL, "CorrelationPlotter");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			FunctionCall massCall = fRservi.createFunctionCall("library");
			massCall.add("package", "MASS");
			massCall.evalVoid(null);
			fRservi.evalVoid("mu <- c(0,0)", null);   // does not change
			fRservi.evalVoid("r <- " + (rValue-1001)/1001, null); // set default value, here: 0
			fRservi.evalVoid("n <- " + nValue, null); // set default value
			makePlot(); // initial plot
		} catch (Exception e){
			e.printStackTrace();
		}
		
		shell.pack();
		shell.open();
		
		while (!shell.isDisposed()){
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		try {
			fRservi.close();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		display.dispose();
	}
	
	public void nPointsChanged(){
		if (!nPointsText.isFocusControl()) {
			return;
		}
		
		double nPointsValue;
		
		try {
			nPointsValue = Double.parseDouble(nPointsText.getText());
			messageLabel.setText("Valid number of points: " + nPointsText.getText());
			messageLabel.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			try {
				if (nPointsValue > 1){
					fRservi.evalVoid("n <- " + nPointsValue, null);
					makePlot();
				} else {
					messageLabel.setText("Invalid number of points: " + nPointsText.getText());
				    messageLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (NumberFormatException nfe) {
			messageLabel.setText("Invalid number of points: " + nPointsText.getText());
			messageLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
		}
		
	}
	
	public void correlationValueChanged(){
		double rValue = (double) correlationScale.getSelection() - 1001; // TODO replace with maximum of scale
		if (rValue >= 0) {
			rValue = rValue/1000;
		} else {
			rValue = rValue/1001;
		}
		rValue = (double) ((int) (rValue * 100.0))/100;
		correlationValueLabel.setText("" + rValue);
		// System.out.println("Correlation: " + rValue);
		try {
			fRservi.evalVoid("r <- " + rValue, null);
			makePlot();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void makePlot(){
		try {
			FunctionCall mvrnormCall = fRservi.createFunctionCall("mvrnorm");
			mvrnormCall.add("n", "n");
			mvrnormCall.add("mu", "mu");
			
			fRservi.evalVoid("sigma <- matrix(rep(r, 4), ncol = 2)", null);
			fRservi.evalVoid("diag(sigma) <- c(1,1)", null);
			fRservi.evalVoid("xy <- mvrnorm(n = n, mu = mu, Sigma = sigma)", null);
			
			// plot(x = xy[,1], y = xy[,2], ylab = "", xlab = "")
			
			final PngGraphic pngGraphic = new PngGraphic();
			pngGraphic.setSize(300, 300, Graphic.UNIT_PX);
			
			final FunctionCall plotFun = fRservi.createFunctionCall("eqscplot");
			plotFun.add("x", "xy[,1]");
			plotFun.add("y", "xy[,2]");
			plotFun.addChar("xlab", "");
			plotFun.addChar("ylab", "");
			
			final byte[] plot = pngGraphic.create(plotFun, fRservi, null);
			final Image image = new Image(display, new ByteArrayInputStream(plot));
			pngLabel.setImage(image);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		new CorrelationPlotter();
	}
	
}

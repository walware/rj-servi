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

package de.walware.rj.servi.internal.rcpdemo;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class Perspective implements IPerspectiveFactory {
	
	
	public Perspective() {
	}
	
	
	public void createInitialLayout(final IPageLayout layout) {
		layout.setEditorAreaVisible(false);
	}
	
}

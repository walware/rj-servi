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

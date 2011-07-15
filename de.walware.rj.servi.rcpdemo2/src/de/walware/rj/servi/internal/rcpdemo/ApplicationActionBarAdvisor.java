package de.walware.rj.servi.internal.rcpdemo;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;


public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	
	
	public ApplicationActionBarAdvisor(final IActionBarConfigurer configurer) {
		super(configurer);
	}
	
	
	@Override
	protected void makeActions(final IWorkbenchWindow window) {
	}
	
}

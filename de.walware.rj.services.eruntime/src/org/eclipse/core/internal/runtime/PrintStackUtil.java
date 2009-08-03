package org.eclipse.core.internal.runtime;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.eclipse.core.runtime.IStatus;


public class PrintStackUtil {
	
	static public void printChildren(IStatus status, PrintStream output) {
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0)
			return;
		for (int i = 0; i < children.length; i++) {
			output.println("Contains: " + children[i].getMessage()); //$NON-NLS-1$
			Throwable exception = children[i].getException();
			if (exception != null)
				exception.printStackTrace();
			printChildren(children[i], output);
		}
	}
	
	static public void printChildren(IStatus status, PrintWriter output) {
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0)
			return;
		for (int i = 0; i < children.length; i++) {
			output.println("Contains: " + children[i].getMessage()); //$NON-NLS-1$
			output.flush(); // call to synchronize output
			Throwable exception = children[i].getException();
			if (exception != null)
				exception.printStackTrace();
			printChildren(children[i], output);
		}
	}
	
}

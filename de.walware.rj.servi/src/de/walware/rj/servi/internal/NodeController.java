/*******************************************************************************
 * Copyright (c) 2009 WalWare/RJ-Project (www.walware.de/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.servi.internal;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import de.walware.rj.server.srvImpl.AbstractServerControl;


public class NodeController extends AbstractServerControl {
	
	
	public static void main(final String args[]) {
		if (System.getProperty("de.walware.rj.debug") != null) {
			System.setProperty("de.walware.rj.verbose", "true");
		}
		else {
			try {
				final File file = new File("out.log");
				file.createNewFile();
				final PrintStream stream = new PrintStream(file);
				System.setOut(stream);
				System.setErr(stream);
			}
			catch (final Throwable _ex) {
				System.exit(EXIT_INIT_PROBLEM | 1);
			}
		}
		
		if (args == null || args.length < 1) {
			System.exit(EXIT_INVALID_ARGS | 1);
		}
		new NodeController(args[0], cliGetArgs(args, 1)).start();
	}
	
	
	public NodeController(final String name, final Map<String, String> args) {
		super(name, args);
	}
	
	
	public void start() {
		LOGGER.log(Level.INFO, "{0} initialize server...", this.logPrefix);
		final NodeServer server = new NodeServer(this.name, this);
		if (!initREngine(server)) {
			System.exit(EXIT_INIT_RENGINE_ERROR | 1);
		}
		try {
			server.start1();
		}
		catch (final Exception e) {
			final LogRecord record = new LogRecord(Level.SEVERE, "{0} init JRI/Rengine and Node failed.");
			record.setParameters(new Object[] { this.logPrefix });
			record.setThrown(e);
			LOGGER.log(record);
			System.exit(EXIT_INIT_RENGINE_ERROR | 2);
		}
		publishServer(server);
	}
	
}

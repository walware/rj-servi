/*******************************************************************************
 * Copyright (c) 2009-2013 WalWare/RJ-Project (www.walware.de/goto/opensource).
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
				final PrintStream stream = new PrintStream(file);
				stream.println("(RServi) R node log");
				stream.flush();
				System.setOut(stream);
				System.setErr(stream);
			}
			catch (final Throwable e) {
				e.printStackTrace();
				exit(EXIT_INIT_LOGGING_ERROR);
			}
		}
		
		if (args == null || args.length < 1) {
			System.exit(EXIT_ARGS_MISSING);
		}
		new NodeController(args[0], cliGetArgs(args, 1)).start();
	}
	
	
	public NodeController(final String name, final Map<String, String> args) {
		super(name, args);
	}
	
	
	public void start() {
		LOGGER.log(Level.INFO, "{0} Initializing R node...", this.logPrefix);
		final NodeServer server = new NodeServer(this.name, this);
		if (!initREngine(server)) {
			exit(EXIT_INIT_RENGINE_ERROR);
		}
		try {
			server.start1();
			LOGGER.log(Level.FINE, "{0} Initializing R node: R engine started and initialized.", this.logPrefix);
		}
		catch (final Exception e) {
			final LogRecord record = new LogRecord(Level.SEVERE, "{0} Initializing R node failed.");
			record.setParameters(new Object[] { this.logPrefix });
			record.setThrown(e);
			LOGGER.log(record);
			exit(EXIT_START_RENGINE_ERROR);
		}
		LOGGER.log(Level.FINE, "{0} Initializing R node: Publishing in registry...", this.logPrefix);
		publishServer(server);
	}
	
}

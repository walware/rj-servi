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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class Utils {
	
	
	public static final String PLUGIN_ID = "de.walware.rj.serv";
	
	public static final Logger LOGGER = Logger.getLogger("de.walware.rj.servi");
	
	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("win");
	
	
	/**
	 * Utility class to parse command line arguments.
	 */
	private static class ArgumentParser {
		
		private final String args;
		private int index = 0;
		private int ch = -1;
		
		public ArgumentParser(final String args) {
			this.args= args;
		}
		
		public List<String> parseArguments() {
			final List<String> v = new ArrayList<String>();
			
			this.ch = getNext();
			while (this.ch > 0) {
				if (Character.isWhitespace((char) this.ch)) {
					this.ch = getNext();	
				}
				else {
					if (this.ch == '"') {
						final StringBuffer buf = new StringBuffer();
						buf.append(parseString());
						if (buf.length() == 0 && IS_WINDOWS) {
							// empty string on windows platform
							buf.append("\"\""); //$NON-NLS-1$
						}
						v.add(buf.toString());
					}
					else {
						v.add(parseToken());
					}
				}
			}
			
			return v;
		}
		
		private int getNext() {
			if (this.index < this.args.length()) {
				return this.args.charAt(this.index++);
			}
			return -1;
		}
		
		private String parseString() {
			this.ch = getNext();
			if (this.ch == '"') {
				this.ch = getNext();
				return ""; //$NON-NLS-1$
			}
			final StringBuffer buf = new StringBuffer();
			while (this.ch > 0 && this.ch != '"') {
				if (this.ch == '\\') {
					this.ch = getNext();
					if (this.ch != '"') {           // Only escape double quotes
						buf.append('\\');
					}
					else {
						if (IS_WINDOWS) {
							// @see Bug 26870. Windows requires an extra escape for embedded strings
							buf.append('\\');
						}
					}
				}
				if (this.ch > 0) {
					buf.append((char) this.ch);
					this.ch = getNext();
				}
			}
			this.ch = getNext();
			return buf.toString();
		}
		
		private String parseToken() {
			final StringBuffer buf = new StringBuffer();
			
			while (this.ch > 0 && !Character.isWhitespace((char) this.ch)) {
				if (this.ch == '\\') {
					this.ch = getNext();
					if (Character.isWhitespace((char) this.ch)) {
						// end of token, don't lose trailing backslash
						buf.append('\\');
						return buf.toString();
					}
					if (this.ch > 0) {
						if (this.ch != '"') {           // Only escape double quotes
							buf.append('\\');
						}
						else {
							if (IS_WINDOWS) {
								// @see Bug 26870. Windows requires an extra escape for embedded strings
								buf.append('\\');
							}
						}
						buf.append((char) this.ch);
						this.ch = getNext();
					}
					else if (this.ch == -1) {     // Don't lose a trailing backslash
						buf.append('\\');
					}
				}
				else if (this.ch == '"') {
					buf.append(parseString());
				}
				else {
					buf.append((char) this.ch);
					this.ch = getNext();
				}
			}
			return buf.toString();
		}
	}
	
	public static List<String> parseArguments(final String args) {
		if (args == null) {
			return new ArrayList<String>(0);
		}
		else {
			final ArgumentParser parser = new ArgumentParser(args);
			return parser.parseArguments();
		}
	}
	
	
	private Utils() {
	}
	
}

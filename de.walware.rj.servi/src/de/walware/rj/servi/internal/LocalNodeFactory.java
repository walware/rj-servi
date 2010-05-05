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

package de.walware.rj.servi.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.net.RMIRegistry;

import de.walware.rj.RjException;
import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.Server;
import de.walware.rj.server.ServerLogin;
import de.walware.rj.server.srvext.ServerUtil;
import de.walware.rj.servi.RServiUtil;
import de.walware.rj.servi.pool.RServiNode;
import de.walware.rj.servi.pool.RServiNodeConfig;


public abstract class LocalNodeFactory implements NodeFactory {
	
	
	public static final String[] CODEBASE_LIBS = new String[] {
			ServerUtil.RJ_SERVER_ID, RServiUtil.RJ_SERVI_ID };
	
	private static class ProcessConfig {
		final Map<String, String> addEnv = new HashMap<String, String>();
		final List<String> command = new ArrayList<String>();
		int nameCommandIdx = -1;
		String baseWd;
		String authConfig;
	}
	
	
	private final String poolId;
	private RServiNodeConfig baseConfig;
	private String[] libIds;
	
	private final String securityPolicyPath;
	private ProcessConfig processConfig;
	
	private String errorMessage;
	
	private final RMIRegistry nodeRegistry;
	
	private boolean verbose;
	
	
	protected LocalNodeFactory(final String poolId, final RMIRegistry registry, final String[] libIds) throws RjInvalidConfigurationException {
		this.nodeRegistry = registry;
		this.poolId = poolId;
		this.libIds = libIds;
		this.baseConfig = new RServiNodeConfig();
		
		this.securityPolicyPath = getPolicyFile();
	}
	
	
	protected abstract String[] getRJLibs(String[] libIds) throws RjInvalidConfigurationException;
	
	protected abstract String getPolicyFile() throws RjInvalidConfigurationException;
	
	
	public void setConfig(final RServiNodeConfig config) throws RjInvalidConfigurationException {
		final ProcessConfig p = new ProcessConfig();
		final StringBuilder sb = new StringBuilder();
		
		String javaHome = config.getJavaHome();
		if (javaHome == null || javaHome.length() == 0) {
			javaHome = System.getProperties().getProperty("java.home");
		}
		p.addEnv.put("JAVA_HOME", javaHome);
		
		p.command.add(javaHome + File.separatorChar + "bin" + File.separatorChar + "java");
		
		{	p.command.add("-classpath");
			final String[] libs;
			try {
				libs = getRJLibs(this.libIds);
			}
			catch (final RjInvalidConfigurationException e) {
				this.errorMessage = e.getMessage();
				throw e;
			}
			p.command.add(ServerUtil.concatPathVar(libs));
		}
		
		String javaArgs = config.getJavaArgs();
		if (javaArgs != null && (javaArgs = javaArgs.trim()).length() > 0) {
			p.command.addAll(Utils.parseArguments(javaArgs));
		}
		else {
			javaArgs = "";
		}
		
		final String hostname = System.getProperty("java.rmi.server.hostname");
		if (hostname != null && hostname.length() > 0) {
			p.command.add("-Djava.rmi.server.hostname=" +  hostname);
		}
		if (!javaArgs.contains("-Djava.security.policy=")) {
			sb.setLength(0);
			sb.append("-Djava.security.policy=");
			sb.append(this.securityPolicyPath);
			p.command.add(sb.toString());
		}
		if (!javaArgs.contains("-Djava.rmi.server.codebase=")) {
			final String[] libs;
			try {
				libs = getRJLibs(CODEBASE_LIBS);
			}
			catch (final RjInvalidConfigurationException e) {
				this.errorMessage = e.getMessage();
				throw e;
			}
			sb.setLength(0);
			sb.append("-Djava.rmi.server.codebase=");
			sb.append(ServerUtil.concatCodebase(libs));
			p.command.add(sb.toString());
		}
		if (!javaArgs.contains("-Xss")) {
			sb.setLength(0);
			sb.append("-Xss");
			sb.append(config.getBits()*256);
			sb.append("k"); 
			p.command.add(sb.toString());
		}
		
		p.command.add("de.walware.rj.servi.internal.NodeController");
		
		p.nameCommandIdx = p.command.size();
		p.command.add("");
		
		String nodeArgs = config.getNodeArgs();
		if (nodeArgs != null && (nodeArgs = nodeArgs.trim()).length() > 0) {
			p.command.addAll(Utils.parseArguments(nodeArgs));
		}
		
		final String rHome = config.getRHome();
		if (rHome == null || rHome.length() == 0) {
			this.errorMessage = "Missing value for R_HOME.";
			throw new RjInvalidConfigurationException(this.errorMessage);
		}
		p.addEnv.put("R_HOME", rHome);
		{	final String rBinDir = rHome + File.separatorChar + "bin";
			final String pathEnv = System.getenv("PATH");
			p.addEnv.put("PATH", (pathEnv != null) ? (rBinDir + File.pathSeparatorChar + pathEnv) : rBinDir);
		}
		if (!Utils.IS_WINDOWS) {
			final String rLibDir = rHome + File.separatorChar + "lib";
			final String libPathEnv = System.getenv("LD_LIBRARY_PATH");
			p.addEnv.put("LD_LIBRARY_PATH", (libPathEnv != null) ? (rLibDir + File.pathSeparatorChar + libPathEnv) : rLibDir);
		}
		
		p.baseWd = config.getBaseWorkingDirectory();
		if (p.baseWd == null || p.baseWd.length() == 0) {
			p.baseWd = System.getProperty("java.io.tmpdir");
		}
		if (!testBaseDir(p.baseWd)) {
			this.errorMessage = "Invalid working directory base path.";
			throw new RjInvalidConfigurationException(this.errorMessage);
		}
		
		p.authConfig = config.getEnableConsole() ? "none" : null;
		
		this.verbose = config.getEnableVerbose();
		this.baseConfig = config;
		this.processConfig = p;
	}
	
	private boolean testBaseDir(final String path) {
		final File file = new File(path + File.separatorChar + this.poolId + "-test");
		if (file.isDirectory()) {
			return true;
		}
		if (file.mkdirs()) {
			file.delete();
			return true;
		}
		return false;
	}
	
	public RServiNodeConfig getConfig() {
		return this.baseConfig;
	}
	
	
	public void createNode(final NodeHandler poolObj) throws RjException {
		final ProcessConfig p = this.processConfig;
		if (p == null) {
			final String message = this.errorMessage;
			throw new RjInvalidConfigurationException((message != null) ? message :
					"Missing configuration.");
		}
		ProcessBuilder pBuilder;
		String id;
		List<String> command = null;
		try {
			synchronized (this) {
				while (true) {
					id = this.poolId + '-' + System.currentTimeMillis();
					poolObj.dir = new File(p.baseWd + File.separatorChar + id);
					if (!poolObj.dir.exists()) {
						poolObj.dir.mkdirs();
						break;
					}
				}
			}
			command = new ArrayList<String>(p.command.size() + 2);
			command.addAll(p.command);
			poolObj.address = new RMIAddress(this.nodeRegistry.getAddress(), id);
			command.set(p.nameCommandIdx, poolObj.address.getAddress());
			if (this.verbose) {
				command.add("-verbose");
			}
			pBuilder = new ProcessBuilder(command);
			pBuilder.environment().remove("Path");
			pBuilder.environment().putAll(p.addEnv);
			pBuilder.directory(poolObj.dir);
			pBuilder.redirectErrorStream(true);
		}
		catch (final Exception e) {
			throw new RjException("Error preparing R node.", e);
		}
		Process process = null;
		try {
			process = pBuilder.start();
			
			for (int i = 1; ; i++) {
				try {
					final Server server = (Server) this.nodeRegistry.getRegistry().lookup(id);
					final ServerLogin login = server.createLogin(Server.C_RSERVI_NODECONTROL);
					final RServiNode node = (RServiNode) server.execute(Server.C_RSERVI_NODECONTROL, null, login);
					
					poolObj.isConsoleEnabled = node.setConsole(p.authConfig);
					poolObj.node = node;
					
					return;
				}
				catch (final NotBoundException e) {
//					System.out.println(Arrays.toString(this.nodeRegistry.registry.list()));
					if (i == 50) {
						throw e;
					}
				};
				
				try {
					final int exitValue = process.exitValue();
					throw new RjException("R node process stopped (exit code = "+exitValue+")");
				}
				catch (final IllegalThreadStateException ok) {}
				
				Thread.sleep(100);
			}
		}
		catch (final Exception e) {
			final StringBuilder sb = new StringBuilder("Error starting R node:");
			if (pBuilder != null) {
				sb.append("\n<COMMAND>");
				ServerUtil.prettyPrint(pBuilder.command(), sb);
				sb.append("\n</COMMAND>");
			}
			if (process != null) {
				final char[] buffer = new char[4096];
				final InputStream stdout = process.getInputStream();
				{
					sb.append("\n<STDOUT>\n");
					final InputStreamReader reader = new InputStreamReader(stdout);
					try { // read non-blocking
						int n;
						while (reader.ready() && (n = reader.read(buffer, 0, buffer.length)) >= 0) {
							sb.append(buffer, 0, n);
						}
					}
					catch (final IOException ignore) {
					}
					process.destroy();
					try {
						int n;
						while ((n = reader.read(buffer, 0, buffer.length)) >= 0) {
							sb.append(buffer, 0, n);
						}
					}
					catch (final IOException ignore) {
					}
					finally {
						if (reader != null) {
							try {
								reader.close();
							}
							catch (final IOException ignore) {}
						}
					}
					sb.append("</STDOUT>");
				}
				final File logfile = new File(poolObj.dir, "out.log");
				if (logfile.exists()) {
					sb.append("\n<LOG file=\"out.log\">\n");
					FileReader reader = null;
					try {
						reader = new FileReader(logfile);
						int n;
						while ((n = reader.read(buffer, 0, buffer.length)) >= 0) {
							sb.append(buffer, 0, n);
							if (sb.length() > 100000) {
								break;
							}
						}
					}
					catch (final IOException ignore) {
					}
					finally {
						if (reader != null) {
							try {
								reader.close();
							}
							catch (final IOException ignore) {}
						}
					}
					sb.append("</LOG>");
				}
				sb.append("\n--------");
			}
			
			Thread.interrupted();
			if (poolObj.dir.exists() && poolObj.dir.isDirectory()) {
				ServerUtil.delDir(poolObj.dir);
			}
			throw new RjException(sb.toString(), e);
		}
	}
	
	public void cleanupNode(final NodeHandler poolObj) {
		if (!this.verbose && poolObj.dir != null
				&& poolObj.dir.exists() && poolObj.dir.isDirectory()) {
			try {
				Thread.sleep(500);
			}
			catch (final InterruptedException e) {
				Thread.interrupted();
			}
			ServerUtil.delDir(poolObj.dir);
		}
	}
	
}

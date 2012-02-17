/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/RJ-Project (www.walware.de/goto/opensource).
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
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;

import de.walware.rj.RjException;
import de.walware.rj.server.DataCmdItem;
import de.walware.rj.server.MainCmdC2SList;
import de.walware.rj.server.MainCmdItem;
import de.walware.rj.server.MainCmdS2CList;
import de.walware.rj.server.RjsComConfig;
import de.walware.rj.server.RjsComObject;
import de.walware.rj.server.RjsStatus;
import de.walware.rj.server.Server;
import de.walware.rj.server.ServerLogin;
import de.walware.rj.server.srvImpl.AbstractServerControl;
import de.walware.rj.server.srvImpl.DefaultServerImpl;
import de.walware.rj.server.srvext.Client;
import de.walware.rj.server.srvext.ServerAuthMethod;
import de.walware.rj.server.srvext.ServerUtil;
import de.walware.rj.server.srvstdext.NoAuthMethod;
import de.walware.rj.servi.pool.RServiNode;


public class NodeServer extends DefaultServerImpl {
	
	
	class ConsoleDummy extends Thread {
		
		private final Client client;
		private final PrintStream out;
		
		private final MainCmdC2SList c2sList = new MainCmdC2SList();
		
		ConsoleDummy(final Client client) {
			setName("R Console");
			setDaemon(true);
			setPriority(NORM_PRIORITY-1);
			this.client = client;
			this.out = System.out;
		}
		
		@Override
		public void run() {
			try {
				synchronized (NodeServer.this.internalEngine) {
					if (NodeServer.this.isConsoleEnabled || NodeServer.this.isConsoleDummyRunning) {
						return;
					}
					NodeServer.this.internalEngine.connect(this.client, new HashMap<String, Object>());
					NodeServer.this.isConsoleDummyRunning = true;
				}
				
				RjsComObject sendCom = null;
				boolean error = false;
				while (true) {
					try {
						if (sendCom == null) {
							this.c2sList.setObjects(null);
							sendCom = this.c2sList;
						}
						final RjsComObject receivedCom = NodeServer.this.internalEngine.runMainLoop(this.client, sendCom);
						sendCom = null;
						error = false;
						if (receivedCom != null) {
							switch (receivedCom.getComType()) {
							case RjsComObject.T_PING:
								sendCom = RjsStatus.OK_STATUS;
								break;
							case RjsComObject.T_MAIN_LIST:
								MainCmdItem item = ((MainCmdS2CList) receivedCom).getItems();
								MainCmdItem tmp;
								ITER_ITEMS : for (; (item != null); tmp = item, item = item.next, tmp.next = null) {
									switch (item.getCmdType()) {
									case MainCmdItem.T_CONSOLE_WRITE_ERR_ITEM:
										this.out.println("R-ERR: " + item.getDataText());
										break;
									case MainCmdItem.T_CONSOLE_WRITE_OUT_ITEM:
										this.out.println("R-OUT: " + item.getDataText());
										break;
									case MainCmdItem.T_CONSOLE_READ_ITEM:
										this.out.println("R-PROMPT: " + item.getDataText());
										break;
									}
								}
								break;
							case RjsComObject.T_STATUS:
								switch (((RjsStatus) receivedCom).getCode()) {
								case Server.S_DISCONNECTED:
									throw new ConnectException("");
								case Server.S_LOST:
								case Server.S_NOT_STARTED:
								case Server.S_STOPPED:
									return;
								}
							}
						}
					}
					catch (final ConnectException e) {
						synchronized (NodeServer.this.internalEngine) {
							if (NodeServer.this.isConsoleEnabled) {
								NodeServer.this.isConsoleDummyRunning = false;
								return;
							}
							NodeServer.this.internalEngine.connect(this.client, new HashMap<String, Object>());
						}
					}
					catch (final Exception e) {
						if (error) {
							throw e;
						}
						LOGGER.log(Level.SEVERE, "An error occurred when running dummy R REPL. Trying to continue REPL.", e);
						error = true;
					}
					if (sendCom == null) {
						try {
							sleep(5000);
						}
						catch (final InterruptedException e) {
						}
					}
				}
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "An error occurred when running dummy R REPL. Stopping REPL.", e);
			}
		}
		
	}
	
	class Node implements RServiNode {
		
		public boolean setConsole(final String authConfig) throws RemoteException, RjException {
			final boolean enabled;
			synchronized (NodeServer.this.internalEngine) {
//				LOGGER.fine("enter lock");
				final Client currentClient = NodeServer.this.internalEngine.getCurrentClient();
				if (currentClient != null) {
					NodeServer.this.internalEngine.disconnect(currentClient);
				}
//				LOGGER.fine("disconnect");
				if (authConfig != null) {
					NodeServer.this.consoleAuthMethod = NodeServer.this.control.createServerAuth(authConfig);
					enabled = NodeServer.this.isConsoleEnabled = true;
				}
				else {
					NodeServer.this.consoleAuthMethod = new NoAuthMethod("<internal>");
					enabled = NodeServer.this.isConsoleEnabled = false;
//					LOGGER.fine("before start");
					if (!NodeServer.this.isConsoleDummyRunning) {
						new ConsoleDummy(NodeServer.this.consoleDummyClient).start();
					}
//					LOGGER.fine("after start");
				}
			}
			return enabled;
		}
		
		public int getEvalTime() throws RemoteException {
			return 0;
		}
		
		public void ping() throws RemoteException {
		}
		
		public String getPoolHost() throws RemoteException {
			try {
				return RemoteServer.getClientHost();
			}
			catch (final ServerNotActiveException e) {
				return "<internal>";
			}
		}
		
		public void runSnippet(final String code) throws RemoteException, RjException {
			NodeServer.this.runServerLoopCommand(null, new DataCmdItem(DataCmdItem.EVAL_VOID, 0,
					code, null, null ));
		}
		
		public RServiBackend bindClient(final String client) throws RemoteException {
			return NodeServer.this.bindClient(client);
		}
		
		public void unbindClient() throws RemoteException {
			NodeServer.this.unbindClient();
		}
		
		public void shutdown() throws RemoteException {
			NodeServer.this.shutdown();
		}
		
	}
	
	class Backend implements RServiBackend {
		
		public Server getPublic() throws RemoteException {
			return null;
		}
		
		public Map<String, Object> getPlatformData() {
			return NodeServer.this.internalEngine.getPlatformData();
		}
		
		public void setProperties(final Map<String, ? extends Object> properties) throws RemoteException {
			NodeServer.this.setProperties(properties, this);
		}
		
		public boolean interrupt() throws RemoteException {
			throw new UnsupportedOperationException();
		}
		
		public void disconnect() throws RemoteException {
			throw new UnsupportedOperationException();
		}
		
		public RjsComObject runMainLoop(final RjsComObject com) throws RemoteException {
			return NodeServer.this.runMainLoop(com, this);
		}
		
		public RjsComObject runAsync(final RjsComObject com) throws RemoteException {
			return NodeServer.this.runAsync(com, this);
		}
		
		public boolean isClosed() throws RemoteException {
			return (NodeServer.this.currentClientBackend != this);
		}
		
	}
	
	
	private boolean isConsoleEnabled;
	private boolean isConsoleDummyRunning;
	
	private final ServerAuthMethod rserviAuthMethod;
	
	private final Client consoleDummyClient;
	
	private String currentClientId;
	private Backend currentClientBackend;
	private RServiBackend currentClientExp;
	
	private final Object serviRunLock = new Object();
	
	private String resetCommand;
	
	
	public NodeServer(final String name, final AbstractServerControl control) {
		super(name, control, new NoAuthMethod("<internal>"));
		this.rserviAuthMethod = new NoAuthMethod("<internal>");
		this.consoleDummyClient = new Client("-", "dummy", (byte) 0);
	}
	
	
	void start1() throws Exception {
		this.resetCommand = "{" +
				"rm(list=ls());" +
				"gc();" +
				".rj.getTmp<-function(o){x<-get(o,pos=.GlobalEnv);rm(list=o,pos=.GlobalEnv);x};" +
				".rj.wd<-\""+this.workingDirectory.replace("\\", "\\\\")+"\";" +
				"setwd(.rj.wd);" +
				"graphics.off();" +
		"}";
		RjsComConfig.setServerPathResolver(this);
		
		final Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("args", new String[0]);
		this.internalEngine.start(this.consoleDummyClient, properties);
		
		try {
			synchronized (this.serviRunLock) {
				LOGGER.log(Level.FINE, "Initializing R node: Loading R package 'rj'...");
				runServerLoopCommand(null, new DataCmdItem(DataCmdItem.EVAL_VOID, 0,
						"library(rj)", null, null ));
				LOGGER.log(Level.FINE, "Initializing R node: Preparing R workspace for first client...");
				runServerLoopCommand(null, new DataCmdItem(DataCmdItem.EVAL_VOID, 0,
						this.resetCommand, null, null ));
			}
		}
		catch (final Exception e) {
			throw new RjException("An error occurred while preparing initially the workspace.", e);
		}
	}
	
	@Override
	protected ServerAuthMethod getAuthMethod(final String command) {
		if (command.startsWith("rservi")) {
			return this.rserviAuthMethod;
		}
		return super.getAuthMethod(command);
	}
	
	@Override
	public Object execute(final String command, final Map<String, ? extends Object> properties, final ServerLogin login) throws RemoteException, LoginException {
		if (command.equals(C_CONSOLE_START)) {
			throw new UnsupportedOperationException();
		}
		if (command.equals(C_CONSOLE_CONNECT)) {
			synchronized (this.internalEngine) {
				if (!this.isConsoleEnabled) {
					throw new RemoteException("Console is not enabled.");
				}
				final Client client = connectClient(command, login);
				return this.internalEngine.connect(client, properties);
			}
		}
		if (command.equals(C_RSERVI_NODECONTROL)) {
			final Client client = connectClient(command, login);
			final Node node = new Node();
			final RServiNode exported = (RServiNode) UnicastRemoteObject.exportObject(node, 0);
			return exported;
		}
		throw new UnsupportedOperationException();
	}
	
	
	RServiBackend bindClient(final String client) throws RemoteException {
		synchronized (this.serverClient) {
			if (NodeServer.this.currentClientBackend != null) {
				throw new IllegalStateException();
			}
			final Backend backend = new Backend();
			final RServiBackend export = (RServiBackend) UnicastRemoteObject.exportObject(backend, 0);
			this.currentClientId = client;
			this.currentClientBackend = backend;
			this.currentClientExp = export;
			DefaultServerImpl.addClient(export);
			return export;
		}
	}
	
	void unbindClient() throws RemoteException {
		synchronized (this.serverClient) {
			final Backend previous = this.currentClientBackend;
			if (previous != null) {
				DefaultServerImpl.removeClient(this.currentClientExp);
				this.currentClientId = null;
				this.currentClientBackend = null;
				this.currentClientExp = null;
				UnicastRemoteObject.unexportObject(previous, true);
				try {
					synchronized (this.serviRunLock) {
						runServerLoopCommand(null, new DataCmdItem(DataCmdItem.EVAL_VOID, 0,
								this.resetCommand, null, null ));
						ServerUtil.cleanDir(new File(this.workingDirectory), "out.log");
					}
				}
				catch (final Exception e) {
					throw new RemoteException("An error occurred while resetting the workspace.", e);
				}
			}
		}
	}
	
	void shutdown() {
		this.control.checkCleanup();
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					unbindClient();
				}
				catch (final Exception e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
		}, 500L);
	}
	
	public void setProperties(final Map<String, ? extends Object> properties, final Object caller) throws RemoteException {
		synchronized (this.serviRunLock) {
			if (caller != null && this.currentClientBackend != caller) {
				throw new IllegalAccessError();
			}
			this.internalEngine.setProperties(this.serverClient, properties);
		}
	}
	
	@Override
	protected RjsComObject runMainLoop(final RjsComObject com, final Object caller) throws RemoteException {
		synchronized (this.serviRunLock) {
			if (caller != null && this.currentClientBackend != caller) {
				throw new IllegalAccessError();
			}
		}
		return super.runMainLoop(com, caller);
	}
	
	private  RjsComObject runAsync(final RjsComObject com, final Backend backend) throws RemoteException {
		if (backend != null && this.currentClientBackend != backend) {
			throw new IllegalAccessError();
		}
		return this.internalEngine.runAsync(this.serverClient, com);
	}
	
}

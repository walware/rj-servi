/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.net.internal;


public class Messages {
	
	
	public static String RMI_status_RegistryAlreadyStarted_message = "RMI Registry at port {0} is already started.";
	public static String RMI_status_RegistryStartFailed_message = "RMI Registry at port {0} could not be started.";
	public static String RMI_status_RegistryStartFailedPortAlreadyUsed_message = "RMI Registry at port {0} could not be started, because the port is already used.";
	public static String RMI_status_RegistryStartFailedWithExitValue_message = "RMI Registry at port {0} could not be started. The new process termintes with exit value {1}.";
	public static String RMI_status_RegistryStopFailedNotFound_message = "RMI Registry at port {0} could not be found. Please note: Only registries started within an Eclipse session can be stopped with this feature.";
	
	
	private Messages() {}
	
}

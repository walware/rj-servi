package de.walware.rj.servi.internal;


public class Messages {
	
	
	public static String BindClient_error_message = "An exception was thrown when trying to bind the client (activate a node).";
	public static String GetRServi_pub_error_message = "Cannot provide RServi instance: Internal error occurred.";
	public static String GetRServi_NoInstance_pub_Embedded_message = "Cannot provide RServi instance: Single instance is already in use.";
	public static String GetRServi_NoInstance_pub_Pool_message = "Cannot provide RServi instance: No free node available.";
	public static String UnbindClient_error_message = "An exception was thrown when trying to unbind the client (passivate a node).";
	public static String StartNode_error_message = "An exception was thrown when trying to start the node (make a node).";
	public static String StartEmbedded_pub_error_message = "Cannot start the RServi instance.";
	public static String ShutdownNode_error_message = "An exception was thrown when trying to shutdown the node (destroy a node).";
	public static String RmiUnexportNode_error_message = "An exception was thrown when trying to unexport the node (destroy a node).";
	
}

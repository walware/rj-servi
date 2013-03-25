<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%--
 ###############################################################################
 # Copyright (c) 2009-2013 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 ###############################################################################
--%>
<f:view>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta http-equiv="Content-Style-Type" content="text/css" />
<link rel="stylesheet" type="text/css" href="css/style.css" />

<title>(RJ) Configuration: General</title>
</head>
<body>
<%@include file="body-header.jspf" %>

<h2>Configuration: General</h2>

<h:form id="net_config">
	<h:messages errorClass="error" />
	
<h:panelGrid columns="3" styleClass="grid" columnClasses="label,input,info">
	<f:facet name="header"><h:outputText value="RMI Parameters" /></f:facet>
	
	<h:outputLabel for="host_address" value="(1) Host address (IP/name):" accesskey="1" />
	<h:inputText id="host_address" label="Host address (1)" value="#{netConfig.hostAddress}" required="false" size="40" />
	<h:outputText>This setting is synchronized with the global Java property 'java.rmi.server.hostname'</h:outputText>
	
	<h:outputLabel for="eff_host_address" value="--> Resolved IP:" />
	<h:inputText id="eff_host_address" label="Host address (1)" value="#{netConfig.effectiveHostaddress}" required="false" readonly="true" size="40" />
	<h:outputText />
	
	<h:outputLabel for="rmi_registry_address_port" value="(2) RMI registry port:" accesskey="2" />
	<h:inputText id="rmi_registry_address_port" label="RMI registry port (2)" value="#{netConfig.registryPort}" required="false" size="6" />
	<h:outputText>(-1 &#x21d2; default)</h:outputText>
	
	<h:outputLabel for="rmi_registry_embed_enabled" value="(3) Start embedded registry:" accesskey="3" />
	<h:selectBooleanCheckbox id="rmi_registry_embed_enabled" label="Start embedded registry (3)" value="#{netConfig.registryEmbed}" required="true" />
	<h:outputText></h:outputText>
	
	<h:outputLabel for="ssl_enabled" value="(4) Enable SSL:" accesskey="4" />
	<h:selectBooleanCheckbox id="ssl_enabled" label="Start embedded registry (4)" value="#{netConfig.SSLEnabled}" required="true" />
	<h:outputText></h:outputText>
	
</h:panelGrid>
	
	<h:commandButton id="loadDefaults" value="Load Defaults" action="#{netConfig.actionLoadDefaults}" type="button" immediate="true" accesskey="L" />
	<h:commandButton id="loadCurrent" value="Load Current" action="#{netConfig.actionLoadCurrent}" type="button" immediate="true" accesskey="C" />
	<br/>
	<h:commandButton id="restart" value="Apply and Restart" action="#{netConfig.actionRestart}" accesskey="R" />
	<h:commandButton id="save" value="Save" action="#{netConfig.actionSave}" accesskey="S" />
	<b>Changes require restart!</b>
</h:form>

<h:panelGrid columns="2" styleClass="grid" columnClasses=",,">
	<f:facet name="header"><h:outputText value="Info" /></f:facet>
	
	<h:outputLabel for="eff_pool_address" value="RMI pool address:" />
	<h:inputText id="eff_pool_address" value="#{netConfig.effectivePoolAddress}" required="false" size="40" />
</h:panelGrid>

<%@include file="body-footer.jspf" %>
</body>
</html>
</f:view>

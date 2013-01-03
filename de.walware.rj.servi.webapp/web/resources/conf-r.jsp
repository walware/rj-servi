<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%--
 ###############################################################################
 # Copyright (c) 2009-2013 WalWare/RJ-Project (www.walware.de/goto/opensource).
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

<title>(RJ) Configuration: R/RJ Nodes</title>
</head>
<body>
<%@include file="body-header.jspf" %>

<h2>Configuration: R/RJ Nodes</h2>

<h:form id="r_config">
	<h:messages errorClass="error" />
	
<h:panelGrid columns="3" styleClass="grid" columnClasses="label,value,info">
	<f:facet name="header"><h:outputText value="Parameters to start R" /></f:facet>
	
	<h:outputLabel for="java_home_path" value="(1) Java home (path):" accesskey="1" />
	<h:inputText id="java_home_path" label="Java home (1)" value="#{rConfig.javaHome}" required="false" size="80" />
	<h:outputText>(empty &#x21d2; same as the server)</h:outputText>
	
	<h:outputLabel for="java_cmd_args" value="(2) Java arguments:" accesskey="2" />
	<h:inputTextarea id="java_cmd_args" label="Java arguments (2)" value="#{rConfig.javaArgs}" required="false" cols="76" rows="4" />
	<h:outputText></h:outputText>
	
	<h:outputLabel for="r_home_path" value="(3) R home / R_HOME (path):" accesskey="3" />
	<h:inputText id="r_home_path" label="R home / R_HOME (3)" value="#{rConfig.RHome}" required="true" size="80" />
	<h:outputText></h:outputText>
	
	<h:outputLabel for="r_arch_code" value="(4) Architecture of binaries / R_ARCH:" accesskey="4" />
	<h:inputText id="r_arch_code" label="Architecture / R_ARCH (4)" value="#{rConfig.RArch}" required="false" size="10" />
	<h:outputText>(empty &#x21d2; autodetection)</h:outputText>
	
	<h:outputLabel for="r_libs_path" value="(5) R libraries / R_LIBS (path):" accesskey="5" />
	<h:inputText id="r_libs_path" label="R libraries / R_LIBS (5)" value="#{rConfig.RLibsVariable}" required="false" size="80" />
	<h:outputText>(optional)</h:outputText>
	
	<h:outputLabel for="base_wd_path" value="(6) Working directory (path):" accesskey="6" />
	<h:inputText id="base_wd_path" label="Working directory (6)" value="#{rConfig.baseWorkingDirectory}" required="false" size="80" />
	<h:outputText>(empty &#x21d2; default temp dir)</h:outputText>
	
	<h:outputLabel for="r_startup_snippet" value="(7) R startup snippet (complete R command per line):" accesskey="7" />
	<h:inputTextarea id="r_startup_snippet" label="R startup snippet (7)" value="#{rConfig.RStartupSnippet}" required="false" cols="76" rows="4" />
	<h:outputText />
	
	<h:outputLabel for="debug_console_enabled" value="(8) Enable debug console:" accesskey="8" />
	<h:selectBooleanCheckbox id="debug_console_enabled" label="Enable debug console by default (8)" value="#{rConfig.enableConsole}" required="true" />
	<h:outputText>Enables debug console automatically at startup (use only for development!)</h:outputText>
	
	<h:outputLabel for="verbose_console_enabled" value="(9) Enables verbose logging:" accesskey="9" />
	<h:selectBooleanCheckbox id="verbose_console_enabled" label="Enables verbose logging (9)" value="#{rConfig.enableVerbose}" required="true" />
	<h:outputText>Enables verbose logging and prevents deletion of the node directory</h:outputText>
	
</h:panelGrid>
	
	<h:commandButton id="loadDefaults" value="Load Defaults" action="#{rConfig.actionLoadDefaults}" type="button" immediate="true" accesskey="L" />
	<h:commandButton id="loadCurrent" value="Load Current" action="#{rConfig.actionLoadCurrent}" type="button" immediate="true" accesskey="C" />
	<br/>
	<h:commandButton id="apply" value="Apply" action="#{rConfig.actionApply}" accesskey="A" />
	<h:commandButton id="saveAndApply" value="Save and Apply" action="#{rConfig.actionSaveAndApply}" accesskey="S" />
</h:form>

<%@include file="body-footer.jspf" %>
</body>
</html>
</f:view>

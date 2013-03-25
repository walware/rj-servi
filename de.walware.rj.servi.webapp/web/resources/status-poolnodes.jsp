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

<title>(RJ) Status: Overview Pool Nodes</title>
</head>

<body onload="<c:if test="${poolStatus.autoRefreshEnabled}">window.setTimeout('javascript:document.getElementById(\'pool_nodes:refresh\').click()', 5000)</c:if>">

<%@include file="body-header.jspf" %>

<h2>Status: Current Pool Nodes</h2>

<h:panelGrid columns="2" styleClass="grid" columnClasses=",," style="border-bottom: 2px solid black">
	<h:outputLabel for="eff_pool_address" value="RMI pool address:" />
	<h:inputText id="eff_pool_address" value="#{netConfig.effectivePoolAddress}" readonly="true" size="40" />
</h:panelGrid>

<h:form id="pool_nodes">
	<h:messages errorClass="error" />
	
<h:inputHidden value="#{poolStatus.autoRefreshEnabled}" immediate="true" />
<h:commandButton id="refresh" value="Refresh" action="#{poolStatus.actionRefresh}" />
<c:if test="${!poolStatus.autoRefreshEnabled}">
<h:commandButton id="enableAutoRefresh" value="Enable Auto Refresh" action="#{poolStatus.actionEnableAutoRefresh}" />
</c:if>
<c:if test="${poolStatus.autoRefreshEnabled}">
<h:commandButton id="disableAutoRefresh" value="Disable Auto Refresh" action="#{poolStatus.actionDisableAutoRefresh}" immediate="true" />
</c:if>

<br/>

<h:panelGrid columns="3" styleClass="table1" columnClasses=",alignright,alignright">
	<f:facet name="header"><h:outputText value="Summary" /></f:facet>
	
	<h:outputText />
	<h:outputText value="Current" />
	<h:outputText value="Max" />
	
	<h:outputText value="Idling:" />
	<h:outputText value="#{poolStatus.numIdling}" />
	<h:outputText value="#{poolStatus.maxIdling}" />
	
	<h:outputText value="In use:" />
	<h:outputText value="#{poolStatus.numInUse}" />
	<h:outputText value="#{poolStatus.maxInUse}" />
	
	<h:outputText value="Total:" />
	<h:outputText value="#{poolStatus.numTotal}" />
	<h:outputText value="#{poolStatus.maxTotal}" />
</h:panelGrid>
	
<br/>
	
<h:dataTable value="#{poolStatus.nodeStates}" var="dataItem" styleClass="table1" columnClasses=",,spanleft,,alignright">
	<h:column>
		<f:facet name="header">
			<h:outputText value="Created" />
		</f:facet>
		<h:outputText value="#{dataItem.creationTime}" >
			<f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/> 
		</h:outputText>
	</h:column>
	
	<h:column>
		<f:facet name="header">
			<h:outputText value="State" />
		</f:facet>
		<h:outputText value="#{dataItem.state}" />
	</h:column>
	
	<h:column headerClass="spanleft">
		<f:facet name="header">
			<h:outputText value="" />
		</f:facet>
		(<h:outputText value="#{dataItem.stateTime - poolStatus.stamp}" >
			<f:converter converterId="convertDuration" /> 
		</h:outputText>)
	</h:column>
	
	<h:column>
		<f:facet name="header">
			<h:outputText value="Client" />
		</f:facet>
		<h:outputText value="#{dataItem.currentClientId}" />
	</h:column>
	
	<h:column>
		<f:facet name="header">
			<h:outputText value="Usage" />
		</f:facet>
		<h:outputText value="#{dataItem.usageCount}" />
	</h:column>
	
	<h:column>
		<f:facet name="header">
			<h:outputText value="Debug Console" />
		</f:facet>
		<h:selectBooleanCheckbox value="#{dataItem.consoleEnabled}" readonly="true" disabled="true" />
		<h:inputText value="#{dataItem.RMIAddress}" readonly="true" size="40" />
		<h:outputText value=" " />
		<h:commandLink value="On" action="#{dataItem.actionEnableConsole}" rendered="#{!dataItem.consoleEnabled}" />
		<h:commandLink value="Off" action="#{dataItem.actionDisableConsole}" rendered="#{dataItem.consoleEnabled}" />
	</h:column>
	
	<h:column>
		<f:facet name="header">
			<h:outputText value="Evict" />
		</f:facet>
		<h:commandLink value="Stop" action="#{dataItem.actionStop}" />
		<h:outputText value=" " />
		<h:commandLink value="Kill" action="#{dataItem.actionKill}" />
	</h:column>
	
</h:dataTable>

<h:panelGrid columns="2" styleClass="grid" columnClasses=",info">
	<f:facet name="header"><h:outputText value="Test Actions" /></f:facet>
	
	<h:commandLink action="#{debug.actionNewNode}" immediate="true">Get node (<code>RServiPool#getRServi</code>)</h:commandLink>
	<h:outputText value="acquire node from pool" />
	
	<h:commandLink action="#{debug.actionCloseAllNodes}" immediate="true">Close all nodes (<code>RServi#close</code>)</h:commandLink>
	<h:outputText value="return acquired nodes to pool" />
</h:panelGrid>
	
</h:form>

<%@include file="body-footer.jspf" %>
</body>
</html>
</f:view>

<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%--
 #=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================#
--%>
<f:view>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta http-equiv="Content-Style-Type" content="text/css" />
<link rel="stylesheet" type="text/css" href="css/style.css" />

<title>(RJ) Configuration: Pool</title>
</head>
<body>
<%@include file="body-header.jspf" %>

<h2>Configuration: Pool</h2>

<h:form id="pool_config">
	<h:messages errorClass="error" />
	
<h:panelGrid columns="3" styleClass="grid" columnClasses="label,value,info special1">
	<f:facet name="header"><h:outputText value="Parameters for dynamic pool" /></f:facet>
	
	<h:outputText/>
	<h:outputText/>
	<h:outputText>Startup new nodes if...</h:outputText>
	
	<h:outputLabel for="max_total_count" value="(1) Max total nodes (count):" accesskey="1" />
	<h:inputText id="max_total_count" label="Max total nodes (1)" value="#{poolConfig.maxTotalCount}" required="true" />
	<h:outputText>... total count smaller than (1) ...</h:outputText>
	
	<h:outputLabel for="min_idle_count" value="(2) Min idle nodes (count):" accesskey="2" />
	<h:inputText id="min_idle_count" label="Min idle nodes (2)" value="#{poolConfig.minIdleCount}" required="true" />
	<h:outputText>... and idle count smaller than (2).</h:outputText>
	
	<h:outputText/>
	<h:outputText/>
	<h:outputText>Evict idling nodes if...</h:outputText>
	
	<h:outputLabel for="max_idle_count" value="(3) Max idle nodes (count):" accesskey="3" />
	<h:inputText id="max_idle_count" label="Max idle nodes (3)" value="#{poolConfig.maxIdleCount}" required="true" />
	<h:outputText>... idle count greater than (3) ...</h:outputText>
	
	<h:outputLabel for="min_idle_millis" value="(4) Min node idle time (millisec):" accesskey="4" />
	<h:inputText id="min_idle_millis" label="Min node idle time (4)" value="#{poolConfig.minIdleTime}" required="true" />
	<h:outputText>... or idle count greater than (2) count and idle time greater than (4).</h:outputText>
	
	<h:outputText/>
	<h:outputText/>
	<h:outputText>When node is requested and max count nodes (1) is in use ...</h:outputText>
	
	<h:outputLabel for="max_wait_millis" value="(5) Max wait time (millisec):" accesskey="5" />
	<h:inputText id="max_wait_millis" label="Max wait time (5)" value="#{poolConfig.maxWaitTime}" required="true"/>
	<h:outputText>... wait for a free node until timeout (5).</h:outputText>
	
	<h:outputText/>
	<h:outputText/>
	<h:outputText>To keep healthy ...</h:outputText>
	
	<h:outputLabel for="max_usage_count" value="(6) Max node reuse (count):" accesskey="6" />
	<h:inputText id="max_usage_count" label="Max node reuse (6)" value="#{poolConfig.maxUsageCount}" required="true" />
	<h:outputText>... recycle a node maximal (6) times.</h:outputText>
	
	<h:outputText/>
	<h:outputText/>
	<h:outputText>When stopping the pool or single nodes:</h:outputText>
	
	<h:outputLabel for="eviction_timeout_millis" value="(7) Timeout when evicting node in use (millisec):" accesskey="7" />
	<h:inputText id="eviction_timeout_millis" label="Timeout when evicting node (7)" value="#{poolConfig.evictionTimeout}" required="true" />
	<h:outputText></h:outputText>
</h:panelGrid>
	
	<h:commandButton id="loadDefaults" value="Load Defaults" action="#{poolConfig.actionLoadDefaults}" type="button" immediate="true" accesskey="L" />
	<h:commandButton id="loadCurrent" value="Load Current" action="#{poolConfig.actionLoadCurrent}" type="button" immediate="true" accesskey="C" />
	<br/>
	<h:commandButton id="apply" value="Apply" action="#{poolConfig.actionApply}" accesskey="A" />
	<h:commandButton id="saveAndApply" value="Save and Apply" action="#{poolConfig.actionSaveAndApply}" accesskey="S" />
</h:form>

<%@include file="body-footer.jspf" %>
</body>
</html>
</f:view>

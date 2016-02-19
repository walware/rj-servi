<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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

<title>(RJ) Welcome</title>
</head>
<body>
<%@include file="body-header.jspf" %>

<h2>Welcome</h2>

<table class="grid">
<tr><th colspan="2" scope="colgroup">Links</th></tr>
<tr><td>WalWare.de Open Source</td><td><a href="http://www.walware.de/goto/opensource">www.walware.de/goto/opensource</a></td></tr>
<tr><td>R-Project</td><td><a href="http://www.r-project.org">www.r-project.org</a></td></tr>
</table>

<table class="grid">
<tr><th colspan="2" scope="colgroup">Sources</th></tr>
<tr><td>RJ-Core</td><td><a href="http://github.com/walware/rj-core">github.com/walware/rj-core</a></td></tr>
<tr><td>RJ-Client</td><td><a href="http://github.com/walware/rj-client">github.com/walware/rj-client</a></td></tr>
<tr><td>RJ-Servi</td><td><a href="http://github.com/walware/rj-servi">github.com/walware/rj-servi</a></td></tr>
</table>

<hr/>

<p>
Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.</p>
<p>
This product includes software developed by The Apache Software Foundation
(<a href="http://www.apache.org">www.apache.org</a>).</p>

<%@include file="body-footer.jspf" %>
</body>
</html>
</f:view>

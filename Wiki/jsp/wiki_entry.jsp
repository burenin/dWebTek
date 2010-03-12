<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="pattern" value="${fn:escapeXml(param.pattern)}" />
<c:set var="wiki" value="${fn:escapeXml(param.wiki)}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
	<head>
		<title>Wiki Entries: Select A Wiki Page</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" media="screen" href="jsp/css/stylesheet.css" />
		<script type="text/javascript" language="JavaScript" src="jsp/javascript/standard.js"></script>
		<script type="text/javascript" language="JavaScript">
		window.onload = function() {
			initFormFields(document.search, document.servers.server);
		}
		</script>
	</head>
	<body>
		<div class="pageContainer">
			<%@ include file="includes/header.jsp" %>
			<div class="main">
				<h2>Wiki Entries:</h2>
				<div class="left">
					<c:set var="entries" value="${model.words}" />
					<c:choose>
						<c:when test="${not empty entries}">
							<c:set var="server" value="" />
							<c:forEach varStatus="i" items="${entries}" var="match">
								<c:if test="${match.server ne server}">
									<c:if test="${i.index ne 0}">
										</ul>
									</c:if>
									<h3 style="display: inline;">${fn:escapeXml(match.server)}</h3>
									<div style="float: right;">[ <a href="Edit?wiki=${fn:escapeXml(match.server)}">Create word</a> ]</div>
									<div class="floatbreaker"></div>
									<ul>
									<c:set var="server" value="${match.server}" />
								</c:if>
								<li><a href="Read?wiki=${fn:escapeXml(match.server)}&amp;word=${fn:escapeXml(match.name)}">${fn:escapeXml(match.name)}</a></li>
								<c:if test="${i.last}">
									</ul>
								</c:if>
							</c:forEach>
						</c:when>
						<c:when test="${(param.pattern ne null and param.pattern ne '') and (param.wiki ne null and param.wiki ne '')}">
							<p>No words matching <i>${pattern}</i> on wiki server(s):<br />
							<ul>
								<c:forEach varStatus="i" items="${fn:split(wiki, ',')}" var="server">
									<li>${server}</li>
								</c:forEach>
							</ul>
						</c:when>
						<c:when test="${(param.pattern ne null and param.pattern ne '') and (param.wiki eq null or param.wiki eq '')}">
							<p>No words matching <i>${pattern}</i>.</p>
						</c:when>
						<c:when test="${(param.pattern eq null or param.pattern eq '') and (param.wiki eq null or param.wiki eq '')}">
							<p>No words.</p>
						</c:when>
						<c:otherwise>
							<p>No words on wiki server(s):<br />
							<ul>
								<c:forEach varStatus="i" items="${fn:split(wiki, ',')}" var="server">
									<li>${server}</li>
								</c:forEach>
							</ul>
						</c:otherwise>
					</c:choose>
				</div>
				<div class="right">
					<h3>Searching in servers</h3>
					<p><i>Default: all</i></p>
					<form name="servers" action="">
						<input type="button" onclick="checkAll(document.servers.server)" value="Check all" />
						<input type="button" onclick="uncheckAll(document.servers.server)" value="Uncheck all" /><br/>
						<c:forEach varStatus="i" items="${model.servers}" var="server">
							<input class="checkbox" type="checkbox" name="server" value="${fn:escapeXml(server.name)}" />
							<label><b>${fn:escapeXml(server.name)}</b>, at <i>http://${fn:escapeXml(server.host)}:${fn:escapeXml(server.port)}/</i></label><br />
						</c:forEach>
					</form>
				</div>
				<div class="floatbreaker"></div>
			</div>
			<div class="editor"></div>
			<%@ include file="includes/footer.jsp" %>
		</div>
	</body>
</html>
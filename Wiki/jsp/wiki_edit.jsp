<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="word" value="${fn:escapeXml(param.word)}" />
<c:set var="wiki" value="${fn:escapeXml(param.wiki)}" />
<c:set var="message" value="${model.message}" />
<c:set var="text" value="${model.text}" />
<c:set var="textlost" value="${model.textlost}" />
<c:set var="conflict" value="${model.conflict}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
	<head>
		<title>
			<c:choose>
				<c:when test="${param.wiki ne null and param.wiki ne '' and (param.word eq null or param.word eq '')}">
					Create new word on server: ${param.wiki}
				</c:when>
				<c:when test="${text ne null}">
					Editing wiki page: ${word}, on server: ${param.wiki}
				</c:when>
				<c:otherwise>
					No wiki page on ${wiki} matching word ${word}
				</c:otherwise>
			</c:choose>
		</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" media="screen" href="jsp/css/stylesheet.css" />
	</head>
	<body>
		<div class="pageContainer">
			<%@ include file="includes/header.jsp" %>
			<div class="main">
				<c:choose>
					<c:when test="${param.wiki ne null and param.wiki ne '' and param.word eq null}">
						<form name="edit" method="post" action="Edit">
							Word name: <input type="text" name="word" value="" /><br />
							<textarea name="text"></textarea><br />
							<input type="hidden" name="wiki" value="${wiki}" />
							<input type="submit" value="Create" />
						</form>
					</c:when>
					<c:when test="${text ne null}">
						<form name="edit" method="post" action="Edit">
							Word name: <input type="text" name="word" value="${word}" /><br />
							<textarea name="text">${model.text}</textarea><br />
							<input type="hidden" name="wiki" value="${wiki}" />
							<input type="hidden" name="oldword" value="${word}" />
							<input type="hidden" name="etag" value="${model.etag}" />
							<input type="submit" value="Edit" />
						</form>
					</c:when>
					<c:when test="${message ne null}">
						${message}
						<c:if test="${textlost ne null}">
							<hr />
							<form name="edit" method="post" action="Edit">
								Word name: <input type="text" name="word" value="${word}" /><br />
								<c:if test="${conflict eq true}">
									Ignore conflict?
									<input type="radio" name="ignoreconflict" value="yes" />Yes
									<input type="radio" name="ignoreconflict" value="no" checked />No
								</c:if>
								<textarea name="text">${textlost}</textarea><br />
								<input type="hidden" name="wiki" value="${wiki}" />
								<c:set var="hidden" value="${model.oldword}" />
								<c:if test="${oldword ne null and oldword ne ''}">
									<input type="hidden" name="oldword" value="${oldword}" />
								</c:if>
								<input type="hidden" name="etag" value="${model.etag}" />
								<input type="submit" value="Try again" />
							</form>
						</c:if>
					</c:when>
				</c:choose>
			</div>
			<div class="editor">
				<div><a href="JavaScript: window.open('editorHelp.html', 'Editor help', 'width=500, height=400');">Help</a></div>
			</div>
			<%@ include file="includes/footer.jsp" %>
		</div>
	</body>
</html>
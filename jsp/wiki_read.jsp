<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="word" value="${fn:escapeXml(param.word)}" />
<c:set var="wiki" value="${fn:escapeXml(param.wiki)}" />
<c:set var="pattern" value="${fn:escapeXml(param.pattern)}" />
<c:set var="xhtml" value="${model.xhtml}" />
<c:set var="message" value="${model.message}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
	<head>
		<title>
			<c:choose>
				<c:when test="${xhtml ne null}">
					Wiki Word: ${word}
				</c:when>
				<c:otherwise>
					Error Reading Wiki Word: ${word}
				</c:otherwise>
			</c:choose>
		</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" media="screen" href="jsp/css/stylesheet.css" />
		<script type="text/javascript" language="JavaScript" src="jsp/javascript/standard.js"></script>
		<script type="text/javascript" language="JavaScript">
		window.onload = function() {
			initFormFields(document.search, new Array());
		}
		</script>
	</head>
	<body>
		<div class="pageContainer">
			<%@ include file="includes/header.jsp" %>
			<div class="main">
				<c:choose>
					<c:when test="${xhtml ne null}">
						<c:set var="versions" value="${model.versions}" />
						<c:if test="${versions > 1}">
							<div class="versions">
								<c:forEach var="i" begin="1" end="${versions}" step="1" varStatus ="status">
									<div><a href="Read?wiki=${wiki}&amp;word=${word}&amp;version=${i}">Version ${i}</a></div>
								</c:forEach>
							</div>
						</c:if>
						${xhtml}
						<div class="floatbreaker"></div>
					</c:when>
					<c:when test="${message ne null}">
						${message}
						<h2>Alternative pages</h2>
						<p>Find the wiki page you are looking for: <a href="Entry">list all words</a>.</p>
						<p>Other wiki pages matching word <i>${word}</i>: <a href="Entry?pattern=${word}&wiki=">click here to get a list of entries.</a></p>
					</c:when>
				</c:choose>
			</div>
			<div class="editor">
			<c:if test="${model.editable eq true}">
				<div><a href="Entry?word=${word}&amp;wiki=${wiki}&amp;delete=yes" onclick="if(${versions}>1){var all=confirm('Delete all versions?');if(all)all='&all=yes';else all='';window.location=this.href+all;return false}">Delete</a></div>
				<div><a href="Edit?word=${word}&amp;wiki=${wiki}">Edit</a></div>
			</c:if>
			</div>
			<%@ include file="includes/footer.jsp" %>
		</div>
	</body>
</html>
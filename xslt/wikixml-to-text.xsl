<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:w="http://cs.au.dk/dWebTek/WikiXML">
	
	<xsl:output
		method="xml"
		encoding="UTF-8"
		omit-xml-declaration="yes"
		media-type="text/html" />
	
	<xsl:template match="/w:wiki">
		<text>
			<xsl:apply-templates select="w:*" />
		</text>
	</xsl:template>
	
	
	
	<!--
		Newline variable
	-->
	<xsl:variable name="lineseparator">
		<xsl:text>
</xsl:text>
	</xsl:variable>
	
	
	
	<!--
		Template: Adds lineseparator in front, if needed
	-->
	<xsl:template name="separatorHandler">
		<xsl:variable name="prevElementName" select="name(preceding-sibling::w:*[1])" />
		
		<xsl:if test="$prevElementName != 'header' and $prevElementName != 'rule' and $prevElementName != 'item' and $prevElementName != 'br'">
			<xsl:value-of select="$lineseparator" />
		</xsl:if>
	</xsl:template>
	
	
	
	<!-- TAGS BELOW -->
	
	
	
	<!--
		The image element has a url attribute locating an image.
	-->
	<xsl:template match="w:image">
		<xsl:text>[[Image:</xsl:text>
		<xsl:value-of select="@url" />
		<xsl:text>]]</xsl:text>
	</xsl:template>
	
	
	
	<!-- Wikilink and link -->
	<xsl:template match="w:wikilink|w:link">
		<xsl:choose>
			<xsl:when test="matches(@word, '[^a-zA-Z_]+')">
				<xsl:value-of select="@word" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="self::w:*" mode="link" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	
	<!--
		The wikilink element has a word attribute denoting the
		name of another Wiki page to which a link is created.
		If the wiki attribute is absent, then the page resides
		on the same Wiki server as the current one, otherwise
		the attribute refers to a Wiki server identified by the
		Wiki metaserver (see below).
		The word attributes above can only contain letters
		(a-zA-Z) and the underscore character (_).
	-->
	<xsl:template match="w:wikilink" mode="link">
		<xsl:text>[[</xsl:text>
		<xsl:if test="@wiki != ''">
			<xsl:value-of select="@wiki" />
			<xsl:text>:</xsl:text>
		</xsl:if>
		<xsl:value-of select="@word" />
		<xsl:text>]]</xsl:text>
	</xsl:template>
	
	
	
	<!--
		The link element has a url attribute and a word attribute
		and creates an ordinary hyperlink.
		The word attributes above can only contain letters
		(a-zA-Z) and the underscore character (_).
	-->
	<xsl:template match="w:link" mode="link">
		<xsl:text>[</xsl:text>
		<xsl:value-of select="@url" />
		<xsl:text> </xsl:text>
		<xsl:value-of select="@word" />
		<xsl:text>]</xsl:text>
	</xsl:template>
	
	
	<!-- Italics, tt and bold -->
	<xsl:template match="w:italics|w:bold|w:tt">
		<xsl:choose>
			<xsl:when test="count(ancestor::w:*[name() = name(current())]) > 0">
				<xsl:apply-templates select="w:*" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="self::w:*" mode="tag" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	
	<!--
		The italics element contains a sequence of Wiki
		elements that are rendered in the italics font
		style. It is not allowed to nest italics tags.
	-->
	<xsl:template match="w:italics" mode="tag">
		<xsl:text>''</xsl:text>
		<xsl:apply-templates select="w:*" />
		<xsl:text>''</xsl:text>
	</xsl:template>
	
	
	
	<!--
		The tt element contains a sequence of Wiki elements
		that are rendered in the fixed-width font style.
		It is not allowed to nest tt tags.
	-->
	<xsl:template match="w:tt" mode="tag">
		<xsl:text disable-output-escaping="yes">&lt;tt&gt;</xsl:text>
		<xsl:apply-templates select="w:*" />
		<xsl:text disable-output-escaping="yes">&lt;/tt&gt;</xsl:text>
	</xsl:template>
	
	
	
	<!--
		The bold element contains a sequence of Wiki elements
		that are rendered in the bold font weight.
		It is not allowed to nest bold tags.
	-->
	<xsl:template match="w:bold" mode="tag">
		<xsl:text>'''</xsl:text>
		<xsl:apply-templates select="w:*" />
		<xsl:text>'''</xsl:text>
	</xsl:template>
	
	
	<!--
		The header element contains a sequence of Wiki
		elements that are rendered as a header. It is not
		allowed to nest header tags, and they cannot contain list or rule tags.
	-->
	<xsl:template match="w:header">
		<xsl:variable name="validElements" select="w:*[name() != 'header' and name() != 'list' and name() != 'rule']" />
		
		<xsl:if test="position() != 1">
			<xsl:call-template name="separatorHandler" />
		</xsl:if>
		<xsl:text>== </xsl:text>
		<xsl:apply-templates select="$validElements" />
		<xsl:text> ==</xsl:text>
		<xsl:value-of select="$lineseparator" />
	</xsl:template>
	
	
	
	<!--
		The rule element denotes a horizontal rule.
	-->
	<xsl:template match="w:rule">
		<xsl:call-template name="separatorHandler" />
		<xsl:text>----</xsl:text>
		<xsl:value-of select="$lineseparator" />
	</xsl:template>
	
	
	
	<!--
		The character has an entity attribute and corresponds
		to an XHMTL character entity reference.
	-->
	<xsl:template match="w:character">
		<xsl:text disable-output-escaping="yes">&amp;</xsl:text>
		<xsl:value-of select="@entity" />
		<xsl:text disable-output-escaping="yes">;</xsl:text>
	</xsl:template>
	
	
	
	<!--
		The list element contains as children item elements
		that each contain sequences of Wiki elements. The item
		elements cannot contain header, list or rule tags.
	-->
	<xsl:template match="w:list">
		<xsl:variable name="items" select="w:item" />
		
		<xsl:if test="count($items) &gt; 0">
			<xsl:call-template name="separatorHandler" />
			<xsl:apply-templates select="$items" />
		</xsl:if>
	</xsl:template>
	
	
	
	<xsl:template match="w:item">
		<xsl:variable name="validElements" select="w:*[name() != 'header' and name() != 'list' and name() != 'rule']" />
		
		<xsl:text>* </xsl:text>
		<xsl:apply-templates select="$validElements" />
		<xsl:value-of select="$lineseparator" />
	</xsl:template>
	
	
	
	<!--
		The br element denotes a linebreak.
	-->
	<xsl:template match="w:br">
		<xsl:variable name="nextElementName" select="name(following-sibling::w:*[1])" />
		
		<xsl:call-template name="separatorHandler" />
		<xsl:text disable-output-escaping="yes">&lt;br/&gt;</xsl:text>
		<xsl:value-of select="$lineseparator" />
	</xsl:template>
	
	
	
	<!--
		The text element contains character data, which
		cannot be empty and cannot have leading or trailing whitespace.
		A sequence of Wiki elements cannot contain two adjacent
		text elements nor two adjacent ws elements.
	-->
	<xsl:template match="w:text">
		<xsl:variable name="nextElementName" select="name(following-sibling::w:*[1])" />
		
		<xsl:value-of select="normalize-space(text())" />
		<xsl:if test="$nextElementName = 'text'">
			<xsl:text> </xsl:text>
		</xsl:if>
	</xsl:template>
	
	
	
	<!--
		The ws element denotes whitespace.
		A sequence of Wiki elements cannot contain two adjacent
		text elements nor two adjacent ws elements.
		In a sequence of Wiki elements, a ws element cannot
		directly follow header, rule, list, item, or br elements.
	-->
	<xsl:template match="w:ws">
		<xsl:variable name="nextElementName" select="name(following-sibling::w:*[1])" />
		<xsl:variable name="prevElementName" select="name(preceding-sibling::w:*[1])" />
		
		<xsl:choose>
			<xsl:when test="$nextElementName != 'ws' and $prevElementName != 'header' and $prevElementName != 'rule' and $prevElementName != 'list' and $prevElementName != 'item' and $prevElementName != 'br'">
				<xsl:text> </xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
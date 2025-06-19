<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:template match="/" >
   <xsl:apply-templates select="EntityDefinition/Transform" />
</xsl:template>

<xsl:template match="Transform" >
   <Transform translation="{@translation}" rotation="{@rotation}">
         <xsl:for-each select="/EntityDefinition/Inline">
         	<Inline url="{@url}" />
	 </xsl:for-each>   
   </Transform>   
</xsl:template>

</xsl:stylesheet>
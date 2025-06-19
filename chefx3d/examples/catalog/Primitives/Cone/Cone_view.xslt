<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

<xsl:template match="Cone">
	<Shape>
   	   <Cone bottom="{@bottom}" bottomRadius="{@bottomRadius}" side="{@side}" solid="{@solid}" height="{@height}" />
	</Shape>
</xsl:template>
</xsl:stylesheet>
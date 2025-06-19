<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

<xsl:template match="Transform">
   <Transform translation="{@translation}" rotation="{@rotation}">
         <xsl:for-each select="/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Cylinder">
		<Shape>
		<Cylinder bottom="{@bottom}" radius="{@radius}" top="{@top}" side="{@side}" solid="{@solid}" height="{@height}" />
		</Shape>
	 </xsl:for-each>   
   </Transform>
</xsl:template>
</xsl:stylesheet>
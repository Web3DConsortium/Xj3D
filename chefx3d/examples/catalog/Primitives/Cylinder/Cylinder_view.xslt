<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

<xsl:template match="Cylinder">

<Shape>
   <Cylinder bottom="{@bottom}" radius="{@radius}" top="{@top}" side="{@side}" solid="{@solid}" height="{@height}" />
</Shape>

</xsl:template>

</xsl:stylesheet>
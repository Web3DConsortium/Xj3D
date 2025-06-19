<xsl:stylesheet version = '2.0'
                xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

    <xsl:template match="Box">
        <WorldInfo />
        <Shape>
            <Box size="{@x} {@y} {@z}"/>
        </Shape>
    </xsl:template>
</xsl:stylesheet>
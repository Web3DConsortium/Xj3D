<xsl:stylesheet version = '2.0'
                xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

    <xsl:template match="Transform">
        <Transform translation="{@translation}" rotation="{@rotation}">
            <xsl:for-each select="/ChefX3D/EntityParams/Sheet[@name='SMAL']/EntityDefinition/Box">
                <Shape>
                    <Box size="{@x} {@y} {@z}"/>
                </Shape>
            </xsl:for-each>
        </Transform>
    </xsl:template>
</xsl:stylesheet>
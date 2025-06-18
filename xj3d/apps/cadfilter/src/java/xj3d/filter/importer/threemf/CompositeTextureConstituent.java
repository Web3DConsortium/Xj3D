/*
 * ****************************************************************************
 *  *                        Shapeways Copyright (c) 2015
 *  *                               Java Source
 *  *
 *  * This source is licensed under the GNU LGPL v2.1
 *  * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  *
 *  * This software comes with the standard NO WARRANTY disclaimer for any
 *  * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *  *
 *  ****************************************************************************
 */

package xj3d.filter.importer.threemf;

import org.xml.sax.Attributes;

/**
 * Base material
 *
 * @author Alan Hudson
 */
public class CompositeTextureConstituent implements ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.BASE;

    private String name;
    private String displayColor;

    public CompositeTextureConstituent(Attributes atts) {
        name = atts.getValue("name");
        displayColor = atts.getValue("displayColor");
    }

    public CompositeTextureConstituent(String name, String displayColor) {
        this.name = name;
        this.displayColor = displayColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayColor() {
        return displayColor;
    }

    public void setDisplayColor(String val) {
        this.displayColor = val;
    }

    @Override
    public void addElement(ThreeMFElement el) {
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }
}

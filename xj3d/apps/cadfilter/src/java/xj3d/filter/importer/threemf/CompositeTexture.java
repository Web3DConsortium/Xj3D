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

import javax.vecmath.Matrix3d;

/**
 * Components object.
 *
 * The <code>components</code> element acts as a container for all components to 
 * be composed into the current object.
 *
 * @author Alan Hudson
 */
public class CompositeTexture implements ModelResource, ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.COMPOSITE_TEXTURE;

    private int id;
    private int matID;
    private int[] matIndices;
    private int[] defaultValues;
    private Matrix3d transform;  // TODO not sure about this
    private CompositeTextureConstituent constituent;


    public CompositeTexture(Attributes atts) {
        String st = atts.getValue("id");
        if (st != null) {
            id = Integer.parseInt(st);
        }
        st = atts.getValue("matid");
        if (st != null) {
            matID = Integer.parseInt(st);
        }
    }

    @Override
    public void addElement(ThreeMFElement el) {
        switch(el.getElementID()) {
            case ThreeMFElementFactory.COMPOSITE_TEXTURE_CONSTITUIENT:
                constituent = (CompositeTextureConstituent) el;
                break;
        }
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public CompositeTextureConstituent getConstituent() {
        return constituent;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }
}

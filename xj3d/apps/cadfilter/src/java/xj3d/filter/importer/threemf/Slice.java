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

import java.util.ArrayList;
import java.util.List;

/**
 * Components object.
 *
 * The <code>components</code> element acts as a container for all components to 
 * be composed into the current object.
 *
 * @author Alan Hudson
 */
public class Slice implements ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.BASE_MATERIALS;

    private long id;
    private double ztop;

    private List<ThreeMFElement> children;

    public Slice(Attributes atts) {
        children = new ArrayList<>();

        String st = atts.getValue("id");
        if (st != null) {
            id = Long.parseLong(st);
        }

        st = atts.getValue("ztop");
        if (st != null) {
            ztop = Double.parseDouble(st);
        }

    }

    @Override
    public void addElement(ThreeMFElement el) {
        switch(el.getElementID()) {
            case ThreeMFElementFactory.COMPOSITE_TEXTURE:
                children.add(el);
                break;
        }
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public List<ThreeMFElement> getChildren() {
        return children;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public double getZTop() {
        return ztop;
    }

    public void setZTop(double ztop) {
        this.ztop = ztop;
    }
}

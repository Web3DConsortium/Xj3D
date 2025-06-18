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
public class SliceStack implements ModelResource, ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.SLICE_STACK;

    private int id;
    private double zbottom;

    private List<Slice> slices;

    public SliceStack(Attributes atts) {
        slices = new ArrayList<>();

        String st = atts.getValue("id");
        if (st != null) {
            id = Integer.parseInt(st);
        }
        st = atts.getValue("zbottom");
        if (st != null) {
            zbottom = Double.parseDouble(st);
        }
    }

    @Override
    public void addElement(ThreeMFElement el) {
        switch(el.getElementID()) {
            case ThreeMFElementFactory.SLICE:
                slices.add((Slice) el);
                break;
        }
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public List<Slice> getSlices() {
        return slices;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    public double getZBottom() {
        return zbottom;
    }

    public void setZBottom(double zbottom) {
        this.zbottom = zbottom;
    }
}

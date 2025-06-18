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
public class Components implements ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.COMPONENTS;

    private List<Component> components;

    public Components(Attributes atts) {
        components = new ArrayList<>();
    }

    @Override
    public void addElement(ThreeMFElement el) {
        switch(el.getElementID()) {
            case ThreeMFElementFactory.COMPONENT:
                components.add((Component)el);
                break;
        }
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public List<Component> getComponents() {
        return components;
    }
}

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

import javax.vecmath.Matrix4d;

/**
 * Item object
 *
 * @author Alan Hudson
 */
public class Item implements ThreeMFElement {
    public static int ELEMENT_ID = ThreeMFElementFactory.ITEM;

    private int objectID;
    private Matrix4d transform;
    private String partNumber;

    public Item(Attributes atts) {
        String oid_st = atts.getValue("objectid");
        if (oid_st != null) {
            objectID = Integer.parseInt(oid_st);
        }

        partNumber = atts.getValue("partnumber");

        String transform_st = atts.getValue("transform");
        if (transform_st != null) {
            try {
                double[] vals = new double[16];
                String[] svals = transform_st.split("\\s+");
                int len = svals.length;

                for (int i = 0; i < len; i++) {
                    vals[i] = Double.parseDouble(svals[i]);
                }

                transform = new Matrix4d();
                transform.m00 = vals[0];
                transform.m01 = vals[1];
                transform.m02 = vals[2];
                transform.m03 = 0;
                transform.m10 = vals[3];
                transform.m11 = vals[4];
                transform.m12 = vals[5];
                transform.m13 = 0;
                transform.m20 = vals[6];
                transform.m21 = vals[7];
                transform.m22 = vals[8];
                transform.m23 = 0;
                transform.m30 = vals[9];
                transform.m31 = vals[10];
                transform.m32 = vals[11];
                transform.m33 = 1;

                transform.transpose();
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Invalid value in transform: " + transform_st);
            }
        }
    }

    public int getObjectID() {
        return objectID;
    }

    @Override
    public void addElement(ThreeMFElement el) {
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public Matrix4d getTransform() {
        return transform;
    }
}

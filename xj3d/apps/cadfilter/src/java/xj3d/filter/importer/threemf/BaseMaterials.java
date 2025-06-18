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
 * Components object.
 *
 * The <code>components</code> element acts as a container for all components to 
 * be composed into the current object.
 *
 * @author Alan Hudson
 */
public class BaseMaterials implements ModelResource, ThreeMFElement, PropertySource {
    public static final int ELEMENT_ID = ThreeMFElementFactory.BASE_MATERIALS;
    private static final int DEFAULT_SIZE = 1000;

    private int id;
    private float[][] colors;
    private int count;

    private boolean hasAlpha;

    public BaseMaterials(Attributes atts) {
        colors = new float[DEFAULT_SIZE][4];

        String st = atts.getValue("id");
        if (st != null) {
            id = Integer.parseInt(st);
        }
    }

    public void addColor(Attributes atts) {
        String color = atts.getValue("displaycolor");

        if ((count + 1)*3 >= colors.length) {
            resizeTris();
        }

        colors[count][0] = Integer.valueOf(color.substring(1,3),16) / 255.0f;
        colors[count][1] = Integer.valueOf(color.substring(3,5),16) / 255.0f;
        colors[count][2] = Integer.valueOf(color.substring(5,7),16) / 255.0f;
        if (color.length() > 8) {
            hasAlpha = true;
            colors[count][3] = 1.0f - Integer.valueOf(color.substring(7,9),16) / 255.0f;
        } else {
            colors[count][3] = 0f;
        }

        count++;
    }

    public int getCount() {
        return count;
    }

    private void resizeTris() {
        float[][] ncolors = new float[colors.length * 2][4];
        int len = colors.length;

        for(int i=0; i < len; i++) {
            ncolors[i][0] = colors[i][0];
            ncolors[i][1] = colors[i][1];
            ncolors[i][2] = colors[i][2];
            ncolors[i][3] = colors[i][3];
        }

        colors = ncolors;
    }
    @Override
    public void addElement(ThreeMFElement el) {
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public float[] getProperty(int i) {
        return colors[i];
    }
}

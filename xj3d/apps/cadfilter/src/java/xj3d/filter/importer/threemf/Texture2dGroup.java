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
 * Texture coordinate container
 *
 * @author Alan Hudson
 */
public class Texture2dGroup implements ModelResource, ThreeMFElement, PropertySource {
    public static final int ELEMENT_ID = ThreeMFElementFactory.TEXTURE_2D_GROUP;

    private static final int DEFAULT_SIZE = 1000;

    private int id;
    private float[][] coords;
    private int count;
    private int texId;

    public Texture2dGroup(Attributes atts) {

        coords = new float[DEFAULT_SIZE][2];

        String st = atts.getValue("id");
        if (st != null) {
            id = Integer.parseInt(st);
        }

        String tid_st = atts.getValue("texid");
        if (tid_st != null) {
            texId = Integer.parseInt(tid_st);
        }
    }

    public void addCoord(Attributes atts) {
        String u = atts.getValue("u");
        String v = atts.getValue("v");

        if ((count + 1)*2 >= coords.length) {
            resizeCoords();
        }

        coords[count][0] = Float.parseFloat(u);
        coords[count][1] = Float.parseFloat(v);

        count++;
    }

    public int getCount() {
        return count;
    }

    private void resizeCoords() {
        float[][] ncoords = new float[coords.length * 2][2];
        int len = coords.length;

        for(int i=0; i < len; i++) {
            ncoords[i][0] = coords[i][0];
            ncoords[i][1] = coords[i][1];
        }

        coords = ncoords;
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

    public int getTexId() {
        return texId;
    }

    public void setTexId(int texId) {
        this.texId = texId;
    }

    @Override
    public float[] getProperty(int i) {
        return coords[i];
    }
}

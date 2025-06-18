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
 * Texture asset
 *
 * @author Alan Hudson
 */
public class Texture2d implements ModelResource, ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.TEXTURE_2D;

    private int id;
    private String path;
    private String contentType;
    private String tileStyleU = "wrap";   // valid is wrap,mirror,clamp,none
    private String tileStyleV = "wrap";
    private float[] box;  // Not sure how to map this to X3D

    public Texture2d(Attributes atts) {
        String st = atts.getValue("id");
        if (st != null) {
            id = Integer.parseInt(st);
        }

        path = atts.getValue("path");
        contentType = atts.getValue("contenttype");

        String atval = atts.getValue("tilestyleu");
        if (atval != null) tileStyleU = atval;

        atval = atts.getValue("tilestylev");
        if (atval != null) tileStyleV = atval;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTileStyleU() {
        return tileStyleU;
    }

    public void setTileStyleU(String tilestyleu) {
        this.tileStyleU = tilestyleu;
    }

    public String getTileStyleV() {
        return tileStyleV;
    }

    public void setTileStyleV(String tilestylev) {
        this.tileStyleV = tilestylev;
    }
}

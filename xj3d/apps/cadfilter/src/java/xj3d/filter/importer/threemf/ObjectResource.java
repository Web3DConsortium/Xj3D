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
 * Object resource object
 *
 * @author Alan Hudson
 */
public class ObjectResource implements ModelResource, ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.OBJECT;

    private int id;
    private String name;
    private String partNumber;
    private ObjectType type;
    private Mesh mesh;
    private Components components;
    private int sliceStackID;
    private int pid = -1;  // Property Group Element reference
    private int pindex = -1;  // Index into property group

    public ObjectResource(Attributes atts) {
        String st = atts.getValue("id");
        if (st != null) {
            id = Integer.parseInt(st);
        }

        name = atts.getValue("name");
        partNumber = atts.getValue("partNumber");

        st = atts.getValue("type");
        if (st != null) {
            type = ObjectType.valueOf(st);
        }

        st = atts.getValue("slicestackid");
        if (st != null) {
            sliceStackID = Integer.parseInt(st);
        }

        st = atts.getValue("pid");
        if (st != null) {
            pid = Integer.parseInt(st);
        }

        st = atts.getValue("pindex");
        if (st != null) {
            pindex = Integer.parseInt(st);
        }
    }

    @Override
    public void addElement(ThreeMFElement el) {
        switch(el.getElementID()) {
            case ThreeMFElementFactory.MESH:
                if (components != null) throw new IllegalArgumentException("An object cannot contain both a mesh and components value");
                mesh = (Mesh) el;
                break;
            case ThreeMFElementFactory.COMPONENTS:
                if (mesh != null) throw new IllegalArgumentException("An object cannot contain both a mesh and components value");
                components = (Components) el;
                break;
        }
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

    public String getName() {
        return name;
    }

    public ObjectType getType() {
        return type;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Components getComponents() {
        return components;
    }

    public long getSliceStackID() {
        return sliceStackID;
    }

    public void setSliceStackID(int sliceStackID) {
        this.sliceStackID = sliceStackID;
    }

    public int getPID() {
        return pid;
    }

    public void setPID(int pid) {
        this.pid = pid;
    }

    public int getPIndex() {
        return pindex;
    }

    public void setPIndex(int pindex) {
        this.pindex = pindex;
    }
}

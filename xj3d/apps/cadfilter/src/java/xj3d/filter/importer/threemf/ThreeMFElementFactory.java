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

import java.util.HashMap;
import java.util.Map;

/**
 * Element factory from SAX strings to objects.
 *
 * Instead of being really namespace aware this code assumes given the parent element and the child name we can find
 * the right thing.
 *
 * @author Alan Hudson
 */
public class ThreeMFElementFactory {
    public static final int MODEL = 0;
    public static final int METADATA = 1;
    public static final int RESOURCES = 2;
    public static final int OBJECT = 3;
    public static final int MESH = 4;
    public static final int VERTICES = 5;
    public static final int VERTEX = 6;
    public static final int TRIANGLES = 7;
    public static final int TRIANGLE = 8;
    public static final int BUILD = 9;
    public static final int ITEM = 10;
    public static final int COMPONENT = 11;
    public static final int COMPONENTS = 12;
    public static final int BASE_MATERIALS = 13;
    public static final int BASE = 14;
    public static final int SLICE_STACK = 15;
    public static final int SLICE = 16;
    public static final int COMPOSITE_TEXTURE = 17;
    public static final int COMPOSITE_TEXTURE_CONSTITUIENT = 18;
    public static final int COLOR_GROUP = 19;
    public static final int COLOR = 20;
    public static final int TEXTURE_2D_GROUP = 21;
    public static final int TEX_2D_COORD = 22;
    public static final int TEXTURE_2D = 23;


    /** Mapping from element name to ThreeMF data model */
    private static final Map<String,Integer> MAPPING;

    static {
        MAPPING = new HashMap<>();
        MAPPING.put("model",MODEL);
        MAPPING.put("metadata",METADATA);
        MAPPING.put("resources",RESOURCES);
        MAPPING.put("object",OBJECT);
        MAPPING.put("mesh",MESH);
        MAPPING.put("vertices",VERTICES);
        MAPPING.put("vertex",VERTEX);
        MAPPING.put("triangles",TRIANGLES);
        MAPPING.put("triangle",TRIANGLE);
        MAPPING.put("build",BUILD);
        MAPPING.put("item",ITEM);
        MAPPING.put("component",COMPONENT);
        MAPPING.put("components",COMPONENTS);
        MAPPING.put("basematerials",BASE_MATERIALS);
        MAPPING.put("base",BASE);
        MAPPING.put("slicestack",SLICE_STACK);
        MAPPING.put("slice",SLICE);
        MAPPING.put("compositetexture",COMPOSITE_TEXTURE);
        MAPPING.put("constituent",COMPOSITE_TEXTURE_CONSTITUIENT);
        MAPPING.put("colorgroup",COLOR_GROUP);
        MAPPING.put("color",COLOR);
        MAPPING.put("texture2dgroup",TEXTURE_2D_GROUP);
        MAPPING.put("tex2dcoord",TEX_2D_COORD);
        MAPPING.put("texture2d",TEXTURE_2D);
    }

    /**
     * Constructor
     */
    public ThreeMFElementFactory() {
    }

    /**
     * Return the CElement per the argument name
     *
     * @param parent The parent element, used for element batching
     * @param name The tag name of the Element
     * @param atts     The Attributes of the Element
     * @return the CElement per the argument name
     */
    public static ThreeMFElement getElement(ThreeMFElement parent, String name, Attributes atts) {
        // Strip namespace for now
        int idx = name.indexOf(":");
        if (idx > -1) {
            name = name.substring(idx+1);
        }
        Integer elemId = MAPPING.get(name);

        if (elemId == null) return null;

        switch(elemId) {
            case MODEL:
                return new Model(atts);
            case METADATA:
                return new ModelMetaData(atts);
            case RESOURCES:
                return parent;
            case OBJECT:
                return new ObjectResource(atts);
            case MESH:
                return new Mesh(atts);
            case VERTICES:
                return new Vertices(atts);
            case TRIANGLES:
                return new Triangles(atts);
            case BUILD:
                return new Build(atts);
            case ITEM:
                return new Item(atts);
            case COMPONENT:
                return new Component(atts);
            case COMPONENTS:
                return new Components(atts);
            case BASE_MATERIALS:
                return new BaseMaterials(atts);
            case SLICE_STACK:
                return new SliceStack(atts);
            case SLICE:
                return new Slice(atts);
            case COMPOSITE_TEXTURE:
                return new CompositeTexture(atts);
            case COMPOSITE_TEXTURE_CONSTITUIENT:
                return new CompositeTextureConstituent(atts);
            case COLOR_GROUP:
                return new ColorGroup(atts);
            case TEXTURE_2D_GROUP:
                return new Texture2dGroup(atts);
            case TEXTURE_2D:
                return new Texture2d(atts);
            default:
                System.out.printf("Cannot find element: %s\n",name);
                return null;
        }
    }
}

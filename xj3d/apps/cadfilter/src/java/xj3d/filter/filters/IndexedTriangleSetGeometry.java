/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import org.j3d.geom.TriangulationUtils;

// Local imports
import org.web3d.vrml.sav.*;

import xj3d.filter.AbstractFilter;


/**
 * Geometry holder that represents the X3D IndexedTriangleSet node.
 * <p>
 *
 * The current implementation does not handle vertex attributes. It doesn't
 * retriangulate the underlying node. It will generate swapped vertices if
 * the geometry has ccw FALSE, otherwise will just pass along.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class IndexedTriangleSetGeometry extends TriangulationGeometry  {

    /** The data to be processed is normals */
    private static final int NORMALS = 1;

    /** The data to be processed is normals */
    private static final int COLORS = 2;

    /** The data to be processed is normals */
    private static final int TEXCOORDS = 3;

    /** The coordinate values from the IFS */
    private float[] coordinates;
    private int numCoordinates;

    /** The normal values from the IFS */
    private float[] normals;
    private int numNormals;

    /** The color values from the IFS */
    private float[] colors;
    private int numColors;

    /** A single set of 2D texture coordinate values from the TFS */
    private float[] texCoords;
    private int numTexCoords;

    /** The fog coordinate values from the IFS */
    private float[] fogCoords;
    private int numFogCoords;

    /** The number of valid coordinate indices in the coordIndex array */
    private int numCoordIndex;

    /** The set of coordinate indices from the IFS */
    private int[] coordIndices;

    /** Indicator if this IFS is solid or not */
    private boolean solid;

    /** Indicator if this IFS is wound conterclockwise */
    private boolean ccw;

    /** Are normals provided per vertex or per face */
    private boolean normalPerVertex;

    /** Flag indicating the colour has 4 components and not 3 */
    private boolean colorHasAlpha;

    /** The def name of the geometry */
    private String defName;

    /**
     * Construct a default instance of this converter.
     */
    IndexedTriangleSetGeometry() {
        solid = true;
        ccw = true;
        normalPerVertex = true;
        colorHasAlpha = false;
    }

    //----------------------------------------------------------
    // Methods defined by TriangulationGeometry
    //----------------------------------------------------------

    /**
     * Clear the currently stored values and return to the defaults for
     * this geometry type.
     */
    @Override
    void reset() {
        solid = true;
        ccw = true;
        normalPerVertex = true;
        colorHasAlpha = false;

        coordinates = null;
        normals = null;
        colors = null;
        fogCoords = null;
        texCoords = null;
        coordIndices = null;
    }

    /**
     * Add a new field value to the geometry. The form of the value is
     * not defined and is up to the implementing class to interpret it
     * according to the needed fields. Note that field names will be
     * compressed from the X3D structure. The coordinate node's point
     * field may be just "coordinate".
     *
     * @param name The name of the field that is to be added
     * @param value The value of the field
     */
    @Override
    void addFieldValue(String name, Object value) {
        if(name.equals("solid")) {
            if(value instanceof String) {
                solid = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                solid = ((Boolean)value);
            }
        } else if(name.equals("ccw")) {
            if(value instanceof String) {
                ccw = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                ccw = ((Boolean)value);
            }
        } else if(name.equals("normalPerVertex")) {
            if(value instanceof String) {
                normalPerVertex = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                normalPerVertex = ((Boolean)value);
            }
        } else if(name.equals("Coordinate")) {
            if(value instanceof String) {
                coordinates = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                coordinates = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                coordinates = (float[])value;
            }
            numCoordinates = coordinates.length / 3;
        } else if(name.equals("Normal")) {
            if(value instanceof String) {
                normals = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                normals = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                normals = (float[])value;
            }
            numNormals = normals.length / 3;
        } else if(name.equals("Color")) {
            if(value instanceof String) {
                colors = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                colors = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                colors = (float[])value;
            }
            numColors = colors.length / 3;
        } else if(name.equals("index")) {
            if(value instanceof String) {
                coordIndices = fieldReader.MFInt32((String)value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof String[]) {
                coordIndices = fieldReader.MFInt32((String[])value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof int[]) {
                coordIndices = (int[])value;
                numCoordIndex = coordIndices.length;
            }
        } else if(name.startsWith("TextureCoordinate")) {
            if(value instanceof String) {
                texCoords = fieldReader.MFVec2f((String)value);
            } else if(value instanceof String[]) {
                texCoords = fieldReader.MFVec2f((String[])value);
            } else if(value instanceof float[]) {
                texCoords = (float[])value;
            }
            numTexCoords = texCoords.length / 3;
        }
    }

    /**
     * Add a new field value to the geometry using array data. The
     * form of the value is  not defined and is up to the implementing
     * class to interpret it according to the needed fields.  The
     * array length is the number of valid items in the passed array.
     * <p>
     * Note that field names will be
     * compressed from the X3D structure. The coordinate node's point
     * field may be just "coordinate".
     *
     * @param name The name of the field that is to be added
     * @param value The value of the field
     * @param len The length of the valid data in the array
     */
    @Override
    void addFieldValue(String name, Object value, int len) {
        if(name.equals("Coordinate")) {
            if(value instanceof String[]) {
                coordinates = fieldReader.MFVec3f((String[])value);
                numCoordinates = coordinates.length / 3;
            } else if(value instanceof float[]) {
                coordinates = (float[])value;
                numCoordinates = len;
            }
        } else if(name.equals("Normal")) {
            if(value instanceof String[]) {
                normals = fieldReader.MFVec3f((String[])value);
                numNormals = normals.length / 3;
            } else if(value instanceof float[]) {
                normals = (float[])value;
                numNormals = len;
            }
        } else if(name.equals("Color")) {
            if(value instanceof String[]) {
                colors = fieldReader.MFVec3f((String[])value);
                numColors = colors.length / 3;
            } else if(value instanceof float[]) {
                colors = (float[])value;
                numColors = len;
            }
        } else if(name.equals("index")) {
            if(value instanceof String[]) {
                coordIndices = fieldReader.MFInt32((String[])value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof int[]) {
                coordIndices = (int[])value;
                numCoordIndex = len;
            }
        } else if(name.startsWith("TextureCoordinate")) {
            if(value instanceof String[]) {
                texCoords = fieldReader.MFVec2f((String[])value);
                numTexCoords = texCoords.length / 2;
            } else if(value instanceof float[]) {
                texCoords = (float[])value;
                numTexCoords = len;
            }
        }
    }

    /**
     * The geometry definition is now finished so take the given field
     * values and generate the triangle output.
     *
     *
     * @param ch The content handler instance to write to
     * @param sh The script handler instance to write to
     * @param ph The proto handler instance to write to
     * @param rh The route handler instance to write to
     */
    @Override
    void generateOutput(ContentHandler ch,
                        ScriptHandler sh,
                        ProtoHandler ph,
                        RouteHandler rh) {

        ch.startNode("IndexedTriangleSet", defName);

        if (coordinates != null) {
            ch.startField("coord");
            ch.startNode("Coordinate", null );
            ch.startField("point");

            if(ch instanceof BinaryContentHandler) {

                ((BinaryContentHandler)ch).fieldValue(coordinates, numCoordinates);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < numCoordinates*3; i++) {
                    buf.append(coordinates[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Coordinate
            ch.endField(); // coord
        }


        int[] output_indices = null;
        int output_idx = 0;
        int input_idx = 0;

        if(ccw) {
            output_indices = coordIndices;
            output_idx = numCoordIndex;
        } else {
            // Not CCW so reverse everything.
            int poly_count = numCoordIndex / 3;
            output_indices = new int[numCoordIndex];

            for(int i = 0; i < poly_count; i++) {
                output_indices[output_idx] = coordIndices[input_idx];
                output_indices[output_idx + 1] = coordIndices[input_idx + 2];
                output_indices[output_idx + 2] = coordIndices[input_idx + 1];

                output_idx += 3;
                input_idx += 3;
            }

        }

        if (output_indices != null) {
            ch.startField("index");

            if(ch instanceof BinaryContentHandler) {
                BinaryContentHandler bch = (BinaryContentHandler)ch;
                ((BinaryContentHandler)ch).fieldValue(output_indices, output_idx);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < output_idx; i++) {
                    buf.append(output_indices[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endField(); // index
        }

        if(normals != null) {
            ch.startField("normal");
            ch.startNode("Normal", null );
            ch.startField("vector");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(normals, numNormals);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < numNormals * 3; i++) {
                    buf.append(normals[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Normal
            ch.endField(); // normals
        }

        if(colors != null) {
            ch.startField("color");
            if(colorHasAlpha)
                ch.startNode("ColorRGBA", null );
            else
                ch.startNode("Color", null );

            ch.startField("color");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(colors, numColors);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < numColors * 3; i++) {
                    buf.append(colors[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Color[RGBA]
            ch.endField(); // color
        }

        if(texCoords != null) {
            ch.startField("texCoord");
            ch.startNode("TextureCoordinate", null );

            ch.startField("point");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(texCoords, numTexCoords);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < numTexCoords * 2; i++) {
                    buf.append(texCoords[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Color[RGBA]
            ch.endField(); // color
        }

        if(!normalPerVertex) {
            ch.startField("normalPerVertex");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(false);
            } else if(ch instanceof StringContentHandler) {

                ((StringContentHandler)ch).fieldValue("FALSE");
            }
        }

        if(!solid) {
            ch.startField("solid");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(solid);
            } else if(ch instanceof StringContentHandler) {

                ((StringContentHandler)ch).fieldValue("FALSE");
            }
        }

        ch.endNode();  // IndexedTriangleSet
    }

    /**
     * Set the def name of the geometry
     *
     * @param defName Set the def name of the geometry
     */
    @Override
    void setDefName(String defName) {
        this.defName = defName;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Convenience method to create a normal for the given vertex coordinates
     * and normal array. This performs a cross product of the two vectors
     * described by the middle and two end points.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the middle point
     * @param p1 The index of the first point
     * @param p2 The index of the second point
     * @param res A temporary value containing the normal value
     */
    private void createFaceNormal(float[] coords,
                                  int[] coordIndex,
                                  int start,
                                  int numVertex,
                                  float[] res) {

        // Uses the Newell method to calculate the face normal
        float nx = 0;
        float ny = 0;
        float nz = 0;
        int x1, y1, z1, x2, y2, z2;

        x1 = coordIndex[start] * 3;
        y1 = x1 + 1;
        z1 = x1 + 2;

        for(int i = 0; i < numVertex - 1; i++) {

            x2 = coordIndex[start + i + 1] * 3;
            y2 = x2 + 1;
            z2 = x2 + 2;

            nx += (coords[y1] - coords[y2]) * (coords[z1] + coords[z2]);
            ny += (coords[z1] - coords[z2]) * (coords[x1] + coords[x2]);
            nz += (coords[x1] - coords[x2]) * (coords[y1] + coords[y2]);

            x1 = x2;
            y1 = y2;
            z1 = z2;
        }

        // The last vertex uses the start position
        x2 = coordIndex[start] * 3;
        y2 = x2 + 1;
        z2 = x2 + 2;

        nx += (coords[y1] - coords[y2]) * (coords[z1] + coords[z2]);
        ny += (coords[z1] - coords[z2]) * (coords[x1] + coords[x2]);
        nz += (coords[x1] - coords[x2]) * (coords[y1] + coords[y2]);

        res[0] = nx;
        res[1] = ny;
        res[2] = nz;

        double len = nx * nx + ny * ny + nz * nz;
        if(len != 0) {
            len = (ccw ? 1 : -1) / Math.sqrt(len);
            res[0] *= len;
            res[1] *= len;
            res[2] *= len;
        }
    }
}

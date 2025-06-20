/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                          Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom;

// External imports
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.GeometryData;

// Local imports
import org.j3d.aviatrix3d.Appearance;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.Shape3D;
import org.j3d.aviatrix3d.TriangleStripArray;
import org.j3d.aviatrix3d.VertexGeometry;

/**
 * A simple box primitive.
 * <p>
 *
 * The geometry is only created with normal and coordinates. Texture
 * coordinates are not generated.
 * <p>
 *
 * As we assume you may want to use this as a collidable object, we store the
 * {@link GeometryData} instance that is used to create the object in the
 * userData of the underlying {@link org.j3d.aviatrix3d.TriangleStripArray}. The
 * geometry does not have texture coordinates set.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class Box extends Shape3D
    implements NodeUpdateListener
{
    /** The default dimension of the box */
    private static final float DEFAULT_SIZE = 2;

    /** The generator used to modify the geometry */
    private BoxGenerator generator;

    /** Data used to regenerate the box */
    private GeometryData data;

    /**
     * Construct a default box with no appearance set. The default size
     * of the box is:<BR>
     * Width: 2.0<BR>
     * Height: 2.0<BR>
     * Depth: 2.0<BR>
     */
    public Box()
    {
        this(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE, null);
    }

    /**
     * Construct a default box with the given appearance. The default size
     * of the box is:<BR>
     * Width: 2.0<BR>
     * Height: 2.0<BR>
     * Depth: 2.0<BR>
     *
     * @param app The appearance to use
     */
    public Box(Appearance app)
    {
        this(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE, app);
    }

    /**
     * Construct a default box with no appearance set. The dimensions
     * are set to the given values
     *
     * @param width The width of the box (X Axis)
     * @param height The height of the box (Y Axis)
     * @param depth The depth of the box (Z Axis)
     */
    public Box(float width, float height, float depth)
    {
        this(width, height, depth, null);
    }

    /**
     * Construct a default box with the given appearance and dimensions.
     *
     * @param width The width of the box (X Axis)
     * @param height The height of the box (Y Axis)
     * @param depth The depth of the box (Z Axis)
     * @param app The appearance to use
     */
    public Box(float width, float height, float depth, Appearance app)
    {
        data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator = new BoxGenerator(width, height, depth);
        generator.generate(data);

        TriangleStripArray geom = new TriangleStripArray();
        geom.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setStripCount(data.stripCounts, data.numStrips);
        geom.setNormals(data.normals);

        geom.setUserData(data);

        setAppearance(app);
        setGeometry(geom);
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src)
    {
        VertexGeometry geom = (VertexGeometry)src;
        geom.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src)
    {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Change the radius and height of the box to the new values. If the
     * geometry write capability has been turned off, this will not do
     * anything.
     *
     * @param width The width of the box (X Axis)
     * @param height The height of the box (Y Axis)
     * @param depth The depth of the box (Z Axis)
     */
    public void setDimensions(float width, float height, float depth)
    {
        generator.setDimensions(width, height, depth);
        generator.generate(data);

        VertexGeometry geom = (VertexGeometry)getGeometry();
        geom.boundsChanged(this);
    }
}

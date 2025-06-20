/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                           Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom;

// External imports
import org.j3d.geom.ConeGenerator;
import org.j3d.geom.GeometryData;

// Local imports
import org.j3d.aviatrix3d.Appearance;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.Shape3D;
import org.j3d.aviatrix3d.TriangleArray;
import org.j3d.aviatrix3d.VertexGeometry;

/**
 * A simple cone that uses triangles.
 * <p>
 *
 * As we assume you may want to use this as a collidable object, we store the
 * {@link GeometryData} instance that is used to create the object in the
 * userData of the underlying {@link org.j3d.aviatrix3d.TriangleArray}. The
 * geometry does not have texture coordinates set.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class Cone extends Shape3D
    implements NodeUpdateListener
{
    /** The default height of the cone */
    private static final float DEFAULT_HEIGHT = 2;

    /** The default radius of the cone */
    private static final float DEFAULT_RADIUS = 1;

    /** Default number of segments used in the cone */
    private static final int DEFAULT_FACETS = 16;

    /** The generator used to modify the geometry */
    private ConeGenerator generator;

    /** Data used to regenerate the cone */
    private GeometryData data;

    /**
     * Construct a default cone with no appearance set. The default size
     * of the cone is: <BR>
     * Height: 2.0<BR>
     * Radius: 1.0<BR>
     * Faces:  16
     */
    public Cone()
    {
        this(DEFAULT_HEIGHT, DEFAULT_RADIUS, DEFAULT_FACETS, null);
    }

    /**
     * Construct a default cone with the given appearance. The default size
     * of the cone is: <BR>
     * Height: 2.0<BR>
     * Radius: 1.0<BR>
     * Faces:  16
     *
     * @param app The appearance to use
     */
    public Cone(Appearance app)
    {
        this(DEFAULT_HEIGHT, DEFAULT_RADIUS, DEFAULT_FACETS, app);
    }

    /**
     * Construct a default cone with no appearance set and a custom
     * number of faces. <BR>
     * Height: 2.0<BR>
     * Radius: 1.0<BR>
     *
     * @param faces The number of faces to use around the side
     */
    public Cone(int faces)
    {
        this(DEFAULT_HEIGHT, DEFAULT_RADIUS, faces, null);
    }

    /**
     * Construct a default cone with no appearance set. The height and
     * radius as set to the new value and uses the default face count of
     * 16.
     *
     * @param height The height of the cone
     * @param radius The radius of the base of the cone
     */
    public Cone(float height, float radius)
    {
        this(height, radius, DEFAULT_FACETS, null);
    }

    /**
     * Construct a default cone with the given appearance and a custom
     * number of faces. <BR>
     * Height: 2.0<BR>
     * Radius: 1.0<BR>
     *
     * @param faces The number of faces to use around the side
     * @param app The appearance to use
     */
    public Cone(int faces, Appearance app)
    {
        this(DEFAULT_HEIGHT, DEFAULT_RADIUS, faces, app);
    }

    /**
     * Construct a default cone with the given appearance. The height and
     * radius as set to the new value and uses the default face count of
     * 16.
     *
     * @param height The height of the cone
     * @param radius The radius of the base of the cone
     * @param app The appearance to use
     */
    public Cone(float height, float radius, Appearance app)
    {
        this(height, radius, DEFAULT_FACETS, app);
    }

    /**
     * Construct a cone with all the values customisable
     *
     * @param height The height of the cone
     * @param radius The radius of the base of the cone
     * @param faces The number of faces to use around the side
     * @param app The appearance to use
     */
    public Cone(float height, float radius, int faces, Appearance app)
    {
        data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator = new ConeGenerator(height, radius, faces);
        generator.generate(data);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
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
        generator.generate(data);

        VertexGeometry geom = (VertexGeometry)src;
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);
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
     * Change the radius and height of the cone to the new values. If the
     * geometry write capability has been turned off, this will not do
     * anything.
     *
     * @param height The height of the cone
     * @param radius The radius of the base of the cone
     */
    public void setDimensions(float height, float radius)
    {
        generator.setDimensions(height, radius, true);

        VertexGeometry geom = (VertexGeometry)getGeometry();
        geom.boundsChanged(this);
    }

    /**
     * Set the facet count of the cone to the new value. If the geometry
     * write capability has been turned off, this will not do anything.
     *
     * @param faces The number of faces to use around the side
     */
    public void setFacetCount(int faces)
    {
        generator.setFacetCount(faces);

        VertexGeometry geom = (VertexGeometry)getGeometry();
        geom.boundsChanged(this);
    }
}

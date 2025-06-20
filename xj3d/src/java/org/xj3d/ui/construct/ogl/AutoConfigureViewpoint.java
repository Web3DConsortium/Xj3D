/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.construct.ogl;

// External Imports
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.j3d.aviatrix3d.BoundingBox;
import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.SimpleLayer;
import org.j3d.aviatrix3d.SimpleScene;
import org.j3d.aviatrix3d.SimpleViewport;

import org.j3d.aviatrix3d.management.DisplayCollection;

import org.j3d.aviatrix3d.rendering.BoundingVolume;

import org.j3d.util.ErrorReporter;
import org.j3d.util.MatrixUtils;

// Local Imports
import org.web3d.vrml.lang.VRMLNodeFactory;

import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;

import org.web3d.vrml.nodes.FrameStateListener;
import org.web3d.vrml.nodes.FrameStateManager;

import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

import org.web3d.vrml.renderer.ogl.nodes.navigation.OGLNavigationInfo;
import org.web3d.vrml.renderer.ogl.nodes.navigation.OGLViewpoint;

/**
 * Utility module for the ThumbnailRecorder that will attempt to create
 * a useful default viewpoint of the loaded scene. The generated viewpoint
 * will be along the vector [1, 1, 1] from the center of the scene bounds,
 * at a distance that -should- have the object(s) in the field of view.
 *
 * @author Rex Melton
 * @version $Revision: 1.10 $
 */
public class AutoConfigureViewpoint implements FrameStateListener {

    /** The logging identifier of this class */
    private static final String LOG_NAME = "AutoConfigureViewpoint";

    /** The construct */
    protected OGLConstruct construct;

    /** Should the headlight be on */
    protected boolean headlight;

    /** The browser core */
    protected OGLStandardBrowserCore core;

    /** The frame state manager */
    protected FrameStateManager fsm;

    /** The error reporting mechanism */
    protected ErrorReporter errorReporter;

    /** Synchronization flag */
    protected boolean configComplete;

    /** The viewpoint */
    protected OGLViewpoint viewpoint;

    /** The navigation info */
    protected OGLNavigationInfo navInfo;

    /** The scene root, parent for the nav info and viewpoint nodes */
    protected VRMLWorldRootNodeType root;

    /** Flag used in the end of frame listener, indicating that the new nodes
    * may be added to the scene */
    protected boolean addNodes;

    /**
     * Constructor
     *
     * @param construct The construct containing the scene for which to configure a viewpoint.
     * @param headlight Should the headlight be on
     */
    public AutoConfigureViewpoint( OGLConstruct construct, boolean headlight ) {
        this.construct = construct;
	this.headlight = headlight;
        core = construct.getBrowserCore( );
        fsm = construct.getFrameStateManager( );
        errorReporter = construct.getErrorReporter( );
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    @Override
    public void allEventsComplete( ) {
        if ( addNodes ) {
            // add and bind the new viewpoint and nav info
            root.addChild( navInfo );
            int index = navInfo.getFieldIndex( "set_bind" );
            navInfo.setValue( index, true );
            navInfo.setupFinished( );

            root.addChild( viewpoint );
            index = viewpoint.getFieldIndex( "set_bind" );
            viewpoint.setValue( index, true );
            viewpoint.setupFinished( );

            addNodes = false;
            fsm.addEndOfThisFrameListener( this );

        } else {
            synchronized ( this ) {
                    configComplete = true;
                    notify( );
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Create and configure a new viewpoint. Wait for the node to be
     * added and bound in the scene before returning. If the bounds of
     * the scene could not be determined, no configuration is performed
     * and the method returns immediately.
     *
     * @return true if the viewpoint was added successfully, false otherwise.
     */
    public boolean configure( ) {

        // bounding box size
        float[] size = new float[3];
        // the extents
        float[] max = new float[3];
        float[] min = new float[3];
        BoundingBox bounds = getWorldBounds( );
        if ( bounds == null ) {
            errorReporter.errorReport(
                    LOG_NAME + ": (warning) Unable to determine scene bounds, possibly no Shape geometry is defined. "
                    + "No Viewpoint configured.", null);
            // original exception: "Error: AutoConfigureViewpoint: Could not determine scene bounds. No Viewpoint configured."
            // test scene C:\x3d-code\www.web3d.org\x3d\content\examples\Basic/UniversalMediaPanoramas/desert2.x3d
            min = new float[]{-10.0f, -10.0f, -10.0f}; // arbitrary bounds, likely for scene with no geometry
            max = new float[]{10.0f, 10.0f, 10.0f};
        } else {
            bounds.getSize(size);
            bounds.getExtents(min, max);
        }

        // roughly, the model center
        float x_center = ( min[0] + max[0] ) / 2;
        float y_center = ( min[1] + max[1] ) / 2;
        float z_center = ( min[2] + max[2] ) / 2;

        float[] center = new float[]{ x_center, y_center, z_center };

        // the extents, by half
        float x = size[0];
        float y = size[1];
        float z = size[2];

        // radius of the bounding sphere
        float radius = (float)Math.sqrt((x * x) + (y * y) + (z * z));

        // given a field-of-view of 45 degrees - the distance from
        // the center of the bounding sphere at which the sphere
        // surface should be within the f-o-v (0.392699 radians == 22.5 degrees)
        // note, the sin gives the distance to the sphere's visible edge,
        // a tan would give the distance to the center. the edge distance
        // is greater and moves the position a bit farther away - providing
        // some margin between the actual f-o-v and the bounding sphere
        float distance = radius / (float)(Math.sin(0.392699));

        // equidistant components of the distance to combine with
        // the object center.
        float offset = ( distance / (float)Math.sqrt(3.0) );

        float near_clip = 0;

        // if the distance away is less than the clipping plane
        // then move the clipping plane
        if (offset < 0.125f) {
            near_clip = offset * 0.9f;
        }

        float[] position = new float[]{
                x_center + offset, y_center + offset, z_center + offset};

        ///////////////////////////////////////////////////////////////////////////////
        // calculate an orientation that places the model at the focus

        Point3f from = new Point3f(position);
        Point3f to = new Point3f(center);
        Vector3f Yup = new Vector3f(0, 1, 0);

        MatrixUtils mu = new MatrixUtils();
        Matrix4f rot = new Matrix4f();
        mu.lookAt(from, to, Yup, rot);
        mu.inverse(rot, rot);

        AxisAngle4f a = new AxisAngle4f();
        a.set(rot);
        float[] orientation = new float[4];
        a.get(orientation);

        ///////////////////////////////////////////////////////////////////////////////

        VRMLScene scene = core.getScene( );
        VRMLNodeFactory factory = scene.getNodeFactory( );
        viewpoint = (OGLViewpoint)factory.createVRMLNode( "Viewpoint", false );

        int index = viewpoint.getFieldIndex( "set_position" );
        viewpoint.setValue( index, position, 3 );

        index = viewpoint.getFieldIndex( "set_orientation" );
        viewpoint.setValue( index, orientation, 4 );

        navInfo = (OGLNavigationInfo)factory.createVRMLNode( "NavigationInfo", false );
        navInfo.setType(new String[0], 0);

        navInfo.setHeadlight( headlight );
        if (near_clip > 0) {
            navInfo.setAvatarSize(new float[] { near_clip * 2, 1.6f, 0.75f }, 3);
        }

        root = (VRMLWorldRootNodeType)scene.getRootNode( );
        addNodes = true;

        // wait for the new nodes to be added to the scene and bound before returning.
        synchronized ( this ) {
            fsm.addEndOfThisFrameListener( this );
            configComplete = false;
            while( !configComplete ) {
                try {
                    wait( );
                } catch ( InterruptedException ie ) {
                }
            }
        }
        return true;
    }

    /**
     * Return the bounds of the 3D world
     *
     * @return the bounds of the 3D world
     */
    private BoundingBox getWorldBounds( ) {

        DisplayCollection displayManager = construct.getDisplayCollection( );
        Layer[] layers = new Layer[displayManager.numLayers()];
        displayManager.getLayers(layers);
        SimpleScene currentScene;
        SimpleViewport viewport;
        SimpleLayer layer;
        Group rootNode;
        BoundingVolume bounds;

        for (Layer layer1 : layers) {
            if (!(layer1 instanceof SimpleLayer)) {
                continue;
            }
            layer = (SimpleLayer) layer1;
            if (!(layer.getViewport() instanceof SimpleViewport)) {
                continue;
            }
            viewport = (SimpleViewport)layer.getViewport();
            if (!(viewport.getScene() instanceof SimpleScene)) {
                continue;
            }
            currentScene = viewport.getScene();
            rootNode = currentScene.getRenderedGeometry();
            bounds = rootNode.getBounds();
            if ( bounds instanceof BoundingBox ) {
                return (BoundingBox) bounds;
            }
        }
        return null;
    }
}

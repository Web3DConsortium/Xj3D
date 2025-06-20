
// External imports
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;


// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;

/**
 * Animator for moving an object about a circular path for the MultiLayerDemo.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ResizeLayerUpdater
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Work variable to update the translation with */
    private Vector3f translation;

    /** Matrix used to update the transform */
    private Matrix4f matrix;

    /** The scene graph node to update */
    private TransformGroup transform;

    /** The current angle */
    private float angle;

    /** Manager for handling resizing updates */
    private ViewportResizeManager resizeManager;

    /**
     *
     */
    public ResizeLayerUpdater(TransformGroup tx, ViewportResizeManager resizer)
    {
        resizeManager = resizer;
        translation = new Vector3f();
        matrix = new Matrix4f();
        matrix.setIdentity();
        transform = tx;
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    @Override
    public void updateSceneGraph()
    {
        resizeManager.sendResizeUpdates();
        transform.boundsChanged(this);
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    @Override
    public void appShutdown()
    {
        // do nothing
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
        angle += Math.PI / 1000;

        float z = 0.5f * (float)Math.sin(angle);
        float y = 0.5f * (float)Math.cos(angle);

//        translation.x = x;
        translation.y = y;
        translation.z = z;

        matrix.setTranslation(translation);

        transform.setTransform(matrix);
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
}

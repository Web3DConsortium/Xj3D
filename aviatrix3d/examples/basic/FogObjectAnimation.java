
// Standard imports
import javax.vecmath.Matrix4f;

// Application Specific imports
import org.j3d.aviatrix3d.*;

/**
 * Simple animator to move the transform in a rotation about the Y axis.
 * Used to illustrate fog effects by moving objects into and out of the
 * fog range.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class FogObjectAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Matrix used to update the transform */
    private Matrix4f matrix;

    /** The scene graph node to update */
    private TransformGroup transform;

    /** The current angle */
    private float angle;

    /**
     *
     * @param tx
     */
    public FogObjectAnimation(TransformGroup tx)
    {
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
    // Methods required by the UpdateListener interface.
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
        angle += Math.PI / 500;

        matrix.rotY(angle);

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

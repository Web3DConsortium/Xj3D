
// Standard imports
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import java.awt.*;
import java.awt.event.*;

import javax.swing.SwingUtilities;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.j3d.util.I18nManager;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class AnimationDemo extends Frame
    implements WindowListener
{
    /** App name to register preferences under */
    private static final String APP_NAME = "AnimationDemo";

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public AnimationDemo()
    {
        super(APP_NAME);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.av3dResources");

        setLayout(new BorderLayout());
        addWindowListener(AnimationDemo.this);

        setupAviatrix();
        setupSceneGraph();

        setSize(600, 600);
        setLocation(40, 40);

        Runnable r = () -> {
            setVisible(true);
        };
        SwingUtilities.invokeLater(r);
    }

    /**
     * Setup the aviatrix pipeline here
     */
    private void setupAviatrix()
    {
        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GraphicsCullStage culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new NullSortStage();
//        surface = new SimpleAWTSurface(caps);
        surface = new DebugAWTSurface(caps, true); // try lightweight
        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.setMinimumFrameInterval(10);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
        // View group

        Viewpoint vp = new Viewpoint();

        Vector3f trans = new Vector3f(0, 0, 1);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        geom.setColors(false, color);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(0.5f, 0, 0);
        Matrix4f mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        scene_root.addChild(shape_transform);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

        RotationAnimation2 anim = new RotationAnimation2(shape_transform);
        sceneManager.setApplicationObserver(anim);
        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    /**
     * Ignored
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, start everything up.
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.addDisplay(displayManager);
//        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        new AnimationDemo();
    }
}

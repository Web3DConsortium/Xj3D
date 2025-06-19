
// Standard imports
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.exit;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GLCapabilities;
import static com.jogamp.opengl.GLProfile.getDefault;


import static javax.swing.SwingUtilities.invokeLater;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.util.I18nManager;


/**
 * Example application that demonstrates how to make use of the explicit
 * rendering call renderOnce().
 *
 * The demo puts up a frame and only repaints it each time it gets iconified.
 * Each time it repaints, it first changes the colour of the object then
 * repaints.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class UserControlledRenderingDemo extends Frame
    implements WindowListener, NodeUpdateListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Where we are going to change the material colour */
    private Material material;

    /** Which iteration are we up to */
    private int colourIteration;

    /** Name of the application that we'll default to */
    private static final String APP_NAME = "renderOnce() Demo";

    /** The location of the resource property bundle in the classpath */
    private static final String CONFIG_FILE = "config.i18n.av3dResources";

    public UserControlledRenderingDemo()
    {
        super(APP_NAME);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, CONFIG_FILE);

        setLayout(new BorderLayout());
        addWindowListener(UserControlledRenderingDemo.this);

        setupAviatrix();
        setupSceneGraph();

        setSize(600, 600);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);
    }

    /**
     * Setup the aviatrix pipeline here
     */
    private void setupAviatrix()
    {
        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities(getDefault());
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GraphicsCullStage culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new NullSortStage();
        surface = new SimpleAWTSurface(caps);
        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(displayManager);
        sceneManager.setMinimumFrameInterval(100);

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

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);

        material = new Material();
        material.setEmissiveColor(new float[] { 0, 0, 1 });

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans.set(0.2f, 0.5f, 0);
        Matrix4f mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(new Vector3f());

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
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * When the window is activated, start everything up.
     */
    @Override
    public void windowActivated(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
    }

    /**
     * Ignored
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
        exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent evt)
    {
        if (sceneManager.isEnabled()) {
            material.dataChanged(this);
            sceneManager.setEnabled(false);
            sceneManager.renderOnce();
        }
    }

    @Override
    public void windowDeiconified(WindowEvent evt)
    {
        if (sceneManager.isEnabled()) {
            material.dataChanged(this);
            sceneManager.setEnabled(false);
            sceneManager.renderOnce();
        }
    }

    /**
     * Ignored
     */
    @Override
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, make the first change.
     */
    @Override
    public void windowOpened(WindowEvent evt)
    {
        material.dataChanged(this);
        sceneManager.setEnabled(false);
        sceneManager.renderOnce();
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
        colourIteration = ++colourIteration % 7;

        switch(colourIteration)
        {
            case 0:
                material.setEmissiveColor(new float[] { 0, 0, 1 });
                break;

            case 1:
                material.setEmissiveColor(new float[] { 0, 1, 0 });
                break;

            case 2:
                material.setEmissiveColor(new float[] { 0, 1, 1 });
                break;

            case 3:
                material.setEmissiveColor(new float[] { 1, 0, 0 });
                break;

            case 4:
                material.setEmissiveColor(new float[] { 1, 0, 1 });
                break;

            case 5:
                material.setEmissiveColor(new float[] { 1, 1, 0 });
                break;

            case 6:
                material.setEmissiveColor(new float[] { 1, 1, 1 });
                break;
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                new UserControlledRenderingDemo();
            }
        };
        invokeLater(r);
    }
}

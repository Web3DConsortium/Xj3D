
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
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.*;

/**
 * Example application that demonstrates having two surfaces sharing
 * GL contexts.
 *
 * Unfortunately the JOGL RI is severely bugged and it won't run currently
 * on windows. It crashes with a wglShareLists exception.
 *
 * To test on Windows change sceneManager to a SingleThreadRenderManager
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class MultiViewDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private RenderManager sceneManager;

    private SimpleAWTSurface main_surface;

    /** Manager for the layers on surface 1 */
    private SingleDisplayCollection displayManager1;

    private SimpleAWTSurface slave_surface;

    /** Manager for the layers on surface 2 */
    private SingleDisplayCollection displayManager2;

    public MultiViewDemo()
    {
        super("MultiView Aviatrix Demo");

        setLayout(new GridLayout(1, 2));
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(800, 800);
        setLocation(40, 40);

        Runnable r = new Runnable() {

            @Override
            public void run() {

                // Need to set visible first before starting the rendering thread due
                // to a bug in JOGL. See JOGL Issue #54 for more information on this.
                // http://jogl.dev.java.net
                setVisible(true);
            }
        };
        invokeLater(r);
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

        main_surface = new SimpleAWTSurface(caps);
        DefaultGraphicsPipeline pipeline1 = new DefaultGraphicsPipeline();

        GraphicsCullStage culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new NullSortStage();

        pipeline1.setCuller(culler);
        pipeline1.setSorter(sorter);
        pipeline1.setGraphicsOutputDevice(main_surface);

        slave_surface = new SimpleAWTSurface(caps, main_surface);
        DefaultGraphicsPipeline pipeline2 = new DefaultGraphicsPipeline();

        culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);
        sorter = new NullSortStage();

        pipeline2.setCuller(culler);
        pipeline2.setSorter(sorter);
        pipeline2.setGraphicsOutputDevice(slave_surface);

        displayManager1 = new SingleDisplayCollection();
        displayManager1.addPipeline(pipeline1);

        displayManager2 = new SingleDisplayCollection();
        displayManager2.addPipeline(pipeline2);

        // Render manager
        sceneManager = new MultiThreadRenderManager();
        //sceneManager = new SingleThreadRenderManager();

        sceneManager.addDisplay(displayManager1);
        sceneManager.addDisplay(displayManager2);
        sceneManager.setMinimumFrameInterval(100);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component main_comp = (Component)main_surface.getSurfaceObject();
        Component slave_comp = (Component)slave_surface.getSurfaceObject();

        add(main_comp);
        add(slave_comp);
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

        trans.set(0.2f, 0.5f, 0);
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
        view.setDimensions(0, 0, 400, 400);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager1.setLayers(layers, 1);
        displayManager2.setLayers(layers, 1);
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     */
    @Override
    public void windowActivated(WindowEvent evt)
    {
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

    /**
     * Ignored
     */
    @Override
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    @Override
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    @Override
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, start everything up.
     */
    @Override
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        new MultiViewDemo();
    }
}

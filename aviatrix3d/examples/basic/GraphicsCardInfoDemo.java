
// External imports
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.exit;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GLCapabilities;
import static com.jogamp.opengl.GLProfile.getDefault;


import static javax.swing.SwingUtilities.invokeLater;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.OutputDevice;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;


/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class GraphicsCardInfoDemo extends Frame
    implements WindowListener, SurfaceInfoListener
{
    /** App name to register preferences under */
    private static final String APP_NAME = "examples.GraphicsCardInfoDemo";

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public GraphicsCardInfoDemo()
    {
        super(APP_NAME);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.av3dResources");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(500, 500);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);
    }

    //---------------------------------------------------------------
    // Methods defined by SurfaceInfoListener
    //---------------------------------------------------------------

    /**
     * Notification that the graphics output device has changed GL context
     * and this is the collection of new information.
     *
     * @param surface The output surface that caused the new info
     * @param info The collected set of information known
     */
    @Override
    public void surfaceInfoChanged(OutputDevice surface, SurfaceInfo info)
    {
        System.out.println("Graphics card info");
        System.out.println("GL Version " + info.getGLMajorVersion() +
                           "." + info.getGLMinorVersion());
        System.out.println("Vendor: " + info.getVendorString());
        System.out.println("Driver: " + info.getDriverInfo());
        System.out.println("Shader Version " + info.getShaderMajorVersion() +
                           "." + info.getShaderMinorVersion());
        System.out.println("Max lights:      " + info.getMaxLightCount());
        System.out.println("Max clip planes: " + info.getMaxClipPlanesCount());
        System.out.println("Max textures:    " + info.getMaxTextureCount());
        System.out.println("Max RTs:         " + info.getMaxMRTCount());
        System.out.println("Max Color Atch:  " + info.getMaxColorAttachmentsCount());

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
        surface = new DebugAWTSurface(caps);
        surface.addSurfaceInfoListener(this);
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
        //float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        //geom.setColors(false, color);

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
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    public static void main(String[] args)
    {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                new GraphicsCardInfoDemo();
            }
        };
        invokeLater(r);
    }
}

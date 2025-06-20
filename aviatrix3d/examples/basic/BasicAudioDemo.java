
// Standard imports
import com.jogamp.openal.util.ALut;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import java.awt.*;
import java.awt.event.*;

import java.nio.ByteBuffer;

import javax.swing.SwingUtilities;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.j3d.util.I18nManager;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.output.audio.OpenALAudioDevice;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.audio.AudioCullStage;
import org.j3d.aviatrix3d.pipeline.audio.AudioOutputDevice;
import org.j3d.aviatrix3d.pipeline.audio.AudioSortStage;
import org.j3d.aviatrix3d.pipeline.audio.DefaultAudioPipeline;
import org.j3d.aviatrix3d.pipeline.audio.NullAudioCullStage;
import org.j3d.aviatrix3d.pipeline.audio.NullAudioSortStage;
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
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public class BasicAudioDemo extends Frame
    implements WindowListener, ApplicationUpdateObserver, NodeUpdateListener
{
    /** Name of the application that we'll default to */
    private static final String APP_NAME = "Basic Aviatrix Audio Demo";

    /** The location of the resource property bundle in the classpath */
    private static final String CONFIG_FILE = "config.i18n.av3dResources";

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Our Audio Device */
    private AudioOutputDevice audioDevice;

    /** Work variable to update the translation with */
    private Vector3f translation;

    /** Matrix used to update the transform */
    private Matrix4f matrix;

    private TransformGroup shape_transform;

    /** The current angle */
    private float angle;

    /** Sound object */
    private Sound sound;

    public BasicAudioDemo()
    {
        super(APP_NAME);

        // Initialize the I18nManager
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, CONFIG_FILE);

        setLayout(new BorderLayout());
        addWindowListener(BasicAudioDemo.this);

        setupAviatrix();
        setupSceneGraph();

        setSize(600, 600);
        setLocation(40, 40);

        Runnable r = () -> {
            // Need to set visible first before starting the rendering thread due
            // to a bug in JOGL. See JOGL Issue #54 for more information on this.
            // http://jogl.dev.java.net
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

        audioDevice = new OpenALAudioDevice();

        AudioCullStage audioCuller = new NullAudioCullStage();
        AudioSortStage audioSorter = new NullAudioSortStage();

        DefaultAudioPipeline audioPipeline = new DefaultAudioPipeline();
        audioPipeline.setCuller(audioCuller);
        audioPipeline.setSorter(audioSorter);
        audioPipeline.setAudioOutputDevice(audioDevice);

        displayManager.addPipeline(audioPipeline);

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
        float[] coord = { 0, 0, 0, 0.25f, 0, 0, 0, 0.25f, 0 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        geom.setColors(false, color);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(0f, 0f, -0.1f);
        Matrix4f mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        sound = new ConeSound();
        loadWav("wavdata/Footsteps.wav", sound);

        sound.setEnabled(true);
        sound.startSound();
        shape_transform.addChild(sound);

        scene_root.addChild(shape_transform);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.makeActiveSoundLayer();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

        translation = new Vector3f();
        matrix = new Matrix4f();
        matrix.setIdentity();

        sceneManager.setApplicationObserver(this);
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
        this.requestFocus();
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
        dispose();
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
        sceneManager.setEnabled(true);
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
        shape_transform.boundsChanged(this);
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
        angle += Math.PI / 100;

        translation.z -= angle/4.0f;

        matrix.setTranslation(translation);

        shape_transform.setTransform(matrix);
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

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Load a Wav file into a sound node.
     */
    private AudioComponent loadWav(String filename, Sound snode)
    {
        int[] format = new int[1];
        int[] size = new int[1];
        ByteBuffer[] data = new ByteBuffer[1];
        int[] freq = new int[1];
        int[] loop = new int[1];

        ALut.alutLoadWAVFile(
            filename,
            format,
            data,
            size,
            freq,
            loop);

        AudioComponent audioComp =
            new ByteAudioComponent(format[0], freq[0], true, data[0]);
        snode.setAudioSource(audioComp);

        return audioComp;
    }

    public static void main(String[] args)
    {
        new BasicAudioDemo();
    }
}

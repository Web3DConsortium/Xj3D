
// External imports

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import javax.swing.SwingUtilities;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

import org.j3d.renderer.aviatrix3d.loader.AVModel;
import org.j3d.renderer.aviatrix3d.loader.AVLoader;
import org.j3d.renderer.aviatrix3d.loader.discreet.MaxLoader;
import org.j3d.renderer.aviatrix3d.texture.TextureCreateUtils;

/**
 * Example application that demonstrates how to use the loader interface
 * to load a file into the scene graph.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class BasicLoaderDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Utility for munging textures to power of 2 size */
    private TextureCreateUtils textureUtils;

    public BasicLoaderDemo()
    {
        super("Basic Aviatrix Loader Demo");

        setLayout(new BorderLayout());
        addWindowListener(BasicLoaderDemo.this);

        setupAviatrix();

        setSize(600, 600);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);
        surface.setClearColor(0.2f, 0.2f, 0.2f, 1);

        textureUtils = new TextureCreateUtils();
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
        surface = new DebugAWTSurface(caps);
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
     * Load the requested file and add it to the scene graph.
     *
     * @param filename Name of the file to load
     */
    private void load(String filename)
    {
        try
        {
            File file = new File(filename);
            AVLoader loader = new MaxLoader();
            AVModel model = loader.load(file);

            SimpleScene scene = setupSceneGraph(model.getModelRoot());

            // Now go off and load textures.
            Map externals = model.getExternallyDefinedFiles();
            if(!externals.isEmpty())
            {
                File parent_dir = file.getParentFile();

                System.out.println("Have " + externals.size() + " files to load");
                Set entries = externals.entrySet();
                Iterator itr = entries.iterator();

                while(itr.hasNext())
                {
                    Map.Entry e = (Map.Entry)itr.next();

                    // Check what the value is. It could be either String or
                    // String[]. The later we aren't dealing with here.
                    if(!(e.getValue() instanceof String))
                        continue;

                    String tex_str = (String)e.getValue();

                    // Assume a texture object right now as that's all we
                    // care about in .3ds files.
                    Texture2D texture = (Texture2D)e.getKey();
                    boolean is_bump = texture.getUserData().equals("BUMP_MAP");

                    File tex_file = new File(parent_dir, tex_str);

                    TextureComponent2D[] tex_comp = {
                        loadImage(tex_file.getCanonicalPath(), is_bump) };

                    if(tex_comp[0] == null)
                        continue;

                    int format = Texture.FORMAT_RGB;

                    switch(tex_comp[0].getFormat(0))
                    {
                        case TextureComponent.FORMAT_RGBA:
                            format = Texture.FORMAT_RGBA;
                            break;

                        case TextureComponent.FORMAT_INTENSITY_ALPHA:
                            format = Texture.FORMAT_INTENSITY_ALPHA;
                            break;

                        case TextureComponent.FORMAT_SINGLE_COMPONENT:
                            if(texture.getUserData().equals("OPACITY_MAP"))
                                format = Texture.FORMAT_ALPHA;
                            else
                                format = Texture.FORMAT_INTENSITY;
                            break;

                    }

                    texture.setSources(Texture.MODE_BASE_LEVEL,
                                      format,
                                      tex_comp,
                                      1);
                }
            }

            // Then the basic layer and viewport at the top:
            SimpleViewport view = new SimpleViewport();
            view.setDimensions(0, 0, 500, 500);
            view.setScene(scene);

            SimpleLayer layer = new SimpleLayer();
            layer.setViewport(view);

            Layer[] layers = { layer };
            displayManager.setLayers(layers, 1);
            sceneManager.setEnabled(true);
        }
        catch(IOException ioe)
        {
            System.out.println("IO Error reading file" + ioe);
        }
    }


    /**
     * Setup the basic scene which is a viewpoint along with the model that
     * was loaded.
     */
    private SimpleScene setupSceneGraph(Group loadedScene)
    {
        // View group

        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(true);

        Vector3f trans = new Vector3f(0, 0, 3);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Create our loader now and read in the given file
        loadedScene.requestBoundsUpdate();
        BoundingVolume bd = loadedScene.getBounds();

        // Scale the scene so that it fits in a 1 unit box in front of the
        // camera.
        float[] center = new float[3];
        float[] min_ext = new float[3];
        float[] max_ext = new float[3];

        bd.getCenter(center);
        bd.getExtents(min_ext, max_ext);

        float max = max_ext[0] - min_ext[0];
        if(Math.abs(max_ext[1] - min_ext[1]) > max)
            max = Math.abs(max_ext[1] - min_ext[1]);
        if(Math.abs(max_ext[2] - min_ext[2]) > max)
            max = Math.abs(max_ext[2] - min_ext[2]);

        trans.set(center[0] / max, center[1] / max, center[2] / max);

        System.out.println("Original model bounds " + bd);
        System.out.println("Scaling by  " + (1 / max));

        mat.setIdentity();
        mat.setScale(1 / max);
        mat.setTranslation(trans);

        TransformGroup tg = new TransformGroup();
        tg.setTransform(mat);
        tg.addChild(loadedScene);

        // A separate TG to rotate the model with as the setRot method
        // on the matrix trashes the rest of the matrix including scale and
        // translation.
        TransformGroup rot_group = new TransformGroup();
        rot_group.addChild(tg);

        scene_root.addChild(rot_group);

        // Add some lights to help illuminate the model
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(0, -1, -1);
        dl.setEnabled(true);
        dl.setAmbientColor(new float[] { 0.5f, 0.5f, 0.5f });
        dl.setDiffuseColor(new float[] { 0.5f, 0.5f, 0.5f });
        scene_root.addChild(dl);


        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        ModelRotationAnimation2 anim = new ModelRotationAnimation2(rot_group);
        sceneManager.setApplicationObserver(anim);

        return scene;
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    @Override
    public void windowActivated(WindowEvent evt)
    {
    }

    @Override
    public void windowClosed(WindowEvent evt)
    {
    }

    @Override
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent evt)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent evt)
    {
    }

    @Override
    public void windowIconified(WindowEvent evt)
    {
    }

    @Override
    public void windowOpened(WindowEvent evt)
    {
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Load a single image
     */
    private TextureComponent2D loadImage(String name, boolean bumpMap)
    {
        TextureComponent2D comp = null;

        System.out.println("Loading external file: " + name);
        try
        {
            File f = new File(name);
            if(!f.exists())
                System.out.println("Can't find texture source file");

            FileInputStream is = new FileInputStream(f);

            BufferedInputStream stream = new BufferedInputStream(is);
            BufferedImage img = ImageIO.read(stream);

            if(img == null)
                return null;

            // Max likes to use non-power of 2 textures. Rescale them if that
            // is what we find here.
            if(bumpMap)
            {
                img = textureUtils.createNormalMap(img, null);
            }

            int img_width = img.getWidth(null);
            int img_height = img.getHeight(null);

            int tex_width = textureUtils.nearestPowerTwo(img_width, true);
            int tex_height = textureUtils.nearestPowerTwo(img_height, true);

            if(tex_width != img_width || tex_height != img_height)
            {
                System.out.println("Rescaling image to " + tex_width +
                                   "x" + tex_height);
                img = (BufferedImage)textureUtils.scaleTexture(img,
                                                               tex_width,
                                                               tex_height);
            }

            int format = TextureComponent.FORMAT_RGB;
            ColorModel cm = img.getColorModel();
            boolean alpha = cm.hasAlpha();

            switch(img.getType())
            {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_BYTE_BINARY:
                case BufferedImage.TYPE_INT_BGR:
                case BufferedImage.TYPE_INT_RGB:
                case BufferedImage.TYPE_USHORT_555_RGB:
                case BufferedImage.TYPE_USHORT_565_RGB:
                    format = TextureComponent.FORMAT_RGB;
                    break;

                case BufferedImage.TYPE_CUSTOM:
                    // no idea what this should be, so default to RGBA
                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_ARGB_PRE:
                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                    format = TextureComponent.FORMAT_RGBA;
                    break;

                case BufferedImage.TYPE_BYTE_GRAY:
                case BufferedImage.TYPE_USHORT_GRAY:
                    format = TextureComponent.FORMAT_SINGLE_COMPONENT;
                    break;

                case BufferedImage.TYPE_BYTE_INDEXED:
                    if(alpha)
                        format = TextureComponent.FORMAT_RGBA;
                    else
                        format = TextureComponent.FORMAT_RGB;
                    break;
            }

            comp = new ImageTextureComponent2D(format,
                                               tex_width,
                                               tex_height,
                                               img);
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
        }

        return comp;
    }

    public static void main(final String[] args)
    {
        Runnable r = new Runnable() {

            @Override
            public void run() {

                // Hmm, what to load here
                new BasicLoaderDemo().load(args[0]); // throws java.lang.ArrayIndexOutOfBoundsException if no content provided
            }
        };
        SwingUtilities.invokeLater(r);
    }
}

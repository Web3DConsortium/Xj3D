
// Standard imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GLCapabilities;
import static com.jogamp.opengl.GLProfile.getDefault;



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

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;

/**
 * Example application that demonstrates how to put together a simple multitextured
 * objects using just two texture units. Should run on any hardware.
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class Dot3MultiTextureDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public Dot3MultiTextureDemo()
    {
        super("Aviatrix Dot3 Bump Map Demo");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(600, 600);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);
        
        surface.enableTwoPassTransparentRendering(true);
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
        // Load the texture image
        TextureComponent2D[] base_img = loadImage("textures/bump_map.jpg");
        TextureComponent2D[] filter_img = loadImage("textures/colour_map.jpg");

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3f trans = new Vector3f(0, 0.1f, 0.8f);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA|
                                  GeometryData.TEXTURE_2D_DATA;

        BoxGenerator generator = new BoxGenerator(0.2f, 0.2f, 0.2f);
        generator.generate(data);

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };
        float[][] tex_coord = new float[1][data.vertexCount * 2];
        int[] tex_sets = { 0, 0 };

        System.arraycopy(data.textureCoordinates, 0, tex_coord[0], 0,
                         data.vertexCount * 2);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
//        geom.setNormals(data.normals);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);
        geom.setTextureSetMap(tex_sets);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 1, 1, 1 });
        material.setEmissiveColor(new float[] { 1, 1, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });
        material.setLightingEnabled(true);

        Texture2D base_texture = new Texture2D();
        base_texture.setSources(Texture.MODE_BASE_LEVEL,
                               Texture.FORMAT_RGB,
                               base_img,
                               1);

        Texture2D filter_texture = new Texture2D();
        filter_texture.setSources(Texture.MODE_BASE_LEVEL,
                                 Texture.FORMAT_RGB,
                                 filter_img,
                                 1);

        TextureAttributes base_ta = new TextureAttributes();
        base_ta.setBlendColor(0.5f, 0, 0, 1);
        base_ta.setTextureMode(TextureAttributes.MODE_COMBINE);
        base_ta.setCombineMode(false, TextureAttributes.COMBINE_DOT3_RGB);
        base_ta.setCombineMode(true, TextureAttributes.COMBINE_REPLACE);
        base_ta.setCombineSource(false, 0, TextureAttributes.SOURCE_CURRENT_TEXTURE);
//        base_ta.setCombineSource(false, 1, TextureAttributes.SOURCE_CURRENT_TEXTURE);
//        base_ta.setCombineSource(false, 1, TextureAttributes.SOURCE_PREVIOUS_UNIT);
        base_ta.setCombineSource(true, 0, TextureAttributes.SOURCE_CONSTANT_COLOR);

        TextureAttributes filter_ta = new TextureAttributes();
        filter_ta.setTextureMode(TextureAttributes.MODE_MODULATE);

        TextureUnit[] tu = new TextureUnit[2];
        tu[0] = new TextureUnit();
        tu[0].setTexture(base_texture);
        tu[0].setTextureAttributes(base_ta);

        tu[1] = new TextureUnit();
        tu[1].setTexture(filter_texture);
        tu[1].setTextureAttributes(filter_ta);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setTextureUnits(tu, 2);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        DirectionalLight light = new DirectionalLight();
        light.setDirection(1, 0, 1);

        scene_root.addChild(shape);
        scene_root.addChild(light);

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
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Load a single image
     */
    private TextureComponent2D[] loadImage(String name)
    {
        TextureComponent2D img_comp = null;

        try
        {
            File f = new File(name);
            if(!f.exists())
                System.out.println("Can't find texture source file");

            FileInputStream is = new FileInputStream(f);

            BufferedInputStream stream = new BufferedInputStream(is);
            BufferedImage img = ImageIO.read(stream);

            int img_width = img.getWidth(null);
            int img_height = img.getHeight(null);
            int format = TextureComponent.FORMAT_RGB;

            switch(img.getType())
            {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_CUSTOM:
                case BufferedImage.TYPE_INT_RGB:
                    break;

                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_INT_ARGB:
                    format = TextureComponent.FORMAT_RGBA;
                    break;
            }

            img_comp = new ImageTextureComponent2D(format,
                                            img_width,
                                            img_height,
                                            img);
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
        }

        return new TextureComponent2D[] { img_comp };
    }

    public static void main(String[] args)
    {
        Dot3MultiTextureDemo demo = new Dot3MultiTextureDemo();
        demo.setVisible(true);
    }
}

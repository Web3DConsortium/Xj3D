
// External imports
import java.awt.*;
import static java.awt.BorderLayout.CENTER;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.awt.image.BufferedImage.TYPE_CUSTOM;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.System.exit;
import static java.lang.System.out;
import static javax.imageio.ImageIO.read;
import com.jogamp.opengl.GLCapabilities;
import static com.jogamp.opengl.GLProfile.getDefault;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

// Local imports
import org.j3d.aviatrix3d.*;
import static org.j3d.aviatrix3d.Texture.FORMAT_RGBA;
import static org.j3d.aviatrix3d.Texture.MODE_BASE_LEVEL;
import static org.j3d.aviatrix3d.TextureSource.FORMAT_RGB;
import static org.j3d.aviatrix3d.VertexGeometry.COORDINATE_3;
import static org.j3d.aviatrix3d.VertexGeometry.TEXTURE_COORDINATE_2;
import static org.j3d.aviatrix3d.VertexGeometry.VBO_HINT_STATIC;

import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.TransparencyDepthSortStage;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.GeometryData;
import static org.j3d.geom.GeometryData.NORMAL_DATA;
import static org.j3d.geom.GeometryData.TEXTURE_2D_DATA;
import static org.j3d.geom.GeometryData.TRIANGLES;
import static org.j3d.geom.GeometryData.TRIANGLE_STRIPS;
import org.j3d.geom.SphereGenerator;

/**
 * Example of per pixel lighting.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class PerPixelLightingDemo extends Frame
	implements WindowListener, ApplicationUpdateObserver, NodeUpdateListener
{
	/** vertex shader */
	private static final String VTX_SHADER_FILE =
		"demo_shaders/phong_vert.glsl";

	/** fragment shader */
	private static final String FRAG_SHADER_FILE =
		"demo_shaders/phong_frag.glsl";

	/** texture file */
	private static final String[] TEXTURE_FILES =
	{
		"textures/flags/uk.png",
		"textures/flags/usa.png",
		"textures/flags/sweden.png",
		"textures/flags/switzerland.png",
	};

	/** orbit distance */
	private static final float ORBIT_DISTANCE = 20;

	/** headlight settings for shader */
	private static final int ENABLE_HEADLIGHT = 1;
	private static final int DISABLE_HEADLIGHT = 0;

	/** Manager for the scene graph handling */
	private SingleThreadRenderManager sceneManager;

	/** Manager for the layers etc */
	private SingleDisplayCollection displayManager;

	/** Our drawing surface */
	private GraphicsOutputDevice surface;

	/** The shader for vertex section */
	private VertexShader vtxShader;

	/** The shader for fragment processing */
	private FragmentShader fragShader;

	/** navigation objs */
	private TransformGroup view_tx;
	private int station;

	private AxisAngle4f rotation;
	private Matrix4f view_mtx;
	private Vector3f translation;

	private float[] transparency;
	private Material[] material;

	/**
	 * Construct a new shader demo instance.
	 */
	public PerPixelLightingDemo()
	{
		super("GLSL Phong");

		setLayout(new BorderLayout());
		addWindowListener(this);

		rotation = new AxisAngle4f(0, 1, 0, 0);
		view_mtx = new Matrix4f();
		translation = new Vector3f();

		transparency = new float[4];
		material = new Material[4];

		setupAviatrix();
		setupSceneGraph();

		setLocation(40, 40);

		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this.
		// http://jogl.dev.java.net
		pack();
		setVisible(true);
                
	}

	//---------------------------------------------------------------
	// ApplicationUpdateObserver methods
	//---------------------------------------------------------------

    @Override
	public void appShutdown() {
	}

    @Override
	public void updateSceneGraph() {

		if (++station == 360) {
			station = 0;
		}
		double angle = station * PI / 180;
		translation.x = (float)(ORBIT_DISTANCE * sin(angle));
		translation.z = (float)(ORBIT_DISTANCE * cos(angle));

		rotation.angle = (float)angle;
		view_mtx.setIdentity();
		view_mtx.setRotation(rotation);
		view_mtx.setTranslation(translation);

		view_tx.boundsChanged(this);

		transparency[0] = 0.99f;
		transparency[1] = 0.99f;
		transparency[2] = 0.99f;
		transparency[3] = 0.99f;
		if ((angle > 3 * PI/2) || (angle < PI/2)) {
			transparency[2] = 1 - (float)cos(angle);
		}
		if ((angle > 0) && (angle < PI)) {
			transparency[1] = 1 - (float)sin(angle);
		}
		if ((angle > PI/2) && (angle < 3 * PI/2)) {
			transparency[0] = 1 + (float)cos(angle);
		}
		if ((angle > PI) && (angle < 2 * PI)) {
			transparency[3] = 1 + (float)sin(angle);
		}
		material[0].dataChanged(this);
		material[1].dataChanged(this);
		material[2].dataChanged(this);
		material[3].dataChanged(this);
	}

	//---------------------------------------------------------------
	// NodeUpdateListener methods
	//---------------------------------------------------------------

    @Override
	public void updateNodeBoundsChanges(Object src) {
		if (src == view_tx) {
			view_tx.setTransform(view_mtx);
		}
	}

    @Override
	public void updateNodeDataChanges(Object src) {
		for (int i = 0; i < 4; i++) {
			if (src == material[i]) {
				material[i].setTransparency(transparency[i]);
			}
		}
	}

	//---------------------------------------------------------------
	// X methods
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

		GraphicsSortStage sorter = new TransparencyDepthSortStage();
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
		//sceneManager.setMinimumFrameInterval(20);
		sceneManager.setApplicationObserver(this);

		// Before putting the pipeline into run mode, put the canvas on
		// screen first.
		Component comp = (Component)surface.getSurfaceObject();
		comp.setPreferredSize(new Dimension(500, 500));
		add(comp, CENTER);
	}

	/**
	 * Setup the basic scene which consists of a quad and a viewpoint
	 */
	private void setupSceneGraph()
	{
		Group scene_root = new Group();

		// pointing down the -X axis
		SpotLight spot_0 = new SpotLight(
			new float[]{1, 1, 1},
			new float[]{10, 0, 0},
			new float[]{-1, 0, 0});
		spot_0.setGlobalOnly(true);
		spot_0.setEnabled(true);
		scene_root.addChild(spot_0);

		// pointing down the -Y axis
		SpotLight spot_1 = new SpotLight(
			new float[]{1, 1, 1},
			new float[]{0, 10, 0},
			new float[]{0, -1, 0});
		spot_1.setGlobalOnly(true);
		spot_1.setEnabled(true);
		scene_root.addChild(spot_1);
		/////////////////////////////////////////////////////////////////

		TransformGroup sphere_tx_0 = getSphere();

        Matrix4f mtx_0 = new Matrix4f();
        mtx_0.setIdentity();
		Vector3f t_0 = new Vector3f(0, 2.5f, 0);
		mtx_0.setTranslation(t_0);

        sphere_tx_0.setTransform(mtx_0);
		scene_root.addChild(sphere_tx_0);

		/////////////////////////////////////////////////////////////////

		for (int i = 0; i < 4; i++) {

			TriangleArray ta = getWall();

			material[i] = new Material();
        	material[i].setDiffuseColor(new float[] { 1, 1, 1 });
			material[i].setSpecularColor(new float[] { 1, 1, 1 });
			material[i].setShininess(0.8f);

			int num_textures = 1;
			TextureUnit[] textures = new TextureUnit[1];
			textures[0] = loadImage(TEXTURE_FILES[i]);

			String[] vert_shader_txt = loadShaderFile(VTX_SHADER_FILE);
			String[] frag_shader_txt = loadShaderFile(FRAG_SHADER_FILE);

			ShaderObject vert_shader = new ShaderObject(true);
			vert_shader.setSourceStrings(vert_shader_txt, 1);
			vert_shader.requestInfoLog();
			vert_shader.compile();

			ShaderObject frag_shader = new ShaderObject(false);
			frag_shader.setSourceStrings(frag_shader_txt, 1);
			frag_shader.requestInfoLog();
			frag_shader.compile();

			ShaderProgram shader_prog = new ShaderProgram();
			shader_prog.addShaderObject(vert_shader);
			shader_prog.addShaderObject(frag_shader);
			shader_prog.requestInfoLog();
			shader_prog.link();

			ShaderArguments shader_args = new ShaderArguments();
			shader_args.setUniform("use_headlight", 1, new int[]{DISABLE_HEADLIGHT}, 1);
			shader_args.setUniform("has_texture", 1, new int[]{1}, 1);
			shader_args.setUniformSampler("tex_unit", 0);

			GLSLangShader shader = new GLSLangShader();
			shader.setShaderProgram(shader_prog);
			shader.setShaderArguments(shader_args);

			Appearance app = new Appearance();
			app.setTextureUnits(textures, num_textures);
			app.setMaterial(material[i]);
			app.setShader(shader);

			Shape3D shape = new Shape3D();
			shape.setGeometry(ta);
			shape.setAppearance(app);

			Matrix4f mat2 = new Matrix4f();
			mat2.setIdentity();
			Vector3f t = new Vector3f();
			AxisAngle4f r = new AxisAngle4f(0, 1, 0, 0);

			switch(i) {
			case 0:
				t.set(0, 0, -2.5f);
				r.angle = 0;
				break;
			case 1:
				t.set(2.5f, 0, 0);
				r.angle = (float)PI/2;
				break;
			case 2:
				t.set(0, 0, 2.5f);
				r.angle = (float)PI;
				break;
			case 3:
				t.set(-2.5f, 0, 0);
				r.angle = -(float)PI/2;
				break;
			}
			mat2.setTranslation(t);
			mat2.setRotation(r);

			TransformGroup shape_tx = new TransformGroup();
			shape_tx.addChild(shape);
			shape_tx.setTransform(mat2);

			scene_root.addChild(shape_tx);
		}
		/////////////////////////////////////////////////////////////////

		TransformGroup sphere_tx_1 = getSphere();

        Matrix4f mtx_1 = new Matrix4f();
        mtx_1.setIdentity();
		Vector3f t_1 = new Vector3f(0, -2.5f, 0);
		mtx_1.setTranslation(t_1);

        sphere_tx_1.setTransform(mtx_1);
		scene_root.addChild(sphere_tx_1);

		/////////////////////////////////////////////////////////////////
		// View group
		Viewpoint vp = new Viewpoint();
		vp.setHeadlightEnabled(true);

		translation.y = 4;
		translation.z = ORBIT_DISTANCE;

		view_mtx.setIdentity();
		view_mtx.setTranslation(translation);

		view_tx = new TransformGroup();
		view_tx.addChild(vp);
		view_tx.setTransform(view_mtx);

		scene_root.addChild(view_tx);

		/////////////////////////////////////////////////////////////////

		ColorBackground bg = new ColorBackground(new float[]{ 0.80f, 0.85f, 0.90f });

		SimpleScene scene = new SimpleScene();
		scene.setActiveBackground(bg);
		scene.setRenderedGeometry(scene_root);
		scene.setActiveView(vp);

		// Then the basic layer and viewport at the top:
		SimpleViewport view = new SimpleViewport();
		view.setDimensions(0, 0, 500, 500);
		view.setScene(scene);

		//ShaderLoadStatusCallback cb =
		//    new ShaderLoadStatusCallback(vert_shader, frag_shader, shader_prog);
		//sceneManager.setApplicationObserver(cb);

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
	 * Load the shader file. Find it relative to the classpath.
	 *
	 * @param file THe name of the file to load
	 */
	private String[] loadShaderFile(String name)
	{
		File file = new File(name);
		if(!file.exists())
		{
			out.println("Cannot find file " + name);
			return null;
		}

		String ret_val = null;

		try
		{
                    StringBuffer buf;
            try (FileReader is = new FileReader(file)) {
                buf = new StringBuffer();
                char[] read_buf = new char[1_024];
                int num_read;
                while ((num_read = is.read(read_buf, 0, 1_024)) != -1) {
                    buf.append(read_buf, 0, num_read);
                }
            }

			ret_val = buf.toString();
		}
		catch(IOException ioe)
		{
			out.println("I/O error " + ioe);
		}
		return new String[] { ret_val };
	}

	//---------------------------------------------------------------
	// Local methods
	//---------------------------------------------------------------

	/**
	 * Load a single image
	 */
	private TextureUnit loadImage(String name)
	{
		TextureComponent2D comp = null;

		try
		{
			File f = new File(name);
			if(!f.exists())
				out.println("Can't find texture source file");

			FileInputStream is = new FileInputStream(f);

			BufferedInputStream stream = new BufferedInputStream(is);
			BufferedImage img = read(stream);

			if(img == null)
				return null;

			int img_width = img.getWidth(null);
			int img_height = img.getHeight(null);
			int format = FORMAT_RGB;

			switch(img.getType())
			{
			case TYPE_3BYTE_BGR:
			case TYPE_CUSTOM:
			case TYPE_INT_RGB:
				break;

			case TYPE_4BYTE_ABGR:
			case TYPE_INT_ARGB:
				format = FORMAT_RGBA;
				break;
			}

			comp = new ImageTextureComponent2D(format,
				img_width,
				img_height,
				img);
		}
		catch(IOException ioe)
		{
			out.println("Error reading image: " + ioe);
		}

		TextureComponent2D[] img_comp = { comp };

		Texture2D texture = new Texture2D();
		texture.setSources(MODE_BASE_LEVEL, FORMAT_RGBA,
			img_comp,
			1);

		TextureUnit tu = new TextureUnit();
		tu.setTexture(texture);

		return tu;
	}

	public static void main(String[] args)
	{
		PerPixelLightingDemo demo = new PerPixelLightingDemo();
		demo.setVisible(true);
	}

	private TriangleArray getWall()
	{
		BoxGenerator generator = new BoxGenerator(5, 5, 0.1f);
		GeometryData data = new GeometryData();

		data.geometryType = TRIANGLES;
		data.geometryComponents = NORMAL_DATA |
			TEXTURE_2D_DATA;

		generator.generate(data);

		TriangleArray ta = new TriangleArray(
			false, VBO_HINT_STATIC);

		ta.setVertices(COORDINATE_3,
			data.coordinates,
			data.vertexCount);

		ta.setNormals(data.normals);

		// Make an array of objects for the texture setting
		float[][] tex_coord = {data.textureCoordinates};
		int[] tex_type = {TEXTURE_COORDINATE_2};
		ta.setTextureCoordinates(tex_type, tex_coord, 1);

		return(ta);
	}

	private TransformGroup getSphere()
	{
        SphereGenerator generator = new SphereGenerator(2, 64);
        GeometryData data = new GeometryData();

        data.geometryType = TRIANGLE_STRIPS;
        data.geometryComponents = NORMAL_DATA |
                                  TEXTURE_2D_DATA;

        generator.generate(data);

        TriangleStripArray tsa = new TriangleStripArray(
            false, VBO_HINT_STATIC);

        tsa.setVertices(COORDINATE_3,
            data.coordinates,
            data.vertexCount);

        tsa.setStripCount(data.stripCounts, data.numStrips);
        tsa.setNormals(data.normals);

        float[][] tex_coord = {data.textureCoordinates};
        int[] tex_type = {TEXTURE_COORDINATE_2};
        tsa.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 1, 0 });
        material.setSpecularColor(new float[] { 1, 1, 1 });
		material.setShininess(0.8f);

        String[] vert_shader_txt = loadShaderFile(VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.requestInfoLog();
        shader_prog.link();

        ShaderArguments shader_args = new ShaderArguments();
		shader_args.setUniform("use_headlight", 1, new int[]{ENABLE_HEADLIGHT}, 1);
		shader_args.setUniform("has_texture", 1, new int[]{0}, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(tsa);
        shape.setAppearance(app);

        TransformGroup shape_tx = new TransformGroup();
        shape_tx.addChild(shape);

		return(shape_tx);
	}
}

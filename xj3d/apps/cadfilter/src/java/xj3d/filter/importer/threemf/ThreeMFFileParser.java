/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2009-2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer.threemf;

// External imports
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.vecmath.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.j3d.util.ErrorReporter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

// Local imports
import org.web3d.vrml.renderer.common.nodes.shape.BaseMaterial;
import org.web3d.vrml.sav.*;

import xj3d.filter.NonWeb3DFileParser;

/**
 * File parser that reads 3mf files and generates an X3D stream
 * of events.
 *
 * Output Styles:
 *
 *    UNCOLORED - Color information is stripped
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class ThreeMFFileParser implements NonWeb3DFileParser {
    
    private static final boolean USE_MATRIX_TRANSFORM = true;

    private static final String MODEL_SCHEMA = "http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel";
    
    /** The System property for the temp directory */
    public final static String TMP_DIR_PROPERTY = "xj3d.filter.importer.threemf.tmpdir";
    
    /** The temp directory to extract 3mf contents to */
    public final static String TMP_DIR;
    
    static {
        TMP_DIR = parseProperty(TMP_DIR_PROPERTY, null);
        System.out.printf("TMP_DIR: %s\n",TMP_DIR);
    }

    /** Identifier */
    private static final String LOG_NAME = "ThreeMFFileParser";

    private static final float[] COLOR_WHITE = new float[] {1,1,1};
    
    private static final String BACKUP_TMP_DIR = "/tmp";

    /** The Document Element */
    private Object doc_element;

    /** Flag indicating that the content handler is an instance of a
    *  BinaryContentHandler, rather than a StringContentHandler */
    private boolean handlerIsBinary;

    /** Binary Content Handler reference */
    private BinaryContentHandler bch;

    /** String Content Handler reference */
    private StringContentHandler sch;

    /** The url of the current document */
    private String documentURL;

    /** Reference to the registered content handler if we have one */
    private ContentHandler contentHandler;

    /** Reference to the registered route handler if we have one */
    private RouteHandler routeHandler;

    /** Reference to the registered script handler if we have one */
    private ScriptHandler scriptHandler;

    /** Reference to the registered proto handler if we have one */
    private ProtoHandler protoHandler;

    /** Reference to the registered error handler if we have one */
    private ErrorReporter errorHandler;

    /** Reference to our DocumentLocator instance to hand to users */
    private DocumentLocator documentLocator;

    // Global scale for units conversion
    private float scale = 1.0f;

    /** How to style our output.  Supports UNCOLORED, MATRIX_TRANSFORM or null for none */
    private Set<String> style;

    /** The main model file identified by the relationships */
    private String modelFile;

    /** What resources have been written */
    private Set<Integer> writtenResources;

    private File tmpDir;

    /** Texture mapping when writing textures */
    private Set<String> textures;

    /**
     * Constructor
     */
    public ThreeMFFileParser() {

        writtenResources = new HashSet<>();
        textures = new HashSet<>();
    }

    /**
     * Initialise the internals of the parser at start up. If you are not using
     * the detailed constructors, this needs to be called to ensure that all
     * internal state is correctly set up.
     */
    @Override
    public void initialize() {
        // Ignored for this implementation.
    }

    /**
     * Set the base URL of the document that is about to be parsed. Users
     * should always call this to make sure we have correct behaviour for the
     * ContentHandler's <code>startDocument()</code> call.
     * <p>
     * The URL is cleared at the end of each document run. Therefore it is
     * imperative that it get's called each time you use the parser.
     *
     * @param url The document url to set
     */
    @Override
    public void setDocumentUrl(String url) {
        documentURL = url;
    }

    /**
     * Fetch the locator used by this parser. This is here so that the user of
     * this parser can ask for it and set it before calling startDocument().
     * Once the scene has started parsing in this class it is too late for the
     * locator to be set. This parser does set it internally when asked for a
     * but there may be other times when it is not set.
     *
     * @return The locator used for syntax errors
     */
    @Override
    public DocumentLocator getDocumentLocator() {
        return documentLocator;
    }

    /**
     * Set the content handler instance.
     *
     * @param ch The content handler instance to use
     */
    @Override
    public void setContentHandler(ContentHandler ch) {
        contentHandler = ch;
        if (contentHandler instanceof BinaryContentHandler) {
            bch = (BinaryContentHandler)contentHandler;
            sch = null;
            handlerIsBinary = true;
        } else if (contentHandler instanceof StringContentHandler) {
            bch = null;
            sch = (StringContentHandler)contentHandler;
            handlerIsBinary = false;
        }
        // otherwise - we don't know how to deal with the content handler
    }

    /**
     * Set the route handler instance.
     *
     * @param rh The route handler instance to use
     */
    @Override
    public void setRouteHandler(RouteHandler rh) {
        routeHandler = rh;
    }

    /**
     * Set the script handler instance.
     *
     * @param sh The script handler instance to use
     */
    @Override
    public void setScriptHandler(ScriptHandler sh) {
        scriptHandler = sh;
    }

    /**
     * Set the proto handler instance.
     *
     * @param ph The proto handler instance to use
     */
    @Override
    public void setProtoHandler(ProtoHandler ph) {
        protoHandler = ph;
    }

    /**
     * Set the error handler instance.
     *
     * @param eh The error handler instance to use
     */
    @Override
    public void setErrorHandler(ErrorHandler eh) {
        errorHandler = eh;

        if(eh != null)
            eh.setDocumentLocator(getDocumentLocator());
    }

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    @Override
    public void setErrorReporter(ErrorReporter eh) {
        if(eh instanceof ErrorHandler)
            setErrorHandler((ErrorHandler)eh);
        else
            errorHandler = eh;
    }

    /**
     * Parse the input now.
     *
     * @param input The stream to read from
     * @param style The style or null or no styling
     * @return True if no parsing issue, False if recovered.
     * @throws IOException An I/O error while reading the stream
     * @throws ImportFileFormatException A parsing error occurred in the file
     */
    @Override
    public List<String> parse(InputSource input, String[] style)
        throws IOException, ImportFileFormatException {

        this.style = new HashSet<>();
        if (style != null) {
            this.style.addAll(Arrays.asList(style));
        }

        // Not good as this opens a second network connection, rather than
        // reusing the one that is already open when we checked the MIME type.
        // Need to recode some to deal with this.
        URL url = new URL(input.getURL());

        // acquire the contents of the document
        ThreeMFReader cr = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try {
            cr = new ThreeMFReader();

            if (TMP_DIR != null) {
                tmpDir = new File(TMP_DIR);
            } else  {
                tmpDir = createTempDir("threemf");
            }

            if (input.getURL().startsWith("file:")) {
                // special case this so we can use ZipFile instead of ZipArchiveEntry
                File f = new File(url.getPath());
                unzip(f,tmpDir);
            } else {
                throw new IllegalArgumentException("Path not implemented");
            }
            
            // Force delete the tmpDir on exit. Will only work if no additional files or folders
            // are created in directory, so must call after unzipping.
            FileUtils.forceDeleteOnExit(tmpDir);

            String main_model = getMainModel(new File(tmpDir.getAbsolutePath() + File.separator + "_rels" + File.separator + ".rels"));
            File model = new File(tmpDir.getAbsolutePath() + File.separator + main_model);
            fis = new FileInputStream(model);
            bis = new BufferedInputStream(fis);

            // Expand the zip to a temporary file and then find the main model file
            cr.parse(new org.xml.sax.InputSource(bis));
        } catch (IOException ioe) {

            ImportFileFormatException iffe = new ImportFileFormatException(
                LOG_NAME + ": IOException reading: "+ url);

            iffe.setStackTrace(ioe.getStackTrace());
            throw iffe;
        } catch (SAXException se) {

            ImportFileFormatException iffe = new ImportFileFormatException(
                LOG_NAME + ": SAXException reading: "+ url);

            iffe.setStackTrace(se.getStackTrace());
            throw iffe;
        } finally {
            if (bis != null) bis.close();
            if (fis != null) fis.close();
        }
        // get the libraries
        doc_element = cr.getResult();

        contentHandler.startDocument(input.getURL(),
            input.getBaseURL(),
            "utf8",
            "#X3D",
            "V3.3",
            "3mf file conversion");

        contentHandler.profileDecl("Interchange");

        if (USE_MATRIX_TRANSFORM) {
            contentHandler.componentDecl("EXT_Grouping:1");
        }

        Model model = (Model) cr.getResult();

        contentHandler.startNode("NavigationInfo",null);
        contentHandler.startField("avatarSize");
        bch.fieldValue(new float[] {0.01f,1.6f,0.75f},3);   // assume small objects
        contentHandler.endNode();
        contentHandler.startNode("Transform",model.getUnit() + "_TRANS");
//        float scale = 1;
        switch(model.getUnit()) {
            case micron:
                scale = (float) (1e-6);
                break;
            case millimeter:
                scale = (float) (1e-3);
                break;
            case centimeter:
                scale = (float) (1e-2);
                break;
            case inch:
                scale = 0.0254f;
                break;
            case foot:
                scale = 0.3048f;
                break;
            case meter:
                scale = 1;
                break;
            default:
                throw new IllegalArgumentException("Unhandled unit");
        }
        contentHandler.startField("scale");
        bch.fieldValue(new float[] {scale,scale,scale},3);
        contentHandler.startField("rotation");
        bch.fieldValue(new float[] {1,0,0,-(float)Math.PI/2},4);  // change z up to y up
        contentHandler.startField("children");

        Build build = model.getBuild();
        List<Item> items = build.getItems();
        for(Item item : items) {
            ObjectResource resource = (ObjectResource) model.getResource(item.getObjectID());

            PropertySource base = null;
            int baseIdx = -1;

            int pid = resource.getPID();
            if (pid > -1) {
                PropertySource bm = (PropertySource) model.getResource(pid);
                if (bm != null) {
                    baseIdx = resource.getPIndex();
                    base = bm;
                }

            }

            writeObject(model,resource,item.getTransform(),base,baseIdx);
        }

        contentHandler.endField();
        contentHandler.endNode();

        // release references to any objects created from parsing the file
        doc_element = null;

        contentHandler.endDocument();

        return null;
    }

    private void writeObject(Model model, ObjectResource resource, Matrix4d transform, PropertySource base, int baseIdx) {
        Mesh mesh = resource.getMesh();
        Components components = resource.getComponents();

        if (mesh != null) {
            writeMesh(model,resource.getID(),mesh,transform,base,baseIdx);
        } else if (components != null) {
            if (transform != null) {
                writeTransform(transform,null);
                contentHandler.startField("children");
            }
            List<Component> list = components.getComponents();
            for(Component comp : list) {
                Object res = model.getResource(comp.getObjectID());

                if (res instanceof ObjectResource) {
                    ObjectResource cresource = (ObjectResource) res;
                    Mesh cmesh = cresource.getMesh();

                    PropertySource base2 = null;
                    int baseIdx2 = -1;

                    int pid2 = cresource.getPID();
                    if (pid2 > -1) {
                        PropertySource bm = (PropertySource) model.getResource(pid2);
                        if (bm != null) {
                            baseIdx2 = cresource.getPIndex();
                            base2 = bm;
                        }
                    }

                    if (cmesh != null) {
                        if (base2 != null) {
                            writeMesh(model,cresource.getID(), cmesh, comp.getTransform(), base2,baseIdx2);
                        } else {
                            writeMesh(model,cresource.getID(), cmesh, comp.getTransform(), base,baseIdx);
                        }
                    } else {
                        if (base2 != null) {
                            writeObject(model,cresource,comp.getTransform(),base2,baseIdx2);
                        } else {
                            writeObject(model,cresource,comp.getTransform(),base,baseIdx);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Component reference is not an ObjectResource.  objectid: " + resource.getID());
                }
            }

            if (transform != null) {
                contentHandler.endField();  // children
                contentHandler.endNode(); // Transform
            }
        }
    }

    public void writeMesh(Model model, int id,Mesh mesh, Matrix4d trans, PropertySource base, int baseIdx) {
        Vertices verts = mesh.getVertices();
        Triangles tris = mesh.getTriangles();

        if (bch == null) {
            throw new IllegalArgumentException("Not implemented");

        }

        String defName = "OBJ_" + id;
        if (writtenResources.contains(id)) {
            if (trans != null) {
                writeTransform(trans, null);
                contentHandler.startField("children");
            }
            contentHandler.useDecl(defName);

            if (trans != null) {
                contentHandler.endField();  // children
                contentHandler.endNode(); // Transform
            }
            return;
        }

        if (trans != null) {
            writeTransform(trans,null);
            contentHandler.startField("children");
        }

        contentHandler.startNode("Group", defName);
        contentHandler.startField("children");

        // Write out one mesh per property
        Set<Integer> shells = tris.getProperties();

        if (shells.size() == 1 && shells.contains(-1)) {  // No color properties
            // No property meshes can be exported as IndexedTriangleSets
            contentHandler.startNode("Shape", null);
            contentHandler.startField("appearance");
            contentHandler.startNode("Appearance", null);
            contentHandler.startField("material");
            contentHandler.startNode("Material", null);
            if (base != null) {
                float[] color = base.getProperty(baseIdx);
                float[] diffuse = new float[3];
                diffuse[0] = color[0];
                diffuse[1] = color[1];
                diffuse[2] = color[2];
                contentHandler.startField("diffuseColor");
                bch.fieldValue(diffuse, 1);
                /*  // TODO: I think transparency only works on multiproperties
                float transparency = color[3];
                contentHandler.startField("transparency");
                bch.fieldValue(transparency);
                */
            }

            contentHandler.endNode();  // Material
            contentHandler.endNode();  // Appearance
            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedTriangleSet", null);
            contentHandler.startField("coord");
            contentHandler.startNode("Coordinate", "COORD_" + defName);
            contentHandler.startField("point");
            bch.fieldValue(verts.getVerts(), verts.getCount() * 3);
            contentHandler.endNode();  // Coordinate

            contentHandler.startField("index");
            int len = tris.getCount();
            List<int[]> tatts = tris.getTris(-1);
            int[] index = new int[len * 3];
            int idx = 0;
            for (int i = 0; i < len; i++) {
                index[idx++] = tatts.get(i)[0];
                index[idx++] = tatts.get(i)[1];
                index[idx++] = tatts.get(i)[2];
            }
            bch.fieldValue(index, len * 3);

            contentHandler.endNode();  // IndexedTriangleSet
            contentHandler.endNode();  // Shape
        }  else {
            // Multiproperty meshes need to use a TriangleSet
            for (Integer shell : shells) {
                if (shell == -1) {
                    exportBaseTriangleArrayWithProperties(model, mesh, base, baseIdx);
                } else {
                    exportTriangleArrayWithProperties(model, mesh, shell);
                }
            }
        }
        contentHandler.endField();  // children
        contentHandler.endNode(); // Shape Group

        if (trans != null) {
            contentHandler.endField();  // children
            contentHandler.endNode(); // Transform
        }

        writtenResources.add(id);
        
    }

    private void exportTriangleArrayWithProperties(Model model, Mesh mesh, int pid, PropertySource base,int baseIdx) {
        float[] verts = mesh.getVertices().getVerts();
        Triangles tris = mesh.getTriangles();

        List<int[]> tlist = tris.getTris(pid);

        if (tlist == null || tlist.isEmpty()) return;

        float[] colors = null;
        float[] coords = new float[tlist.size() * 9];
        float[] texCoords = null;

        ModelResource res = model.getResource(pid);
        if (res instanceof BaseMaterials) {
            colors = new float[tlist.size() * 9];

            BaseMaterials bms = (BaseMaterials) res;

            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                float[] c = base.getProperty(baseIdx);
                if (tri[3] != -1) c = bms.getProperty(tri[3]);

                colors[i*9  ] = c[0];
                colors[i*9+1] = c[1];
                colors[i*9+2] = c[2];

                if (tri[4] != -1 && tri[5] != -1) c = bms.getProperty(tri[4]);
                colors[i*9+3] = c[0];
                colors[i*9+4] = c[1];
                colors[i*9+5] = c[2];

                if (tri[4] != -1 && tri[5] != -1) c = bms.getProperty(tri[5]);
                colors[i*9+6] = c[0];
                colors[i*9+7] = c[1];
                colors[i*9+8] = c[2];
            }
        } else if (res instanceof ColorGroup) {
            ColorGroup cg = (ColorGroup) res;

            colors = new float[tlist.size() * 9];

            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                float[] c = base.getProperty(baseIdx);
                if (tri[3] != -1) c = cg.getProperty(tri[3]);

                colors[i*9  ] = c[0];
                colors[i*9+1] = c[1];
                colors[i*9+2] = c[2];

                if (tri[4] != -1 && tri[5] != -1) c = cg.getProperty(tri[4]);
                colors[i*9+3] = c[0];
                colors[i*9+4] = c[1];
                colors[i*9+5] = c[2];

                if (tri[4] != -1 && tri[5] != -1) c = cg.getProperty(tri[5]);
                colors[i*9+6] = c[0];
                colors[i*9+7] = c[1];
                colors[i*9+8] = c[2];
            }
        } else if (res instanceof Texture2dGroup) {
            int len = tlist.size();

            Texture2dGroup tg = (Texture2dGroup) res;
            texCoords = new float[tlist.size() * 6];

            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                // TODO: How should we fall back to base color here if were missing properties?
                float[] c = tg.getProperty(tri[3]);
                texCoords[i*6  ] = c[0];
                texCoords[i*6+1] = c[1];

                if (tri[4] != -1) c = tg.getProperty(tri[4]);
                texCoords[i*6+2] = c[0];
                texCoords[i*6+3] = c[1];

                if (tri[5] != -1) c = tg.getProperty(tri[5]);
                texCoords[i*6+4] = c[0];
                texCoords[i*6+5] = c[1];
            }
        } else {
            // Unknown property group, just export geometry
            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];
            }
        }

        contentHandler.startNode("Shape", null);
        contentHandler.startField("appearance");
        contentHandler.startNode("Appearance", null);
        if (texCoords != null) {
            Texture2dGroup tg = (Texture2dGroup) res;
            Texture2d tex = (Texture2d) model.getResource(tg.getTexId());

            if (tex == null) {
                throw new IllegalArgumentException("Tex not found.  id: " + tg.getTexId());
            }
            String defName = "TEX_" + tg.getTexId();
            contentHandler.startField("texture");
            if (writtenResources.contains(tg.getTexId())) {
                contentHandler.useDecl(defName);
            }  else {
                contentHandler.startNode("ImageTexture",defName);
                contentHandler.startField("url");
                String path = convPathToUniqueUrl(tex.getPath(), tex.getPath());
                bch.fieldValue(path);
                if (!tex.getTileStyleU().equals("wrap")) {
                    // TODO: not sure how to map clamp without using TextureProperties
                    contentHandler.startField("repeatS");
                    bch.fieldValue(false);
                }
                if (!tex.getTileStyleV().equals("wrap")) {
                    // TODO: not sure how to map clamp without using TextureProperties
                    contentHandler.startField("repeatT");
                    bch.fieldValue(false);
                }
                contentHandler.endNode();   // Texture
                writtenResources.add(tg.getTexId());
            }
        }
        contentHandler.startField("material");
        contentHandler.startNode("Material", null);
        if (base != null) {
            if (base instanceof BaseMaterial || base instanceof ColorGroup) {
                float[] color = base.getProperty(baseIdx);
                float[] diffuse = new float[3];
                diffuse[0] = color[0];
                diffuse[1] = color[1];
                diffuse[2] = color[2];
                contentHandler.startField("diffuseColor");
                bch.fieldValue(diffuse, 1);

                /*  // TODO: I think transparency only works on multiproperties
                float transparency = color[3];
                contentHandler.startField("transparency");
                bch.fieldValue(transparency);
                */
            } else {
                // TODO: This is likely from Texture2dGroup references.  We'd either have to use Multitexture or sample
                // the underlying image to pull the color for a material node.

                // 3mf spec doesn't really say much about lighting etc.  This looks pleasing to me
                System.out.printf("Unhandled base property type.  Ignored.  %s\n",base.getClass());
                contentHandler.startField("diffuseColor");
                bch.fieldValue(COLOR_WHITE,1);
                contentHandler.startField("specularColor");
                bch.fieldValue(COLOR_WHITE,1);
            }
        }

        contentHandler.endNode();  // Material
        contentHandler.endNode();  // Appearance
        contentHandler.startField("geometry");
        contentHandler.startNode("TriangleSet", null);
        contentHandler.startField("coord");
        contentHandler.startNode("Coordinate", null);
        contentHandler.startField("point");
        bch.fieldValue(coords, coords.length);
        contentHandler.endNode();  // Coordinate

        if (colors != null) {
            contentHandler.startField("color");
            contentHandler.startNode("Color", null);
            contentHandler.startField("color");
            bch.fieldValue(colors, colors.length);
            contentHandler.endNode();  // Color
        }

        if (texCoords != null) {
            contentHandler.startField("texCoord");
            contentHandler.startNode("TextureCoordinate", null);
            contentHandler.startField("point");
            bch.fieldValue(texCoords, texCoords.length);
            contentHandler.endNode();  // TextureCoordinate
        }

        contentHandler.endNode();  // TriangleSet
        contentHandler.endNode();  // Shape
    }

    /**
     * Export triangles using a property source
     * @param model
     * @param mesh
     * @param pid
     */
    private void exportTriangleArrayWithProperties(Model model, Mesh mesh, int pid) {
        float[] verts = mesh.getVertices().getVerts();
        Triangles tris = mesh.getTriangles();

        List<int[]> tlist = tris.getTris(pid);

        if (tlist == null || tlist.isEmpty()) return;

        float[] colors = null;
        float[] coords = new float[tlist.size() * 9];
        float[] texCoords = null;

        ModelResource res = model.getResource(pid);
        if (res instanceof BaseMaterials) {
            colors = new float[tlist.size() * 9];

            BaseMaterials bms = (BaseMaterials) res;

            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                float[] c = bms.getProperty(tri[3]);

                colors[i*9  ] = c[0];
                colors[i*9+1] = c[1];
                colors[i*9+2] = c[2];

                if (tri[4] != -1 && tri[5] != -1) c = bms.getProperty(tri[4]);
                colors[i*9+3] = c[0];
                colors[i*9+4] = c[1];
                colors[i*9+5] = c[2];

                if (tri[4] != -1 && tri[5] != -1) c = bms.getProperty(tri[5]);
                colors[i*9+6] = c[0];
                colors[i*9+7] = c[1];
                colors[i*9+8] = c[2];
            }
        } else if (res instanceof ColorGroup) {
            ColorGroup cg = (ColorGroup) res;

            colors = new float[tlist.size() * 9];

            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                float[] c = cg.getProperty(tri[3]);

                colors[i*9  ] = c[0];
                colors[i*9+1] = c[1];
                colors[i*9+2] = c[2];

                if (tri[4] != -1 && tri[5] != -1) c = cg.getProperty(tri[4]);
                colors[i*9+3] = c[0];
                colors[i*9+4] = c[1];
                colors[i*9+5] = c[2];

                if (tri[4] != -1 && tri[5] != -1) c = cg.getProperty(tri[5]);
                colors[i*9+6] = c[0];
                colors[i*9+7] = c[1];
                colors[i*9+8] = c[2];
            }
        } else if (res instanceof Texture2dGroup) {
            int len = tlist.size();

            Texture2dGroup tg = (Texture2dGroup) res;
            texCoords = new float[tlist.size() * 6];

            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                float[] c = tg.getProperty(tri[3]);
                texCoords[i*6  ] = c[0];
                texCoords[i*6+1] = c[1];

                if (tri[4] != -1) c = tg.getProperty(tri[4]);
                texCoords[i*6+2] = c[0];
                texCoords[i*6+3] = c[1];

                if (tri[5] != -1) c = tg.getProperty(tri[5]);
                texCoords[i*6+4] = c[0];
                texCoords[i*6+5] = c[1];
            }
        } else {
            // Unknown property group, just export geometry
            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];
            }
        }

        contentHandler.startNode("Shape", null);
        contentHandler.startField("appearance");
        contentHandler.startNode("Appearance", null);
        if (texCoords != null) {
            Texture2dGroup tg = (Texture2dGroup) res;
            Texture2d tex = (Texture2d) model.getResource(tg.getTexId());

            if (tex == null) {
                throw new IllegalArgumentException("Tex not found.  id: " + tg.getTexId());
            }
            String defName = "TEX_" + tg.getTexId();
            contentHandler.startField("texture");
            if (writtenResources.contains(tg.getTexId())) {
                contentHandler.useDecl(defName);
            }  else {
                contentHandler.startNode("ImageTexture",defName);
                contentHandler.startField("url");
                String path = convPathToUniqueUrl(tex.getPath(), tex.getPath());
                bch.fieldValue(path);
                if (!tex.getTileStyleU().equals("wrap")) {
                    // TODO: not sure how to map clamp without using TextureProperties
                    contentHandler.startField("repeatS");
                    bch.fieldValue(false);
                }
                if (!tex.getTileStyleV().equals("wrap")) {
                    // TODO: not sure how to map clamp without using TextureProperties
                    contentHandler.startField("repeatT");
                    bch.fieldValue(false);
                }
                contentHandler.endNode();   // Texture
                writtenResources.add(tg.getTexId());
            }
        }
        contentHandler.startField("material");
        contentHandler.startNode("Material", null);
        float[] color = new float[] {1f,1f,1f};
        contentHandler.startField("diffuseColor");
        bch.fieldValue(color, 1);

        contentHandler.endNode();  // Material
        contentHandler.endNode();  // Appearance
        contentHandler.startField("geometry");
        contentHandler.startNode("TriangleSet", null);
        contentHandler.startField("coord");
        contentHandler.startNode("Coordinate", null);
        contentHandler.startField("point");
        bch.fieldValue(coords, coords.length);
        contentHandler.endNode();  // Coordinate

        if (colors != null) {
            contentHandler.startField("color");
            contentHandler.startNode("Color", null);
            contentHandler.startField("color");
            bch.fieldValue(colors, colors.length);
            contentHandler.endNode();  // Color
        }

        if (texCoords != null) {
            contentHandler.startField("texCoord");
            contentHandler.startNode("TextureCoordinate", null);
            contentHandler.startField("point");
            bch.fieldValue(texCoords, texCoords.length);
            contentHandler.endNode();  // TextureCoordinate
        }

        contentHandler.endNode();  // TriangleSet
        contentHandler.endNode();  // Shape
    }

    /**
     * Export triangles using the base object property
     * @param model
     * @param mesh
     * @param base
     * @param baseIdx
     */
    private void exportBaseTriangleArrayWithProperties(Model model, Mesh mesh, PropertySource base,int baseIdx) {
        float[] verts = mesh.getVertices().getVerts();
        Triangles tris = mesh.getTriangles();

        List<int[]> tlist = tris.getTris(-1);

        if (tlist == null || tlist.isEmpty()) return;

        float[] colors = null;
        float[] coords = new float[tlist.size() * 9];
        float[] texCoords = null;

        if (base instanceof BaseMaterials) {
            colors = new float[tlist.size() * 9];

            BaseMaterials bms = (BaseMaterials) base;

            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                float[] c = bms.getProperty(baseIdx);

                colors[i*9  ] = c[0];
                colors[i*9+1] = c[1];
                colors[i*9+2] = c[2];

                colors[i*9+3] = c[0];
                colors[i*9+4] = c[1];
                colors[i*9+5] = c[2];

                colors[i*9+6] = c[0];
                colors[i*9+7] = c[1];
                colors[i*9+8] = c[2];
            }
        } else if (base instanceof ColorGroup) {
            ColorGroup cg = (ColorGroup) base;

            colors = new float[tlist.size() * 9];

            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                float[] c = cg.getProperty(baseIdx);

                colors[i*9  ] = c[0];
                colors[i*9+1] = c[1];
                colors[i*9+2] = c[2];

                colors[i*9+3] = c[0];
                colors[i*9+4] = c[1];
                colors[i*9+5] = c[2];

                colors[i*9+6] = c[0];
                colors[i*9+7] = c[1];
                colors[i*9+8] = c[2];
            }
        } else if (base instanceof Texture2dGroup) {
            int len = tlist.size();

            Texture2dGroup tg = (Texture2dGroup) base;
            texCoords = new float[tlist.size() * 6];

            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];

                float[] c = tg.getProperty(baseIdx);
                texCoords[i*6  ] = c[0];
                texCoords[i*6+1] = c[1];

                texCoords[i*6+2] = c[0];
                texCoords[i*6+3] = c[1];

                texCoords[i*6+4] = c[0];
                texCoords[i*6+5] = c[1];
            }
        } else {
            // Unknown property group, just export geometry
            int len = tlist.size();
            for (int i = 0; i < len; i++) {
                int[] tri = tlist.get(i);

                coords[i*9  ] = verts[tri[0] * 3    ];
                coords[i*9+1] = verts[tri[0] * 3 + 1];
                coords[i*9+2] = verts[tri[0] * 3 + 2];

                coords[i*9+3] = verts[tri[1] * 3    ];
                coords[i*9+4] = verts[tri[1] * 3 + 1];
                coords[i*9+5] = verts[tri[1] * 3 + 2];

                coords[i*9+6] = verts[tri[2] * 3    ];
                coords[i*9+7] = verts[tri[2] * 3 + 1];
                coords[i*9+8] = verts[tri[2] * 3 + 2];
            }
        }

        contentHandler.startNode("Shape", null);
        contentHandler.startField("appearance");
        contentHandler.startNode("Appearance", null);
        if (texCoords != null) {
            Texture2dGroup tg = (Texture2dGroup) base;
            Texture2d tex = (Texture2d) model.getResource(tg.getTexId());

            if (tex == null) {
                throw new IllegalArgumentException("Tex not found.  id: " + tg.getTexId());
            }
            String defName = "TEX_" + tg.getTexId();
            contentHandler.startField("texture");
            if (writtenResources.contains(tg.getTexId())) {
                contentHandler.useDecl(defName);
            }  else {
                contentHandler.startNode("ImageTexture",defName);
                contentHandler.startField("url");
                String path = convPathToUniqueUrl(tex.getPath(), tex.getPath());
                bch.fieldValue(path);
                if (!tex.getTileStyleU().equals("wrap")) {
                    // TODO: not sure how to map clamp without using TextureProperties
                    contentHandler.startField("repeatS");
                    bch.fieldValue(false);
                }
                if (!tex.getTileStyleV().equals("wrap")) {
                    // TODO: not sure how to map clamp without using TextureProperties
                    contentHandler.startField("repeatT");
                    bch.fieldValue(false);
                }
                contentHandler.endNode();   // Texture
                writtenResources.add(tg.getTexId());
            }
        }
        contentHandler.startField("material");
        contentHandler.startNode("Material", null);
        float[] color = new float[] {1f,1f,1f};
        contentHandler.startField("diffuseColor");
        bch.fieldValue(color, 1);

        contentHandler.endNode();  // Material
        contentHandler.endNode();  // Appearance
        contentHandler.startField("geometry");
        contentHandler.startNode("TriangleSet", null);
        contentHandler.startField("coord");
        contentHandler.startNode("Coordinate", null);
        contentHandler.startField("point");
        bch.fieldValue(coords, coords.length);
        contentHandler.endNode();  // Coordinate

        if (colors != null) {
            contentHandler.startField("color");
            contentHandler.startNode("Color", null);
            contentHandler.startField("color");
            bch.fieldValue(colors, colors.length);
            contentHandler.endNode();  // Color
        }

        if (texCoords != null) {
            contentHandler.startField("texCoord");
            contentHandler.startNode("TextureCoordinate", null);
            contentHandler.startField("point");
            bch.fieldValue(texCoords, texCoords.length);
            contentHandler.endNode();  // TextureCoordinate
        }

        contentHandler.endNode();  // TriangleSet
        contentHandler.endNode();  // Shape
    }

    /**
     * Converts a 3mf path to a unique url.  3mf paths are absolute references from the top of the zip.
     * Only rename the file if we have conflicts.  Write the file to the current working directory
     * @param path
     * @return
     */
    private String convPathToUniqueUrl(String path, String defaultPath) {
        if (path.startsWith("/")) {

            String fdest;
            File f;
            try {
                f = new File(tmpDir,path.substring(1));

                // Return default path if texture does not exist
                if (!f.exists()) {
                    String msg = "*** Failed converting 3mf path to unique url." +
                                 "\n    Referenced file does not exist: " + path +
                                 "\n    Setting url to: " + defaultPath;
                    System.out.println(msg);
                    return defaultPath;
                }
                
                boolean conv = needsConversion(f.getAbsolutePath());

                if (conv) {
                    fdest = FilenameUtils.getBaseName(path) + "_conv." + FilenameUtils.getExtension(path);
                } else {
                    fdest = FilenameUtils.getBaseName(path) + "." + FilenameUtils.getExtension(path);
                }

                int cnt = 0;
                while(textures.contains(fdest)) {
                    cnt++;
                    fdest = FilenameUtils.getBaseName(path) + "_" + cnt + "." + FilenameUtils.getExtension(path);
                }

                File dest = new File(fdest);
                textures.add(fdest);
                boolean copy = true;

                if (dest.exists()) {
                    if (conv || dest.length() != f.length()) {
                        dest.delete();
                    } else {
                        copy = false;
                    }
                }

                if (conv) {
                    ImageUtils iutils = new ImageUtils();
                    try {
                        iutils.convToPng32(f.getAbsolutePath(), dest.getAbsolutePath());
                    } catch(Exception e) {
                        System.err.println(e);
//                        e.printStackTrace();
                    }
                } else if (copy) {
                    FileUtils.moveFile(f, dest);
                }
            } catch(IOException ioe) {
                String msg = "*** Failed converting 3mf path to unique url." +
                             "\n    Setting url to: " + defaultPath;
                System.err.println(msg);
//                ioe.printStackTrace();
                
                // Return default path on exception
                return defaultPath;
            }

            return fdest;
        }

        return defaultPath;
    }

    /**
     * Check if an image needs conversion.  This happens for png's that are using indexed color modes.  X3D spec
     * is weird around them
     * @param path
     * @return
     */
    private boolean needsConversion(String path) throws IOException {
        String ext = FilenameUtils.getExtension(path);

        //System.out.printf("file: %s  ext: %s\n",path,ext);
        if (!ext.equalsIgnoreCase("png")) return false;

        BufferedImage img = ImageIO.read(new File(path));
        ColorModel cm = img.getColorModel();
        //System.out.printf("File: %s  CM: %s\n",path,cm);
        // TODO: I kinda expect PackedModel might need this as well

        return cm instanceof IndexColorModel;
    }


    /**
     * Write out the transform node and values.  Will not start children
     * @param trans
     */
    private void writeTransform(Matrix4d trans,String defName) {
        if (USE_MATRIX_TRANSFORM) {
            contentHandler.startNode("MatrixTransform", defName);
            contentHandler.startField("matrix");
/*
            // convert to X3D matrix row major
            float[] mat = new float[16];
            for(int r=0; r < 4; r++) {
                for(int c=0; c < 4; c++) {
                    mat[c*4+r] = (float)trans.getElement(c,r);
                }
            }
*/
            float[] mat = new float[16];
            for(int c=0; c < 4; c++) {
                for(int r=0; r < 4; r++) {
                    mat[r*4+c] = (float)trans.getElement(c,r);
                }
            }
            bch.fieldValue(mat, mat.length);
        } else {
            contentHandler.startNode("Transform", defName);

            if (trans.m30 != 0 || trans.m31 != 0 || trans.m32 != 0) {
                float[] translation = new float[3];
                translation[0] = (float) trans.m30;
                translation[1] = (float) trans.m31;
                translation[2] = (float) trans.m32;
                contentHandler.startField("translation");
                bch.fieldValue(translation, 3);
            }

            AxisAngle4f aa = new AxisAngle4f();
            aa.set(trans);
            if (aa.angle != 0) {
                float[] value = new float[4];
                aa.get(value);

                contentHandler.startField("rotation");
                bch.fieldValue(value, 4);
            }

            double s = trans.getScale();
            if (s != 1.0) {
                float[] value = new float[]{(float) s, (float) s, (float) s};
                contentHandler.startField("scale");
                bch.fieldValue(value, 3);
            }
        }

    }

    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    /**
     * Get the main model file by parsing the .rels file
     * @param relFile The relationship file
     * @return The main model or null if not found
     */
    private String getMainModel(File relFile) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(relFile);

            NodeList list = doc.getElementsByTagName("Relationship");
            for(int i=0; i < list.getLength(); i++) {
                Element n = (Element) list.item(i);
                String type = n.getAttribute("Type");
                if (type == null) continue;

                if (type.equals(MODEL_SCHEMA)) {
                    // found primary model
                    return n.getAttribute("Target");
                }
            }
        } catch(IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace(System.err);
            return null;
        }

        return null;
    }

    /**
     * Unzip a file into a destination directory
     *
     * @param src
     * @param dest
     */
    private void unzip(File src, File dest) throws IOException {
        try (ZipFile zipFile = new ZipFile(src)) {

            for (Enumeration e = zipFile.getEntries(); e.hasMoreElements(); ) {
                ZipArchiveEntry entry = (ZipArchiveEntry) e.nextElement();
                unzipEntry(zipFile, entry, dest);
            }
        }
    }

    private void unzipEntry(ZipFile zipFile, ZipArchiveEntry entry, File dest) throws IOException {

        if (entry.isDirectory()) {
            new File(dest, entry.getName()).mkdirs();
            return;
        }

        File outputFile = new File(dest, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            if (outputStream != null ) 
                outputStream.close();
            if (inputStream != null) 
                inputStream.close();
        }
    }

    public static File createTempDir(String prefix)
            throws IOException
    {
        File tmpDir = getTmpDir();
        
        if (tmpDir == null) {
            throw new IOException("Unable to create tmp dir");
        }

        File resultDir = null;
        int suffix = (int)System.currentTimeMillis();
        int failureCount = 0;
        do {
            resultDir = new File(tmpDir, prefix + suffix % 10000);
            suffix++;
            failureCount++;
        }
        while (resultDir.exists() && failureCount < 50);

        if (resultDir.exists()) {
            throw new IOException(failureCount +
                    " attempts to generate a non-existent directory name failed, giving up");
        }
        boolean created = resultDir.mkdir();
        if (!created) {
            throw new IOException("Failed to create directory: " + resultDir.getAbsolutePath());
        }

        return resultDir;
    }
    
    /**
     * Get a tmp directory. Tries the system tmpdir first.
     * Will create the BACKUP_TMP_DIR if the system tmpdir does not exist or is not writeable.
     * 
     * @return File of the tmpdir
     */
    public static File getTmpDir() {
        String tmpDirStr = System.getProperty("java.io.tmpdir");
        
        File tmpDir;
        Path p;
        
        if (tmpDirStr != null) {
            tmpDir = new File(tmpDirStr);
            p = tmpDir.toPath();
            
            if (tmpDir.exists()) {
                if (java.nio.file.Files.isWritable(p)) {
                    return tmpDir;
                }
            } else {
                boolean created = tmpDir.mkdirs();
                if (created) {
                    return tmpDir;
                }
            }
        }
        
        System.out.println("Failed to get writeable System tmp dir: " + tmpDirStr);
        System.out.println("Trying backup tmp dir: " + BACKUP_TMP_DIR);
        
        tmpDirStr = BACKUP_TMP_DIR;
        tmpDir = new File(tmpDirStr);
        p = tmpDir.toPath();
        
        if (tmpDir.exists()) {
            if (java.nio.file.Files.isWritable(p)) {
                return tmpDir;
            }
        } else {
            boolean created = tmpDir.mkdirs();
            if (created) {
                return tmpDir;
            }
        }
        
        System.out.println("Failed to get writeable tmp dir: " + tmpDirStr);
        
        return null;
    }
    
    private static String parseProperty(String prop, String defVal) {
        String ret = defVal;

        try {
            String propVal = System.getProperty(prop);
            System.out.printf("ThreeMFFileParser.  Prop: %s  val: %s\n",prop,propVal);
            if (propVal == null) return ret;
            
            ret = propVal;
            
        } catch(Exception e) {
            e.printStackTrace(System.err);
            return defVal;
        }

        return ret;
    }
}

/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer;

// External imports
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.j3d.geom.GeometryData;
import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.obj.OBJFileReader;
//import org.j3d.loaders.obj.ObjMaterial;
//import org.j3d.loaders.obj.ObjModel;
//import org.j3d.loaders.obj.ShapeData;
import org.j3d.util.ErrorReporter;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NonWeb3DFileParser;
import xj3d.filter.FilterProcessingException;
import xj3d.filter.FieldValueHandler;
import xj3d.filter.FilterExitCodes;

/**
 * File parser implementation that reads OBJ files and generates an X3D stream
 * of events.
 * 
 * 
 * NOTE: Not used in current codebase due to missing J3D classes ObjMaterial,
 * ObjModel and ShapeData
 *
 * @author Alan Hudson, Tony Wong
 */
public class OBJFileParser implements NonWeb3DFileParser {
    
    private static final String DEF_NAME_COORDS = "COORDS";
    private static final String DEF_NAME_NORMAL_COORDS = "NORM-COORDS";
    private static final String DEF_NAME_TEX_COORDS = "TEX-COORDS";

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

    /** Reference to our Locator instance to hand to users */
    private DocumentLocator documentLocator;
    
    /** Binary Content Handler reference */
    private BinaryContentHandler bch;

    /** String Content Handler reference */
    private StringContentHandler sch;
    
    /** Flag indicating that the content handler is an instance of a
    *  BinaryContentHandler, rather than a StringContentHandler */
    private boolean handlerIsBinary;
    
    private float[] rgb;

    /**
     * Create a new instance of this parser
     */
    public OBJFileParser() {
        rgb = new float[3];
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
     * {@link #Scene()} but there may be other times when it is not set.
     *
     * @return The locator used for syntax errors
     */
    @Override
    public Locator getDocumentLocator() {
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
     * @return Null if no parsing issues or a message detailing the issues
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     */
    @Override
    public List<String> parse(InputSource input, String[] style)
        throws IOException, VRMLException {

        // Not good as this opens a second network connection, rather than
        // reusing the one that is already open when we checked the MIME type.
        // Need to recode some of the OBJ parser to deal with this.
        URL url = new URL(input.getURL());

        OBJFileReader reader = null;
        try {
            reader = new OBJFileReader(url, false);
        } catch(InvalidFormatException ife) {
            throw new FilterProcessingException(ife.getMessage(),
                FilterExitCodes.INVALID_INPUT_FILE);
        }

        contentHandler.startDocument(input.getURL(),
                                     input.getBaseURL(),
                                     "utf8",
                                     "#X3D",
                                     "V3.0",
                                     "Auto converted OBJ file");

        contentHandler.profileDecl("Interchange");
        contentHandler.componentDecl("Rendering:3");

//        generateIndexedFaceSet(reader);

        contentHandler.endDocument();
        reader.close();

        return reader.getParsingMessages();
    }

//    /**
//     * Generate the coordinate and normal information for the IndexedFaceSet node
//     * based on that read from the OBJ file.
//     */
//    private void generateIndexedFaceSet(OBJFileReader rdr) throws IOException {
//        ObjModel model = rdr.getModel();
//        
//        // Define the coordinates, normals, and texture coordinates
//        generateDefs(model);
//        
//        List<ShapeData> shapes = model.shapes;
//        String name =  null;    // TODO: get object name
//        
//        for (ShapeData shape : shapes) {
//            GeometryData data = shape.getGeometryData();
//            ObjMaterial material = shape.getMaterial();
//            
//            if (data.indexes == null || data.indexes.length == 0) {
//                continue;
//            }
//            
//            contentHandler.startNode("Shape", name);
//            
//            if (material != null) {
//                createMaterial(material);
//            }
//            
//            contentHandler.startField("geometry");
//            contentHandler.startNode("IndexedFaceSet", null);
//            
//            if(handlerIsBinary) {
//                contentHandler.startField("coord");
//                contentHandler.useDecl(DEF_NAME_COORDS);
//
//                contentHandler.startField("coordIndex");
//                bch.fieldValue(data.indexes, data.indexes.length);
//
//                if (shape.isHasNormalCoords() && model.normals != null) {
//                    contentHandler.startField("normal");
//                    contentHandler.useDecl(DEF_NAME_NORMAL_COORDS);
//                }
//
//                if (shape.isHasNormalCoords() && data.normalIndexes != null) {
//                    contentHandler.startField("normalIndex");
//                    bch.fieldValue(data.normalIndexes, data.normalIndexes.length);
//                }
//
//                if (shape.isHasTextureCoords() && model.textureCoordinates != null) {
//                    contentHandler.startField("texCoord");
//                    contentHandler.useDecl(DEF_NAME_TEX_COORDS);
//                }
//
//                if (shape.isHasTextureCoords() && data.texCoordIndexes != null) {
//                    contentHandler.startField("texCoordIndex");
//                    bch.fieldValue(data.texCoordIndexes, data.texCoordIndexes.length);
//                }
//
//            } else {
//                // TODO: What does this path do? out_coords do not have real values
///*
//                //StringContentHandler sch = (StringContentHandler)contentHandler;
//                StringArray out_coords = new StringArray();
//                StringArray out_normals = new StringArray();
//
//                NumberFormat formatter = NumberFormat.getInstance();
//                formatter.setMaximumFractionDigits(5);
//
//                contentHandler.startField("coord");
//                contentHandler.startNode("Coordinate", null);
//                contentHandler.startField("point");
//                sch.fieldValue(out_coords.toArray());
//                contentHandler.endNode();
//                //contentHandler.endField();
//
//                contentHandler.startField("normal");
//                contentHandler.startNode("Normal", null);
//                contentHandler.startField("vector");
//                sch.fieldValue(out_normals.toArray());
//                contentHandler.endNode();
//                //contentHandler.endField();
//*/
//            }
//            
//            contentHandler.endNode();  // IndexedFaceSet
//            //contentHandler.endField();
//            contentHandler.endNode();  // Shape
//        }
//    }
    
    /**
     * Create DEFs for coordinates, normals, and texture coords.
     * @param model
     */
//    private void generateDefs(ObjModel model) {
//        if (model.coordinates == null || model.coordinates.length == 0) {
//            // TODO: Do something?
//            throw new FilterProcessingException("Model missing coordinates",
//                    FilterExitCodes.INVALID_INPUT_FILE);
//        }
//        
//        contentHandler.startNode("Shape", "OBJ-SHAPE-DEF-ZYX210");
//        contentHandler.startField("geometry");
//        contentHandler.startNode("IndexedFaceSet", "ifs");
//
//        // Face coordinates
//        int len = model.coordinates.length;
//
//        if (len > 0) {
//            contentHandler.startField("coord");
//            contentHandler.startNode("Coordinate", DEF_NAME_COORDS);
//            contentHandler.startField("point");
//            
//            if (handlerIsBinary) {
//                bch.fieldValue(model.coordinates, model.coordinates.length);
//            } else {
//                sch.fieldValue(FieldValueHandler.toString(model.coordinates));
////                throw new IllegalArgumentException("StringContentHandler not supported");
//            }
//            
//            contentHandler.endNode(); // Coordinate
//        }
//
//        // Normal coordinates
//        if (model.normals != null) {
//            len = model.normals.length;
//    
//            if (len > 0) {
//                if (handlerIsBinary) {
//                    contentHandler.startField("normal");
//                    contentHandler.startNode("Normal", DEF_NAME_NORMAL_COORDS);
//                    contentHandler.startField("vector");
//                    bch.fieldValue(model.normals, model.normals.length);
//                    contentHandler.endNode();
//                } else {
//                    sch.fieldValue(FieldValueHandler.toString(model.normals));
//    //                throw new IllegalArgumentException("StringContentHandler not supported");
//                }
//            }
//        }
//        
//        // Texture coordinates
//        if (model.textureCoordinates != null) {
//            len = model.textureCoordinates.length;
//            if (len > 0) {
//                if (handlerIsBinary) {
//                    contentHandler.startField("texCoord");
//                    contentHandler.startNode("TextureCoordinate", DEF_NAME_TEX_COORDS);
//                    contentHandler.startField("point");
//                    bch.fieldValue(model.textureCoordinates, model.textureCoordinates.length);
//                    contentHandler.endNode();
//                } else {
//                    sch.fieldValue(FieldValueHandler.toString(model.textureCoordinates));
//    //                throw new IllegalArgumentException("StringContentHandler not supported");
//                }
//            }
//        }
//
//        contentHandler.endNode();  // IndexedFaceSet
//        //contentHandler.endField(); // geometry
//        contentHandler.endNode();  // Shape
//    }
//    
//    /**
//     * Creates the appearance and material.
//     *
//     * @param mat The obj material.
//     * @return 
//     */
//    public boolean createMaterial(ObjMaterial mat) {
//        contentHandler.startField("appearance");
//        contentHandler.startNode("Appearance", null);
//        
//        if (mat.getRGBColor() != null) {
//            contentHandler.startField("material");
//            contentHandler.startNode("Material", null);
//            
//            float[] color = mat.getRGBColor();
//            float[] diffuse = {color[0], color[1], color[2]};
//            contentHandler.startField("diffuseColor");
//            
//            if (handlerIsBinary) {
//                bch.fieldValue(diffuse, diffuse.length);
//            } else {
//                sch.fieldValue(diffuse[0] + " " + diffuse[1] + " " + diffuse[2]);
//            }
//            
//            contentHandler.endNode();  // Material
//        }
//
//        if(mat.getTexture() != null) {
//            contentHandler.startField("texture");
//            contentHandler.startNode("ImageTexture", null);
//            contentHandler.startField("url");
//
//            if (handlerIsBinary) {
//                bch.fieldValue(mat.getTexture());
//            } else {
//                sch.fieldValue(mat.getTexture());
//            }
//
//            contentHandler.endField(); // url
//            contentHandler.endNode();  // ImageTexture
//        }
//
//        contentHandler.endNode();   // Appearance
//        //contentHandler.endField();    // appearance
//
//        return true;
//    }
}

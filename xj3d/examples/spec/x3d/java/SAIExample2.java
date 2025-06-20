/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.util.Map;

// Application specific imports
import org.web3d.x3d.sai.*;

public class SAIExample2
    implements X3DScriptImplementation {

    /** Color Constant, RED */
    private static final float[] RED = new float[] {1.0f, 0, 0};

    /** A mapping for fieldName(String) to an X3DField object */
    private Map<String, X3DField> fields;

    /** A reference to the browser */
    private Browser browser;

    /** The field to place the generated nodes via createX3DFromString */
    private MFNode children;

    //----------------------------------------------------------
    // Methods from the X3DScriptImplementation interface.
    //----------------------------------------------------------

    /**
     * Set the browser instance to be used by this script implementation.
     *
     * @param browser The browser reference to keep
     */
    @Override
    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    /**
     * Set the listing of fields that have been declared in the file for
     * this node. .
     *
     * @param externalView the external view of ourselves, so you can add routes to yourself
     *    using the standard API calls
     * @param fields The mapping of field names to instances
     */
    @Override
    public void setFields(X3DScriptNode externalView, Map<String, X3DField> fields) {
        this.fields = fields;
    }

    /**
     * Notification that the script has completed the setup and should go
     * about its own internal initialization.
     */
    @Override
    public void initialize() {
        children = (MFNode) fields.get("children");

        // Create nodes directly in the parent scene
        X3DScene scene = (X3DScene) browser.getExecutionContext();

        X3DShapeNode shape = (X3DShapeNode) scene.createNode("Shape");
        X3DGeometryNode box = (X3DGeometryNode) scene.createNode("Box");

        shape.setGeometry(box);
        scene.addRootNode(shape);

        // Create children using the createX3DFromString service
        String vrmlCmd =
            "PROFILE Interchange  Shape { geometry Sphere{} }";

        X3DScene tmpScene = browser.createX3DFromString(vrmlCmd);
        X3DNode[] nodes = tmpScene.getRootNodes();

        // Nodes must be removed before adding to another scene
        for (X3DNode node : nodes) {
            tmpScene.removeRootNode(node);
        }

        children.setValue(nodes.length,nodes);
    }

    /**
     * Notification that this script instance is no longer in use by the
     * scene graph and should now release all resources.
     */
    @Override
    public void shutdown() {
    }

    /**
     * Notification that all the events in the current cascade have finished
     * processing.
     */
    @Override
    public void eventsProcessed() {
    }
}

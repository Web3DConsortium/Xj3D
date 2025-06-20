import java.util.Map;

import org.web3d.x3d.sai.*;

public class CreateStringTest
    implements X3DScriptImplementation, X3DFieldEventListener {

    public static final int TOUCH_INPUT = 0;
    public static final String NODE_STRING = "PROFILE Immersive Shape { geometry Box {} appearance Appearance { texture ImageTexture { url [\"vts.jpg\"]}}}";

    private Browser browser;
    private SFNode targetField;
    private X3DScene scene;

    //----------------------------------------------------------
    // Methods defined by X3DScriptImplementation
    //----------------------------------------------------------

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        targetField = (SFNode) fields.get("target");
        X3DField touchInput = (X3DField) fields.get("touchInput");
        touchInput.addX3DEventListener(this);
        touchInput.setUserData(TOUCH_INPUT);
    }

    public void initialize() {
        scene = (X3DScene)browser.getExecutionContext();
    }

    public void eventsProcessed() {
    }

    public void shutdown() {
    }

    public void readableFieldChanged(X3DFieldEvent evt) {
        int eventId = ((Integer)evt.getData());

        switch(eventId) {
            case TOUCH_INPUT:
                createNode(NODE_STRING);
                break;
        }
    }

    public void createNode(String node) {
        X3DScene tmpScene = browser.createX3DFromString(node);
        X3DNode[] nodes = tmpScene.getRootNodes();

        // Nodes must be removed before adding to another scene
        for (X3DNode node1 : nodes) {
            tmpScene.removeRootNode(node1);
        }

        X3DNode wgrp = targetField.getValue();

        MFNode grp = (MFNode) wgrp.getField("set_children");
        grp.setValue(nodes.length,nodes);
    }
}
package nodetest;

import org.web3d.x3d.sai.MFImage;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/**
 * Test wrapper for the MFImage X3DField type
 */
public class tMFImage implements tX3DField {

    /**
     * The field
     */
    final MFImage field;

    /**
     * The field name
     */
    final String fieldName;

    /**
     * The field access type
     */
    final int access;

    /**
     * The node name
     */
    final String nodeName;

    /**
     * The test controller
     */
    final tController control;

    /**
     * Constructor
     *
     * @param node the <code>X3DNode</code> this field belongs to
     * @param def the <code>X3DFieldDefinition</code> of the field
     * @param control
     */
    public tMFImage(final X3DNode node, final X3DFieldDefinition def, final tController control) {
        this.nodeName = node.getNodeName();
        this.fieldName = def.getName();
        this.field = (MFImage) node.getField(fieldName);
        this.access = def.getAccessType();
        this.control = control;
    }

    /**
     * Execute a 'smoke' test.
     *
     * @return results, <code>true</code> for pass, <code>false</code> for fail
     */
    @Override
    public boolean smoke() {
        switch (access) {
            case X3DFieldTypes.INPUT_ONLY:
            case X3DFieldTypes.INITIALIZE_ONLY:
                break;
            case X3DFieldTypes.OUTPUT_ONLY:
                break;
            case X3DFieldTypes.INPUT_OUTPUT:
                break;
            default:
                control.logMessage(tMessageType.ERROR, nodeName + ":" + fieldName + " invalid access type: " + access);
                return (FAIL);
        }
        control.logMessage(tMessageType.SUCCESS, nodeName + ":" + fieldName);
        return (SUCCESS);
    }

    /**
     * Return the field value in an encoded string
     *
     * @param source the identifier of the source of the value to encode
     * @param encode the identifier of encoding scheme
     * @return the field value in an encoded string
     */
    @Override
    public String encode(final tValue source, final tEncode encode) {
        return (fieldName + ": not supported yet");
    }
}

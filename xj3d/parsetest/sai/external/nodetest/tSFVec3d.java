package nodetest;

import org.web3d.x3d.sai.SFVec3d;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/**
 * Test wrapper for the SFVec3d X3DField type
 */
public class tSFVec3d implements tX3DField {

    /**
     * Default smoke test write value
     */
    public final static double[] smoke_value = new double[]{0.1, 0.1, 0.1};

    /**
     * The field
     */
    final SFVec3d field;

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
    public tSFVec3d(final X3DNode node, final X3DFieldDefinition def, final tController control) {
        this.nodeName = node.getNodeName();
        this.fieldName = def.getName();
        this.field = (SFVec3d) node.getField(fieldName);
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
        final double[] r_value = new double[3];
        final double[] w_value = smoke_value;
        switch (access) {
            case X3DFieldTypes.INPUT_ONLY:
            case X3DFieldTypes.INITIALIZE_ONLY:
                field.setValue(w_value);
                break;
            case X3DFieldTypes.OUTPUT_ONLY:
                field.getValue(r_value);
                break;
            case X3DFieldTypes.INPUT_OUTPUT:
                //
                control.bufferUpdate();
                field.setValue(w_value);
                control.flushUpdate();
                //
                field.getValue(r_value);
                for (int i = 0; i < r_value.length; i++) {
                    if (w_value[i] != r_value[i]) {
                        control.logMessage(tMessageType.ERROR, new String[]{
                            nodeName + ":" + fieldName,
                            "\twrote [ " + w_value[0] + " " + w_value[1] + " " + w_value[2] + " ]",
                            "\tread  [ " + r_value[0] + " " + r_value[1] + " " + r_value[2] + " ]"});
                        return (FAIL);
                    }
                }
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
        double[] r_value = null;
        if (source == tValue.FIELD) {
            r_value = new double[3];
            field.getValue(r_value);
        } else if (source == tValue.SMOKE) {
            r_value = smoke_value;
        }
        if (encode == tEncode.XML) {
            return (tEncodingUtils.encodeXML(fieldName, r_value));
        } else if (encode == tEncode.CLASSIC) {
            return (tEncodingUtils.encodeClassic(fieldName, r_value));
        } else {
            return (null);
        }
    }
}

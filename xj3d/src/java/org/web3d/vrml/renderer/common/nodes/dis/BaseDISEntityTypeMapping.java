/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.renderer.common.nodes.dis;

// Standard imports
import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of EntityTypeMapping.
 * <p>
 * A data holder for mapping DIS entity information to an X3D file.
 *
 * @author Vivian Gottesman, Alan Hudson
 * @version $Revision: 1.4 $
 */
public abstract class BaseDISEntityTypeMapping extends AbstractNode {

    /** Field Index */
    protected static final int FIELD_DOMAIN = LAST_NODE_INDEX + 1;

    protected static final int FIELD_COUNTRY = LAST_NODE_INDEX + 2;

    protected static final int FIELD_CATEGORY = LAST_NODE_INDEX + 3;

    protected static final int FIELD_SUBCATEGORY = LAST_NODE_INDEX + 4;

    protected static final int FIELD_SPECIFIC = LAST_NODE_INDEX + 5;

    protected static final int FIELD_KIND = LAST_NODE_INDEX + 6;

    protected static final int FIELD_EXTRA = LAST_NODE_INDEX + 7;

    protected static final int FIELD_URL = LAST_NODE_INDEX + 8;

    /** The last field index used by this class */
    protected static final int LAST_DIS_ENTITY_MAPPING_INDEX = FIELD_URL;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_DIS_ENTITY_MAPPING_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> FIELD_MAP;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** category  SFInt32 0 */
    protected int vfEntityCategory;

    /** domain  SFInt32 0 */
    protected int vfEntityDomain;

    /** extra  SFInt32 0 */
    protected int vfEntityExtra;

    /** kind  SFInt32 0 */
    protected int vfEntityKind;

    /** entitySpecific  SFInt32 0 */
    protected int vfEntitySpecific;

    /** subcategory  SFInt32 0 */
    protected int vfEntitySubcategory;

    /** country  SFInt32 0 */
    protected int vfEntityCountry;

    /** vfUrl    SFString "" */
    protected String[] vfUrl;

    /** Factory for creating EspduTransform nodes */
    protected VRMLNodeFactory nodeFactory;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];

        FIELD_MAP = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_CATEGORY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "category");

        fieldDecl[FIELD_DOMAIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "domain");

        fieldDecl[FIELD_KIND] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "kind");

        fieldDecl[FIELD_SPECIFIC] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "specific");

        fieldDecl[FIELD_SUBCATEGORY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "subcategory");

        fieldDecl[FIELD_COUNTRY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "country");

        fieldDecl[FIELD_EXTRA] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "extra");

        fieldDecl[FIELD_URL] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "url");

        Integer idx = FIELD_METADATA;
        FIELD_MAP.put("metadata", idx);
        FIELD_MAP.put("set_metadata", idx);
        FIELD_MAP.put("metadata_changed", idx);

        idx = FIELD_CATEGORY;
        FIELD_MAP.put("category", idx);
        FIELD_MAP.put("set_category", idx);
        FIELD_MAP.put("category_changed", idx);

        idx = FIELD_COUNTRY;
        FIELD_MAP.put("country", idx);
        FIELD_MAP.put("set_country", idx);
        FIELD_MAP.put("country_changed", idx);

        idx = FIELD_DOMAIN;
        FIELD_MAP.put("domain", idx);
        FIELD_MAP.put("set_domain", idx);
        FIELD_MAP.put("domain_changed", idx);

        idx = FIELD_EXTRA;
        FIELD_MAP.put("extra", idx);
        FIELD_MAP.put("set_extra", idx);
        FIELD_MAP.put("extra_changed", idx);

        idx = FIELD_KIND;
        FIELD_MAP.put("kind", idx);
        FIELD_MAP.put("set_kind", idx);
        FIELD_MAP.put("kind_changed", idx);

        idx = FIELD_SPECIFIC;
        FIELD_MAP.put("specific", idx);
        FIELD_MAP.put("set_specific", idx);
        FIELD_MAP.put("specific_changed", idx);

        idx = FIELD_SUBCATEGORY;
        FIELD_MAP.put("subcategory", idx);
        FIELD_MAP.put("set_subcategory", idx);
        FIELD_MAP.put("subcategory_changed", idx);

        idx = FIELD_URL;
        FIELD_MAP.put("url", idx);
        FIELD_MAP.put("set_url", idx);
        FIELD_MAP.put("url_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseDISEntityTypeMapping() {
        super("DISEntityTypeMapping");

        hasChanged = new boolean[LAST_DIS_ENTITY_MAPPING_INDEX + 1];

        vfEntityCategory = 0;
        vfEntityCountry = 0;
        vfEntityDomain = 0;
        vfEntityKind = 0;
        vfEntitySpecific = 0;
        vfEntityExtra = 0;
        vfEntitySubcategory = 0;
        vfUrl = FieldConstants.EMPTY_MFSTRING;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseDISEntityTypeMapping(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("category");
            VRMLFieldData field = node.getFieldValue(index);
            vfEntityCategory = field.intValue;

            index = node.getFieldIndex("domain");
            field = node.getFieldValue(index);
            vfEntityDomain = field.intValue;

            index = node.getFieldIndex("extra");
            field = node.getFieldValue(index);
            vfEntityExtra = field.intValue;

            index = node.getFieldIndex("kind");
            field = node.getFieldValue(index);
            vfEntityKind = field.intValue;

            index = node.getFieldIndex("specific");
            field = node.getFieldValue(index);
            vfEntitySpecific = field.intValue;

            index = node.getFieldIndex("subcategory");
            field = node.getFieldValue(index);
            vfEntitySubcategory = field.intValue;

            index = node.getFieldIndex("country");
            field = node.getFieldValue(index);
            vfEntityCountry = field.intValue;

            index = node.getFieldIndex("url");
            field = node.getFieldValue(index);

            if (field.numElements != 0) {
                vfUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfUrl, 0,
                  field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    @Override
    public int getPrimaryType() {
        return TypeConstants.InfoNodeType;
    }

    @Override
    public void setupFinished() {
        if(!inSetup) {
            return;
        }

        super.setupFinished();

        inSetup = false;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType interface.
    //----------------------------------------------------------

    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = FIELD_MAP.get(fieldName);

        return (index == null) ? -1 : index;
    }

    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_DIS_ENTITY_MAPPING_INDEX) {
            return null;
        }

        return fieldDecl[index];
    }

    @Override
    public int getNumFields() {
        return fieldDecl.length;
    }

    @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        fieldData.clear();
        fieldData.numElements = 1;

        switch(index) {

            case FIELD_URL:
                fieldData.clear();
                fieldData.stringArrayValues = vfUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfUrl.length;
                break;

            case FIELD_CATEGORY:
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityCategory;
                break;

            case FIELD_COUNTRY:
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityCountry;
                break;

            case FIELD_SUBCATEGORY:
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntitySubcategory;
                break;

            case FIELD_DOMAIN:
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityDomain;
                break;

            case FIELD_KIND:
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityKind;
                break;

            case FIELD_SPECIFIC:
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntitySpecific;
                break;

            case FIELD_EXTRA:
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.intValue = vfEntityExtra;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        try {
            switch(srcIndex) {
                case FIELD_CATEGORY:
                    destNode.setValue(destIndex, vfEntityCategory);
                    break;
                case FIELD_DOMAIN:
                    destNode.setValue(destIndex, vfEntityDomain);
                    break;
                case FIELD_COUNTRY:
                    destNode.setValue(destIndex, vfEntityCountry);
                    break;
                case FIELD_SUBCATEGORY:
                    destNode.setValue(destIndex, vfEntitySubcategory);
                    break;
                case FIELD_SPECIFIC:
                    destNode.setValue(destIndex, vfEntitySpecific);
                    break;
                case FIELD_KIND:
                    destNode.setValue(destIndex, vfEntityKind);
                    break;
                case FIELD_EXTRA:
                    destNode.setValue(destIndex, vfEntityExtra);
                    break;
                case FIELD_URL:
                    destNode.setValue(destIndex, vfUrl, vfUrl.length);
                    break;
                default: super.sendRoute(time,srcIndex,destNode,destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTransform.sendRoute: No field!" + srcIndex);
            ife.printStackTrace(System.err);
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_KIND:
                vfEntityKind = value;

                if(!inSetup) {
                    hasChanged[FIELD_KIND] = true;
                    fireFieldChanged(FIELD_KIND);
                }
                break;
            case FIELD_COUNTRY:
                vfEntityCountry = value;

                if(!inSetup) {
                    hasChanged[FIELD_COUNTRY] = true;
                    fireFieldChanged(FIELD_COUNTRY);
                }
                break;
            case FIELD_CATEGORY:
                vfEntityCategory = value;

                if(!inSetup) {
                    hasChanged[FIELD_CATEGORY] = true;
                    fireFieldChanged(FIELD_CATEGORY);
                }
                break;
            case FIELD_SUBCATEGORY:
                vfEntitySubcategory = value;

                if(!inSetup) {
                    hasChanged[FIELD_SUBCATEGORY] = true;
                    fireFieldChanged(FIELD_SUBCATEGORY);
                }
                break;
            case FIELD_SPECIFIC:
                vfEntitySpecific = value;

                if(!inSetup) {
                    hasChanged[FIELD_SPECIFIC] = true;
                    fireFieldChanged(FIELD_SPECIFIC);
                }
                break;
            case FIELD_EXTRA:
                vfEntityExtra = value;

                if(!inSetup) {
                    hasChanged[FIELD_EXTRA] = true;
                    fireFieldChanged(FIELD_EXTRA);
                }
                break;
            case FIELD_DOMAIN:
                vfEntityDomain = value;

                if(!inSetup) {
                    hasChanged[FIELD_DOMAIN] = true;
                    fireFieldChanged(FIELD_DOMAIN);
                }
                break;
            default:
                super.setValue(index, value);
        }
    }

    @Override
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_URL:
                vfUrl = new String[1];
                vfUrl[0] = value;


                if (!inSetup) {
                    fireUrlChanged(index);
                }

                break;


            default:
                super.setValue(index, value);
        }

    }

    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_URL :
                vfUrl = new String[numValid];

                System.arraycopy(value, 0, vfUrl, 0, numValid);

                if (!inSetup) {
                    fireUrlChanged(index);
                }

                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireUrlChanged(int index) {
    // TODO: Implement
System.out.println("DISEntityTypeMappping url changed, need to handle");
/*
        // Notify listeners of new value
        int num_listeners = urlListeners.size();
        VRMLUrlListener ul;

        for(int i = 0; i < num_listeners; i++) {
            ul = (VRMLUrlListener)urlListeners.get(i);
            ul.urlChanged(this, index);
        }
*/
    }
}

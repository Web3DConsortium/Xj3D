/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.enveffects;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseBindableNode;

import org.web3d.vrml.util.URLChecker;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base implementation of a Background node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.22 $
 */
public abstract class BaseBackground extends BaseBindableNode
    implements VRMLBackgroundNodeType, VRMLMultiExternalNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.BindableNodeType,
        TypeConstants.MultiExternalNodeType
    };

    /** Index of the groundAngle field */
    protected static final int FIELD_GROUND_ANGLE = LAST_BINDABLE_INDEX + 1;

    /** Index of the groundColor field */
    protected static final int FIELD_GROUND_COLOR = LAST_BINDABLE_INDEX + 2;

    /** Index of the skyAngle field */
    protected static final int FIELD_SKY_ANGLE = LAST_BINDABLE_INDEX + 3;

    /** Index of the skyColor field */
    protected static final int FIELD_SKY_COLOR = LAST_BINDABLE_INDEX + 4;

    /** Index of the backUrl field */
    protected static final int FIELD_BACK_URL = LAST_BINDABLE_INDEX + 5;

    /** Index of the frontUrl field */
    protected static final int FIELD_FRONT_URL = LAST_BINDABLE_INDEX + 6;

    /** Index of the leftUrl field */
    protected static final int FIELD_LEFT_URL = LAST_BINDABLE_INDEX + 7;

    /** Index of the rightUrl field */
    protected static final int FIELD_RIGHT_URL = LAST_BINDABLE_INDEX + 8;

    /** Index of the bottomUrl field */
    protected static final int FIELD_BOTTOM_URL = LAST_BINDABLE_INDEX + 9;

    /** Index of the topUrl field */
    protected static final int FIELD_TOP_URL = LAST_BINDABLE_INDEX + 10;

    /** Index of the topUrl field */
    protected static final int FIELD_TRANSPARENCY = LAST_BINDABLE_INDEX + 11;

    // Local working constants

    /** The last field index used by this class */
    protected static final int LAST_BACKGROUND_INDEX = FIELD_TRANSPARENCY;

    /** The number of fields implemented */
    protected static final int NUM_FIELDS = LAST_BACKGROUND_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;


    /** An empty list of URL fields for initialization */
    protected static final String[] EMPTY_LIST = {};

    // Side constants for readability when generating the background box.
    // Also used to control the shown SwitchGroup as children

    /**
     *
     */
    protected static final int BACK   = 0;

    /**
     *
     */
    protected static final int FRONT  = 1;

    /**
     *
     */
    protected static final int LEFT   = 2;

    /**
     *
     */
    protected static final int RIGHT  = 3;

    /**
     *
     */
    protected static final int TOP    = 4;

    /**
     *
     */
    protected static final int BOTTOM = 5;

    /**
     *
     */
    protected static final int SKY_SPHERE = 6;

    /**
     *
     */
    protected static final int GROUND_SPHERE = 7;

    /**
     *
     */
    protected static final int NUM_BG_OBJECTS = 8;

    // Common for all instances

    /** The array of fields that need URL content */
    protected static final int[] urlFieldIndexList;

    /** The class types that we want for our images to load */
    private static final Class[] requiredImageTypes;

    // Field declarations

    /** The world URL for correcting relative URL values */
    protected String worldURL;

    /** Flag to indicate if we've checked the URLs for relative references */
    protected boolean urlRelativeCheck;

    /** The state of the load for the various fields */
    protected int[] loadState;

    /** List of loaded URI strings */
    protected String[] loadedUri;

    /** MFString backUrl list */
    protected String[] vfBackUrl;

    /** MFString frontUrl list */
    protected String[] vfFrontUrl;

    /** MFString leftUrl list */
    protected String[] vfLeftUrl;

    /** MFString rightUrl list */
    protected String[] vfRightUrl;

    /** MFString topUrl list */
    protected String[] vfTopUrl;

    /** MFString bottomUrl list */
    protected String[] vfBottomUrl;

    /** MFFloat groundAngle */
    protected float[] vfGroundAngle;

    /** MFColor groundColor */
    protected float[] vfGroundColor;

    /** MFFloat skyAngle */
    protected float[] vfSkyAngle;

    /** MFColor skyColor */
    protected float[] vfSkyColor;

    /** SFFloat transparency */
    protected float vfTransparency;

    /** Number of valid values in vfGroundAngle */
    protected int numGroundAngle;

    /** Number of valid values in vfGroundColor */
    protected int numGroundColor;

    /** Number of valid values in vfSkyAngle */
    protected int numSkyAngle;

    /** Number of valid values in vfSkyColor */
    protected int numSkyColor;

    /** Flag indicating a spec version threshold */
    protected boolean isVersionPost_3_2;

    /** List of those who want to know about url changes. Likely 1 */
    private List<VRMLUrlListener> urlListeners;

    /** List of those who want to know about content state changes. Likely 1 */
    private List<VRMLContentStateListener> contentListeners;

    /**
     * Static constructor builds the type lists for use by all instances as
     * well as the field handling.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BIND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "set_bind");
        fieldDecl[FIELD_IS_BOUND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isBound");
        fieldDecl[FIELD_BIND_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "bindTime");
        fieldDecl[FIELD_GROUND_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "groundAngle");
        fieldDecl[FIELD_GROUND_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFColor",
                                     "groundColor");
        fieldDecl[FIELD_SKY_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "skyAngle");
        fieldDecl[FIELD_SKY_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFColor",
                                     "skyColor");
        fieldDecl[FIELD_BACK_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "backUrl");
        fieldDecl[FIELD_FRONT_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "frontUrl");
        fieldDecl[FIELD_LEFT_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "leftUrl");
        fieldDecl[FIELD_RIGHT_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "rightUrl");
        fieldDecl[FIELD_TOP_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "topUrl");
        fieldDecl[FIELD_BOTTOM_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "bottomUrl");
        fieldDecl[FIELD_TRANSPARENCY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "transparency");

        fieldMap.put("set_bind", FIELD_BIND);
        fieldMap.put("isBound", FIELD_IS_BOUND);

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_BIND_TIME;
        fieldMap.put("bindTime", idx);
        fieldMap.put("bindTime_changed", idx);

        idx = FIELD_BACK_URL;
        fieldMap.put("backUrl", idx);
        fieldMap.put("set_backUrl", idx);
        fieldMap.put("backUrl_changed", idx);

        idx = FIELD_FRONT_URL;
        fieldMap.put("frontUrl", idx);
        fieldMap.put("set_frontUrl", idx);
        fieldMap.put("frontUrl_changed", idx);

        idx = FIELD_LEFT_URL;
        fieldMap.put("leftUrl", idx);
        fieldMap.put("set_leftUrl", idx);
        fieldMap.put("leftUrl_changed", idx);

        idx = FIELD_RIGHT_URL;
        fieldMap.put("rightUrl", idx);
        fieldMap.put("set_rightUrl", idx);
        fieldMap.put("rightUrl_changed", idx);

        idx = FIELD_TOP_URL;
        fieldMap.put("topUrl", idx);
        fieldMap.put("set_topUrl", idx);
        fieldMap.put("topUrl_changed", idx);

        idx = FIELD_BOTTOM_URL;
        fieldMap.put("bottomUrl", idx);
        fieldMap.put("set_bottomUrl", idx);
        fieldMap.put("bottomUrl_changed", idx);

        idx = FIELD_GROUND_ANGLE;
        fieldMap.put("groundAngle", idx);
        fieldMap.put("set_groundAngle", idx);
        fieldMap.put("groundAngle_changed", idx);

        idx = FIELD_GROUND_COLOR;
        fieldMap.put("groundColor", idx);
        fieldMap.put("set_groundColor", idx);
        fieldMap.put("groundColor_changed", idx);

        idx = FIELD_SKY_ANGLE;
        fieldMap.put("skyAngle", idx);
        fieldMap.put("set_skyAngle", idx);
        fieldMap.put("skyAngle_changed", idx);

        idx = FIELD_SKY_COLOR;
        fieldMap.put("skyColor", idx);
        fieldMap.put("set_skyColor", idx);
        fieldMap.put("skyColor_changed", idx);

        idx = FIELD_TRANSPARENCY;
        fieldMap.put("transparency", idx);
        fieldMap.put("set_transparency", idx);
        fieldMap.put("transparency_changed", idx);

        urlFieldIndexList = new int[6];
        urlFieldIndexList[0] = FIELD_BACK_URL;
        urlFieldIndexList[1] = FIELD_FRONT_URL;
        urlFieldIndexList[2] = FIELD_LEFT_URL;
        urlFieldIndexList[3] = FIELD_RIGHT_URL;
        urlFieldIndexList[4] = FIELD_TOP_URL;
        urlFieldIndexList[5] = FIELD_BOTTOM_URL;

        requiredImageTypes = null;
    }

    /**
     * Create a new, default instance of this class.
     */
    protected BaseBackground() {
        super("Background");

        urlListeners = new ArrayList<>(1);
        contentListeners = new ArrayList<>(1);

        hasChanged = new boolean[NUM_FIELDS];
        loadState = new int[NUM_FIELDS];
        loadedUri = new String[NUM_FIELDS];

        vfBackUrl = FieldConstants.EMPTY_MFSTRING;
        vfFrontUrl = FieldConstants.EMPTY_MFSTRING;
        vfLeftUrl = FieldConstants.EMPTY_MFSTRING;
        vfRightUrl = FieldConstants.EMPTY_MFSTRING;
        vfTopUrl = FieldConstants.EMPTY_MFSTRING;
        vfBottomUrl = FieldConstants.EMPTY_MFSTRING;

        vfSkyColor = new float[] {0, 0, 0};

        vfGroundAngle = FieldConstants.EMPTY_MFFLOAT;
        vfGroundColor = FieldConstants.EMPTY_MFFLOAT;
        vfSkyAngle = FieldConstants.EMPTY_MFFLOAT;
        vfTransparency = 0;

        numGroundAngle = 0;
        numGroundColor = 0;
        numSkyAngle = 0;
        numSkyColor = 3;

        urlRelativeCheck = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     *  <p>
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type.
     */
    protected BaseBackground(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index;
            VRMLFieldData field;

            index = node.getFieldIndex("transparency");
            field = node.getFieldValue(index);

            vfTransparency = field.floatValue;

            index = node.getFieldIndex("backUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfBackUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfBackUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("frontUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfFrontUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfFrontUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("leftUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfLeftUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfLeftUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("rightUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfRightUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfRightUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("topUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfTopUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfTopUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("bottomUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfBottomUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfBottomUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("groundAngle");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfGroundAngle = new float[field.numElements];
                System.arraycopy(field.floatArrayValues,0,vfGroundAngle,0,
                    field.numElements);

                numGroundAngle = field.numElements;
            }

            index = node.getFieldIndex("groundColor");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfGroundColor = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValues,0,vfGroundColor,0,
                    field.numElements * 3);

                numGroundColor = field.numElements * 3;
            }

            index = node.getFieldIndex("skyAngle");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfSkyAngle = new float[field.numElements];
                System.arraycopy(field.floatArrayValues,0,vfSkyAngle,0,
                    field.numElements);

                numSkyAngle = field.numElements;
            }

            index = node.getFieldIndex("skyColor");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfSkyColor = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValues,0,vfSkyColor,0,
                    field.numElements * 3);

                numSkyColor = field.numElements * 3;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLBackgroundNodeType
    //----------------------------------------------------------

    @Override
    public float getTransparency() {
        return( vfTransparency );
    }

    @Override
    public void setTransparency(float val) {
        vfTransparency = val;

        if(!inSetup) {
            hasChanged[FIELD_TRANSPARENCY] = true;
            fireFieldChanged(FIELD_TRANSPARENCY);
        }
    }

    @Override
    public int getNumSkyColors() {
        return numSkyColor / 3;
    }

    @Override
    public int getNumGroundColors() {
        return numGroundColor / 3;
    }

    @Override
    public void getSkyValues(float[] color, float[] angle) {
        System.arraycopy(vfSkyColor, 0, color, 0, numSkyColor);

        if(numSkyAngle != 0)
            System.arraycopy(vfSkyAngle, 0, angle, 0, numSkyAngle);
    }

    @Override
    public void getGroundValues(float[] color, float[] angle) {
        System.arraycopy(vfGroundColor, 0, color, 0, numGroundColor);

        if(numGroundAngle != 0)
            System.arraycopy(vfGroundAngle, 0, angle, 0, numGroundAngle);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLMultiExternalNodeType
    //----------------------------------------------------------

    @Override
    public int[] getUrlFieldIndexes() {
        return urlFieldIndexList;
    }

    @Override
    public String[] getUrl(int index) throws InvalidFieldException {

        String[] ret_val = null;

        switch(index) {
            case FIELD_BACK_URL:
                ret_val = vfBackUrl;
                break;

            case FIELD_FRONT_URL:
                ret_val = vfFrontUrl;
                break;

            case FIELD_LEFT_URL:
                ret_val = vfLeftUrl;
                break;

            case FIELD_RIGHT_URL:
                ret_val = vfRightUrl;
                break;

            case FIELD_TOP_URL:
                ret_val = vfTopUrl;
                break;

            case FIELD_BOTTOM_URL:
                ret_val = vfBottomUrl;
                break;

            default:
                throw new InvalidFieldException("getURL invalid index");
        }

        return ret_val;
    }

    @Override
    public int getLoadState(int index) {
        return loadState[index];
    }

    @Override
    public void setLoadState(int index, int state) {
        loadState[index] = state;
        fireContentStateChanged(index);
    }

    @Override
    public boolean checkValidContentType(int index, String mimetype) {
        // Only accept content that is images.
        return mimetype.startsWith("image/");
    }

    @Override
    public Class[] getPreferredClassTypes(int index)
        throws InvalidFieldException {
        return requiredImageTypes;
    }

    @Override
    public void setLoadedURI(int fieldIdx, String uri) {
        loadedUri[fieldIdx] = uri;
    }

    @Override
    public void addUrlListener(VRMLUrlListener ul) {
        if(!urlListeners.contains(ul))
            urlListeners.add(ul);
    }

    @Override
    public void removeUrlListener(VRMLUrlListener ul) {
        urlListeners.remove(ul);
    }

    @Override
    public void addContentStateListener(VRMLContentStateListener l) {
        if(!contentListeners.contains(l))
            contentListeners.add(l);
    }

    @Override
    public void removeContentStateListener(VRMLContentStateListener l) {
        contentListeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExternalNodeType
    //----------------------------------------------------------

    @Override
    public void setWorldUrl(String url) {
        if((url == null) || (url.length() == 0))
            return;

        // check for a trailing slash. If it doesn't have one, append it.
        if(url.charAt(url.length() - 1) != '/') {
            worldURL = url + '/';
        } else {
            worldURL = url;
        }

        checkURLs();
    }

    @Override
    public String getWorldUrl() {
        return worldURL;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    @Override
    public void setVersion( int major, int minor, boolean isStatic ) {
        super.setVersion( major, minor, isStatic );

        isVersionPost_3_2 =
            ( vrmlMajorVersion > 3 ) | (( vrmlMajorVersion == 3 ) && ( vrmlMinorVersion >= 2 ));
    }

    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();
        checkURLs();
    }

    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = fieldMap.get(fieldName);
        int idxValue = (index == null) ? -1 : index;
        if ( idxValue == FIELD_TRANSPARENCY ) {
            // the transparency field was added to the background
            // node as of spec version 3.2
            if ( !isVersionPost_3_2 ) {
                // profess ignorance of this field if an earlier
                // version of the spec is in use
                idxValue = -1;
            }
        }
        return( idxValue );
    }

    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_BACKGROUND_INDEX)
            return null;

        return fieldDecl[index];
    }

    @Override
    public int getNumFields() {
        int numFields = fieldDecl.length;
        if ( !isVersionPost_3_2 ) {
            // adjust to account for the transparency field NOT
            // being present prior to version 3.2 of the spec
            numFields--;
        }
        return( numFields );
    }

    @Override
    public int getPrimaryType() {
        return TypeConstants.BackgroundNodeType;
    }

    @Override
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

    @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_BACK_URL:
                fieldData.clear();
                fieldData.stringArrayValues = vfBackUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfBackUrl.length;
                break;

            case FIELD_FRONT_URL:
                fieldData.clear();
                fieldData.stringArrayValues = vfFrontUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfFrontUrl.length;
                break;

            case FIELD_TOP_URL:
                fieldData.clear();
                fieldData.stringArrayValues = vfTopUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfTopUrl.length;
                break;

            case FIELD_BOTTOM_URL:
                fieldData.clear();
                fieldData.stringArrayValues = vfBottomUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfBottomUrl.length;
                break;

            case FIELD_LEFT_URL:
                fieldData.clear();
                fieldData.stringArrayValues = vfLeftUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfLeftUrl.length;
                break;

            case FIELD_RIGHT_URL:
                fieldData.clear();
                fieldData.stringArrayValues = vfRightUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfRightUrl.length;
                break;

            case FIELD_SKY_COLOR:
                fieldData.clear();
                fieldData.floatArrayValues = vfSkyColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numSkyColor / 3;
                break;

            case FIELD_SKY_ANGLE:
                fieldData.clear();
                fieldData.floatArrayValues = vfSkyAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numSkyAngle;
                break;

            case FIELD_GROUND_COLOR:
                fieldData.clear();
                fieldData.floatArrayValues = vfGroundColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numGroundColor / 3;
                break;

            case FIELD_GROUND_ANGLE:
                fieldData.clear();
                fieldData.floatArrayValues = vfGroundAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numGroundAngle;
                break;

            case FIELD_TRANSPARENCY:
                fieldData.clear();
                fieldData.floatValue = vfTransparency;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
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

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_BACK_URL:
                    destNode.setValue(destIndex, vfBackUrl, vfBackUrl.length);
                    break;

                case FIELD_FRONT_URL:
                    destNode.setValue(destIndex, vfFrontUrl, vfFrontUrl.length);
                    break;

                case FIELD_LEFT_URL:
                    destNode.setValue(destIndex, vfLeftUrl, vfLeftUrl.length);
                    break;

                case FIELD_RIGHT_URL:
                    destNode.setValue(destIndex, vfRightUrl, vfRightUrl.length);
                    break;

                case FIELD_TOP_URL:
                    destNode.setValue(destIndex, vfTopUrl, vfTopUrl.length);
                    break;

                case FIELD_BOTTOM_URL:
                    destNode.setValue(destIndex, vfBottomUrl, vfBottomUrl.length);
                    break;

                case FIELD_GROUND_ANGLE:
                    destNode.setValue(destIndex, vfGroundAngle, numGroundAngle);
                    break;

                case FIELD_GROUND_COLOR:
                    destNode.setValue(destIndex, vfGroundColor, numGroundColor);
                    break;

                case FIELD_SKY_ANGLE:
                    destNode.setValue(destIndex, vfSkyAngle, numSkyAngle);
                    break;

                case FIELD_SKY_COLOR:
                    destNode.setValue(destIndex, vfSkyColor, numSkyColor);
                    break;

                case FIELD_TRANSPARENCY:
                    destNode.setValue(destIndex, vfTransparency);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TRANSPARENCY:
               setTransparency(value);
               break;

            default:
                super.setValue(index, value);
        }
    }

    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GROUND_ANGLE:
                if(numValid > vfGroundAngle.length)
                    vfGroundAngle = new float[numValid];

                System.arraycopy(value, 0, vfGroundAngle, 0, numValid);
                numGroundAngle = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_GROUND_ANGLE] = true;
                    fireFieldChanged(FIELD_GROUND_ANGLE);
                }
                break;

            case FIELD_GROUND_COLOR:
                FieldValidator.checkColorArray("Background.GroundColor", value);

                if(numValid > vfGroundColor.length)
                    vfGroundColor = new float[numValid];

                System.arraycopy(value, 0, vfGroundColor, 0, numValid);
                numGroundColor = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_GROUND_COLOR] = true;
                    fireFieldChanged(FIELD_GROUND_COLOR);
                }
                break;

            case FIELD_SKY_ANGLE:
                if(value.length > vfSkyAngle.length)
                    vfSkyAngle = new float[numValid];

                System.arraycopy(value, 0, vfSkyAngle, 0, numValid);
                numSkyAngle = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_SKY_ANGLE] = true;
                    fireFieldChanged(FIELD_SKY_ANGLE);
                }
                break;

            case FIELD_SKY_COLOR:
                FieldValidator.checkColorArray("Background.SkyColor", value);
                if(numValid > vfSkyColor.length)
                    vfSkyColor = new float[numValid];

                System.arraycopy(value, 0, vfSkyColor, 0, numValid);
                numSkyColor = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_SKY_COLOR] = true;
                    fireFieldChanged(FIELD_SKY_COLOR);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BACK_URL:
                if(vfBackUrl.length != numValid)
                    vfBackUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfBackUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_BACK_URL] = true;
                    fireFieldChanged(FIELD_BACK_URL);
                }
                break;

            case FIELD_FRONT_URL:
                if(vfFrontUrl.length != numValid)
                    vfFrontUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfFrontUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_FRONT_URL] = true;
                    fireFieldChanged(FIELD_FRONT_URL);
                }
                break;

            case FIELD_LEFT_URL:
                if(vfLeftUrl.length != numValid)
                    vfLeftUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfLeftUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_LEFT_URL] = true;
                    fireFieldChanged(FIELD_LEFT_URL);
                }
                break;

            case FIELD_RIGHT_URL:
                if(vfRightUrl.length != numValid)
                    vfRightUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfRightUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_RIGHT_URL] = true;
                    fireFieldChanged(FIELD_RIGHT_URL);
                }
                break;

            case FIELD_TOP_URL:
                if(vfTopUrl.length != numValid)
                    vfTopUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfTopUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_TOP_URL] = true;
                    fireFieldChanged(FIELD_TOP_URL);
                }
                break;

            case FIELD_BOTTOM_URL:
                if(vfBottomUrl.length != numValid)
                    vfBottomUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfBottomUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_BOTTOM_URL] = true;
                    fireFieldChanged(FIELD_BOTTOM_URL);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Check the URL arrays for relative references. If found, add the
     * base URL to it to make them all fully qualified. This will also set the
     * urlRelativeCheck flag to true.
     */
    private void checkURLs() {
        URLChecker.checkURLsInPlace(worldURL, vfBackUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfFrontUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfLeftUrl, false);
        URLChecker.checkURLsInPlace(worldURL,  vfRightUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfTopUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfBottomUrl, false);
    }

    /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireUrlChanged(int index) {
        // Notify listeners of new value
        int num_listeners = urlListeners.size();
        VRMLUrlListener ul;

        for(int i = 0; i < num_listeners; i++) {
            ul = urlListeners.get(i);
            ul.urlChanged(this, index);
        }
    }

    /**
     * Send a notification to the registered listeners that the content state
     * has been changed. If no listeners have been registered, then this does
     * nothing, so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireContentStateChanged(int index) {
        // Notify listeners of new value
        int num_listeners = contentListeners.size();
        VRMLContentStateListener csl;

        for(int i = 0; i < num_listeners; i++) {
            csl = contentListeners.get(i);
            csl.contentStateChanged(this, index, loadState[index]);
        }
    }
}

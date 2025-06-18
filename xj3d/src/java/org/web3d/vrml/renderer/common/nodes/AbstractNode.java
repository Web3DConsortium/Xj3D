/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;
import org.j3d.util.IntHashMap;

// Local imports
import org.web3d.util.PropertyTools;

import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldAccessException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;

import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.NodeListenerMulticaster;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLFieldDataThreaded;
import org.web3d.vrml.nodes.VRMLMetadataObjectNodeType;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;

/**
 * Base node for all implementations that define their own field handling.
 * <p>
 * Each node will keep its own fieldDeclarations and fieldMaps.  These will be
 * created in a static constructor so only one copy per class will be created.
 * <p>
 *
 * Each node will maintain its own LAST_*_INDEX which tells others what the
 * last field declared by this node.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>vrmlMetaMsg: Error when someone attempts to set meta data objects in
 *    in a VRML97 scene.</li>
 * <li>badNodeCopyMsg: Attempting to copy a node that doesn't have the right
 *     type match.</li>
 * <li>defSetTimingMsg: Caller attempting to set the DEF name for this node
 *     after it has been realised.</li>
 * <li>noLayerRefMsg: In doing our layer reference counting, the caller gave
 *     us an unknown layer ID</li>
 * <li>invalidNodeMsg: The node given is not a node of the required type. The
 *     message is dynamically generated with the node type given as an argument
 *     to the generation method.</li>
 * <li>invalidProtoMsg: The proto given is not a node of the required type. The
 *     message is dynamically generated with the node type given as an argument
 *     to the generation method.</li>
 * <li>readOnlyWriteMsg: Attempting to write to an initializeOnly/field field
 *     after the  node has been realised.</li>
 * <li>outputOnlyWriteMsg: Attempting to write to an outputOnly/eventOut field</li>
 * <li>inputOnlyWriteMsg: Attempting to write to an inputOnly/eventIn field
 *     before the node has been realised.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.32 $
 */
public abstract class AbstractNode implements VRMLNodeType {

    /** The field index for the metadata node field */
    public static final int FIELD_METADATA = 0;

    /** The last field index used by this class */
    public static final int LAST_NODE_INDEX = 0;

    /** Property describing whether the sceneGraph will be static.
        Static means to add or remove nodes */
    private static final String STATIC_SG_PROP =
        "org.web3d.vrml.renderer.common.nodes.staticSceneGraph";

    /** Is the scene graph static */
    public static final boolean STATIC_SCENE_GRAPH;

    /** Message when a user tries to set metadata field in VRML97 */
    private static final String VRML_META_ERR_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.vrmlMetaMsg";

    /**
     * A standard message for when the supplied node is wrong. Just add the
     * node name of the wrong type to the end.
     */
    private static final String BAD_NODE_MSG_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.badNodeCopyMsg";

    /** Message when a user tries to set metadata field in VRML97 */
    private static final String DEF_SET_TIMING_ERR_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.defSetTimingMsg";

    /**
     * Standard message for when the user is attempting to set a value for
     * an initializeOnly field (field access field in VRML97 terms) after the
     * setup process is complete.
     */
    private static final String READ_ONLY_WRITE_MSG_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.readOnlyWriteMsg";

    /**
     * Standard message for when the user is attempting to write to an
     * output-only field (eventOut in VRML97 terms).
     */
    private static final String OUT_ONLY_WRITE_MSG_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.outputOnlyWriteMsg";

    /**
     * Standard message for when the user is attempting to write to an
     * input-only field (eventInt in VRML97 terms) before the node is realised.
     */
    private static final String IN_ONLY_WRITE_MSG_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.inputOnlyWriteMsg";

    /** Message for when the proto is not a Metadata */
    private static final String NOT_REQD_PROTO_MSG_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.invalidProtoMsg";

    /** Message for when the node in setValue() is not a Metadata */
    private static final String NOT_REQD_NODE_MSG_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.invalidNodeMsg";

    /**
     * Message when the reference count attempts to decrement a layer ID that
     * does not currently reference this node. Under correct implementation,
     * this message should never be seen.
     */
    private static final String NO_LAYER_REF_PROP =
        "org.web3d.vrml.renderer.common.nodes.AbstractNode.noLayerRefMsg";

    /** The name of this node */
    protected final String nodeName;

    /** Scratch class var for returning field data. Assigned at construction. */
    protected final ThreadLocal<VRMLFieldData> fieldLocalData;

    /** Mapping of field index to user data object */
    private IntHashMap<Object> userData;

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    /** hasChanged flags for fields */
    protected boolean[] hasChanged;

    /** Is this node still being setup/parsed.  Cleared by setupFinished */
    protected boolean inSetup;

    /** Flag indicating this is a DEF node */
    protected boolean isDEF;

    /** Flag for the node being static */
    protected boolean isStatic;

    /** The major version of the spec this instance belongs to. */
    protected int vrmlMajorVersion;

    /** The minor version of the spec this instance belongs to. */
    protected int vrmlMinorVersion;

    /** State manager for propagating updates */
    protected FrameStateManager stateManager;

    /** The current listener(s) registered */
    private VRMLNodeListener nodeListener;

    /**
     * The current number of references to this node. This is for informational
     * purposes only and should never be touched by derived classes.
     */
    protected int[] refCounts;

    /**
     * The list of layer IDs that reference this node. Should correspond 1:1
     * to the counts in {@link #refCounts}. If none, this is null.
     */
    protected int[] layerIds;

    /** The list of IDs that have been marked as being removed. */
    protected int[] removedLayerIds;

    /** SFNode metadata NULL */
    protected VRMLNodeType vfMetadata;

    /** proto representation of the metadata node */
    protected VRMLProtoInstance pMetadata;

    static {
        STATIC_SCENE_GRAPH = PropertyTools.fetchSystemProperty(STATIC_SG_PROP, false);
    }

    /**
     * Create a new instance of this node with the given node type name.
     * inSetup will be set to true and isDEF set to false.
     *
     * @param name The name of the type of node
     */
    public AbstractNode(String name) {
        this.nodeName = name;
        fieldLocalData = new VRMLFieldDataThreaded();

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        inSetup = true;
        isDEF = false;
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    @Override
    public void allEventsComplete() {
        // Ignored by this base implementation
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    @Override
    public String getVRMLNodeName() {
        return nodeName;
    }

    @Override
    public boolean isDEF() {
        return isDEF;
    }

    @Override
    public int[] getSecondaryType() {
        return TypeConstants.NO_SECONDARY_TYPE;
    }

    @Override
    public void setVersion(int major, int minor, boolean isStatic) {
        vrmlMajorVersion = major;
        vrmlMinorVersion = minor;
        this.isStatic = isStatic;
    }

    @Override
    public void setUserData(int index, Object data)
        throws InvalidFieldException {

        if (index < 0 || index > hasChanged.length) {
            throw new InvalidFieldException(this.getClass().getName() +
                " Invalid index in getUserData");
		}
		if (userData == null) {
			userData = new IntHashMap<>(1);
		}
        userData.put(index, data);
    }

    @Override
    public Object getUserData(int index) throws InvalidFieldException {

        if (index < 0 || index > hasChanged.length) {
            throw new InvalidFieldException(this.getClass().getName() +
                " Invalid index in getUserData");
	}
        Object rval = null;
        if (userData != null) {
            rval = userData.get(index);
        }
        return(rval);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    @Override
    public void setMetadataObject(VRMLNodeType data)
        throws InvalidFieldValueException {

        if(vrmlMajorVersion == 2) {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(VRML_META_ERR_PROP);
            throw new InvalidFieldException(msg);
        }

        if(data == null) {
            vfMetadata = null;
            pMetadata = null;
        } else {
            // check for the right interface
            if(data instanceof VRMLMetadataObjectNodeType) {
                vfMetadata = data;
                pMetadata = null;
            } else if(data instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)data;
                VRMLNodeType impl = proto.getImplementationNode();

                while(impl != null && impl instanceof VRMLProtoInstance)
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if(impl != null && !(impl instanceof VRMLMetadataObjectNodeType))
                    throwInvalidProtoException("X3DMetadataObject");

                vfMetadata = impl;
                pMetadata = proto;
            } else
                throwInvalidNodeException("X3DMetadataObject");
        }

        if(!inSetup) {
            hasChanged[FIELD_METADATA] = true;
            fireFieldChanged(FIELD_METADATA);
        }
    }

    @Override
    public VRMLNodeType getMetadataObject() {
        if(pMetadata != null)
            return pMetadata;
        else
            return vfMetadata;
    }

    @Override
    public boolean isSetupFinished() {
        return !inSetup;
    }

    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        if(pMetadata != null)
            pMetadata.setupFinished();
        else if(vfMetadata != null)
            vfMetadata.setupFinished();

        inSetup = false;
    }

    @Override
    public void setDEF() {
        if(!inSetup) {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(DEF_SET_TIMING_ERR_PROP);

            throw new IllegalStateException(msg);
        }

        isDEF = true;
    }

    @Override
    public int getRefCount(int layer) {
        if(layerIds == null)
            return 0;

        int ret_val = 0;

        for(int i = 0; i < layerIds.length; i++) {
            if(layerIds[i] == layer) {
                ret_val = refCounts[i];
                break;
            }
        }

        return ret_val;
    }

    @Override
    public synchronized void updateRefCount(int layer, boolean add) {

        // Go looking for the layer first to see if we have it.
        if(layerIds != null) {
            int ref_idx = -1;

            for(int i = 0; i < layerIds.length; i++) {
                if(layerIds[i] == layer) {
                    ref_idx = i;
                    break;
                }
            }

	    // TODO investigate case where no layerIds exist for Inline, seems acceptable
            if(ref_idx == -1) {
                // Decrementing a layer Id that doesn't exist? Something is
                // badly wrong, so let's toss an exception and debug WTF just
                // happened.
                if(!add) {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(NO_LAYER_REF_PROP);

                    Object[] msg_args = { layer};
                    Format[] fmts = { NumberFormat.getInstance() };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern,
                                          intl_mgr.getFoundLocale());
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalStateException(msg);
                }

                int[] tmp = new int[layerIds.length + 1];
                System.arraycopy(layerIds, 0, tmp, 0, layerIds.length);
                tmp[layerIds.length] = layer;
                layerIds = tmp;

                tmp = new int[refCounts.length + 1];
                System.arraycopy(refCounts, 0, tmp, 0, refCounts.length);
                tmp[refCounts.length] = 1;
                refCounts = tmp;

                // Check the removed layer ID list ot make sure that we don't
                // have this layer on the list. If so, remove it.
                int size = removedLayerIds == null ? 0 : removedLayerIds.length;

                for(int i = 0; i < size; i++) {
                    if(removedLayerIds[i] != layer)
                        continue;

                    if(size == 1) {
                        removedLayerIds = null;
                    } else {
                        int[] tmp1 = new int[size - 1];
                        System.arraycopy(removedLayerIds, 0, tmp1, 0, i - 1);
                        System.arraycopy(removedLayerIds,
                                         i + 1,
                                         tmp1,
                                         i,
                                         size - i - 1);
                        removedLayerIds = tmp1;
                    }
                }
            } else {
                if(add) {
                    refCounts[ref_idx]++;
                } else {
                    refCounts[ref_idx]--;

                    if(refCounts[ref_idx] <= 0) {
                        int[] tmp1 = new int[refCounts.length - 1];
                        int[] tmp2 = new int[refCounts.length - 1];

                        for(int i = 0; i < ref_idx; i++) {
                            tmp1[i] = refCounts[i];
                            tmp2[i] = layerIds[i];
                        }

                        for(int i = ref_idx + 1; i < refCounts.length; i++) {
                            tmp1[i - 1] = refCounts[i];
                            tmp2[i - 1] = layerIds[i];
                        }

                        refCounts = tmp1;
                        layerIds = tmp2;

                        if(removedLayerIds == null) {
                            removedLayerIds = new int[] { layer } ;
                        } else {
                            int[] tmp3 = new int[removedLayerIds.length + 1];
                            System.arraycopy(removedLayerIds,
                                             0,
                                             tmp3,
                                             0,
                                             removedLayerIds.length);
                            removedLayerIds = tmp3;
                        }
                    }

                    // Do we have no references left?
                    if(refCounts.length == 1 && refCounts[0] == 0) {
                        refCounts = null;
                        layerIds = null;
                    }
                }
            }
        } else {
            if(!add) {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(NO_LAYER_REF_PROP);

                Object[] msg_args = { layer};
                Format[] fmts = { NumberFormat.getInstance() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern,
                                      intl_mgr.getFoundLocale());
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);

                throw new IllegalStateException(msg);
            }
            layerIds = new int[] { layer };
            refCounts = new int[] { 1 };
        }
    }

    @Override
    public int[] getLayerIds() {
        return layerIds;
    }

    @Override
    public int[] getRemovedLayerIds() {
        return removedLayerIds;
    }
    
    @Override
    public void clearRemovedLayerIds() {
        removedLayerIds = null;
    }

    @Override
    public void setFrameStateManager(FrameStateManager mgr) {
        stateManager = mgr;
    }

    @Override
    public boolean hasFieldChanged(int index) {
        boolean ret_val=false;

        if (index < 0 || index > hasChanged.length - 1)
            return ret_val;

        ret_val = hasChanged[index];
        hasChanged[index] = false;

        return ret_val;
    }

    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        try {
            if(srcIndex == FIELD_METADATA) {
                if(pMetadata != null)
                    destNode.setValue(destIndex, pMetadata);
                else
                    destNode.setValue(destIndex, vfMetadata);
            } else
                errorReporter.warningReport("Invalid field " + destIndex +
                                            " for AbstractNode.sendRoute", null);
        } catch(InvalidFieldException ife) {
            errorReporter.warningReport("sendRoute: No field!" + ife.getFieldName() +
                                        " in node " + destNode.getVRMLNodeName(), null);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.warningReport("sendRoute: Invalid field value.",
                                        ifve);
        }
    }

    @Override
    public void addNodeListener(VRMLNodeListener l) {
        nodeListener = NodeListenerMulticaster.add(nodeListener, l);
    }

    @Override
    public void removeNodeListener(VRMLNodeListener l) {
        nodeListener = NodeListenerMulticaster.remove(nodeListener, l);
    }

    @Override
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch (index) {
            case FIELD_METADATA:
                fieldData.clear();
                if(pMetadata != null)
                    fieldData.nodeValue = pMetadata;
                else
                    fieldData.nodeValue = vfMetadata;
                
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;
             case -1:
                // no fields found, don't report a problem
//                System.out.println("fieldData=" + fieldData.nodeValue); // debug (if here, returns null)
                break;
            default:
                String fieldName = "(Unknown field)";
                try {
                    VRMLFieldDeclaration decl = getFieldDeclaration(index);
                    
                    if (decl != null) {
                        fieldName = decl.getName();
                    }
                } catch (Exception e) {
                    //ignore
                }   
                errorReporter.warningReport("Invalid Index: " + index + " for fieldName: " + fieldName + " in " + getClass().getName(), null);
                break;
        }

        return fieldData;
    }

    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(int): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {


        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(int[]): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(boolean): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(boolean[]): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(float): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(float[]): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, long value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(long): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, long[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(long[]): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(double): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(double[]): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(String): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(String): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        if(index == FIELD_METADATA)
            setMetadataObject(child);
        else {

            String fieldName = "(Unknown field)";

            try {
                VRMLFieldDeclaration decl = getFieldDeclaration(index);

                if (decl != null) {
                    fieldName = decl.getName();
                }
            } catch (Exception e) {
               //ignore
            }

            throw new InvalidFieldException(this.getClass().getName() +
                " setValue(VRMLNode): Invalid Index: " + index + " fieldName: " + fieldName);
        }
    }

    @Override
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        String fieldName = "(Unknown field)";

        try {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if (decl != null) {
                fieldName = decl.getName();
            }
        } catch (Exception e) {
           //ignore
        }

        throw new InvalidFieldException(this.getClass().getName() +
            " setValue(VRMLNode[]): Invalid Index: " + index + " fieldName: " + fieldName);
    }

    @Override
    public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        errorReporter.messageReport(nodeName +
            " notifyExternProtoLoaded not implemented.");
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
    protected void fireFieldChanged(int index) {
        if(nodeListener != null) {
            try {
                nodeListener.fieldChanged(index);
            } catch(Throwable th) {
                errorReporter.errorReport(th.getLocalizedMessage(), th);
            }
        }
    }

    /**
     * Check to see if the supplied node type is the same as this node. It
     * does a case sensitive string comparison based on their node name. If
     * they are not the same then an IllegalArgumentException is thrown. If
     * the same, nothing happens.
     *
     * @param node The node to check
     * @throws IllegalArgumentException The nodes are not the same
     */
    protected void checkNodeType(VRMLNodeType node) {
        String type = node.getVRMLNodeName();

        if(!type.equals(nodeName)) {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(BAD_NODE_MSG_PROP) +
                         type;

            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Verify that one of a protoInstance's secondary type is the supplied type.
     * This method will throw an InvalidFieldValueException if its not the
     * right type.
     *
     * @param instance The proto instance
     * @param type The type to check for
     * @param msg The error to message for the InvalidFieldValueException thrown
     * @throws InvalidFieldValueException if its not the right type
     */
     protected static void checkSecondaryType(VRMLNodeType instance,
                                              int type,
                                              String msg) {

        int[] stypes = instance.getSecondaryType();
        boolean ok = false;

        for(int i=0; i < stypes.length; i++) {
            if (stypes[i] == type) {
                ok=true;
                break;
            }
        }

        if(!ok)
            throw new InvalidFieldValueException(msg);
     }

    /**
     * Verify that one of a protoInstance's secondary type is one of the
     * supplied types.  This method will throw an InvalidFieldValueException
     * if its not the right type.
     *
     * @param instance The proto instance
     * @param type The types to check for
     * @param msg The error to message for the InvalidFieldValueException thrown
     * @throws InvalidFieldValueException if its not the right type
     */
     protected static void checkSecondaryType(VRMLNodeType instance,
                                              int[] type,
                                              String msg) {

        int[] stypes = instance.getSecondaryType();
        boolean ok = false;

        int len1 = stypes.length;
        int len2 = type.length;

        for(int i=0; i < len1; i++) {
            for (int j=0; j < len2; j++) {
                if (stypes[i] == type[j]) {
                    ok=true;
                    break;
                }
            }
        }

        if(!ok)
            throw new InvalidFieldValueException(msg);
    }

    /**
     * Generate an error message for the node type not being of the right
     * type that is requested. The type is passed in as a string and substituted
     * in to the right place in the internationalised string.
     *
     * @param nodeName The type name string for the node to complain about
     * @throws InvalidFieldValueException Always throws this as a response to
     *    this method call
     */
    protected void throwInvalidNodeException(String nodeName)
        throws InvalidFieldValueException {
        I18nManager intl_mgr = I18nManager.getManager();
        String msg_pattern = intl_mgr.getString(NOT_REQD_NODE_MSG_PROP);

        Object[] msg_args = { nodeName };
        Format[] fmts = { null };
        MessageFormat msg_fmt =
            new MessageFormat(msg_pattern,
                              intl_mgr.getFoundLocale());
        msg_fmt.setFormats(fmts);

        String msg = msg_fmt.format(msg_args);
        throw new InvalidFieldValueException(msg);
    }

    /**
     * Generate an error message for the proto type not being of the right
     * type that is requested. The type is passed in as a string and substituted
     * in to the right place in the internationalised string.
     *
     * @param protoName The type name string for the proto to complain about
     * @throws InvalidFieldValueException Always throws this as a response to
     *    this method call
     */
    protected void throwInvalidProtoException(String protoName)
        throws InvalidFieldValueException {
        I18nManager intl_mgr = I18nManager.getManager();
        String msg_pattern = intl_mgr.getString(NOT_REQD_NODE_MSG_PROP);

        Object[] msg_args = { protoName };
        Format[] fmts = { null };
        MessageFormat msg_fmt =
            new MessageFormat(msg_pattern,
                              intl_mgr.getFoundLocale());
        msg_fmt.setFormats(fmts);
        String msg = msg_fmt.format(msg_args);
        throw new InvalidFieldValueException(msg);
    }

    /**
     * Generate an error message for attempting to write an initOnly field
     * after setup has finished.
     *
     * @param fieldName name of the field that we are complaining about
     * @throws InvalidFieldAccessException Always throws this as a response to
     *    this method call
     */
    protected void throwInitOnlyWriteException(String fieldName)
        throws InvalidFieldAccessException {
        I18nManager intl_mgr = I18nManager.getManager();
        String msg_pattern = intl_mgr.getString(READ_ONLY_WRITE_MSG_PROP);

        String field_type = vrmlMajorVersion == 2 ?  "field" : "initializeOnly";

        Object[] msg_args = { field_type, fieldName };
        Format[] fmts = { null, null };
        MessageFormat msg_fmt =
            new MessageFormat(msg_pattern,
                              intl_mgr.getFoundLocale());
        msg_fmt.setFormats(fmts);
        String msg = msg_fmt.format(msg_args);
        throw new InvalidFieldAccessException(msg, fieldName);
    }

    /**
     * Generate an error message for attempting to write an inputOnly field
     * before setup has finished.
     *
     * @param fieldName name of the field that we are complaining about
     * @throws InvalidFieldAccessException Always throws this as a response to
     *    this method call
     */
    protected void throwInputOnlyWriteException(String fieldName)
        throws InvalidFieldAccessException {
        I18nManager intl_mgr = I18nManager.getManager();
        String msg_pattern = intl_mgr.getString(IN_ONLY_WRITE_MSG_PROP);

        String field_type = vrmlMajorVersion == 2 ? "eventIn" : "inputOnly";

        Object[] msg_args = { field_type, fieldName };
        Format[] fmts = { null, null };
        MessageFormat msg_fmt =
            new MessageFormat(msg_pattern,
                              intl_mgr.getFoundLocale());
        msg_fmt.setFormats(fmts);
        String msg = msg_fmt.format(msg_args);
        throw new InvalidFieldAccessException(msg, fieldName);
    }

    /**
     * Generate an error message for attempting to write an outputOnly field.
     *
     * @param fieldName name of the field that we are complaining about
     * @throws InvalidFieldAccessException Always throws this as a response to
     *    this method call
     */
    protected void throwOutputOnlyWriteException(String fieldName)
        throws InvalidFieldAccessException {
        I18nManager intl_mgr = I18nManager.getManager();
        String msg_pattern = intl_mgr.getString(OUT_ONLY_WRITE_MSG_PROP);

        String field_type = vrmlMajorVersion == 2 ? "eventOut" : "outputOnly";

        Object[] msg_args = { field_type, fieldName };
        Format[] fmts = { null, null };
        MessageFormat msg_fmt =
            new MessageFormat(msg_pattern,
                              intl_mgr.getFoundLocale());
        msg_fmt.setFormats(fmts);
        String msg = msg_fmt.format(msg_args);
        throw new InvalidFieldAccessException(msg, fieldName);
    }

    /**
     * Internal convenience method to update references on the given child node
     * of the current node.
     *
     * @param node The child node of this group to send updates to
     * @param add true if this is adding a new reference, false for delete
     */
    protected void updateRefs(VRMLNodeType node, boolean add) {
        if(layerIds == null)
            return;

        for(int i = 0; i < layerIds.length; i++)
            node.updateRefCount(layerIds[i], add);
    }
}

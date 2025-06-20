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

package org.web3d.vrml.renderer.common.nodes;

// External imports
// none

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.*;

/**
 * Base implementation of a 2D texture object.
 * <p>
 * Performs the VRML field handling for REPEAT_S and REPEAT_T.
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public abstract class BaseTexture2DNode extends BaseTextureNode
    implements VRMLTexture2DNodeType {

    /** Field Index for the common repeatS field */
    protected static final int FIELD_REPEATS = LAST_NODE_INDEX + 1;

    /** Field Index for the common repeatT field */
    protected static final int FIELD_REPEATT = LAST_NODE_INDEX + 2;

    /**
     * Field Index for the textureProperties field that is available from
     * 3.2 or later specs.
     */
    protected static final int FIELD_TEXTURE_PROPERTIES = LAST_NODE_INDEX + 3;

    /** Index of the description field */
    protected static final int FIELD_DESCRIPTION = LAST_NODE_INDEX + 4;

    /** Index of the last common field for all 2D textures */
    protected static final int LAST_TEXTURENODETYPE_INDEX = FIELD_DESCRIPTION;

    /** Message for when the proto is not a TextureProperties */
    protected static final String TEXTURE_PROPS_PROTO_MSG =
        "Proto does not describe a TextureProperties object";

    /** Message for when the node in setValue() is not a TextureProperty */
    protected static final String TEXTURE_PROPS_NODE_MSG =
        "Node does not describe a TextureProperties object";

    /**
     * Message when accessing the texture properties field in a file with
     * the spec version earlier than 3.2.
     */
    protected static final String TEXPROPS_VERSION_MSG =
        "The textureProperties field is not available before X3D V3.2";

    // VRML Field declarations

    /** field SFBool repeatS TRUE */
    protected boolean vfRepeatS;

    /** field SFBool repeatT TRUE */
    protected boolean vfRepeatT;

    /** PROTO version of the textureProperties node */
    protected VRMLProtoInstance pTextureProperties;

    /** inputOutput SFNode textureProperties main field */
    protected VRMLTextureProperties2DNodeType vfTextureProperties;

    /** SFString description "" */
    protected String vfDescription;

    /** The base image for this texture */
    protected NIOBufferImage implImage;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Default constructor for the node. The repeat flags are set to TRUE.
     *
     * @param name The name of the type of node
     */
    public BaseTexture2DNode(String name) {
        super(name);

        vfDescription = "";
        vfRepeatS = true;
        vfRepeatT = true;
    }

    /**
     * Set the fields of the grouping node that has the fields set
     * based on the fields of the passed in node. This will not copy any
     * children nodes, only the local fields.
     *
     * @param node The grouping node to copy info from
     */
    protected void copy(VRMLTexture2DNodeType node)
    {
        vfDescription = node.getDescription();

        boolean field = node.getRepeatS();
        vfRepeatS = field;

        field = node.getRepeatT();
        vfRepeatT = field;
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLTexture2DNodeType interface.
    //-------------------------------------------------------------

   @Override
    public int getTextureType() {
        return TextureConstants.TYPE_SINGLE_2D;
    }

    /**
     * Get the image representation of this texture.
     *
     * @return The image.
     */
    @Override
    public NIOBufferImage getImage( ) {
        return implImage;
    }

    /**
     * Get the value of field description. The field is writable.
     * Default value is empty string <code>""</code>.
     *
     * @return The current value of description
     */
    @Override
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Get the value of field repeatS. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatS
     */
    @Override
    public boolean getRepeatS() {
        return vfRepeatS;
    }

    /**
     * Get the value of field repeatT. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatT
     */
    @Override
    public boolean getRepeatT() {
        return vfRepeatT;
    }

    /**
     * Get node content for the textureProperties field. This field is only
     * available for X3D 3.2 or later.
     *
     * @return The current field value
     * @throws InvalidFieldException This field was request in a field with
     *    spec version &lt; 3.2
     */
    @Override
    public VRMLNodeType getTextureProperties()
        throws InvalidFieldException {

        if(vrmlMajorVersion <= 3 && vrmlMinorVersion < 2) {
            InvalidFieldException ife =
                new InvalidFieldException(TEXPROPS_VERSION_MSG);
            ife.setFieldName("TextureProperties");
            throw ife;
        }

        if (pTextureProperties != null) {
            return pTextureProperties;
        } else {
            return vfTextureProperties;
        }
    }

    /**
     * Set node content as replacement for the textureProperties field. This
     * field is only available for X3D 3.2 or later.
     *
     * @param props The new value for geometry.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     * @throws InvalidFieldException This field was request in a field with
     *    spec version &lt; 3.2
     */
    @Override
    public void setTextureProperties(VRMLNodeType props)
        throws InvalidFieldValueException, InvalidFieldException {

        if(vrmlMajorVersion <= 3 && vrmlMinorVersion < 2) {
            InvalidFieldException ife =
                new InvalidFieldException(TEXPROPS_VERSION_MSG);
            ife.setFieldName("TextureProperties");
            throw ife;
        }

        VRMLNodeType old_node;

        if(pTextureProperties != null)
            old_node = pTextureProperties;
        else
            old_node = vfTextureProperties;

        if(props instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)props).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLTextureProperties2DNodeType))
                throw new InvalidFieldValueException(TEXTURE_PROPS_PROTO_MSG);

            pTextureProperties = (VRMLProtoInstance)props;
            vfTextureProperties = (VRMLTextureProperties2DNodeType)impl;

        } else if(props != null && !(props instanceof VRMLTextureProperties2DNodeType)) {
            throw new InvalidFieldValueException(TEXTURE_PROPS_NODE_MSG);
        } else {
            pTextureProperties = null;
            vfTextureProperties = (VRMLTextureProperties2DNodeType)props;
        }

        if(props != null)
            updateRefs(props, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(props != null)
                stateManager.registerAddedNode(props);

            hasChanged[FIELD_TEXTURE_PROPERTIES] = true;
            fireFieldChanged(FIELD_TEXTURE_PROPERTIES);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pTextureProperties != null)
            pTextureProperties.setupFinished();
        else if(vfTextureProperties != null)
            vfTextureProperties.setupFinished();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_DESCRIPTION:
                fieldData.clear();
                fieldData.stringValue = vfDescription;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_REPEATS:
                fieldData.clear();
                fieldData.booleanValue = vfRepeatS;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_REPEATT:
                fieldData.clear();
                fieldData.booleanValue = vfRepeatT;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_TEXTURE_PROPERTIES:
                if(vrmlMajorVersion <= 3 && vrmlMinorVersion < 2) {
                    InvalidFieldException ife =
                        new InvalidFieldException(TEXPROPS_VERSION_MSG);
                    ife.setFieldName("TextureProperties");
                    throw ife;
                }

                fieldData.clear();
                if(pTextureProperties != null)
                    fieldData.nodeValue = pTextureProperties;
                else
                    fieldData.nodeValue = vfTextureProperties;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_TEXTURE_PROPERTIES:
                    if(pTextureProperties != null)
                        destNode.setValue(destIndex, pTextureProperties);
                    else
                        destNode.setValue(destIndex, vfTextureProperties);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException | InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the field
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException {

        switch(index) {                
            case FIELD_REPEATS:
               if(!inSetup)
                   throwInitOnlyWriteException("repeatS");

                vfRepeatS = value;
                break;

            case FIELD_REPEATT :
               if(!inSetup)
                   throwInitOnlyWriteException("repeatT");

                vfRepeatT = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a String. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the field
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DESCRIPTION:
//               if(!inSetup)
//                   throwInitOnlyWriteException("description");

                vfDescription = value;
                break;
                
            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     * @throws InvalidFieldException This field was request in a field with
     *    spec version &lt; 3.2
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_TEXTURE_PROPERTIES:
                setTextureProperties(node);
                break;

            default:
                super.setValue(index, child);
        }
    }

}

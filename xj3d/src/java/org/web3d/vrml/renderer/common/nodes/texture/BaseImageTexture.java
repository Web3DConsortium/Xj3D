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

package org.web3d.vrml.renderer.common.nodes.texture;

// External imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLSingleExternalNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLUrlListener;
import org.web3d.vrml.nodes.VRMLContentStateListener;

import org.web3d.vrml.renderer.common.nodes.BaseTexture2DNode;

import org.web3d.vrml.util.URLChecker;

/**
 * Common implementation of a ImageTexture node.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.20 $
 */
public class BaseImageTexture extends BaseTexture2DNode
    implements VRMLSingleExternalNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.SingleExternalNodeType };

    /** Field Index */
    protected static final int FIELD_URL = LAST_TEXTURENODETYPE_INDEX + 1;

    /** Index of the autoRefresh field */
    protected static final int FIELD_AUTOREFRESH = LAST_TEXTURENODETYPE_INDEX + 2;

    /** Index of the autoRefreshTimeLimit field */
    protected static final int FIELD_AUTOREFRESHTIMELIMIT = LAST_TEXTURENODETYPE_INDEX + 3;

    /** Index of the load field */
    protected static final int FIELD_LOAD = LAST_TEXTURENODETYPE_INDEX + 4;

    private static final int LAST_IMAGETEXTURE_INDEX = FIELD_LOAD;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_IMAGETEXTURE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** exposedField MFString url [] */
    protected String[] vfURL;

    /** SFBool autoRefresh */
    protected double vfAutoRefresh;

    /** SFBool autoRefreshTimeLimit */
    protected double vfAutoRefreshTimeLimit;

    /** SFBool load */
    protected boolean vfLoad;

    /** URL Load State */
    protected int loadState;

    /** Class vars for performance */
    private String worldURL;

    /** List of those who want to know about Url changes.  Likely 1 */
    private List<VRMLUrlListener> urlListeners;

    /** List of those who want to know about content state changes. Likely 1 */
    private List<VRMLContentStateListener> contentListeners;

    /** The URI of the finally loaded texture. Null if not loaded yet */
    protected String loadedURI;

    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_TEXTURE_PROPERTIES
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_DESCRIPTION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "description");
        fieldDecl[FIELD_REPEATS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatS");
        fieldDecl[FIELD_REPEATT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatT");
        fieldDecl[FIELD_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "url");
        fieldDecl[FIELD_TEXTURE_PROPERTIES] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "textureProperties");
        fieldDecl[FIELD_AUTOREFRESH] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFTime",
                                     "autoRefresh");
        fieldDecl[FIELD_AUTOREFRESHTIMELIMIT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFTime",
                                     "autoRefreshTimeLimit");
        fieldDecl[FIELD_LOAD] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "load");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_URL;
        fieldMap.put("url", idx);
        fieldMap.put("set_url", idx);
        fieldMap.put("url_changed", idx);

        fieldMap.put("description", FIELD_DESCRIPTION);
        fieldMap.put("repeatS", FIELD_REPEATS);
        fieldMap.put("repeatT", FIELD_REPEATT);
        fieldMap.put("textureProperties", FIELD_TEXTURE_PROPERTIES);

        fieldMap.put("autoRefresh",FIELD_AUTOREFRESH);
        fieldMap.put("autoRefreshTimeLimit",FIELD_AUTOREFRESHTIMELIMIT);
        fieldMap.put("load",FIELD_LOAD);
    }

    /**
     * Default constructor builds the default node defined in the spec.
     */
    protected BaseImageTexture() {
        super("ImageTexture");

        contentListeners = new ArrayList<>();
        urlListeners = new ArrayList<>(1);

        hasChanged = new boolean[NUM_FIELDS];
        vfURL = FieldConstants.EMPTY_MFSTRING;
        vfDescription = "";
        vfAutoRefresh = -1;
        vfAutoRefreshTimeLimit = -1;
        vfLoad = true;

        loadState = NOT_LOADED;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseImageTexture(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("url");
            VRMLFieldData field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfURL = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfURL, 0, field.numElements);
            }
            index = node.getFieldIndex("description");
            field = node.getFieldValue(index);
            vfDescription = field.stringValue;

            index = node.getFieldIndex("autoRefresh");
            field = node.getFieldValue(index);
            vfAutoRefresh = field.doubleValue;

            index = node.getFieldIndex("autoRefreshTimeLimit");
            field = node.getFieldValue(index);
            vfAutoRefreshTimeLimit = field.doubleValue;

            index = node.getFieldIndex("load");
            field = node.getFieldValue(index);
            vfLoad = field.booleanValue;
        }
        catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureNodeType
    //----------------------------------------------------------

    // TODO inconsistent implementation of accessors for description
    /**
     * Get the autoRefresh value to associate with the link.
     * @return The current autoRefresh value
     */
    public double getAutoRefresh() {
        return vfAutoRefresh;
    }

    /**
     * Set the autoRefresh value for this link.
     * @param newValue The new autoRefresh value to set
     */
    public void setAutoRefresh (double newValue) {
        vfAutoRefresh = newValue;

        if(!inSetup) {
            hasChanged[FIELD_AUTOREFRESH] = true;
            fireFieldChanged(FIELD_AUTOREFRESH);
        }
    }

    /**
     * Get the autoRefreshTimeLimit value to associate with the link.
     * @return The current TimeLimit value
     */
    public double getAutoRefreshTimeLimit() {
        return vfAutoRefreshTimeLimit;
    }

    /**
     * Set the autoRefreshTimeLimit value for this link.
     * @param newValue The new autoRefreshTimeLimit value to set
     */
    public void setAutoRefreshTimeLimit (double newValue) {
        vfAutoRefreshTimeLimit = newValue;

        if(!inSetup) {
            hasChanged[FIELD_AUTOREFRESHTIMELIMIT] = true;
            fireFieldChanged(FIELD_AUTOREFRESHTIMELIMIT);
        }
    }

    /**
     * Get the load value to associate with the link.
     * @return The current TimeLimit value
     */
    public boolean getLoad() {
        return vfLoad;
    }

    /**
     * Set the load value for this link.
     * @param newValue The new load value to set
     */
    public void setLoad (boolean newValue) {
        vfLoad = newValue;

        if(!inSetup) {
            hasChanged[FIELD_LOAD] = true;
            fireFieldChanged(FIELD_LOAD);
        }
    }

    /**
     * Get a string for caching this object.  Null means do not cache this
     * texture.
     *
     * @param stage The stage number,  0 for all single stage textures.
     * @return A string to use in lookups.  Typically the url loaded.
     */
    @Override
    public String getCacheString(int stage) {
        return null;
    }

    //----------------------------------------------------------
    // Methods defined  by VRMLExternalNodeType
    //----------------------------------------------------------

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @return The current load state of the node
     */
    @Override
    public int getLoadState() {
        return loadState;
    }

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param state The new state of the node
     */
    @Override
    public void setLoadState(int state) {
        switch(state) {
            case VRMLSingleExternalNodeType.NOT_LOADED:
                break;
            case 2 :
//                System.out.println("Loading: " + loadedURI);
                break;

            case 3 :
//                System.out.println("Loading complete: " + loadedURI);
                break;

            case 4 :
                if(loadedURI != null)
                    System.out.println("Loading failed: " + loadedURI);
                break;

            default :
                System.out.println("Unknown state: " + state);
        }

        loadState = state;

        // Only file complete events when texture is ready to use
        if (state != VRMLSingleExternalNodeType.LOAD_COMPLETE)
            fireContentStateChanged();
    }

    /**
     * Set the world URL so that any relative URLs may be corrected to the
     * fully qualified version. Guaranteed to be non-null.
     *
     * @param url The world URL.
     */
    @Override
    public void setWorldUrl(String url) {
        if((url == null) || (url.isEmpty()))
            return;

        worldURL = url;
    }

    /**
     * Get the world URL so set for this node.
     *
     * @return url The world URL.
     */
    @Override
    public String getWorldUrl() {
        return worldURL;
    }

    /**
     * Sets the URL to a new value.  We will load only one
     * of these URL's.  The list provides alternates.
     *
     * @param newURL Array of candidate URL strings
     * @param numValid The number of valid values to copy from the array
     */
    @Override
    public void setUrl(String[] newURL, int numValid) {
        if(numValid > 0) {
            if(worldURL != null) {
                vfURL = URLChecker.checkURLs(worldURL, newURL, false);
            } else
                vfURL = newURL;
        } else {
            vfURL = FieldConstants.EMPTY_MFSTRING;
        }

        if (!inSetup) {
            hasChanged[FIELD_URL] = true;
            fireFieldChanged(FIELD_URL);
        }
    }

    /**
     * Get the list of URLs requested by this node. If there are no URLs
     * supplied in the text file then this will return a zero length array.
     *
     * @return The list of URLs to attempt to load
     */
    @Override
    public String[] getUrl() {
        if(worldURL != null) {
            URLChecker.checkURLsInPlace(worldURL, vfURL, false);
        }

        return vfURL;
    }

    //----------------------------------------------------------
    // Methods overriding BaseTexture2DNode abstract class.
    //----------------------------------------------------------

    /**
     * Get the description to associate with the link. This is a line of text
     * suitable for mouseovers, status information etc. If there is no
     * description set then it returns null.
     *
     * @return The current description or null
     */
    @Override
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Set the description string for this link. Setting a value of null will
     * clear the current description.
     *
     * @param desc The new description to set
     */
    // TODO inconsistent overrides
    public void setDescription(String desc) {
        vfDescription = desc;

        if(!inSetup) {
            hasChanged[FIELD_DESCRIPTION] = true;
            fireFieldChanged(FIELD_DESCRIPTION);
        }
    }
    //----------------------------------------------------------

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    @Override
    public boolean checkValidContentType(String mimetype) {
        return mimetype.contains("image");
    }

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArgumentException The content object is not supported
     */
    @Override
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param uri The URI used to load this content
     */
    @Override
    public void setLoadedURI(String uri) {
        loadedURI = uri;
    }

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param ul The listener instance to add
     */
    @Override
    public void addUrlListener(VRMLUrlListener ul) {
        if (!urlListeners.contains(ul))
            urlListeners.add(ul);
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param ul The listener to be removed
     */
    @Override
    public void removeUrlListener(VRMLUrlListener ul) {
        urlListeners.remove(ul);
    }

    /**
     * Add a listener to this node instance for the content state changes. If
     * the listener is already added or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    @Override
    public void addContentStateListener(VRMLContentStateListener l) {
        if(!contentListeners.contains(l))
            contentListeners.add(l);
    }

    /**
     * Remove a listener from this node instance for the content state changes.
     * If the listener is null or not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    @Override
    public void removeContentStateListener(VRMLContentStateListener l) {
        contentListeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = fieldMap.get(fieldName);

        return (index == null) ? -1 : index;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0 || index > LAST_IMAGETEXTURE_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @return The number of fields.
     */
    @Override
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.TextureNodeType;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    @Override
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

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
        fieldData.clear();
        switch(index) {
            case FIELD_URL:
                fieldData.stringArrayValues = vfURL;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfURL.length;
                break;
            case FIELD_AUTOREFRESH:
                fieldData.doubleValue = vfAutoRefresh;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;
            case FIELD_AUTOREFRESHTIMELIMIT:
                fieldData.doubleValue = vfAutoRefreshTimeLimit;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;
            case FIELD_LOAD:
                fieldData.booleanValue = vfLoad;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            default:
                return super.getFieldValue(index);
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
                case FIELD_URL :
                    destNode.setValue(destIndex, vfURL, vfURL.length);
                    break;

                default:
                    super.sendRoute(time,srcIndex,destNode,destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException {

        switch(index)
        {
            case FIELD_LOAD:
                setLoad(value);
                break;

            default :
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a double.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, double value)
        throws InvalidFieldException {

        switch(index)
        {
            case FIELD_AUTOREFRESH:
                setAutoRefresh(value);
                break;
            case FIELD_AUTOREFRESHTIMELIMIT:
                setAutoRefreshTimeLimit(value);
                break;

            default :
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_URL:
                vfURL = new String[1];
                vfURL[0] = value;

                if (!inSetup) {
                    loadState = NOT_LOADED;
                    fireUrlChanged(index);
                    hasChanged[FIELD_URL] = true;
                    fireFieldChanged(FIELD_URL);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_URL :
                vfURL = new String[numValid];

                System.arraycopy(value, 0, vfURL, 0, numValid);

                if (!inSetup) {
                    loadState = NOT_LOADED;
                    fireUrlChanged(index);
                    hasChanged[FIELD_URL] = true;
                    fireFieldChanged(FIELD_URL);
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
     */
    protected void fireContentStateChanged() {
        // Notify listeners of new value
        int num_listeners = contentListeners.size();
        VRMLContentStateListener csl;

        for(int i = 0; i < num_listeners; i++) {
            csl = contentListeners.get(i);
            csl.contentStateChanged(this, FIELD_URL, loadState);
        }
    }

}

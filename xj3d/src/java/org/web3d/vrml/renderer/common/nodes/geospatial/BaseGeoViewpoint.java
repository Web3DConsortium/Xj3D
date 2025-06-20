/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.vecmath.*;

import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.operation.transform.GeocentricTransform;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;
import org.web3d.vrml.renderer.common.nodes.BaseBindableNode;

import org.xj3d.core.eventmodel.OriginManager;
import org.xj3d.impl.core.eventmodel.OriginManagerFactory;

/**
 * Common implementation of a GeoViewpoint node.
 * <p>
 * This implementation implements both the viewpoint and navInfo interfaces
 * so that it can be internally detected by the system as being functional
 * for both parts of the scene graph. The primary type is defined to be the
 * viewpoint, and the secondaries are the bindable and nav info node types.
 * Hopefully this will allow system to work out what is going on here. Note
 * the following paragraph from the X3D spec:
 * </p>
 * <blockquote>
 * "The GeoViewpoint node may be implemented as if there is an embedded
 * NavigationInfo node that is bound and unbound with the GeoViewpoint
 * node. As such, a X3D browser should internally set the speed,
 * avatarSize, and visibilityLimit fields to an appropriate value for
 * the viewpoint's elevation."
 * </blockquote>
 * <p>
 *
 * Viewpoints cannot be shared using DEF/USE. They may be named as such for
 * Anchor purposes, but attempting to reuse them will cause an error. This
 * implementation does not provide any protection against USE of this node
 * and attempting to do so will result in throwing exceptions - most
 * probably in the grouping node that includes this node.
 *
 * TODO:
 *  CenterOfRotation is missing from regular viewpoint
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.28 $
 */
public abstract class BaseGeoViewpoint extends BaseBindableNode
    implements VRMLViewpointNodeType, VRMLNavigationInfoNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.BindableNodeType,
        TypeConstants.TimeDependentNodeType,
        TypeConstants.NavigationInfoNodeType,
        TypeConstants.OriginManagedNodeType
    };

    /** Index of the fieldOfView field */
    protected static final int FIELD_FIELDOFVIEW = LAST_BINDABLE_INDEX + 1;

    /** Index of the jump field */
    protected static final int FIELD_JUMP = LAST_BINDABLE_INDEX + 2;

    /** Index of the orientation field */
    protected static final int FIELD_SET_ORIENTATION = LAST_BINDABLE_INDEX + 3;

    /** Index of the position field */
    protected static final int FIELD_SET_POSITION = LAST_BINDABLE_INDEX + 4;

    /** Index of the headlight field */
    protected static final int FIELD_HEADLIGHT = LAST_BINDABLE_INDEX + 5;

    /** Index of the nayType field */
    protected static final int FIELD_NAV_TYPE = LAST_BINDABLE_INDEX + 7;

    /** Index of the description field */
    protected static final int FIELD_DESCRIPTION = LAST_BINDABLE_INDEX + 8;

    /** Index of the geoOrigin field */
    protected static final int FIELD_GEO_ORIGIN = LAST_BINDABLE_INDEX + 9;

    /** Index of the geoSystem field */
    protected static final int FIELD_GEO_SYSTEM = LAST_BINDABLE_INDEX + 10;

    /** Index of the orientation field */
    protected static final int FIELD_ORIENTATION = LAST_BINDABLE_INDEX + 11;

    /** Index of the position field */
    protected static final int FIELD_POSITION = LAST_BINDABLE_INDEX + 12;

    /** Index of the position field */
    protected static final int FIELD_SPEED_FACTOR = LAST_BINDABLE_INDEX + 13;

    /** Index of the centerOfRotation field */
    protected static final int FIELD_CENTER_OF_ROTATION = LAST_BINDABLE_INDEX + 14;

    /** Index of the retainUserOffsets field */
    protected static final int FIELD_RETAIN_USER_OFFSETS = LAST_BINDABLE_INDEX + 15;

    /** Index of the viewAll field */
    protected static final int FIELD_VIEWALL = LAST_BINDABLE_INDEX + 16;

    /** Index of the farDistance field */
    protected static final int FIELD_FARDISTANCE = LAST_BINDABLE_INDEX + 17;

    /** Index of the nearDistance field */
    protected static final int FIELD_NEARDISTANCE = LAST_BINDABLE_INDEX + 18;

    /** The last index of the nodes used by the viewpoint */
    protected static final int LAST_VIEWPOINT_INDEX = FIELD_NEARDISTANCE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_VIEWPOINT_INDEX + 1;

    /** Message for when the proto is not a GeoOrigin */
    private static final String GEO_ORIGIN_PROTO_MSG =
        "Proto does not describe a GeoOrigin object";

    /** Message for when the node in setValue() is not a GeoOrigin */
    private static final String GEO_ORIGIN_NODE_MSG =
        "Node does not describe a GeoOrigin object";

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** Message when the mathTransform.transform() fails */
    private static final String TRANSFORM_ERR_MSG =
        "Unable to transform the coordinate values for some reason.";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Y-UP Vector */
    private static final Vector3d YUP;

    /** X-UP Vector */
    private Vector3d XUP = new Vector3d(1,0,0);

    // VRML Field declarations

    /** SFFloat fieldOfView 0.785398 */
    protected float vfFieldOfView;

    /** SFBool jump TRUE */
    protected boolean vfJump;

    /** SFBool retainUserOffsets FALSE */
    protected boolean vfRetainUserOffsets;

    /** SFBool viewAll FALSE */
    protected boolean vfViewAll;

    /** SFBool headlight TRUE */
    protected boolean vfHeadlight;

    /** SFRotation orientation 0 0 1 0 */
    protected float[] vfOrientation;

    /** SFVec3f position 0 0 100000 */
    protected double[] vfPosition;

    /** SFString description "" */
    protected String vfDescription;

    /** SFFloat farDistance -1 */
    protected float vfFarDistance;

    /** SFFloat nearDistance -1 */
    protected float vfNearDistance;

    /** MFString navType ["EXAMINE", "ANY"] */
    protected String[] vfNavType;

    /** field MFString geoSystem ["GD","WE"] */
    protected String[] vfGeoSystem;

    /** Proto version of the geoOrigin */
    protected VRMLProtoInstance pGeoOrigin;

    /** field SFNode geoOrigin */
    protected VRMLNodeType vfGeoOrigin;

    /** SFFloat speedFactor 1.0 */
    protected float vfSpeedFactor;

    /** A corrected speed calculation based on current ellipsoid height */
    private float correctedSpeed;

    /** Corrected avatar size based on ellipsoid height */
    private float[] correctedAvatarSize;

    /**
     * The calculated local version of the points taking into account both the
     * projection information and the GeoOrigin setting.
     */
    protected double[] localPosition;

    /**
     * Transformation used to make the coordinates to the local system. Does
     * not include the geoOrigin offset calculations.
     */
    private MathTransform geoTransform;

    /**
     * Flag to say if the translation geo coords need to be swapped before
     * conversion.
     */
    private boolean geoCoordSwap;

    /** The listener for navigation info changes */
    protected List<NavigationInfoChangeListener> changeListener;


    /** List of those who want to know about role changes, likely 1 */
    protected List<ViewpointListener> viewpointListeners;

    /** Calculated value based on origin if provided */
    protected float[] centerOfRotation;

    /**
     *
     */
    protected AxisAngle4f axis;

    // Scratch Vars
    private Vector3d posVec;
    private Quat4d qx;
    private Quat4d qz;
    private Quat4d qr;
    private AxisAngle4d tmpAxis;

    private Quat4d local_quat;
    private Quat4d rel_quat;
    private Quat4d comb_quat;

    /** Transform for converting geocentric position
     * to wgs84 geographic position */
    private GeocentricTransform gt;

    /** Scratch coord arrays for converting geocentric position to wgs84
     * geographic position.
     */
    private double[] wgs84;
    private double[] ecef;

    /** Manager for precision control */
    protected OriginManager originManager;

    /** Flag indicating that the OriginManager is enabled */
    protected boolean useOriginManager;

    /** The origin in use */
    protected double[] local_origin;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_GEO_ORIGIN
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BIND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "set_bind");
        fieldDecl[FIELD_BIND_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "bindTime");
        fieldDecl[FIELD_IS_BOUND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isBound");
        fieldDecl[FIELD_FIELDOFVIEW] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "fieldOfView");
        fieldDecl[FIELD_JUMP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "jump");
        fieldDecl[FIELD_RETAIN_USER_OFFSETS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "retainUserOffsets");
        fieldDecl[FIELD_VIEWALL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "viewAll");
        fieldDecl[FIELD_HEADLIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "headlight");
        fieldDecl[FIELD_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFRotation",
                                     "set_orientation");
        fieldDecl[FIELD_SET_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFRotation",
                                     "orientation");
        fieldDecl[FIELD_POSITION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3d",
                                     "position");
        fieldDecl[FIELD_SET_POSITION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFVec3d",
                                     "set_position");
        fieldDecl[FIELD_DESCRIPTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "description");
        fieldDecl[FIELD_FARDISTANCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "farDistance");
        fieldDecl[FIELD_NEARDISTANCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "nearDistance");
        fieldDecl[FIELD_NAV_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "navType");
        fieldDecl[FIELD_GEO_SYSTEM] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "geoSystem");
        fieldDecl[FIELD_GEO_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "geoOrigin");
        fieldDecl[FIELD_SPEED_FACTOR] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "speedFactor");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("set_bind", FIELD_BIND);
        fieldMap.put("bindTime", FIELD_BIND_TIME);
        fieldMap.put("isBound", FIELD_IS_BOUND);

        idx = FIELD_FIELDOFVIEW;
        fieldMap.put("fieldOfView", idx);
        fieldMap.put("set_fieldOfView", idx);
        fieldMap.put("fieldOfView_changed", idx);

        idx = FIELD_JUMP;
        fieldMap.put("jump", idx);
        fieldMap.put("set_jump", idx);
        fieldMap.put("jump_changed", idx);

        idx = FIELD_RETAIN_USER_OFFSETS;
        fieldMap.put("retainUserOffsets", idx);
        fieldMap.put("set_retainUserOffsets", idx);
        fieldMap.put("retainUserOffsets_changed", idx);

        idx = FIELD_VIEWALL;
        fieldMap.put("viewAll", idx);
        fieldMap.put("set_viewAll", idx);
        fieldMap.put("viewAll_changed", idx);

        idx = FIELD_HEADLIGHT;
        fieldMap.put("headlight", idx);
        fieldMap.put("set_headlight", idx);
        fieldMap.put("headlight_changed", idx);

        idx = FIELD_NAV_TYPE;
        fieldMap.put("navType", idx);
        fieldMap.put("set_navType", idx);
        fieldMap.put("navType_changed", idx);

        fieldMap.put("geoSystem", FIELD_GEO_SYSTEM);
        fieldMap.put("geoOrigin", FIELD_GEO_ORIGIN);
        fieldMap.put("speedFactor", FIELD_SPEED_FACTOR);

        fieldMap.put("orientation", FIELD_ORIENTATION);
        fieldMap.put("set_orientation", FIELD_SET_ORIENTATION);
        fieldMap.put("position", FIELD_POSITION);
        fieldMap.put("set_position", FIELD_SET_POSITION);

        idx = FIELD_DESCRIPTION;
        fieldMap.put("description", idx);
        fieldMap.put("set_description", idx);
        fieldMap.put("description_changed", idx);

        idx = FIELD_FARDISTANCE;
        fieldMap.put("farDistance", idx);
        fieldMap.put("set_farDistance", idx);
        fieldMap.put("farDistance_changed", idx);

        idx = FIELD_NEARDISTANCE;
        fieldMap.put("nearDistance", idx);
        fieldMap.put("set_nearDistance", idx);
        fieldMap.put("nearDistance_changed", idx);

        YUP = new Vector3d(0,1,0);
    }

    /**
     * Construct a default geo viewpoint instance
     */
    protected BaseGeoViewpoint() {
        super("GeoViewpoint");

        vfFieldOfView = 0.785398f;
        vfJump = true;
        vfRetainUserOffsets = false;
        vfViewAll = false;
        vfHeadlight = true;
        vfOrientation = new float[] { 0, 0, 1, 0 };
        vfPosition = new double[] { 0, 0, 100_000};
        centerOfRotation = new float[] { 0, 0, 0 };
        vfSpeedFactor = 1;
        vfGeoSystem = new String[] {"GD","WE"};
        vfNavType = new String[] { NAV_TYPE_EXAMINE, NAV_TYPE_ANY };
        correctedAvatarSize = new float[] { 0.25f, 1.6f, 0.75f };
        correctedSpeed = 1;
        posVec = new Vector3d();
        vfDescription = "";
        vfFarDistance = -1;
        vfNearDistance = -1;        

        qx = new Quat4d();
        qz = new Quat4d();
        qr = new Quat4d();
        tmpAxis = new AxisAngle4d();
        axis = new AxisAngle4f();

        local_quat = new Quat4d();
        rel_quat   = new Quat4d();
        comb_quat  = new Quat4d();

        hasChanged = new boolean[NUM_FIELDS];
        localPosition = new double[3];

        viewpointListeners = new ArrayList<>(1);
        changeListener = new ArrayList<>();

        gt = new GeocentricTransform( DefaultEllipsoid.WGS84, true );
        wgs84 = new double[3];
        ecef = new double[3];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseGeoViewpoint(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("fieldOfView");
            VRMLFieldData field = node.getFieldValue(index);
            vfFieldOfView = field.floatValue;

            index = node.getFieldIndex("jump");
            field = node.getFieldValue(index);
            vfJump = field.booleanValue;

            index = node.getFieldIndex("retainUserOffsets");
            field = node.getFieldValue(index);
            vfRetainUserOffsets = field.booleanValue;

            index = node.getFieldIndex("viewAll");
            field = node.getFieldValue(index);
            vfViewAll = field.booleanValue;

            index = node.getFieldIndex("headlight");
            field = node.getFieldValue(index);
            vfHeadlight = field.booleanValue;

            index = node.getFieldIndex("orientation");
            field = node.getFieldValue(index);
            vfOrientation[0] = field.floatArrayValues[0];
            vfOrientation[1] = field.floatArrayValues[1];
            vfOrientation[2] = field.floatArrayValues[2];
            vfOrientation[3] = field.floatArrayValues[3];

            index = node.getFieldIndex("position");
            field = node.getFieldValue(index);
            vfPosition[0] = field.doubleArrayValues[0];
            vfPosition[1] = field.doubleArrayValues[1];
            vfPosition[2] = field.doubleArrayValues[2];

            index = node.getFieldIndex("description");
            field = node.getFieldValue(index);
            vfDescription = field.stringValue;

            index = node.getFieldIndex("farDistance");
            field = node.getFieldValue(index);
            vfFarDistance = field.floatValue;

            index = node.getFieldIndex("nearDistance");
            field = node.getFieldValue(index);
            vfNearDistance = field.floatValue;

            index = node.getFieldIndex("geoSystem");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfGeoSystem = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfGeoSystem, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("navType");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfNavType = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfNavType, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("speedFactor");
            field = node.getFieldValue(index);
            vfSpeedFactor = field.floatValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLViewpointNodeType
    //-------------------------------------------------------------

    /**
     * Add a ViewpointListener.
     *
     * @param l The listener.  Duplicates and nulls are ignored.
     */
    @Override
    public void addViewpointListener(ViewpointListener l) {
        if(!viewpointListeners.contains(l))
            viewpointListeners.add(l);
    }

    /**
     * Remove a ViewpointListener.
     *
     * @param l The listener
     */
    @Override
    public void removeViewpointListener(ViewpointListener l) {
        viewpointListeners.remove(l);
    }

    /**
     * Get the projection type.
     *
     * @return The type of projection.  One of the PROJECTION_ constants.
     */
    @Override
    public int getProjectionType() {
        return VRMLViewpointNodeType.PROJECTION_PERSPECTIVE;
    }

    /**
     * Get the field of view used by this viewpoint. The value returned
     * is an angle that is not less than zero and less than or equal to PI.
     * The number of items in the list is dependent on the Viewpoint type.
     *
     * @return The field of view used by this viewpoint
     */
    @Override
    public float[] getFieldOfView() {
        return new float[] { vfFieldOfView };
    }

    /**
     * Set the field of view for this viewpoint. The value must be between
     * zero and pie or an exception will be thrown.
     *
     * @param fov The new field of view to use
     * @throws InvalidFieldValueException The field used is out of range
     */
    public void setFieldOfView(float fov) throws InvalidFieldValueException {

        if(fov <= 0.0f || fov >= (float) Math.PI)
            throw new InvalidFieldValueException("FieldOfView must be (0,PI)");

        vfFieldOfView = fov;
        if(!inSetup) {
            hasChanged[FIELD_FIELDOFVIEW] = true;
            fireFieldChanged(FIELD_FIELDOFVIEW);

            fireFieldOfViewChanged( new float[] { fov } );
        }
    }

    /**
     * Get the Jump field value of this viewpoint.
     *
     * @return true if this viewpoint should jump to new positions
     */
    @Override
    public boolean getJump() {
        return vfJump;
    }

    /**
     * Set the jump field value of this viewpoint to the new value
     *
     * @param jump True if the viewpoint should jump to ne positions
     */
    @Override
    public void setJump(boolean jump) {
        vfJump = jump;

        if(!inSetup) {
            hasChanged[FIELD_JUMP] = true;
            fireFieldChanged(FIELD_JUMP);
        }
    }

    /**
     * Get the retainUserOffsets field value of this viewpoint.
     *
     * @return true if this viewpoint should retainUserOffsets on a bind
     */
    @Override
    public boolean getRetainUserOffsets() {
        return vfRetainUserOffsets;
    }

    /**
     * Set the retainUserOffsets field value of this viewpoint to the new value
     *
     * @param retainUserOffsets True if the viewpoint should retainUserOffsets on a bind
     */
    @Override
    public void setRetainUserOffsets(boolean retainUserOffsets) {
        vfRetainUserOffsets = retainUserOffsets;

        if(!inSetup) {
            hasChanged[FIELD_RETAIN_USER_OFFSETS] = true;
            fireFieldChanged(FIELD_RETAIN_USER_OFFSETS);
        }
    }

    /**
     * Get the viewAll field value of this viewpoint.
     *
     * @return true if this viewpoint should viewAll geometry
     */
    @Override
    public boolean getViewAll() {
        return vfViewAll;
    }

    /**
     * Set the viewAll field value of this viewpoint to the new value
     *
     * @param viewAll True if the viewpoint should viewAll geometry
     */
    @Override
    public void setViewAll(boolean viewAll) {
        vfViewAll = viewAll;

        if(!inSetup) {
            hasChanged[FIELD_RETAIN_USER_OFFSETS] = true;
            fireFieldChanged(FIELD_RETAIN_USER_OFFSETS);
        }
    }

    /**
     * Get the description string associated with this viewpoint. If no
     * description is set, this will return null.
     *
     * @return The description string of this viewpoint
     */
    @Override
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Set the description string of this viewpoint. A zero length string or
     * null will clear the currently set description.
     *
     * @param desc The new description to use
     */
    @Override
    public void setDescription(String desc) {
        vfDescription = desc;

        if(!inSetup) {
            hasChanged[FIELD_DESCRIPTION] = true;
            fireFieldChanged(FIELD_DESCRIPTION);
        }
    }

    /**
     * Get the farDistance value associated with this viewpoint.
     * @return The value for this viewpoint
     */
    public float getFarDistance() {
        return vfFarDistance;
    }

    /**
     * Set the farDistance value of this viewpoint
     * @param newValue The new value to use
     */
    public void setFarDistance(float newValue) {
        vfFarDistance = newValue;

        if(!inSetup) {
            hasChanged[FIELD_FARDISTANCE] = true;
            fireFieldChanged(FIELD_FARDISTANCE);
        }
    }

    /**
     * Get the nearDistance value associated with this viewpoint.
     * @return The value for this viewpoint
     */
    public float getNearDistance() {
        return vfNearDistance;
    }

    /**
     * Set the nearDistance value of this viewpoint
     * @param newValue The new value to use
     */
    public void setNearDistance(float newValue) {
        vfNearDistance = newValue;

        if(!inSetup) {
            hasChanged[FIELD_NEARDISTANCE] = true;
            fireFieldChanged(FIELD_NEARDISTANCE);
        }
    }

    /**
     * Sets the current position in world coordinates.
     *
     * @param wcpos Location of the user in world coordinates(x,y,z)
     */
    @Override
    public void setWorldLocation(Vector3f wcpos) {
        // Let's approximate the speced goal.
        if (local_origin != null) {
            wcpos.x += local_origin[0];
            wcpos.y += local_origin[1];
            wcpos.z += local_origin[2];
        }
/*
        float dist = ((float)Math.sqrt(wcpos.x * wcpos.x + wcpos.y * wcpos.y + wcpos.z * wcpos.z));

//System.out.println("dist: " + dist);
//        if (dist < 6378100)
        if (dist < 6378100)
            dist = 20;
        else
            dist = (dist - 6378023) / 10;
*/
        // note: z & y are reversed, geotools uses z-up
        ecef[0] = wcpos.x;
        ecef[1] = wcpos.z;
        ecef[2] = wcpos.y;
        gt.inverseTransform( ecef, 0, wgs84, 0, 1 );
        double elevation = wgs84[2];
        float dist = 20;
        if ( elevation > 200 ) {
            dist = (float)elevation / 10;
        }

//System.out.println("factor: " + dist);
        correctedSpeed = dist * vfSpeedFactor;
        correctedAvatarSize[0] = 0.25f * dist;
        correctedAvatarSize[1] = 1.6f * dist;
        correctedAvatarSize[2] = 0.75f * dist;

        for (NavigationInfoChangeListener l : changeListener) {
            l.notifyAvatarSizeChanged(correctedAvatarSize,
                    correctedAvatarSize.length);
            l.notifyNavigationSpeedChanged(correctedSpeed);
        }
    }

    /**
     * Get the center of rotation defined by this viewpoint. The center of
     * rotation is a point in space relative to the coordinate systems of
     * this node.
     *
     * @return The position of the center of rotation
     */
    @Override
    public float[] getCenterOfRotation() {
         return centerOfRotation;
    }

    /**
     * Set the center of rotation of this viewpoint. The center is a position
     * in 3-space.
     *
     * @param pos The new position to use
     * @throws InvalidFieldValueException The field used is not 3 values
     */
    @Override
    public void setCenterOfRotation(float[] pos)
        throws InvalidFieldValueException {

        // ignored No such field in geoViewpoint

        // rem: spec bug, centerOfRotation IS part of X3DViewpointNode,
        // which GeoViewpoint inherits from.....
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNavigationInfoNodeType
    //-------------------------------------------------------------

    /**
     * Get the list of navigation types that are to be used. It may contain
     * some or all of the constants above. The array will always be non-null.
     *
     * @return The list of navigation types set
     */
    @Override
    public String[] getType() {
        return vfNavType;
    }

    /**
     * Get the number of valid navigation types in the result from
     * getType().
     *
     * @return The number of elements in getType().
     */
    @Override
    public int getNumTypes() {
        return vfNavType.length;
    }

    /**
     * Add a listener for navigation info changes. Duplicate adds are
     * ignored.
     *
     * @param listener The new navigation info change listener
     */
    @Override
    public void addNavigationChangedListener( NavigationInfoChangeListener listener) {

        if(!changeListener.contains(listener))
            changeListener.add(listener);
    }

    /**
     * Remove the listener for navigation info changes. If not already added,
     * this request is ignored.
     *
     * @param listener The new navigation info change listener
     */
    @Override
    public void removeNavigationChangedListener(NavigationInfoChangeListener listener) {
        changeListener.remove(listener);
    }

    /**
     * Set the navigation type to the new value(s). The array must be non-null.
     * If the underlying implementation does not support any of the types
     * requested, it shall default to the type NONE. If the array is empty,
     * it defaults to NONE.
     *
     * @param types The list of types to now use in order of preference
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldValueException The list was null or empty
     */
    @Override
    public void setType(String[] types, int numValid)
        throws InvalidFieldValueException {

        if(numValid == 0) {
            vfNavType = new String[] { NAV_TYPE_NONE };
        } else {
            if(vfNavType.length != numValid)
                vfNavType = new String[numValid];

            System.arraycopy(types, 0, vfNavType, 0, numValid);
        }

        if(!inSetup) {
            hasChanged[FIELD_NAV_TYPE] = true;
            fireFieldChanged(FIELD_NAV_TYPE);
            for (NavigationInfoChangeListener l : changeListener) {
                l.notifyNavigationModesChanged(vfNavType, numValid);
            }
        }
    }

    /**
     * Get the dimensions of the avatar in use.
     *
     * @return A list of floats describing the dimension of the avatar.
     */
    @Override
    public float[] getAvatarSize() {
        return correctedAvatarSize;
    }

    /**
     * Set the dimensions of the avatar in use. The array must have at least
     * three values in it as required by the specification.
     *
     * @param size The new size values to use
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldValueException The array did not contain 3 values
     */
    @Override
    public void setAvatarSize(float[] size, int numValid)
        throws InvalidFieldValueException {

        // ignored because avatarSize is not a valid field for GeoViewpoint.
    }

    /**
     * Get the speed that we are currently moving at.
     *
     * @return The current movement speed.
     */
    @Override
    public float getSpeed() {
        return correctedSpeed;
    }

    /**
     * Set the speed to move at. The speed value must be non-negative.
     *
     * @param newSpeed The new speed value to use
     * @throws InvalidFieldValueException The speed was negative
     */
    @Override
    public void setSpeed(float newSpeed) throws InvalidFieldValueException {
        // ignored because speed is not a valid field for GeoViewpoint.
    }

    /**
     * Get the visibility limit that we are currently operating at.
     *
     * @return The current movement visibility limit.
     */
    @Override
    public float getVisibilityLimit() {
        // Unknown for now. Needs work. Spec just says:
        // "The GeoViewpoint node may be implemented as if there is an embedded
        // NavigationInfo node that is bound and unbound with the GeoViewpoint
        // node. As such, a X3D browser should internally set the speed,
        // avatarSize, and visibilityLimit fields to an appropriate value for
        // the viewpoint's elevation."

        return 0;
    }

    /**
     * Set the visibility limie to move at. The visibility limit value must be
     * non-negative.
     *
     * @param limit The new visibility limit value to use
     * @throws InvalidFieldValueException The visibility limit was negative
     */
    @Override
    public void setVisibilityLimit(float limit)
        throws InvalidFieldValueException {
        // Ignored because visibilityLimit is not a valid field for GeoViewpoint
    }

    /**
     * Get the status of the headlight that we are operating with. A true
     * value represents the headlight being on.
     *
     * @return true if the headlight is to be used
     */
    @Override
    public boolean getHeadlight() {
        return vfHeadlight;
    }

    /**
     * Set the state of the headlight to the new value.
     *
     * @param enable True if we are to use the headlight
     */
    @Override
    public void setHeadlight(boolean enable) {

        if(enable != vfHeadlight) {
            vfHeadlight = enable;

            if(!inSetup) {
                hasChanged[FIELD_HEADLIGHT] = true;
                fireFieldChanged(FIELD_HEADLIGHT);
            }
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

        if(pGeoOrigin != null)
            pGeoOrigin.setupFinished();
        else if(vfGeoOrigin != null)
            vfGeoOrigin.setupFinished();

        originManager = OriginManagerFactory.getInstance(stateManager);
        useOriginManager = originManager.getEnabled();
        configLocalOrigin();

        // Fetch the geo transform and shift the first set of points
        try {
            GTTransformUtils gtu = GTTransformUtils.getInstance();
            boolean[] swap = new boolean[1];

            geoTransform = gtu.createSystemTransform(vfGeoSystem, swap);
            geoCoordSwap = swap[0];

            if(geoCoordSwap) {
                double tmp = vfPosition[0];
                vfPosition[0] = vfPosition[1];
                vfPosition[1] = tmp;
                geoTransform.transform(vfPosition, 0, localPosition, 0, 1);

                tmp = vfPosition[0];
                vfPosition[0] = vfPosition[1];
                vfPosition[1] = tmp;
            } else
                geoTransform.transform(vfPosition, 0, localPosition, 0, 1);

            if (local_origin != null) {
                localPosition[0] -= local_origin[0];
                localPosition[1] -= local_origin[1];
                localPosition[2] -= local_origin[2];

                centerOfRotation[0] -= (float)local_origin[0];
                centerOfRotation[1] -= (float)local_origin[1];
                centerOfRotation[2] -= (float)local_origin[2];
            }
        } catch(FactoryException fe) {
            errorReporter.errorReport(FACTORY_ERR_MSG, fe);
        } catch(TransformException te) {
            errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
        }
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

        int ret_val = (index == null) ? -1 : index;
        // retainUserOffsets was added in 3.2. Change
        // the field index to say that they don't exist for VRML or
        // X3D 3.0 and 3.1
        if((ret_val == FIELD_RETAIN_USER_OFFSETS)
            &&
           ((vrmlMajorVersion == 2) ||
            ((vrmlMajorVersion == 3) && (vrmlMinorVersion < 2))))
            ret_val = -1;

        return ret_val;
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        return (index < 0 || index > LAST_VIEWPOINT_INDEX) ?
            null : fieldDecl[index];
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.ViewpointNodeType;
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

        switch(index) {
            case FIELD_DESCRIPTION:
                fieldData.clear();
                fieldData.stringValue = vfDescription;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.numElements = 1;
                break;
                
            case FIELD_FARDISTANCE:
                fieldData.clear();
                fieldData.floatValue = vfFarDistance;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
                break;
                
            case FIELD_NEARDISTANCE:
                fieldData.clear();
                fieldData.floatValue = vfNearDistance;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_FIELDOFVIEW:
                fieldData.clear();
                fieldData.floatValue = vfFieldOfView;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_JUMP:
                fieldData.clear();
                fieldData.booleanValue = vfJump;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_RETAIN_USER_OFFSETS:
                fieldData.clear();
                fieldData.booleanValue = vfRetainUserOffsets;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_VIEWALL:
                fieldData.clear();
                fieldData.booleanValue = vfViewAll;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_HEADLIGHT:
                fieldData.clear();
                fieldData.booleanValue = vfHeadlight;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ORIENTATION:
                fieldData.clear();
                fieldData.floatArrayValues = vfOrientation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_POSITION:
                fieldData.clear();
                fieldData.doubleArrayValues = vfPosition;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_GEO_ORIGIN:
                fieldData.clear();
                if (pGeoOrigin != null)
                    fieldData.nodeValue = pGeoOrigin;
                else
                    fieldData.nodeValue = vfGeoOrigin;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_GEO_SYSTEM:
                fieldData.clear();
                fieldData.stringArrayValues = vfGeoSystem;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfGeoSystem.length;
                break;

            case FIELD_SPEED_FACTOR:
                fieldData.clear();
                fieldData.floatValue = vfSpeedFactor;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_NAV_TYPE:
                fieldData.clear();
                fieldData.stringArrayValues = vfNavType;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfNavType.length;
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
                case FIELD_FIELDOFVIEW:
                    destNode.setValue(destIndex, vfFieldOfView);
                    break;

                case FIELD_JUMP:
                    destNode.setValue(destIndex, vfJump);
                    break;

                case FIELD_RETAIN_USER_OFFSETS:
                    destNode.setValue(destIndex, vfRetainUserOffsets);
                    break;

                case FIELD_VIEWALL:
                    destNode.setValue(destIndex, vfViewAll);
                    break;

                case FIELD_HEADLIGHT:
                    destNode.setValue(destIndex, vfHeadlight);
                    break;

                case FIELD_DESCRIPTION:
                    destNode.setValue(destIndex, vfDescription);
                    break;

                case FIELD_FARDISTANCE:
                    destNode.setValue(destIndex, vfFarDistance);
                    break;

                case FIELD_NEARDISTANCE:
                    destNode.setValue(destIndex, vfNearDistance);
                    break;

                case FIELD_NAV_TYPE:
                    destNode.setValue(destIndex, vfNavType, vfNavType.length);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseViewpoint.sendRoute: No field!" + srcIndex);
            ife.printStackTrace(System.err);
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
            ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not relate to one of our
     *    fields
     * @throws InvalidFieldValueException The value does not contain an
     *    in range value or bad numerical type
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch (index) 
        {
            case FIELD_JUMP:
                setJump(value);
                break;

            case FIELD_RETAIN_USER_OFFSETS:
                setRetainUserOffsets(value);
                break;

            case FIELD_VIEWALL:
                setViewAll(value);
                break;

            case FIELD_HEADLIGHT:
                setHeadlight(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not relate to one of our
     *    fields
     * @throws InvalidFieldValueException The value does not contain an
     *    in range value or bad numerical type
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {
            case FIELD_FIELDOFVIEW:
                setFieldOfView(value);
                break;

            case FIELD_FARDISTANCE:
                setFarDistance(value);
                break;

            case FIELD_NEARDISTANCE:
                setNearDistance(value);
                break;

            case FIELD_SPEED_FACTOR:
                if(!inSetup)
                    throwInitOnlyWriteException("geoSystem");

                vfSpeedFactor = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not relate to one of our
     *    fields
     * @throws InvalidFieldValueException The value does not contain an
     *    in range value or bad numerical type
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ORIENTATION:
            case FIELD_SET_ORIENTATION:
                setOrientation(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not relate to one of our
     *    fields
     * @throws InvalidFieldValueException The value does not contain an
     *    in range value or bad numerical type
     */
    @Override
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_POSITION:
            case FIELD_SET_POSITION:
                setPosition(value);
                break;

            default:
                super.setValue(index, value, numValid);
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
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DESCRIPTION:
                vfDescription = value;
                if(!inSetup) {
                    hasChanged[FIELD_DESCRIPTION] = true;
                    fireFieldChanged(FIELD_DESCRIPTION);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the MFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GEO_SYSTEM:
                if(!inSetup)
                    throwInitOnlyWriteException("geoSystem");

                if(vfGeoSystem.length != numValid)
                    vfGeoSystem = new String[numValid];

                System.arraycopy(value, 0, vfGeoSystem, 0, numValid);
                break;

            case FIELD_NAV_TYPE:
                setType(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_GEO_ORIGIN:
                if(!inSetup)
                    throwInitOnlyWriteException("geoOrigin");

                setGeoOrigin(node);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Public access methods
    //----------------------------------------------------------

    /**
     * Convenience method to set the position of the viewpoint. Designed to
     * be overridden by derived classes. Make sure you also call this first
     * to set the field values.
     *
     * @param position The position vector to use
     */
    protected void setPosition(double[] position) {
        vfPosition[0] = position[0];
        vfPosition[1] = position[1];
        vfPosition[2] = position[2];

        if((!inSetup) && (geoTransform != null)) {
            try {
                geoTransform.transform(vfPosition, 0, localPosition, 0, 1);

                if(geoCoordSwap) {
                    double tmp = vfPosition[0];
                    vfPosition[0] = vfPosition[1];
                    vfPosition[1] = tmp;
                    geoTransform.transform(vfPosition, 0, localPosition, 0, 1);

                    tmp = vfPosition[0];
                    vfPosition[0] = vfPosition[1];
                    vfPosition[1] = tmp;
                } else
                    geoTransform.transform(vfPosition, 0, localPosition, 0, 1);

                if (local_origin != null) {
                    localPosition[0] -= local_origin[0];
                    localPosition[1] -= local_origin[1];
                    localPosition[2] -= local_origin[2];
                }
            } catch(TransformException te) {
                errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
            }
        }
    }

    /**
     * Convenience method to set the orientation of the viewpoint. Designed to
     * be overridden by derived classes. Make sure you also call this first
     * to set the field values.
     *
     * @param dir The orientation quaternion to use
     */
    protected void setOrientation(float[] dir) {
        vfOrientation[0] = dir[0];
        vfOrientation[1] = dir[1];
        vfOrientation[2] = dir[2];
        vfOrientation[3] = dir[3];
    }

    /**
     * Transform the orientation of the object to one in the local coordinate
     * system.
     */
    private void getLocalOrientation(double[] position, AxisAngle4d axis) {
        posVec.x = position[0];
        posVec.y = position[1];
        posVec.z = position[2];

        double norm = posVec.x * posVec.x + posVec.y * posVec.y + posVec.z * posVec.z;

        if(norm != 0) {
            norm = 1 / Math.sqrt(norm);
            posVec.x *= norm;
            posVec.y *= norm;
            posVec.z *= norm;
        } else {
            posVec.x = 0.0f;
            posVec.y = 1.0f;
            posVec.z = 0.0f;
        }

        double angle = YUP.angle(posVec);

        posVec.cross(YUP, posVec);
        posVec.normalize();

        axis.x = (float) posVec.x;
        axis.y = (float) posVec.y;
        axis.z = (float) posVec.z;
        axis.angle = (float) angle;

        angle = XUP.angle(posVec);

        posVec.cross(XUP, posVec);
        posVec.normalize();

        Quat4d orig = new Quat4d();
        orig.set(axis);
        Quat4d rot = new Quat4d();
        rot.set(new AxisAngle4d(posVec.x, posVec.y, posVec.z, angle));
    //////////////////////////////////////////////////////////////////
    // using the vecmath Quat4d (or Matrix3d) gives a bogus result
    // when the longitude is exactly +/- 90 degrees, replacing the
    // multiply calculation with the jeospace version
        //orig.mul(rot);
    orig.set(
        orig.w * rot.x + orig.x * rot.w + orig.y * rot.z - orig.z * rot.y,
        orig.w * rot.y + orig.y * rot.w + orig.z * rot.x - orig.x * rot.z,
        orig.w * rot.z + orig.z * rot.w + orig.x * rot.y - orig.y * rot.x,
        orig.w * rot.w - orig.x * rot.x - orig.y * rot.y - orig.z * rot.z);
    //////////////////////////////////////////////////////////////////
        axis.set(orig);
    }

    /**
     * Convert specified orientation into an axis angle.
     */
    protected void convOriToAxisAngle() {

        if (local_origin != null) {
            localPosition[0] += local_origin[0];
            localPosition[1] += local_origin[1];
            localPosition[2] += local_origin[2];
        }

        getLocalOrientation(localPosition, tmpAxis);

        if (local_origin != null) {
            localPosition[0] -= local_origin[0];
            localPosition[1] -= local_origin[1];
            localPosition[2] -= local_origin[2];
        }

        /* make quaternions out of the two VRML axis/angle rotations */
        local_quat.set(tmpAxis);

        tmpAxis.x = vfOrientation[0];
        tmpAxis.y = vfOrientation[1];
        tmpAxis.z = vfOrientation[2];
        tmpAxis.angle = vfOrientation[3];

        rel_quat.set(tmpAxis);

        /* now combine these together and return the axis/angle */

        comb_quat.mul(local_quat, rel_quat);

        tmpAxis.set(comb_quat);

        axis.x = (float) tmpAxis.x;
        axis.y = (float) tmpAxis.y;
        axis.z = (float) tmpAxis.z;
        axis.angle = (float) tmpAxis.angle;

        double norm = axis.x * axis.x + axis.y * axis.y + axis.z * axis.z;

        if(norm != 0) {
            norm = 1 / Math.sqrt(norm);
            axis.x *= norm;
            axis.y *= norm;
            axis.z *= norm;
        } else {
            axis.x = 0.0f;
            axis.y = 1.0f;
            axis.z = 0.0f;
        }

        axis.angle = (float)Math.IEEEremainder(axis.angle, Math.PI * 2);
    }

    /**
     * Send a notification to the registered listeners the center of rotation has
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param val The new center of Rotation
     */
    protected void fireCenterOfRotationChanged(float[] val) {
        // Notify listeners of new value
        int num_listeners = viewpointListeners.size();

        for(int i = 0; i < num_listeners; i++) {
            ViewpointListener ul = viewpointListeners.get(i);
            ul.centerOfRotationChanged(val);
        }
    }

    /**
     * Send a notification to the registered listeners the field of view has
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param val The new center of Rotation
     */
    protected void fireFieldOfViewChanged(float[] val) {
        // Notify listeners of new value
        int num_listeners = viewpointListeners.size();

        for(int i = 0; i < num_listeners; i++) {
            ViewpointListener  ul = viewpointListeners.get(i);
            ul.fieldOfViewChanged(val);
        }
    }

    //----------------------------------------------------------
    // Internal methods
    //----------------------------------------------------------

    /**
     * Set node content for the geoOrigin node.
     *
     * @param geo The new geoOrigin
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setGeoOrigin(VRMLNodeType geo)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        BaseGeoOrigin node;
        VRMLNodeType old_node;

        if(pGeoOrigin != null)
            old_node = pGeoOrigin;
        else
            old_node = vfGeoOrigin;

        if(geo instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geo).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseGeoOrigin))
                throw new InvalidFieldValueException(GEO_ORIGIN_PROTO_MSG);

            node = (BaseGeoOrigin)impl;
            pGeoOrigin = (VRMLProtoInstance)geo;

        } else if(geo != null && !(geo instanceof BaseGeoOrigin)) {
            throw new InvalidFieldValueException(GEO_ORIGIN_NODE_MSG);
        } else {
            pGeoOrigin = null;
            node = (BaseGeoOrigin)geo;
        }

        vfGeoOrigin = node;
        if(geo != null)
            updateRefs(geo, true);

        if(old_node != null)
            updateRefs(old_node, false);
    }

    /**
     * Configure the local_origin
     */
    private void configLocalOrigin() {
        if (useOriginManager) {
            Vector3d origin = originManager.getOrigin();
            if (origin == null) {
                local_origin = null;
            } else {
                if (local_origin == null) {
                    local_origin = new double[3];
                }
                if ((local_origin[0] != origin.x) || (local_origin[1] != origin.y) ||
                    (local_origin[2] != origin.z)) {

                    local_origin[0] = origin.x;
                    local_origin[1] = origin.y;
                    local_origin[2] = origin.z;
                }
            }
        } else {
            if (vfGeoOrigin != null) {
                local_origin = ((VRMLLocalOriginNodeType)vfGeoOrigin).getConvertedCoordRef();
            } else {
                local_origin = null;
            }
        }
    }
}

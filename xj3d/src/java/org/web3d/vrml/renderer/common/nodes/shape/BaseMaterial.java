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

package org.web3d.vrml.renderer.common.nodes.shape;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base implementation of a material node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseMaterial extends AbstractNode
    implements VRMLMaterialNodeType {

    /** Field index for ambientIntensity */
    protected static final int FIELD_AMBIENT_INTENSITY = LAST_NODE_INDEX + 1;

    /** Field index for diffuseColor */
    protected static final int FIELD_DIFFUSE_COLOR = LAST_NODE_INDEX + 2;

    /** Field index for emissiveColor */
    protected static final int FIELD_EMISSIVE_COLOR = LAST_NODE_INDEX + 3;

    /** Field index for shininess */
    protected static final int FIELD_SHININESS = LAST_NODE_INDEX + 4;

    /** Field index for specularColor */
    protected static final int FIELD_SPECULAR_COLOR = LAST_NODE_INDEX + 5;

    /** Field index for transparency */
    protected static final int FIELD_TRANSPARENCY = LAST_NODE_INDEX + 6;

    /** Field index for normalScale */
    protected static final int FIELD_NORMALSCALE = LAST_NODE_INDEX + 7;

    /** Field index for normalScale */
    protected static final int FIELD_OCCLUSIONSTRENGTH = LAST_NODE_INDEX + 8;

    /** Field index for ambientTextureMapping */
    protected static final int FIELD_AMBIENTTEXTUREMAPPING = LAST_NODE_INDEX + 9;

    /** Field index for diffuseTextureMapping */
    protected static final int FIELD_DIFFUSETEXTUREMAPPING = LAST_NODE_INDEX + 10;

    /** Field index for emissiveTextureMapping */
    protected static final int FIELD_EMISSIVETEXTUREMAPPING = LAST_NODE_INDEX + 11;

    /** Field index for normalTextureMapping */
    protected static final int FIELD_NORMALTEXTUREMAPPING = LAST_NODE_INDEX + 12;

    /** Field index for occlusionTextureMapping */
    protected static final int FIELD_OCCLUSIONTEXTUREMAPPING = LAST_NODE_INDEX + 13;

    /** Field index for shininessTextureMapping */
    protected static final int FIELD_SHININESSTEXTUREMAPPING = LAST_NODE_INDEX + 14;

    /** Field index for specularTextureMapping */
    protected static final int FIELD_SPECULARTEXTUREMAPPING = LAST_NODE_INDEX + 15;

    /** Field index for SFNode ambientTexture */
    protected static final int FIELD_AMBIENTTEXTURE = LAST_NODE_INDEX + 16;

    /** Field index for SFNode diffuseTexture */
    protected static final int FIELD_DIFFUSETEXTURE = LAST_NODE_INDEX + 17;

    /** Field index for SFNode emissiveTexture */
    protected static final int FIELD_EMISSIVETEXTURE = LAST_NODE_INDEX + 18;

    /** Field index for SFNode normalTexture */
    protected static final int FIELD_NORMALTEXTURE = LAST_NODE_INDEX + 19;

    /** Field index for SFNode occlusionTexture */
    protected static final int FIELD_OCCLUSIONTEXTURE = LAST_NODE_INDEX + 20;

    /** Field index for SFNode shininessTexture */
    protected static final int FIELD_SHININESSTEXTURE = LAST_NODE_INDEX + 21;

    /** Field index for SFNode specularTexture */
    protected static final int FIELD_SPECULARTEXTURE = LAST_NODE_INDEX + 22;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = FIELD_SPECULARTEXTURE + 1;

    /** Error message when the user code barfs */
    private static final String EMISSIVE_ERROR_MSG =
        "Error sending emissive changed notification to: ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** exposedField SFFloat ambientIntensity 0.2 */
    protected float vfAmbientIntensity;

    /** exposedField SFColor diffuseColor 0.8 0.8 0.8 */
    protected float[] vfDiffuseColor;

    /** exposedField SFColor emissiveColor 0 0 0 */
    protected float[] vfEmissiveColor;

    /** exposedField SFFloat shininess 0.2 */
    protected float vfShininess;

    /** exposedField SFColor specularColor 0 0 0 */
    protected float[] vfSpecularColor;

    /** exposedField SFFloat transparency 0 */
    protected float vfTransparency;

    /** exposedField SFFloat normalScale 1 */
    protected float vfNormalScale;

    /** exposedField SFFloat occlusionStrength 1 */
    protected float vfOcclusionStrength;

    // not bothering with proto nodes for these SFNode fields
    
    /** SFNode ambientTexture. */
    protected VRMLTextureNodeType vfAmbientTexture;

    /** SFNode texture diffuseTexture. */
    protected VRMLTextureNodeType vfDiffuseTexture;

    /** SFNode texture emissiveTexture. */
    protected VRMLTextureNodeType vfEmissiveTexture;

    /** SFNode texture normalTexture. */
    protected VRMLTextureNodeType vfNormalTexture;

    /** SFNode texture occlusionTexture. */
    protected VRMLTextureNodeType vfOcclusionTexture;

    /** SFNode texture shininessTexture. */
    protected VRMLTextureNodeType vfShininessTexture;

    /** SFNode texture specularTexture. */
    protected VRMLTextureNodeType vfSpecularTexture;

    /** Should we ignore the specified diffuseColor */
    protected boolean ignoreDiffuse;

    /** Color listener for passing back emissive changes */
    private MaterialColorListener colorListener;

    // Performance class vars

    /** Scratch variable for float[] conversion */
    private float[] flScratch;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_AMBIENT_INTENSITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "ambientIntensity");
        fieldDecl[FIELD_DIFFUSE_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "diffuseColor");
        fieldDecl[FIELD_EMISSIVE_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "emissiveColor");
        fieldDecl[FIELD_SHININESS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "shininess");
        fieldDecl[FIELD_SPECULAR_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "specularColor");
        fieldDecl[FIELD_TRANSPARENCY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "transparency");
        fieldDecl[FIELD_NORMALSCALE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "normalScale");
        fieldDecl[FIELD_OCCLUSIONSTRENGTH] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "occlusionStrength");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_AMBIENT_INTENSITY;
        fieldMap.put("ambientIntensity", idx);
        fieldMap.put("set_ambientIntensity", idx);
        fieldMap.put("ambientIntensity_changed", idx);

        idx = FIELD_TRANSPARENCY;
        fieldMap.put("transparency", idx);
        fieldMap.put("set_transparency", idx);
        fieldMap.put("transparency_changed", idx);

        idx = FIELD_NORMALSCALE;
        fieldMap.put("normalScale", idx);
        fieldMap.put("set_normalScale", idx);
        fieldMap.put("normalScale_changed", idx);

        idx = FIELD_OCCLUSIONSTRENGTH;
        fieldMap.put("occlusionStrength", idx);
        fieldMap.put("set_occlusionStrength", idx);
        fieldMap.put("occlusionStrength_changed", idx);

        idx = FIELD_DIFFUSE_COLOR;
        fieldMap.put("diffuseColor", idx);
        fieldMap.put("set_diffuseColor", idx);
        fieldMap.put("diffuseColor_changed", idx);

        idx = FIELD_EMISSIVE_COLOR;
        fieldMap.put("emissiveColor", idx);
        fieldMap.put("set_emissiveColor", idx);
        fieldMap.put("emissiveColor_changed", idx);

        idx = FIELD_SHININESS;
        fieldMap.put("shininess", idx);
        fieldMap.put("set_shininess", idx);
        fieldMap.put("shininess_changed", idx);

        idx = FIELD_SPECULAR_COLOR;
        fieldMap.put("specularColor", idx);
        fieldMap.put("set_specularColor", idx);
        fieldMap.put("specularColor_changed", idx);
    }

    /**
     * Construct a default instance of the material
     */
    protected BaseMaterial() {
        super("Material");

        hasChanged = new boolean[NUM_FIELDS];
        vfAmbientIntensity = 0.2f;
        vfDiffuseColor = new float[] { 0.8f, 0.8f, 0.8f };
        vfEmissiveColor = new float[] { 0, 0, 0 };
        vfShininess = 0.2f;
        vfSpecularColor = new float[] { 0, 0, 0 };
        vfTransparency = 0;
        vfNormalScale = 1;
        vfOcclusionStrength=  1;
        
        vfAmbientTexture = null;
        vfDiffuseTexture = null;
        vfEmissiveTexture = null;
        vfNormalTexture = null;
        vfOcclusionTexture = null;
        vfShininessTexture = null;
        vfSpecularTexture = null;

        ignoreDiffuse = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseMaterial(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("shininess");
            VRMLFieldData field = node.getFieldValue(index);
            vfShininess = field.floatValue;

            index = node.getFieldIndex("transparency");
            field = node.getFieldValue(index);
            vfTransparency = field.floatValue;

            index = node.getFieldIndex("normalscale");
            field = node.getFieldValue(index);
            vfNormalScale = field.floatValue;

            index = node.getFieldIndex("occlusionstrength");
            field = node.getFieldValue(index);
            vfOcclusionStrength = field.floatValue;

            index = node.getFieldIndex("ambientTexture");
            field = node.getFieldValue(index);
            vfAmbientTexture = (VRMLTextureNodeType) field.nodeValue;

            index = node.getFieldIndex("diffuseTexture");
            field = node.getFieldValue(index);
            vfDiffuseTexture = (VRMLTextureNodeType) field.nodeValue;

            index = node.getFieldIndex("emissiveTexture");
            field = node.getFieldValue(index);
            vfEmissiveTexture = (VRMLTextureNodeType) field.nodeValue;

            index = node.getFieldIndex("normalTexture");
            field = node.getFieldValue(index);
            vfNormalTexture = (VRMLTextureNodeType) field.nodeValue;

            index = node.getFieldIndex("occlusionTexture");
            field = node.getFieldValue(index);
            vfOcclusionTexture = (VRMLTextureNodeType) field.nodeValue;

            index = node.getFieldIndex("shininessTexture");
            field = node.getFieldValue(index);
            vfShininessTexture = (VRMLTextureNodeType) field.nodeValue;

            index = node.getFieldIndex("specularTexture");
            field = node.getFieldValue(index);
            vfSpecularTexture = (VRMLTextureNodeType) field.nodeValue;

            index = node.getFieldIndex("ambientIntensity");
            field = node.getFieldValue(index);
            vfAmbientIntensity = field.floatValue;

            index = node.getFieldIndex("diffuseColor");
            field = node.getFieldValue(index);
            vfDiffuseColor[0] = field.floatArrayValues[0];
            vfDiffuseColor[1] = field.floatArrayValues[1];
            vfDiffuseColor[2] = field.floatArrayValues[2];

            index = node.getFieldIndex("emissiveColor");
            field = node.getFieldValue(index);
            vfEmissiveColor[0] = field.floatArrayValues[0];
            vfEmissiveColor[1] = field.floatArrayValues[1];
            vfEmissiveColor[2] = field.floatArrayValues[2];

            index = node.getFieldIndex("specularColor");
            field = node.getFieldValue(index);
            vfSpecularColor[0] = field.floatArrayValues[0];
            vfSpecularColor[1] = field.floatArrayValues[1];
            vfSpecularColor[2] = field.floatArrayValues[2];
        }
        catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLMaterialNodeType
    //-------------------------------------------------------------

    /**
     * Accessor method to set a new value for field attribute
     * <b>ambientIntensity</b>. How much ambient omnidirectional light is
     * reflected from all light sources.
     *
     * @param newAmbientIntensity The new intensity value
     */
    @Override
    public void setAmbientIntensity(float newAmbientIntensity)
        throws InvalidFieldValueException {

        try {
            FieldValidator.checkFloat("Material.ambientIntensity",newAmbientIntensity);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.messageReport("AmbientIntensity value outside spec: " + newAmbientIntensity + " Value clamped.");
            if (newAmbientIntensity < 0) {
                newAmbientIntensity = 0;
            } else if (newAmbientIntensity > 1) {
                newAmbientIntensity = 1.0f;
            }
        }
        vfAmbientIntensity = newAmbientIntensity;

        if(!inSetup) {
            hasChanged[FIELD_AMBIENT_INTENSITY] = true;
            fireFieldChanged(FIELD_AMBIENT_INTENSITY);
        }
    }

    /**
     * Accessor method to get current value of field <b>ambientIntensity</b>,
     * default value is <code>0.2</code>
     *
     * @return The current ambientIntensity
     */
    @Override
    public float getAmbientIntensity() {
        return vfAmbientIntensity;
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>diffuseColor</b>.  How much direct, angle-dependent light is
     * reflected from all light sources.
     *
     * @param newDiffuseColor The new value of diffuseColor
     */
    @Override
    public void setDiffuseColor(float[] newDiffuseColor)
        throws InvalidFieldValueException {

        try {
            FieldValidator.checkColorVector("Material.diffuseColor",newDiffuseColor);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.messageReport("DiffuseColor value outside spec: " + java.util.Arrays.toString(newDiffuseColor) + " Value clamped.");
            for(int i=0; i < 3; i++) {
                if (newDiffuseColor[i] < 0) {
                    newDiffuseColor[i] = 0;
                } else if (newDiffuseColor[i] > 1) {
                    newDiffuseColor[i] = 1;
                }
            }
        }
        vfDiffuseColor[0] = newDiffuseColor[0];
        vfDiffuseColor[1] = newDiffuseColor[1];
        vfDiffuseColor[2] = newDiffuseColor[2];

        if(!inSetup) {
            hasChanged[FIELD_DIFFUSE_COLOR] = true;
            fireFieldChanged(FIELD_DIFFUSE_COLOR);
        }
    }

    /**
     * Accessor method to get current value of field <b>diffuseColor</b>,
     * default value is <code>0.8 0.8 0.8</code>.
     *
     * @return The current value of diffuseColor
     */
    @Override
    public float[] getDiffuseColor() {
        return vfDiffuseColor;
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>emissiveColor</b>. How much glowing light is emitted from this object.
     *
     * @param newEmissiveColor The new value of EmissiveColor
     */
    @Override
    public void setEmissiveColor(float[] newEmissiveColor)
        throws InvalidFieldValueException {

        try {
            FieldValidator.checkColorVector("Material.emmissiveColor",newEmissiveColor);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.messageReport("EmissiveColor value outside spec: " + java.util.Arrays.toString(newEmissiveColor) + " Value clamped.");
            for(int i=0; i < 3; i++) {
                if (newEmissiveColor[i] < 0) {
                    newEmissiveColor[i] = 0;
                } else if (newEmissiveColor[i] > 1) {
                    newEmissiveColor[i] = 1;
                }
            }
        }
        vfEmissiveColor[0] = newEmissiveColor[0];
        vfEmissiveColor[1] = newEmissiveColor[1];
        vfEmissiveColor[2] = newEmissiveColor[2];

        if(!inSetup) {
            hasChanged[FIELD_EMISSIVE_COLOR] = true;
            fireFieldChanged(FIELD_EMISSIVE_COLOR);
            fireEmissiveColorChanged(colorListener);
        }
    }

    /**
     * Accessor method to get current value of field <b>emissiveColor</b>,
     * default value is <code>0 0 0</code>.
     *
     * @return The current value of EmissiveColor
     */
    @Override
    public float[] getEmissiveColor() {
        return vfEmissiveColor;
    }

    /**
     * Accessor method to set a new value for field attribute <b>shininess</b>.
     * Low values provide soft specular glows, high values provide sharper,
     * smaller highlights.
     *
     * @param newShininess The new value of Shininess
     */
    @Override
    public void setShininess(float newShininess)
        throws InvalidFieldValueException  {

        try {
            FieldValidator.checkFloat("Material.shininess",newShininess);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.messageReport("Shininess value outside spec: " + newShininess + " Value clamped.");
            if (newShininess < 0) {
                newShininess = 0;
            } else if (newShininess > 1) {
                newShininess = 1.0f;
            }
        }
        vfShininess = newShininess;

        if(!inSetup) {
            hasChanged[FIELD_SHININESS] = true;
            fireFieldChanged(FIELD_SHININESS);
        }
    }

    /**
     * Accessor method to get current value of field <b>shininess</b>,
     * default value is <code>0.2</code>.
     *
     * @return The current value of Shininess
     */
    @Override
    public float getShininess() {
        return vfShininess;
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>specularColor</b>. Specular highlights are brightness
     * reflections (example:  shiny spots on an apple).
     *
     * @param newSpecularColor The new value of SpecularColor
     */
    @Override
    public void setSpecularColor (float[] newSpecularColor)
        throws InvalidFieldValueException {

        try {
            FieldValidator.checkColorVector("Material.specularColor",newSpecularColor);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.messageReport("SpecularColor value outside spec: " + java.util.Arrays.toString(newSpecularColor) + " Value clamped.");
            for(int i=0; i < 3; i++) {
                if (newSpecularColor[i] < 0) {
                    newSpecularColor[i] = 0;
                } else if (newSpecularColor[i] > 1) {
                    newSpecularColor[i] = 1;
                }
            }
        }
        vfSpecularColor[0] = newSpecularColor[0];
        vfSpecularColor[1] = newSpecularColor[1];
        vfSpecularColor[2] = newSpecularColor[2];

        if(!inSetup) {
            hasChanged[FIELD_SPECULAR_COLOR] = true;
            fireFieldChanged(FIELD_SPECULAR_COLOR);
        }
    }

    /**
     * Accessor method to get current value of field <b>specularColor</b>,
     * default value is <code>0 0 0</code>.
     *
     * @return The current value of SpecularColor
     */
    @Override
    public float[] getSpecularColor () {
        return vfSpecularColor;
    }

    /**
     * Get the current ambientTexture.
     *
     * @return The currently set ambientTexture
     */
    // TODO consistent @Override
    public VRMLNodeType getAmbientTexture() {
        return vfAmbientTexture;
    }

    /**
     * Get the current diffuseTexture.
     *
     * @return The currently set diffuseTexture
     */
    // TODO consistent @Override
    public VRMLNodeType getDiffuseTexture() {
        return vfDiffuseTexture;
    }

    /**
     * Get the current emissiveTexture.
     *
     * @return The currently set emissiveTexture
     */
    // TODO consistent @Override
    public VRMLNodeType getEmissiveTexture() {
        return vfEmissiveTexture;
    }

    /**
     * Get the current normalTexture.
     *
     * @return The currently set normalTexture
     */
    // TODO consistent @Override
    public VRMLNodeType getNormalTexture() {
        return vfNormalTexture;
    }

    /**
     * Get the current occlusionTexture.
     *
     * @return The currently set occlusionTexture
     */
    // TODO consistent @Override
    public VRMLNodeType getOcclusionTexture() {
        return vfOcclusionTexture;
    }

    /**
     * Get the current shininessTexture.
     *
     * @return The currently set shininessTexture
     */
    // TODO consistent @Override
    public VRMLNodeType getShininessTexture() {
        return vfShininessTexture;
    }

    /**
     * Get the current specularTexture.
     *
     * @return The currently set specularTexture
     */
    // TODO consistent @Override
    public VRMLNodeType getSpecularTexture() {
        return vfSpecularTexture;
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>transparency</b>.  How "clear" an object is:  1.0 is completely
     * transparent, 0.0 is completely opaque .
     *
     * @param newTransparency The new value of transparency
     */
    @Override
    public void setTransparency(float newTransparency)
        throws InvalidFieldValueException {

        try {
            FieldValidator.checkFloat("Material.transparency",newTransparency);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.messageReport("Transparency value outside spec: " + newTransparency + " Value clamped.");
            if (newTransparency < 0) {
                newTransparency = 0;
            } else if (newTransparency > 1) {
                newTransparency = 1.0f;
            }
        }

        vfTransparency = newTransparency;

        if(!inSetup) {
            hasChanged[FIELD_TRANSPARENCY] = true;
            fireFieldChanged(FIELD_TRANSPARENCY);
        }
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>normalScale</b>.  
     *
     * @param newNormalScale The new value of normalScale
     */
    // TODO inconsistent @Override
    public void setNormalScale(float newNormalScale)
        throws InvalidFieldValueException {

        try {
            FieldValidator.checkFloatPositiveInfinity("Material.normalScale",newNormalScale);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.messageReport("normalScale value outside spec: " + newNormalScale + " Value clamped.");
            if (newNormalScale < 0) {
                newNormalScale = 0;
            } else if (newNormalScale > 1) {
                newNormalScale = 1.0f;
            }
        }

        vfNormalScale = newNormalScale;

        if(!inSetup) {
            hasChanged[FIELD_NORMALSCALE] = true;
            fireFieldChanged(FIELD_NORMALSCALE);
        }
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>occlusionStrength</b>.
     *
     * @param newOcclusionStrength The new value of occlusionStrength
     */
    // TODO inconsistent @Override
    public void setOcclusionStrength(float newOcclusionStrength)
        throws InvalidFieldValueException {

        try {
            FieldValidator.checkFloat("Material.occlusionStrength",newOcclusionStrength);
        } catch(InvalidFieldValueException ifve) {
            errorReporter.messageReport("occlusionStrength value outside spec: " + newOcclusionStrength + " Value clamped.");
            if (newOcclusionStrength < 0) {
                newOcclusionStrength = 0;
            } else if (newOcclusionStrength > 1) {
                newOcclusionStrength = 1.0f;
            }
        }

        vfOcclusionStrength = newOcclusionStrength;

        if(!inSetup) {
            hasChanged[FIELD_OCCLUSIONSTRENGTH] = true;
            fireFieldChanged(FIELD_OCCLUSIONSTRENGTH);
        }
    }

    /**
     * Ignore the diffuseColor color term and use 1,1,1 for the diffuse color.
     *
     * @param ignore True to ignore the diffuse term
     */
    @Override
    public void setIgnoreDiffuse(boolean ignore) {
        ignoreDiffuse = ignore;
    }

    /**
     * Accessor method to get current value of field <b>transparency</b>,
     * default value is <code>0</code>
     *
     * @return The current value of Transparency
     */
    @Override
    public float getTransparency() {
        return vfTransparency;
    }

    /**
     * Accessor method to get current value of field <b>normalCcale</b>,
     * default value is <code>1</code>
     *
     * @return The current value of NormalScale
     */
    // TODO inconsistent @Override
    public float getNormalScale() {
        return vfNormalScale;
    }

    /**
     * Accessor method to get current value of field <b>occlusionstrength</b>,
     * default value is <code>1</code>
     *
     * @return The current value of OcclusionStrength
     */
    // TODO inconsistent @Override
    public float getOcclusionStrength() {
        return vfOcclusionStrength;
    }

    /**
     * Add a listener instance for the material color change notifications.
     * Adding the same instance more than once is ignored. Adding null values
     * are ignored.
     *
     * @param l The new instance to add
     */
    @Override
    public void addMaterialColorListener(MaterialColorListener l) {
        colorListener = MaterialColorListenerMulticaster.add(colorListener, l);

        // Send a color update to the geometry right now
        if(l != null)
            fireEmissiveColorChanged(l);
    }

    /**
     * Remove a listener instance from this node. If the listener is not
     * currently registered, the request is silently ignored.
     *
     * @param l The new instance to remove
     */
    @Override
    public void removeMaterialColorListener(MaterialColorListener l) {
        colorListener =
            MaterialColorListenerMulticaster.remove(colorListener, l);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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
        if(index < 0  || index > NUM_FIELDS - 1)
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.MaterialNodeType;
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
            case FIELD_AMBIENT_INTENSITY:
                fieldData.clear();
                fieldData.floatValue = vfAmbientIntensity;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_DIFFUSE_COLOR:
                fieldData.clear();
                fieldData.floatArrayValues = vfDiffuseColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_EMISSIVE_COLOR:
                fieldData.clear();
                fieldData.floatArrayValues = vfEmissiveColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_TRANSPARENCY:
                fieldData.clear();
                fieldData.floatValue = vfTransparency;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_NORMALSCALE:
                fieldData.clear();
                fieldData.floatValue = vfNormalScale;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_OCCLUSIONSTRENGTH:
                fieldData.clear();
                fieldData.floatValue = vfOcclusionStrength;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_AMBIENTTEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfAmbientTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_DIFFUSETEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfDiffuseTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_EMISSIVETEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfEmissiveTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_NORMALTEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfNormalTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_OCCLUSIONTEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfOcclusionTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_SHININESSTEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfShininessTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_SPECULARTEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfSpecularTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_SHININESS:
                fieldData.clear();
                fieldData.floatValue = vfShininess;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SPECULAR_COLOR:
                fieldData.clear();
                fieldData.floatArrayValues = vfSpecularColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
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
                case FIELD_AMBIENT_INTENSITY :
                    destNode.setValue(destIndex, vfAmbientIntensity);
                    break;

                case FIELD_DIFFUSE_COLOR:
                    destNode.setValue(destIndex, vfDiffuseColor, 3);
                    break;

                case FIELD_EMISSIVE_COLOR:
                    destNode.setValue(destIndex, vfEmissiveColor, 3);
                    break;

                case FIELD_SHININESS:
                    destNode.setValue(destIndex, vfShininess);
                    break;

                case FIELD_SPECULAR_COLOR:
                    destNode.setValue(destIndex, vfSpecularColor, 3);
                    break;

                case FIELD_TRANSPARENCY:
                    destNode.setValue(destIndex, vfTransparency);
                    break;

                case FIELD_NORMALSCALE:
                    destNode.setValue(destIndex, vfNormalScale);
                    break;

                case FIELD_OCCLUSIONSTRENGTH:
                    destNode.setValue(destIndex, vfOcclusionStrength);
                    break;

                case FIELD_AMBIENTTEXTURE:
                    destNode.setValue(destIndex, vfAmbientTexture);
                    break;

                case FIELD_DIFFUSETEXTURE:
                    destNode.setValue(destIndex, vfDiffuseTexture);
                    break;

                case FIELD_EMISSIVETEXTURE:
                    destNode.setValue(destIndex, vfEmissiveTexture);
                    break;

                case FIELD_NORMALTEXTURE:
                    destNode.setValue(destIndex, vfNormalTexture);
                    break;

                case FIELD_OCCLUSIONTEXTURE:
                    destNode.setValue(destIndex, vfOcclusionTexture);
                    break;

                case FIELD_SHININESSTEXTURE:
                    destNode.setValue(destIndex, vfShininessTexture);
                    break;

                case FIELD_SPECULARTEXTURE:
                    destNode.setValue(destIndex, vfSpecularTexture);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_AMBIENT_INTENSITY:
                setAmbientIntensity(value);
                break;

            case FIELD_SHININESS:
                setShininess(value);
                break;

            case FIELD_TRANSPARENCY:
                setTransparency(value);
                break;

            case FIELD_NORMALSCALE:
                setNormalScale(value);
                break;

            case FIELD_OCCLUSIONSTRENGTH:
                setOcclusionStrength(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFColor field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DIFFUSE_COLOR:
                setDiffuseColor(value);
                break;

            case FIELD_EMISSIVE_COLOR:
                setEmissiveColor(value);
                break;

            case FIELD_SPECULAR_COLOR:
                setSpecularColor(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Send the emissive color change event to our internal listeners.
     *
     * @param l The listener instance
     */
    private void fireEmissiveColorChanged(MaterialColorListener l) {
        try {
            if(l != null)
                l.emissiveColorChanged(vfEmissiveColor);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(EMISSIVE_ERROR_MSG + l,
                                          (Exception)th);
            else {
                System.err.println("Unknown BAAAAD error: " + th);
                th.printStackTrace(System.err);
            }
        }
    }
}

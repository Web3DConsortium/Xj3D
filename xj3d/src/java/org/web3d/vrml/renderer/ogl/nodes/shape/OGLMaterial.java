/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.shape;

// External imports
import org.j3d.aviatrix3d.Material;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.NodeUpdateListener;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.shape.BaseMaterial;
import org.web3d.vrml.renderer.ogl.nodes.OGLMaterialNodeType;

/**
 * OpenGL implementation of a material node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.18 $
 */
public class OGLMaterial extends BaseMaterial
    implements OGLMaterialNodeType, NodeUpdateListener {

    /** White color for ignoring diffuse */
    private static float[] ignoreColor;

    /** Local version of the ambient colour that is the intensity field */
    private float[] lfAmbientColor;

    /** The OpenGL material node */
    private Material material;

    /** Flag for the ambient colour changing */
    private boolean ambientChanged;

    /** Flag for the diffuse colour changing */
    private boolean diffuseChanged;

    /** Flag for the emissive colour changing */
    private boolean emissiveChanged;

    /** Flag for the specular colour changing */
    private boolean specularChanged;

    /** Flag for the shininess colour changing */
    private boolean shininessChanged;

    /** Flag for the shininess colour changing */
    private boolean transparencyChanged;

    /** Flag controlling the lighting state */
    private boolean lightingState;

    /** Flag for the shininess colour changing */
    private boolean lightingChanged;

    /** Flaf controlling whether local color is used */
    private boolean localColor;

    /** Whether the local color also includes alpha values */
    private boolean localColorAlpha;

    /** Flag for the shininess colour changing */
    private boolean localColorChanged;


    static {
        ignoreColor = new float[] {1.0f, 1.0f, 1.0f}; // rgb white
    }

    /**
     * Construct a default instance of the material
     */
    public OGLMaterial() {
        super();
        
        // Default ambient color is 0.2(ambientIntensity) * (0.8 0.8 0.8) diffuseColor
        lfAmbientColor = new float[] {0.16f, 0.16f, 0.16f};
        localColorAlpha = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLMaterial(VRMLNodeType node) {
        super(node);

        // Default ambient color is 0.2(ambientIntensity) * (0.8 0.8 0.8) diffuseColor
        lfAmbientColor = new float[] {0.16f, 0.16f, 0.16f};
        localColorAlpha = false;
    }


    //----------------------------------------------------------
    // Methods defined by OGLMaterialNodeType
    //----------------------------------------------------------

    /**
     * Request the OpenGL material node structure.
     *
     * @return The material node
     */
    @Override
    public Material getMaterial() {
        return material;
    }

    /**
     * Set whether lighting will be used for this material.  In general
     * you should let the material node decide this.  Needed to handle
     * IndexedLineSets or other geometry that specifically declares lighting
     * be turned off.
     *
     * @param enable Whether lighting is enabled
     */
    @Override
    public void setLightingEnable(boolean enable) {
        lightingState = enable;

        if (inSetup)
            return;

        lightingChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
    }

    /**
     * Set whether the geometry has local colors to override the diffuse color.
     *
     * @param enable Whether local color is enabled
     * @param hasAlpha true with the local color also contains alpha values
     */
    @Override
    public void setLocalColor(boolean enable, boolean hasAlpha) {
        localColor = enable;
        localColorAlpha = hasAlpha;

        if(inSetup)
            return;

        localColorChanged = true;
        if (material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
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

        material = new Material(lfAmbientColor,
                                vfEmissiveColor,
                                vfDiffuseColor,
                                vfSpecularColor,
                                vfShininess,
                                1.0f - vfTransparency);

        if (ignoreDiffuse)
            material.setDiffuseColor(ignoreColor);

        material.setSeparateTransparencyEnabled(!localColorAlpha);
        material.setColorMaterialTarget(Material.DIFFUSE_TARGET);
        material.setLightingEnabled(lightingState);

        ambientChanged = false;
        diffuseChanged = false;
        emissiveChanged = false;
        specularChanged = false;
        shininessChanged = false;
        transparencyChanged = false;
        lightingChanged = false;
        localColorChanged = false;
    }

    //----------------------------------------------------------------------
    // Interface FrameStateListener
    //----------------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. If the node needs to update itself for this
     * frame, it should do so now before the render pass takes place.
     */
    @Override
    public void allEventsComplete()
    {
        if(diffuseChanged && (material != null)) {
            if (material.isLive())
                material.dataChanged(this);
            else
                updateNodeDataChanges(material);
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseMaterial
    //----------------------------------------------------------

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

        super.setAmbientIntensity(newAmbientIntensity);

        lfAmbientColor[0] = vfDiffuseColor[0] * vfAmbientIntensity;
        lfAmbientColor[1] = vfDiffuseColor[1] * vfAmbientIntensity;
        lfAmbientColor[2] = vfDiffuseColor[2] * vfAmbientIntensity;

        if (inSetup)
            return;

        ambientChanged = true;
        if (material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
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

        super.setDiffuseColor(newDiffuseColor);

        lfAmbientColor[0] = vfDiffuseColor[0] * vfAmbientIntensity;
        lfAmbientColor[1] = vfDiffuseColor[1] * vfAmbientIntensity;
        lfAmbientColor[2] = vfDiffuseColor[2] * vfAmbientIntensity;

        if (inSetup)
            return;

        diffuseChanged = true;
        ambientChanged = true;

        if(material != null) {
            if (material.isLive())
                material.dataChanged(this);
            else
                updateNodeDataChanges(material);
        }
    }

    /**
     * Ignore the diffuseColor color term and use 1,1,1 for the diffuse color.
     *
     * @param ignore True to ignore the diffuse term
     */
    @Override
    public void setIgnoreDiffuse(boolean ignore) {

        super.setIgnoreDiffuse(ignore);

        if (inSetup)
            return;

        // This method is called from the texture loading queue so it must
        // sync itself to the app thread

        diffuseChanged = true;

        if (material.isLive())
            stateManager.addEndOfThisFrameListener(this);
        else
            updateNodeDataChanges(material);
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

        super.setEmissiveColor(newEmissiveColor);

        if (inSetup)
            return;

        emissiveChanged = true;
        if (material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
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

        super.setShininess(newShininess);

        if (inSetup)
            return;

        shininessChanged = true;
        if (material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
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

        super.setSpecularColor(newSpecularColor);

        if (inSetup)
            return;

        specularChanged = true;
        if (material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>transparency</b>.  How "clear" an object is:  1.0 is completely
     * transparent, 0.0 is completely opaque .
     *
     * @param newTransparency The new value of Transparency
     */
    @Override
    public void setTransparency(float newTransparency)
        throws InvalidFieldValueException {

        super.setTransparency(newTransparency);

        if (inSetup)
            return;

        transparencyChanged = true;
        if (material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return material;
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src) {
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src) {

        if(ambientChanged) {
            material.setAmbientColor(lfAmbientColor);
            ambientChanged = false;
        }

        if(diffuseChanged) {
            if (ignoreDiffuse) {
                material.setDiffuseColor(ignoreColor);
            } else {
                material.setDiffuseColor(vfDiffuseColor);
            }
            diffuseChanged = false;
        }

        if(emissiveChanged) {
            material.setEmissiveColor(vfEmissiveColor);
            emissiveChanged = false;
        }

        if(specularChanged) {
            material.setSpecularColor(vfSpecularColor);
            specularChanged = false;
        }

        if(shininessChanged) {
            material.setShininess(vfShininess);
            shininessChanged = false;
        }

        if(transparencyChanged) {
            material.setTransparency(1.0f - vfTransparency);
            transparencyChanged = false;
        }

        if(lightingChanged) {
            material.setLightingEnabled(lightingState);
            lightingChanged = false;
        }

        if(localColorChanged) {
            material.setSeparateTransparencyEnabled(!localColorAlpha);
            material.setColorMaterialEnabled(localColor);
            localColorChanged = false;
        }
    }
}

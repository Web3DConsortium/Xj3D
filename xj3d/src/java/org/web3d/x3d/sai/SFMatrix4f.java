/*****************************************************************************
 *                        Shapeways Copyright (c) 2017
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Representation of a SFMatrix4f field.
 *
 * @author Alan Hudson
 */
public interface SFMatrix4f extends X3DField, Matrix4 {
    
    @Override
    void getTransform(SFVec3f transform, SFRotation rotation, SFVec3f scale);
    
    @Override
    Matrix4 multiplyColVector(SFVec3f vec3f);
    
    @Override
    Matrix4 multiplyRowVector(SFVec3f vec3f);
    
    @Override
    void setTransform(SFVec3f transform, SFRotation rotation, SFVec3f scale, SFRotation scaleOrientation, SFVec3f center);
}

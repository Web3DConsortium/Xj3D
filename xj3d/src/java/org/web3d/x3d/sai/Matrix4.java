/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
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
 * Describes a 4x4 Matrix as required by the SAIMatrix abstract type.
 *
 * TODO: This interface is completely useless.  For now just implement the same
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface Matrix4 {

    void set(int row, int column);

    float get(int row, int column);

    void setTransform(SFVec3f translation,
                             SFRotation rotation,
                             SFVec3f scale,
                             SFRotation scaleOrientation,
                             SFVec3f center);

    void getTransform(SFVec3f translation,
                             SFRotation rotation,
                             SFVec3f scale);

    Matrix4 inverse();

    Matrix4 transpose();

    Matrix4 multiplyLeft(Matrix4 mat);

    Matrix4 multiplyRight(Matrix4 mat);

    Matrix4 multiplyRowVector(SFVec3f vec);

    Matrix4 multiplyColVector(SFVec3f vec);

    // Copied from x3d dom docs
    void setFromArray(float[] val);
}

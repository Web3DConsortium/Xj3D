/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.spline;

// Standard imports
import javax.vecmath.Vector3f;

// Application specific imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.UnsupportedTypeException;

/**
 * Base geometry generator definition for all forms of spline-based patches.
 * <P>
 *
 * Patches of all orders are permitted. Order information is derived
 * from the provided controlPoint coordinates. When generating a patch, the values
 * for the coordinates are nominally provided in the X and Z plane although no
 * explicit checking is performed to ensure that controlPoint coordinates do not
 * self-intersect or do anything nasty. Normals are always generated as the
 * average between the adjacent faces.
 * <p>
 *
 * A user may optionally provide a weighting for each control point to make for
 * rational patches. By default, all control points start with a weight of
 * one. The implementation automatically resets the weights back to one each
 * time the control points are changed, unless otherwise specified. If the patch
 * coordinate size changes, then the weights will always be reset, otherwise if
 * the size is the same as previously set, then the weights are left alone.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public abstract class PatchGenerator extends GeometryGenerator
{
    /** ControlPoint values used to generate patches */
    protected float[][] controlPointCoordinates;

    /**
     * The control point weightings on the patch. This will be the same
     * size as controlPointCoordinates.
     */
    protected float[][] controlPointWeights;

    /** The number of control points in the width */
    protected int numWidthControlPoints;

    /** The number of control points in the depth */
    protected int numDepthControlPoints;

    /** The points on the patch. */
    protected float[][] patchCoordinates;

    /** The smoothed normal for each point on the patch. */
    protected float[][] patchNormals;

    /** The texture coordinate for each point on the patch. */
    protected float[][] patchTexcoords;

    /** The number of patch coordinates in depth */
    protected int numPatchValues;

    /** The number of patch coordinates in depth */
    protected int numNormalValues;

    /** The number of patch coordinates in depth */
    protected int numTexcoordValues;

    /** Flag indicating base values have changed */
    protected boolean patchChanged;

    /** Flag indicating base values have changed */
    protected boolean normalsChanged;

    /** Flag indicating base values have changed */
    protected boolean texCoordsChanged;

    /** The number of sections used around the patch width */
    protected int widthFacetCount;

    /** The number of sections used around the patch depth */
    protected int depthFacetCount;

    /** Should we use control point weights. Defaults to false. */
    protected boolean useControlPointWeights;

    /**
     * Construct a new generator with no control points set.
     */
    protected PatchGenerator()
    {
        numWidthControlPoints = 0;
        numDepthControlPoints = 0;
        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
        useControlPointWeights = false;
    }

    /**
     * Regenerate the patch coordinate points in accordance with the derived
     * classes algorithm type.
     */
    protected abstract void regeneratePatch();


    /**
     * Change the number of facets used to create this cone. This will cause
     * the geometry to be regenerated next time they are asked for.
     * The minimum number of facets is 3.
     *
     * @param widthFacets The number of facets on the width of the patch
     * @param depthFacets The number of facets on the width of the patch
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public void setFacetCount(int widthFacets, int depthFacets)
    {
        if(widthFacets < 3)
            throw new IllegalArgumentException("Number of width facets is < 3");

        if(depthFacets < 3)
            throw new IllegalArgumentException("Number of depth facets is < 3");

        if(widthFacetCount != widthFacets) {
            patchChanged = true;
            normalsChanged = true;
            texCoordsChanged = true;
        }

        if(depthFacetCount != depthFacets) {
            patchChanged = true;
            normalsChanged = true;
            texCoordsChanged = true;
        }

        widthFacetCount = widthFacets;
        depthFacetCount = depthFacets;
    }

    /**
     * Set the flag to say that calculations should be using the control
     * point weights. Initially this starts as false, so if the user wants
     * to create a rational surface then they should call this method with a
     * value of true.
     *
     * @param state true if the weights should be used
     */
    public void enableControlPointWeights(boolean state)
    {
        useControlPointWeights = state;
    }

    /**
     * Get the current setting of the control point weight usage flag.
     *
     * @return true if the control point weights are in use
     */
    public boolean hasControlPointWeights()
    {
        return useControlPointWeights;
    }

    /**
     * Set the patch control point weights. The array is presented as
     * [width][depth] with the coordinates flattened as [Xn, Yn, Zn] in the
     * depth array.
     * <p>
     * If the arrays are not of minimum length 3 and equal length an exception
     * is generated.
     *
     * @param weights The weight values to use
     */
    public void setPatchWeights(float[][] weights)
    {
        int min_length = weights[0].length;

        if(min_length < 3)
            throw new IllegalArgumentException("Weight size < 3");

        // second check for consistent lengths of the individual points
        int i;

        for(i = 1; i < weights.length; i++)
        {
            if(weights[i].length != min_length)
                throw new IllegalArgumentException("Non-equal array lengths");
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (controlPointWeights.length < weights.length))
        {
            controlPointWeights = new float[weights.length][min_length];
        }

        // Copy the values of the new array into the internal structures
        for(i = 0; i < weights.length; i++)
        {
            System.arraycopy(weights[i],
                             0,
                             controlPointWeights[i],
                             0,
                             min_length);
        }

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch control point weights. The array is presented as
     * [width][depth] with the coordinates flattened as [Xn, Yn, Zn] in the
     * depth array.
     * <p>
     * If the arrays are not of minimum length 3 and equal length an exception
     * is generated.
     *
     * @param weights The weight values to use
     */
    public void setPatchWeights(double[][] weights)
    {
        int min_length = weights[0].length;

        if(min_length < 3)
            throw new IllegalArgumentException("Weight size < 3");

        // second check for consistent lengths of the individual points
        int i;

        for(i = 1; i < weights.length; i++)
        {
            if(weights[i].length != min_length)
                throw new IllegalArgumentException("Non-equal array lengths");
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (controlPointWeights.length < weights.length))
        {
            controlPointWeights = new float[weights.length][min_length];
        }

        // Copy the values of the new array into the internal structures
        for(i = 0; i < weights.length; i++)
        {
            for(int j = 0; j < min_length; j++)
                controlPointWeights[i][j] = (float)weights[i][j];
        }

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch controlPoints. The array is presented as a flat
     * array where coordinates are [depth * width Xn, Yn, Zn] in the array.
     * The
     * order of the patch is determined by the passed array. If the arrays are
     * not of minimum length 3 and equal length an exception is generated.
     *
     * @param weights The weight values to use
     * @param numWidth The number of points in the width
     * @param numDepth The number of points in the depth
     */
    public void setPatchWeights(float[] weights, int numWidth, int numDepth)
    {
        if(weights.length < 3)
            throw new IllegalArgumentException("Depth weight patch size < 3");

        if(weights.length < numWidth * numDepth)
            throw new IllegalArgumentException("Array not big enough ");


        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (controlPointWeights.length < numWidth))
        {
            controlPointWeights = new float[numWidth][numDepth];
        }

        int pos = 0;

        // Copy the values of the new array into the internal structures
        for(int i = 0; i < numWidth; i++)
        {
            System.arraycopy(weights,
                             pos,
                             controlPointWeights[i],
                             0,
                             numDepth);
            pos += numDepth;
        }

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch controlPoints. The array is presented as a flat
     * array where coordinates are [depth * width Xn, Yn, Zn] in the array.
     * The
     * order of the patch is determined by the passed array. If the arrays are
     * not of minimum length 3 and equal length an exception is generated.
     *
     * @param weights The weight values to use
     * @param numWidth The number of points in the width
     * @param numDepth The number of points in the depth
     */
    public void setPatchWeights(double[] weights, int numWidth, int numDepth)
    {
        if(weights.length < 3)
            throw new IllegalArgumentException("Depth weight patch size < 3");

        if(weights.length < numWidth * numDepth)
            throw new IllegalArgumentException("Array not big enough ");


        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (controlPointWeights.length < numWidth))
        {
            controlPointWeights = new float[numWidth][numDepth];
        }

        int pos = 0;

        // Copy the values of the new array into the internal structures
        for(int i = 0; i < numWidth; i++)
        {
            for(int j = 0; j < numDepth; j++)
                controlPointWeights[i][j] = (float)weights[pos++];
        }

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch control points. The array is presented as
     * [width][depth] with the coordinates flattened as [Xn, Yn, Zn] in the
     * depth array. The order of the patch is determined by the passed array.
     * If the arrays are not of minimum length 3 and equal length an exception
     * is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     */
    public void setPatchControlPoints(float[][] controlPoints)
    {
        int min_length = controlPoints[0].length;

        if((controlPoints.length < 3) || (min_length < 3))
            throw new IllegalArgumentException("Control point size < 3");

        // second check for consistent lengths of the individual points
        int i;

        for(i = 1; i < controlPoints.length; i++)
        {
            if(controlPoints[i].length != min_length)
                throw new IllegalArgumentException("Non-equal array lengths");
        }

        int num_depth_weights = controlPoints[0].length / 3;
        boolean reset_weights =
            (controlPointCoordinates == null) ||
            (controlPoints.length != numWidthControlPoints) ||
            (controlPoints[0].length != numDepthControlPoints);

        // Adjust the control point array size if needed.
        if((controlPointCoordinates == null) ||
           ((controlPoints.length != controlPointCoordinates.length) &&
            (min_length != controlPointCoordinates[0].length)))
        {
            if((controlPointCoordinates == null) ||
               (controlPoints.length != controlPointCoordinates.length))
            {
                controlPointCoordinates = new float[controlPoints.length][min_length];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[min_length];
            }
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           ((controlPoints.length != controlPointWeights.length) &&
            (num_depth_weights != controlPointWeights[0].length)))
        {
            if((controlPointWeights == null) ||
               (controlPoints.length != controlPointWeights.length))
            {
                controlPointWeights =
                    new float[controlPoints.length][num_depth_weights];
            }
            else
            {
                for(i = 0; i < controlPointWeights.length; i++)
                    controlPointWeights[i] = new float[num_depth_weights];
            }
        }

        // Copy the values of the new array into the internal structures
        for(i = 0; i < controlPoints.length; i++)
        {
            System.arraycopy(controlPoints[i],
                             0,
                             controlPointCoordinates[i],
                             0,
                             min_length);
        }

        // Reset all the weights to one if required.
        if(reset_weights)
        {
            for(i = controlPointWeights.length; --i >= 0; )
                for(int j = controlPointWeights[0].length; --j >= 0; )
                    controlPointWeights[i][j] = 1;
        }

        numWidthControlPoints = controlPoints.length;
        numDepthControlPoints = min_length / 3;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch control points. The array is presented as
     * [width][depth] with the coordinates flattened as [Xn, Yn, Zn] in the
     * depth array. The order of the patch is determined by the passed array.
     * If the arrays are not of minimum length 3 and equal length an exception
     * is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     */
    public void setPatchControlPoints(double[][] controlPoints)
    {
        int min_length = controlPoints[0].length;

        if((controlPoints.length < 3) || (min_length < 3))
            throw new IllegalArgumentException("Control point size < 3");

        // second check for consistent lengths of the individual points
        int i;

        for(i = 1; i < controlPoints.length; i++)
        {
            if(controlPoints[i].length != min_length)
                throw new IllegalArgumentException("Non-equal array lengths");
        }

        int num_depth_weights = controlPoints[0].length / 3;
        boolean reset_weights =
            (controlPointCoordinates == null) ||
            (controlPoints.length != numWidthControlPoints) ||
            (controlPoints[0].length != numDepthControlPoints);

        // Adjust the control point array size if needed.
        if((controlPointCoordinates == null) ||
           ((controlPoints.length != controlPointCoordinates.length) &&
            (min_length != controlPointCoordinates[0].length)))
        {
            if((controlPointCoordinates == null) ||
               (controlPoints.length != controlPointCoordinates.length))
            {
                controlPointCoordinates = new float[controlPoints.length][min_length];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[min_length];
            }
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           ((controlPoints.length != controlPointWeights.length) &&
            (num_depth_weights != controlPointWeights[0].length)))
        {
            if((controlPointWeights == null) ||
               (controlPoints.length != controlPointWeights.length))
            {
                controlPointWeights =
                    new float[controlPoints.length][num_depth_weights];
            }
            else
            {
                for(i = 0; i < controlPointWeights.length; i++)
                    controlPointWeights[i] = new float[num_depth_weights];
            }
        }

        // Copy the values of the new array into the internal structures
        for(i = 0; i < controlPoints.length; i++)
        {
            for(int j = 0; j < min_length; j++)
                controlPointCoordinates[i][j] = (float)controlPoints[i][j];
        }

        // Reset all the weights to one if required.
        if(reset_weights)
        {
            for(i = controlPointWeights.length; --i >= 0; )
                for(int j = controlPointWeights[0].length; --j >= 0; )
                    controlPointWeights[i][j] = 1;
        }

        numWidthControlPoints = controlPoints.length;
        numDepthControlPoints = min_length / 3;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch control points and weights at the same time. The array is
     * presented as [width][depth] with the coordinates flattened as [Xn, Yn, Zn]
     * in the depth array. The order of the patch is determined by the passed array.
     * If the arrays are not of minimum length 3 and equal length an exception
     * is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param weights The weight values to use
     */
    public void setPatchControlPoints(float[][] controlPoints,
                                      float[][] weights)
    {
        int min_length = controlPoints[0].length;

        if((controlPoints.length < 3) || (min_length < 3))
            throw new IllegalArgumentException("Control point size < 3");

        // second check for consistent lengths of the individual points
        int i;

        for(i = 1; i < controlPoints.length; i++)
        {
            if(controlPoints[i].length != min_length)
                throw new IllegalArgumentException("Non-equal array lengths");
        }

        int num_depth_weights = controlPoints[0].length / 3;

        // Adjust the control point array size if needed.
        if((controlPointCoordinates == null) ||
           ((controlPoints.length != controlPointCoordinates.length) &&
            (min_length != controlPointCoordinates[0].length)))
        {
            if((controlPointCoordinates == null) ||
               (controlPoints.length != controlPointCoordinates.length))
            {
                controlPointCoordinates = new float[controlPoints.length][min_length];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[min_length];
            }
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           ((controlPoints.length != controlPointWeights.length) &&
            (num_depth_weights != controlPointWeights[0].length)))
        {
            if((controlPointWeights == null) ||
               (controlPoints.length != controlPointWeights.length))
            {
                controlPointWeights =
                    new float[controlPoints.length][num_depth_weights];
            }
            else
            {
                for(i = 0; i < controlPointWeights.length; i++)
                    controlPointWeights[i] = new float[num_depth_weights];
            }
        }

        // Copy the values of the new array into the internal structures
        for(i = 0; i < controlPoints.length; i++)
        {
            System.arraycopy(controlPoints[i],
                             0,
                             controlPointCoordinates[i],
                             0,
                             min_length);
        }

        // Copy the values of the new array into the internal structures
        for(i = 0; i < weights.length; i++)
        {
            System.arraycopy(weights[i],
                             0,
                             controlPointWeights[i],
                             0,
                             min_length);
        }

        numWidthControlPoints = controlPoints.length;
        numDepthControlPoints = min_length / 3;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch control points and weights at the same time. The array is
     * presented as [width][depth] with the coordinates flattened as [Xn, Yn, Zn]
     * in the depth array. The order of the patch is determined by the passed array.
     * If the arrays are not of minimum length 3 and equal length an exception
     * is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param weights The weight values to use
     */
    public void setPatchControlPoints(double[][] controlPoints,
                                      double[][] weights)
    {
        int min_length = controlPoints[0].length;

        if((controlPoints.length < 3) || (min_length < 3))
            throw new IllegalArgumentException("Control point size < 3");

        // second check for consistent lengths of the individual points
        int i;

        for(i = 1; i < controlPoints.length; i++)
        {
            if(controlPoints[i].length != min_length)
                throw new IllegalArgumentException("Non-equal array lengths");
        }

        int num_depth_weights = controlPoints[0].length / 3;

        // Adjust the control point array size if needed.
        if((controlPointCoordinates == null) ||
           ((controlPoints.length != controlPointCoordinates.length) &&
            (min_length != controlPointCoordinates[0].length)))
        {
            if((controlPointCoordinates == null) ||
               (controlPoints.length != controlPointCoordinates.length))
            {
                controlPointCoordinates = new float[controlPoints.length][min_length];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[min_length];
            }
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           ((controlPoints.length != controlPointWeights.length) &&
            (num_depth_weights != controlPointWeights[0].length)))
        {
            if((controlPointWeights == null) ||
               (controlPoints.length != controlPointWeights.length))
            {
                controlPointWeights =
                    new float[controlPoints.length][num_depth_weights];
            }
            else
            {
                for(i = 0; i < controlPointWeights.length; i++)
                    controlPointWeights[i] = new float[num_depth_weights];
            }
        }

        // Copy the values of the new array into the internal structures
        for(i = 0; i < controlPoints.length; i++)
        {
            for(int j = 0; j < min_length; j++)
                controlPointCoordinates[i][j] = (float)controlPoints[i][j];
        }

        // Copy the values of the new array into the internal structures
        for(i = 0; i < weights.length; i++)
        {
            for(int j = 0; j < min_length; j++)
                controlPointWeights[i][j] = (float)weights[i][j];
        }

        numWidthControlPoints = controlPoints.length;
        numDepthControlPoints = min_length / 3;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch controlPoints. The array is presented as a flat
     * array where coordinates are [depth * width Xn, Yn, Zn] in the array.
     * The
     * order of the patch is determined by the passed array. If the arrays are
     * not of minimum length 3 and equal length an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numWidth The number of points in the width
     * @param numDepth The number of points in the depth
     */
    public void setPatchControlPoints(float[] controlPoints,
                                      int numWidth,
                                      int numDepth)
    {
        if(controlPoints.length < 3)
            throw new IllegalArgumentException("Depth patch size < 3");

        if(controlPoints.length < numWidth * numDepth * 3)
            throw new IllegalArgumentException("Array not big enough ");

        int i;
        boolean reset_weights =
            (controlPointCoordinates == null) ||
            (numWidth != numWidthControlPoints) ||
            (numDepth != numDepthControlPoints);

        // Adjust the control point array size if needed.
        if((controlPointCoordinates == null) ||
           (controlPointCoordinates.length < numWidth) ||
           (controlPointCoordinates[0].length < numDepth * 3))
        {
            if((controlPointCoordinates == null) ||
               (controlPointCoordinates.length < numDepth))
            {
                controlPointCoordinates = new float[numWidth][numDepth * 3];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[numDepth * 3];
            }
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           ((controlPoints.length != controlPointWeights.length) &&
            (numDepth != controlPointWeights[0].length)))
        {
            if((controlPointWeights == null) ||
               (controlPoints.length != controlPointWeights.length))
            {
                controlPointWeights =
                    new float[controlPoints.length][numDepth];
            }
            else
            {
                for(i = 0; i < controlPointWeights.length; i++)
                    controlPointWeights[i] = new float[numDepth];
            }
        }


        // Copy the values of the new array into the internal structures
        int offset = 0;

        for(i = 0; i < numWidth; i++)
        {
            System.arraycopy(controlPoints,
                             offset,
                             controlPointCoordinates[i],
                             0,
                             numDepth * 3);
            offset += numDepth * 3;
        }

        // Reset all the weights to one if required.
        if(reset_weights)
        {
            for(i = controlPointWeights.length; --i >= 0; )
                for(int j = controlPointWeights[0].length; --j >= 0; )
                    controlPointWeights[i][j] = 1;
        }

        numWidthControlPoints = numWidth;
        numDepthControlPoints = numDepth;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch controlPoints. The array is presented as a flat
     * array where coordinates are [depth * width Xn, Yn, Zn] in the array.
     * The
     * order of the patch is determined by the passed array. If the arrays are
     * not of minimum length 3 and equal length an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numWidth The number of points in the width
     * @param numDepth The number of points in the depth
     */
    public void setPatchControlPoints(double[] controlPoints,
                                      int numWidth,
                                      int numDepth)
    {
        if(controlPoints.length < 3)
            throw new IllegalArgumentException("Depth patch size < 3");

        if(controlPoints.length < numWidth * numDepth * 3)
            throw new IllegalArgumentException("Array not big enough ");

        int i;
        boolean reset_weights =
            (controlPointCoordinates == null) ||
            (numWidth != numWidthControlPoints) ||
            (numDepth != numDepthControlPoints);

        // Adjust the control point array size if needed.
        if((controlPointCoordinates == null) ||
           (controlPointCoordinates.length < numWidth) ||
           (controlPointCoordinates[0].length < numDepth * 3))
        {
            if((controlPointCoordinates == null) ||
               (controlPointCoordinates.length < numDepth))
            {
                controlPointCoordinates = new float[numWidth][numDepth * 3];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[numDepth * 3];
            }
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           ((controlPoints.length > controlPointWeights.length) &&
            (numDepth != controlPointWeights[0].length)))
        {
            if((controlPointWeights == null) ||
               (controlPoints.length != controlPointWeights.length))
            {
                controlPointWeights =
                    new float[controlPoints.length][numDepth];
            }
            else
            {
                for(i = 0; i < controlPointWeights.length; i++)
                    controlPointWeights[i] = new float[numDepth];
            }
        }


        // Copy the values of the new array into the internal structures
        int offset = 0;
        for(i = 0; i < numWidth; i++)
        {
            for(int j = 0; j < numDepth * 3; )
            {
                controlPointCoordinates[i][j++] = (float)controlPoints[offset++];
                controlPointCoordinates[i][j++] = (float)controlPoints[offset++];
                controlPointCoordinates[i][j++] = (float)controlPoints[offset++];
            }
        }

        // Reset all the weights to one if required.
        if(reset_weights)
        {
            for(i = controlPointWeights.length; --i >= 0; )
                for(int j = controlPointWeights[0].length; --j >= 0; )
                    controlPointWeights[i][j] = 1;
        }

        numWidthControlPoints = numWidth;
        numDepthControlPoints = numDepth;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch controlPoints. The array is presented as a flat
     * array where coordinates are [depth * width Xn, Yn, Zn] in the array.
     * The
     * order of the patch is determined by the passed array. If the arrays are
     * not of minimum length 3 and equal length an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numWidth The number of points in the width
     * @param numDepth The number of points in the depth
     * @param weights
     */
    public void setPatchControlPoints(float[] controlPoints,
                                      int numWidth,
                                      int numDepth,
                                      float[] weights)
    {
        if(controlPoints.length < 3)
            throw new IllegalArgumentException("Depth patch size < 3");

        if(controlPoints.length < numWidth * numDepth * 3)
            throw new IllegalArgumentException("Array not big enough ");

        int i;

        // Adjust the control point array size if needed.
        if((controlPointCoordinates == null) ||
           (controlPointCoordinates.length < numWidth) ||
           (controlPointCoordinates[0].length < numDepth * 3))
        {
            if((controlPointCoordinates == null) ||
               (controlPointCoordinates.length < numDepth))
            {
                controlPointCoordinates = new float[numWidth][numDepth * 3];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[numDepth * 3];
            }
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           ((controlPoints.length != controlPointWeights.length) &&
            (numDepth != controlPointWeights[0].length)))
        {
            if((controlPointWeights == null) ||
               (controlPoints.length != controlPointWeights.length))
            {
                controlPointWeights =
                    new float[controlPoints.length][numDepth];
            }
            else
            {
                for(i = 0; i < controlPointWeights.length; i++)
                    controlPointWeights[i] = new float[numDepth];
            }
        }


        // Copy the values of the new array into the internal structures
        int offset = 0;
        for(i = 0; i < numWidth; i++)
        {
            System.arraycopy(controlPoints,
                             offset,
                             controlPointCoordinates[i],
                             0,
                             numDepth * 3);

            offset += numDepth * 3;
        }

        offset = 0;
        for(i = 0; i < numWidth; i++)
        {
            System.arraycopy(weights,
                             offset,
                             controlPointWeights[i],
                             0,
                             numDepth);
            offset += numDepth;
        }

        numWidthControlPoints = numWidth;
        numDepthControlPoints = numDepth;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Set the patch controlPoints. The array is presented as a flat
     * array where coordinates are [depth * width Xn, Yn, Zn] in the array.
     * The
     * order of the patch is determined by the passed array. If the arrays are
     * not of minimum length 3 and equal length an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numWidth The number of points in the width
     * @param numDepth The number of points in the depth
     * @param weights
     */
    public void setPatchControlPoints(double[] controlPoints,
                                      int numWidth,
                                      int numDepth,
                                      double[] weights)
    {
        if(controlPoints.length < 3)
            throw new IllegalArgumentException("Depth patch size < 3");

        if(controlPoints.length < numWidth * numDepth * 3)
            throw new IllegalArgumentException("Array not big enough ");

        int i;

        // Adjust the control point array size if needed.
        if((controlPointCoordinates == null) ||
           (controlPointCoordinates.length < numWidth) ||
           (controlPointCoordinates[0].length < numDepth * 3))
        {
            if((controlPointCoordinates == null) ||
               (controlPointCoordinates.length < numDepth))
            {
                controlPointCoordinates = new float[numWidth][numDepth * 3];
            }
            else
            {
                for(i = 0; i < controlPointCoordinates.length; i++)
                    controlPointCoordinates[i] = new float[numDepth * 3];
            }
        }

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           ((controlPoints.length != controlPointWeights.length) &&
            (numDepth != controlPointWeights[0].length)))
        {
            if((controlPointWeights == null) ||
               (controlPoints.length != controlPointWeights.length))
            {
                controlPointWeights =
                    new float[controlPoints.length][numDepth];
            }
            else
            {
                for(i = 0; i < controlPointWeights.length; i++)
                    controlPointWeights[i] = new float[numDepth];
            }
        }


        // Copy the values of the new array into the internal structures
        int offset = 0;
        for(i = 0; i < numWidth; i++)
        {
            for(int j = 0; j < numDepth * 3; )
            {
                controlPointCoordinates[i][j++] = (float)controlPoints[offset++];
                controlPointCoordinates[i][j++] = (float)controlPoints[offset++];
                controlPointCoordinates[i][j++] = (float)controlPoints[offset++];
            }
        }

        offset = 0;
        for(i = 0; i < numWidth; i++)
        {
            for(int j = 0; j < numDepth; )
                controlPointWeights[i][j++] = (float)weights[offset++];
        }

        numWidthControlPoints = numWidth;
        numDepthControlPoints = numDepth;

        patchChanged = true;
        normalsChanged = true;
        texCoordsChanged = true;
    }

    /**
     * Get the number of vertices that this generator will create for the
     * shape given in the definition.
     *
     * @param data The data to patch the calculations on
     * @return The vertex count for the object
     * @throws UnsupportedTypeException The generator cannot handle the type
     *   of geometry you have requested.
     */
    @Override
    public int getVertexCount(GeometryData data)
        throws UnsupportedTypeException
    {
        int ret_val = 0;

        switch(data.geometryType)
        {
            case GeometryData.TRIANGLES:
                ret_val = widthFacetCount * depthFacetCount * 6 ;
                break;
            case GeometryData.QUADS:
                ret_val = widthFacetCount * depthFacetCount * 4;
                break;

            // These all have the same vertex count
            case GeometryData.TRIANGLE_STRIPS:
                ret_val = (widthFacetCount + 1) * depthFacetCount * 2;
                break;

            case GeometryData.TRIANGLE_FANS:
            case GeometryData.INDEXED_TRIANGLES:
            case GeometryData.INDEXED_QUADS:
            case GeometryData.INDEXED_TRIANGLE_STRIPS:
            case GeometryData.INDEXED_TRIANGLE_FANS:
                ret_val = (widthFacetCount + 1) * (depthFacetCount + 1);
                break;

            default:
                throw new UnsupportedTypeException("Unknown geometry type: " +
                                                   data.geometryType);
        }

        return ret_val;
    }

    /**
     * Generate a new set of geometry items patchd on the passed data. If the
     * data does not contain the right minimum array lengths an exception will
     * be generated. If the array reference is null, this will create arrays
     * of the correct length and assign them to the return value.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     * @throws UnsupportedTypeException The generator cannot handle the type
     *   of geometry you have requested
     */
    @Override
    public void generate(GeometryData data)
        throws UnsupportedTypeException, InvalidArraySizeException
    {
        switch(data.geometryType)
        {
            case GeometryData.TRIANGLES:
                unindexedTriangles(data);
                break;
            case GeometryData.QUADS:
                unindexedQuads(data);
                break;
            case GeometryData.TRIANGLE_STRIPS:
                triangleStrips(data);
                break;
//            case GeometryData.TRIANGLE_FANS:
//                triangleFans(data);
//                break;
            case GeometryData.INDEXED_QUADS:
                indexedQuads(data);
                break;
            case GeometryData.INDEXED_TRIANGLES:
                indexedTriangles(data);
                break;
            case GeometryData.INDEXED_TRIANGLE_STRIPS:
                indexedTriangleStrips(data);
                break;
            case GeometryData.INDEXED_TRIANGLE_FANS:
                indexedTriangleFans(data);
                break;

            default:
                throw new UnsupportedTypeException("Unknown geometry type: " +
                                                   data.geometryType);
        }
    }

   /**
     * Generate a new set of points for an unindexed quad array
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void unindexedTriangles(GeometryData data)
        throws InvalidArraySizeException
    {
        generateUnindexedTriCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateUnindexedTriNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateUnindexedTriTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateUnindexedTriTexture3D(data);
    }


    /**
     * Generate a new set of points for an unindexed quad array
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void unindexedQuads(GeometryData data)
        throws InvalidArraySizeException
    {
        generateUnindexedQuadCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateUnindexedQuadNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateUnindexedQuadTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateUnindexedQuadTexture3D(data);
    }

    /**
     * Generate a new set of points for an indexed quad array. Uses the same
     * points as an indexed triangle, but repeats the top coordinate index.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedQuads(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateIndexedTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateIndexedTexture3D(data);

        // now let's do the index list
        int index_size = (widthFacetCount * widthFacetCount) * 4;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Coordinates",
                                                data.indexes.length,
                                                index_size);

        int[] indexes = data.indexes;
        data.indexesCount = index_size;
        int idx = 0;
        int vtx = 0;

        // each face consists of an anti-clockwise
        for(int i = (widthFacetCount * depthFacetCount); --i >= 0; )
        {
            indexes[idx++] = vtx;
            indexes[idx++] = vtx + widthFacetCount + 1;
            indexes[idx++] = vtx + widthFacetCount + 2;
            indexes[idx++] = vtx + 1;

            vtx++;

            if((i % widthFacetCount) == 0)
                vtx++;
        }
    }

    /**
     * Generate a new set of points for an indexed triangle array
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedTriangles(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateIndexedTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateIndexedTexture3D(data);

        // now let's do the index list
        int index_size = (widthFacetCount * widthFacetCount) * 6;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Coordinates",
                                                data.indexes.length,
                                                index_size);

        int[] indexes = data.indexes;
        data.indexesCount = index_size;
        int idx = 0;
        int vtx = 0;

        // each face consists of an anti-clockwise triangle
        for(int i = (widthFacetCount * depthFacetCount); --i >= 0; )
        {
            // triangle 1
            indexes[idx++] = vtx;
            indexes[idx++] = vtx + widthFacetCount + 2;
            indexes[idx++] = vtx + 1;

            // triangle 2
            indexes[idx++] = vtx + widthFacetCount + 1;
            indexes[idx++] = vtx + widthFacetCount + 2;
            indexes[idx++] = vtx;

            vtx++;

            if((i % widthFacetCount) == 0)
                vtx++;
        }
    }

    /**
     * Generate a new set of points for a triangle strip array. Each side is a
     * strip of two faces.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void triangleStrips(GeometryData data)
        throws InvalidArraySizeException
    {
        generateUnindexedTriStripCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateUnindexedTriStripNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateUnindexedTriStripTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateUnindexedTriTexture3D(data);

        int num_strips = widthFacetCount;

        if(data.stripCounts == null)
            data.stripCounts = new int[num_strips];
        else if(data.stripCounts.length < num_strips)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                num_strips);

        for(int i = num_strips; --i >= 0; )
            data.stripCounts[i] = (widthFacetCount + 1) * 2;
    }

    /**
     * Generate a new set of points for a triangle fan array. Each facet on the
     * side of the cone is a single fan, but the patch is one big fan.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void triangleFans(GeometryData data)
        throws InvalidArraySizeException
    {
    }

    /**
     * Generate a new set of points for an indexed triangle strip array. We
     * build the strip from the existing points, and there's no need to
     * re-order the points for the indexes this time.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedTriangleStrips(GeometryData data)
        throws InvalidArraySizeException
    {
          generateIndexedCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateIndexedTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateIndexedTexture3D(data);

        // now let's do the index list
        int index_size = (widthFacetCount + 1) * widthFacetCount * 2;
        int num_strips = widthFacetCount;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Indexes",
                                                data.indexes.length,
                                                index_size);

        if(data.stripCounts == null)
            data.stripCounts = new int[num_strips];
        else if(data.stripCounts.length < num_strips)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                num_strips);

        int[] indexes = data.indexes;
        int[] stripCounts = data.stripCounts;
        data.indexesCount = index_size;
        data.numStrips = num_strips;
        int idx = 0;
        int vtx = 0;
        int total_points = (widthFacetCount + 1) * depthFacetCount;

        // The side is one big strip
        for(int i = total_points; --i >= 0; )
        {
            indexes[idx++] = vtx;
            indexes[idx++] = vtx + (widthFacetCount + 1);

            vtx++;
        }

        for(int i = num_strips; --i >= 0; )
            stripCounts[i] = (widthFacetCount + 1) * 2;
  }

    /**
     * Generate a new set of points for an indexed triangle fan array. We
     * build the strip from the existing points, and there's no need to
     * re-order the points for the indexes this time. As for the simple fan,
     * we use the first index, the lower-right corner as the apex for the fan.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedTriangleFans(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateIndexedTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateIndexedTexture3D(data);

        // now let's do the index list
        int index_size = (widthFacetCount * widthFacetCount) * 4;
        int num_strips = widthFacetCount * widthFacetCount;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Indexes",
                                                data.indexes.length,
                                                index_size);

        if(data.stripCounts == null)
            data.stripCounts = new int[num_strips];
        else if(data.stripCounts.length < num_strips)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                num_strips);

        int[] indexes = data.indexes;
        int[] stripCounts = data.stripCounts;
        data.indexesCount = index_size;
        data.numStrips = num_strips;
        int idx = 0;
        int vtx = 0;

        // each face consists of an anti-clockwise quad
        for(int i = (widthFacetCount * depthFacetCount); --i >= 0; )
        {
            indexes[idx++] = vtx + widthFacetCount + 1;
            indexes[idx++] = vtx + widthFacetCount + 2;
            indexes[idx++] = vtx + 1;
            indexes[idx++] = vtx;

            stripCounts[i] = 4;

            vtx++;

            if((i % widthFacetCount) == 0)
                vtx++;
        }
    }

    //------------------------------------------------------------------------
    // Coordinate generation routines
    //------------------------------------------------------------------------

    /**
     * Generates new set of points suitable for use in an unindexed array. Each
     * patch coordinate will appear twice in this list. The first half of the
     * array is the top, the second half, the bottom.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriCoordinates(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = getVertexCount(data);

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        regeneratePatch();

        // now just build a grid of coordinates
        int cnt;
        int vtx = 0;
        for(int i = 0; i < depthFacetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < widthFacetCount; j++)
            {
                coords[vtx++] = patchCoordinates[i][cnt + 3];
                coords[vtx++] = patchCoordinates[i][cnt + 4];
                coords[vtx++] = patchCoordinates[i][cnt + 5];

                coords[vtx++] = patchCoordinates[i][cnt];
                coords[vtx++] = patchCoordinates[i][cnt + 1];
                coords[vtx++] = patchCoordinates[i][cnt + 2];

                coords[vtx++] = patchCoordinates[i + 1][cnt];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 1];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 2];

                // Now the second triangle for the upper half
                coords[vtx++] = patchCoordinates[i + 1][cnt];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 1];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 2];

                coords[vtx++] = patchCoordinates[i + 1][cnt + 3];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 4];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 5];

                coords[vtx++] = patchCoordinates[i][cnt + 3];
                coords[vtx++] = patchCoordinates[i][cnt + 4];
                coords[vtx++] = patchCoordinates[i][cnt + 5];

                cnt += 3;
            }
        }
    }

    /**
     * Generates new set of points suitable for use in an unindexed array. Each
     * patch coordinate will appear twice in this list. The first half of the
     * array is the top, the second half, the bottom.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedQuadCoordinates(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = getVertexCount(data);

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        regeneratePatch();

        // now just build a grid of coordinates
        int cnt;
        int vtx = 0;
        for(int i = 0; i < depthFacetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < widthFacetCount; j++)
            {
                coords[vtx++] = patchCoordinates[i][cnt + 3];
                coords[vtx++] = patchCoordinates[i][cnt + 4];
                coords[vtx++] = patchCoordinates[i][cnt + 5];

                coords[vtx++] = patchCoordinates[i][cnt];
                coords[vtx++] = patchCoordinates[i][cnt + 1];
                coords[vtx++] = patchCoordinates[i][cnt + 2];

                coords[vtx++] = patchCoordinates[i + 1][cnt];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 1];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 2];

                coords[vtx++] = patchCoordinates[i + 1][cnt + 3];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 4];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 5];

                cnt += 3;
            }
        }
    }

    /**
     * Generates new set of points suitable for use in an unindexed array. Each
     * patch coordinate will appear twice in this list. The first half of the
     * array is the top, the second half, the bottom.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripCoordinates(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = getVertexCount(data);

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);


        regeneratePatch();

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        int i, j;
        int count = 0;
        int base_count = 0;

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = 0; i < depthFacetCount; i++)
        {
            base_count = 0;

            for(j = 0; j < widthFacetCount + 1; j++)
            {
                coords[count++] = patchCoordinates[i][base_count];
                coords[count++] = patchCoordinates[i][base_count + 1];
                coords[count++] = patchCoordinates[i][base_count + 2];

                coords[count++] = patchCoordinates[i + 1][base_count];
                coords[count++] = patchCoordinates[i + 1][base_count + 1];
                coords[count++] = patchCoordinates[i + 1][base_count + 2];

                base_count += 3;
            }
        }
    }

    /**
     * Generate a new set of points for use in an indexed array. The first
     * index will always be the cone tip - parallel for each face so that we
     * can get the smoothing right. If the array is to use the bottom,
     * a second set of coordinates will be produced separately for the patch
     * so that independent surface normals can be used. These values will
     * start at vertexCount / 2 with the first value as 0,0,0 (the center of
     * the patch) and then all the following values as the patch.
     */
    private void generateIndexedCoordinates(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = getVertexCount(data);

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        regeneratePatch();

        int offset = 0;

        for(int i = 0; i <= widthFacetCount; i++)
        {
            System.arraycopy(patchCoordinates[i],
                             0,
                             coords,
                             offset,
                             numPatchValues);
            offset += numPatchValues;
        }
    }

    //------------------------------------------------------------------------
    // Normal generation routines
    //------------------------------------------------------------------------

    /**
     * Generate a new set of normals for a normal set of unindexed points.
     * Smooth normals are used for the sides at the average between the faces.
     * Bottom normals always point down.
     * <p>
     * This must always be called after the coordinate generation. The
     * top normal of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        regenerateNormals();

        float[] normals = data.normals;
        int cnt;
        int vtx = 0;
        for(int i = 0; i < depthFacetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < widthFacetCount; j++)
            {
                normals[vtx++] = patchNormals[i][cnt + 3];
                normals[vtx++] = patchNormals[i][cnt + 4];
                normals[vtx++] = patchNormals[i][cnt + 5];

                normals[vtx++] = patchNormals[i][cnt];
                normals[vtx++] = patchNormals[i][cnt + 1];
                normals[vtx++] = patchNormals[i][cnt + 2];

                normals[vtx++] = patchNormals[i + 1][cnt];
                normals[vtx++] = patchNormals[i + 1][cnt + 1];
                normals[vtx++] = patchNormals[i + 1][cnt + 2];

                // Now the second triangle for the upper half
                normals[vtx++] = patchNormals[i + 1][cnt];
                normals[vtx++] = patchNormals[i + 1][cnt + 1];
                normals[vtx++] = patchNormals[i + 1][cnt + 2];

                normals[vtx++] = patchNormals[i + 1][cnt + 3];
                normals[vtx++] = patchNormals[i + 1][cnt + 4];
                normals[vtx++] = patchNormals[i + 1][cnt + 5];

                normals[vtx++] = patchNormals[i][cnt + 3];
                normals[vtx++] = patchNormals[i][cnt + 4];
                normals[vtx++] = patchNormals[i][cnt + 5];

                cnt += 3;
            }
        }
    }

    /**
     * Generate a new set of normals for a normal set of unindexed points.
     * Smooth normals are used for the sides at the average between the faces.
     * Bottom normals always point down.
     * <p>
     * This must always be called after the coordinate generation. The
     * top normal of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedQuadNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        regenerateNormals();

        // now just build a grid of coordinates
        float[] normals = data.normals;
        int cnt;
        int vtx = 0;
        for(int i = 0; i < depthFacetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < widthFacetCount; j++)
            {
                normals[vtx++] = patchNormals[i][cnt + 3];
                normals[vtx++] = patchNormals[i][cnt + 4];
                normals[vtx++] = patchNormals[i][cnt + 5];

                normals[vtx++] = patchNormals[i][cnt];
                normals[vtx++] = patchNormals[i][cnt + 1];
                normals[vtx++] = patchNormals[i][cnt + 2];

                normals[vtx++] = patchNormals[i + 1][cnt];
                normals[vtx++] = patchNormals[i + 1][cnt + 1];
                normals[vtx++] = patchNormals[i + 1][cnt + 2];

                normals[vtx++] = patchNormals[i + 1][cnt + 3];
                normals[vtx++] = patchNormals[i + 1][cnt + 4];
                normals[vtx++] = patchNormals[i + 1][cnt + 5];

                cnt += 3;
            }
        }
    }

    /**
     * Generate a new set of normals for unindexed points in a triangle strip.
     * Smooth normals are used for all.
     * <p>
     * This must always be called after the coordinate generation. The
     * top normal of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = widthFacetCount * (widthFacetCount + 1) * 6;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        regenerateNormals();

        int i, j;
        float[] normals = data.normals;
        int count = 0;
        int base_count = 0;

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = 0; i < depthFacetCount; i++)
        {
            base_count = 0;

            for(j = 0; j < widthFacetCount + 1; j++)
            {
                normals[count++] = patchNormals[i][base_count];
                normals[count++] = patchNormals[i][base_count + 1];
                normals[count++] = patchNormals[i][base_count + 2];

                normals[count++] = patchNormals[i + 1][base_count];
                normals[count++] = patchNormals[i + 1][base_count + 1];
                normals[count++] = patchNormals[i + 1][base_count + 2];

                base_count += 3;
            }
        }
    }

    /**
     * Generate a new set of normals for a normal set of indexed points.
     * Handles both flat and smooth shading of normals. Flat just has them
     * perpendicular to the face. Smooth has them at the value at the
     * average between the faces. Bottom normals always point down.
     * <p>
     * This must always be called after the coordinate generation. The
     * top normal of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateIndexedNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        // Just copy the values straight into the array.
        regenerateNormals();

        float[] normals = data.normals;
        int offset = 0;

        for(int i = 0; i <= widthFacetCount; i++)
        {
            System.arraycopy(patchNormals[i],
                             0,
                             normals,
                             offset,
                             numNormalValues);
            offset += numNormalValues;
        }
    }

    //------------------------------------------------------------------------
    // Texture coordinate generation routines
    //------------------------------------------------------------------------

    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        float[] tex_coords = data.textureCoordinates;
        int cnt;
        int vtx = 0;

        for(int i = 0; i < depthFacetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < widthFacetCount; j++)
            {
                tex_coords[vtx++] = patchTexcoords[i][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 3];

                tex_coords[vtx++] = patchTexcoords[i][cnt];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 1];

                tex_coords[vtx++] = patchTexcoords[i + 1][cnt];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 1];

                // Now the second triangle for the upper half
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 1];

                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 3];

                tex_coords[vtx++] = patchTexcoords[i][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 3];

                cnt += 2;
            }
        }
    }

    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedQuadTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        // now just build a grid of coordinates
        float[] tex_coords = data.textureCoordinates;
        int cnt;
        int vtx = 0;
        for(int i = 0; i < depthFacetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < widthFacetCount; j++)
            {
                tex_coords[vtx++] = patchTexcoords[i][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 3];

                tex_coords[vtx++] = patchTexcoords[i][cnt];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 1];

                tex_coords[vtx++] = patchTexcoords[i + 1][cnt];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 1];

                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 3];

                cnt += 2;
            }
        }
    }

    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = widthFacetCount * (widthFacetCount + 1) * 4;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        int i, j;
        float[] texcoords = data.textureCoordinates;
        int count = 0;
        int base_count = 0;

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = 0; i < depthFacetCount; i++)
        {
            base_count = 0;

            for(j = 0; j < widthFacetCount + 1; j++)
            {
                texcoords[count++] = patchTexcoords[i][base_count];
                texcoords[count++] = patchTexcoords[i][base_count + 1];

                texcoords[count++] = patchTexcoords[i + 1][base_count];
                texcoords[count++] = patchTexcoords[i + 1][base_count + 1];

                base_count += 2;
            }
        }
    }

    /**
     * Generate a new set of texCoords for a texCoord set of indexed points.
     * This must always be called after the coordinate generation. The
     * top texCoord of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateIndexedTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("Texcoords",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        // Just copy the values straight into the array.
        regenerateTexcoords();

        float[] tex_coords = data.textureCoordinates;
        int offset = 0;

        for(int i = 0; i <= widthFacetCount; i++)
        {
            System.arraycopy(patchTexcoords[i],
                             0,
                             tex_coords,
                             offset,
                             numTexcoordValues);
            offset += numTexcoordValues;
        }
    }

    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("3D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        float[] texCoords = data.textureCoordinates;
    }

    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedQuadTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("3D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        float[] texCoords = data.textureCoordinates;
    }

    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateIndexedTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("3D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        float[] texCoords = data.textureCoordinates;
    }
    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = widthFacetCount * (widthFacetCount + 1) * 6;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        int i, j;
        float[] texcoords = data.textureCoordinates;
        int count = 0;
        int base_count = 0;

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = 0; i < depthFacetCount; i++)
        {
            base_count = 0;

            for(j = 0; j < widthFacetCount + 1; j++)
            {
                texcoords[count++] = patchTexcoords[i][base_count];
                texcoords[count++] = patchTexcoords[i][base_count + 1];
                texcoords[count++] = patchTexcoords[i][base_count + 2];

                texcoords[count++] = patchTexcoords[i + 1][base_count];
                texcoords[count++] = patchTexcoords[i + 1][base_count + 1];
                texcoords[count++] = patchTexcoords[i + 1][base_count + 2];

                base_count += 3;
            }
        }
    }

    /**
     * Convenience method to regenerate the smoothed normals for the
     * patch. It assumes that the patch has been regenerated just before
     * this call.
     */
    protected void regenerateNormals()
    {
        if(!normalsChanged)
            return;

        normalsChanged = false;
        numNormalValues = numPatchValues;

        if((patchNormals == null) ||
           (patchNormals.length <= widthFacetCount) ||
           (patchNormals[0].length < numNormalValues))
        {
            patchNormals = new float[widthFacetCount + 1][numNormalValues];
        }

        Vector3f norm;
        int count = 0;
        int i, j;

        // The first edge
        // corner point - normal based on only that face
        norm = createFaceNormal(patchCoordinates, 1, 0, 0, 0, 0, 3);

        patchNormals[0][count++] = norm.x;
        patchNormals[0][count++] = norm.y;
        patchNormals[0][count++] = norm.z;

        for(i = 1; i < widthFacetCount; i++)
        {
            norm = calcSideAverageNormal(0, count,
                                         0, count + 3,
                                         1, count,
                                         0, count - 3);

            patchNormals[0][count++] = norm.x;
            patchNormals[0][count++] = norm.y;
            patchNormals[0][count++] = norm.z;
        }

        // Last corner point of the first row
        norm = createFaceNormal(patchCoordinates,
                                0, count,
                                1, count,
                                1, count - 3);

        patchNormals[0][count++] = norm.x;
        patchNormals[0][count++] = norm.y;
        patchNormals[0][count++] = norm.z;

        // Now, process all of the internal points
        for(i = 1; i < widthFacetCount; i++)
        {
            count = 0;
            norm = calcSideAverageNormal(i, count,
                                         i - 1, count,
                                         i, count + 3,
                                         i + 1, count);

            patchNormals[i][count++] = norm.x;
            patchNormals[i][count++] = norm.y;
            patchNormals[i][count++] = norm.z;

            for(j = 1; j < widthFacetCount; j++)
            {

                norm = calcQuadAverageNormal(i, count,
                                             i, count + 3,
                                             i + 1, count,
                                             i, count - 3,
                                             i - 1, count);

                patchNormals[i][count++] = norm.x;
                patchNormals[i][count++] = norm.y;
                patchNormals[i][count++] = norm.z;
            }

            // Last point of the row
            norm = calcSideAverageNormal(i, count,
                                         i + 1, count,
                                         i, count - 3,
                                         i - 1, count);

            patchNormals[i][count++] = norm.x;
            patchNormals[i][count++] = norm.y;
            patchNormals[i][count++] = norm.z;
        }

        // The last edge
        // corner point - normal based on only that face
        count = 0;
        norm = createFaceNormal(patchCoordinates,
                                widthFacetCount, count,
                                widthFacetCount - 1, count,
                                widthFacetCount, count + 3);

        patchNormals[widthFacetCount][count++] = norm.x;
        patchNormals[widthFacetCount][count++] = norm.y;
        patchNormals[widthFacetCount][count++] = norm.z;

        for(i = 1; i < widthFacetCount; i++)
        {
            norm = calcSideAverageNormal(i, count,
                                         i, count - 3,
                                         i - 1, count,
                                         i, count + 3);

            patchNormals[widthFacetCount][count++] = norm.x;
            patchNormals[widthFacetCount][count++] = norm.y;
            patchNormals[widthFacetCount][count++] = norm.z;
        }

        // Last corner point of the first row
        norm = createFaceNormal(patchCoordinates,
                                widthFacetCount, count,
                                widthFacetCount, count - 3,
                                widthFacetCount - 1, count);

        patchNormals[widthFacetCount][count++] = norm.x;
        patchNormals[widthFacetCount][count++] = norm.y;
        patchNormals[widthFacetCount][count++] = norm.z;
    }

    /**
     * Convenience method to calculate the average normal value between
     * two quads - ie along the side of an object
     *
     * @param coords The coordinates to generate from
     * @param p The centre point
     * @param p1 The first point of the first side
     * @param p2 The middle, shared side point
     * @param p3 The last point of the second side
     * @return The averaged vector
     */
    private Vector3f calcSideAverageNormal(int w, int p,
                                           int w1, int p1,
                                           int w2, int p2,
                                           int w3, int p3)
    {
        Vector3f norm;
        float x, y, z;

        // Normal first for the previous quad
        norm = createFaceNormal(patchCoordinates, w, p, w1, p1, w2, p2);
        x = norm.x;
        y = norm.y;
        z = norm.z;

        // Normal for the next quad
        norm = createFaceNormal(patchCoordinates, w, p, w2, p2, w3, p3);

        // create the average of each compoenent for the final normal
        norm.x = (norm.x + x) / 2;
        norm.y = (norm.y + y) / 2;
        norm.z = (norm.z + z) / 2;

        norm.normalize();

        return norm;
    }

    /**
     * Convenience method to create quad average normal amongst four
     * quads based around a common centre point (the one having the normal
     * calculated).
     *
     * @param coords The coordinates to generate from
     * @param p The centre point
     * @param p1 shared point between first and last quad
     * @param p2 shared point between first and second quad
     * @param p3 shared point between second and third quad
     * @param p4 shared point between third and fourth quad
     * @return The averaged vector
     */
    private Vector3f calcQuadAverageNormal(int w, int p,
                                           int w1, int p1,
                                           int w2, int p2,
                                           int w3, int p3,
                                           int w4, int p4)
    {
        Vector3f norm;
        float x, y, z;

        // Normal first for quads 1 & 2
        norm = createFaceNormal(patchCoordinates, w, p, w2, p2, w1, p1);
        x = norm.x;
        y = norm.y;
        z = norm.z;

        // Normal for the quads 2 & 3
        norm = createFaceNormal(patchCoordinates, w, p, w2, p2, w3, p3);

        x += norm.x;
        y += norm.y;
        z += norm.z;

        // Normal for quads 3 & 4
        norm = createFaceNormal(patchCoordinates, w, p, w3, p3, w4, p4);

        x += norm.x;
        y += norm.y;
        z += norm.z;

        // Normal for quads 1 & 4
        norm = createFaceNormal(patchCoordinates, w, p, w4, p4, w1, p1);

        // create the average of each compoenent for the final normal
        norm.x = (norm.x + x) / 4;
        norm.y = (norm.y + y) / 4;
        norm.z = (norm.z + z) / 4;

        norm.normalize();

        return norm;
    }

    /**
     * Regenerate the texture coordinate points.
     * Assumes regenerateBase has been called before this
     */
    protected final void regenerateTexcoords()
    {
        if(!texCoordsChanged)
            return;

        texCoordsChanged = false;

        numTexcoordValues = (widthFacetCount + 1) * 2;

        if((patchTexcoords == null) ||
           (patchTexcoords.length <= widthFacetCount) ||
           (patchTexcoords[0].length < numTexcoordValues))
        {
            patchTexcoords = new float[widthFacetCount + 1][numTexcoordValues];
        }

        int count;
        float w;
        float d = 0;
        float width_inc = 1.0f / widthFacetCount;
        float depth_inc = 1.0f / widthFacetCount;

        for(int i = 0; i < widthFacetCount; i++)
        {
            count = 0;
            w = 0;

            for(int j = 0;  j < widthFacetCount; j++)
            {
                patchTexcoords[i][count++] = w;
                patchTexcoords[i][count++] = d;

                w += width_inc;
            }

            patchTexcoords[i][count++] = 1;
            patchTexcoords[i][count++] = d;

            d += depth_inc;
        }

        count = 0;
        w = 0;

        for(int j = 0;  j < widthFacetCount; j++)
        {
            patchTexcoords[widthFacetCount][count++] = w;
            patchTexcoords[widthFacetCount][count++] = 1;

            w += width_inc;
        }

        patchTexcoords[widthFacetCount][count++] = 1;
        patchTexcoords[widthFacetCount][count++] = 1;
    }
}

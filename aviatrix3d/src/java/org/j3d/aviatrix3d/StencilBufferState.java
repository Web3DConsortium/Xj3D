/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.BufferStateRenderable;
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * Describes attributes used when interacting with the stencil buffer.
 * <p>
 *
 * Including this state class in the global setup automatically enables
 * stencil testing and the stencil buffer.
 * <p>
 *
 * The default setup for this class is:
 * <p>
 * <ul>
 * <li>Compare Mask: ~0</li>
 * <li>Write Mask: ~0</li>
 * <li>Reference Value: 0</li>
 * <li>Stencil Function: FUNCTION_ALWAYS</li>
 * <li>Fail Operation: STENCIL_KEEP</li>
 * <li>Depth Fail Operation = STENCIL_KEEP</li>
 * <li>Depth Pass Operation = STENCIL_KEEP</li>
 * </ul>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidStencilFunctionMsg: Error message when the given function type
 *     does not match one of the available types.</li>
 * <li>invalidStencilOpMsg: Error message when the given operation type
 *     does not match one of the available types.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.7 $
 */
public class StencilBufferState extends BufferState
    implements BufferStateRenderable
{
    /** Message when the stencil operation is not a valid value */
    private static final String INVALID_OP_PROP =
        "org.j3d.aviatrix3d.StencilBufferState.invalidStencilOpMsg";

    /** Message when the stencil function is not a valid value */
    private static final String INVALID_FUNCTION_PROP =
        "org.j3d.aviatrix3d.StencilBufferState.invalidStencilFunctionMsg";

    /** When the stencil function matches, increment the stencil value. */
    public static final short STENCIL_INCREMENT = GL.GL_INCR;

    /** When the stencil function matches, decrement the stencil value. */
    public static final short STENCIL_DECREMENT = GL.GL_DECR;

    /** When the stencil function matches, keep the current stencil value. */
    public static final short STENCIL_KEEP = GL.GL_KEEP;

    /** When the stencil function matches, bitwise invert the stencil value. */
    public static final short STENCIL_INVERT = GL.GL_INVERT;

    /** When the stencil function matches, set the stencil value to zero. */
    public static final short STENCIL_ZERO = GL.GL_ZERO;

    /**
     * When the stencil function matches, set the stencil value to the
     * reference value.
     */
    public static final short STENCIL_REPLACE = GL.GL_REPLACE;

    /** Comparison function that always fails.*/
    public static final int FUNCTION_NEVER = GL.GL_NEVER;

    /** Comparison function that passes if (ref & mask) < (stencil & mask). */
    public static final int FUNCTION_LESS = GL.GL_LESS;

    /** Comparison function that passes if (ref & mask) <= (stencil & mask). */
    public static final int FUNCTION_LESS_OR_EQUAL = GL.GL_LEQUAL;

    /** Comparison function that passes if (ref & mask) > (stencil & mask). */
    public static final int FUNCTION_GREATER = GL.GL_GREATER;

    /** Comparison function that passes if (ref & mask) >= (stencil & mask). */
    public static final int FUNCTION_GREATER_OR_EQUAL = GL.GL_GEQUAL;

    /** Comparison function that passes if (ref & mask) == (stencil & mask). */
    public static final int FUNCTION_EQUAL = GL.GL_EQUAL;

    /** Comparison function that passes if (ref & mask) not equal to (stencil & mask). */
    public static final int FUNCTION_NOTEQUAL = GL.GL_NOTEQUAL;

    /** Comparison function that always passes. */
    public static final int FUNCTION_ALWAYS = GL.GL_ALWAYS;


    /** The bit-plane definition for compare operations. */
    private int compareMask;

    /** The bit-plane definition for write operations. */
    private int writeMask;

    /** The reference value to compare for the stencil check */
    private int referenceValue;

    /** The action to take on a general stencil fail */
    private int failOp;

    /** The action to take on a depth fail */
    private int depthFailOp;

    /** The action to take on a depth pass  */
    private int depthPassOp;

    /** The stencil function mode to use */
    private int function;

    /** The stencil mask used to clear to */
    private int clearMask;

    /** Flag indicating if this state should clear the current buffer state */
    private boolean clearState;

    /**
     * Constructs a state set with default values.
     */
    public StencilBufferState()
    {
        clearMask = 0;
        compareMask = ~0;
        writeMask = ~0;
        referenceValue = 0;
        function = FUNCTION_ALWAYS;
        failOp = STENCIL_KEEP;
        depthFailOp = STENCIL_KEEP;
        depthPassOp = STENCIL_KEEP;

        clearState = true;
    }

    //---------------------------------------------------------------
    // Methods defined by BufferStateRenderable
    //---------------------------------------------------------------

    /**
     * Get the type of buffer this state represents.
     *
     * @return One of the _BUFFER constants
     */
    @Override
    public int getBufferType()
    {
        return STENCIL_BUFFER;
    }

    /**
     * Get the GL buffer bit flag that this state class represents. Used for
     * bulk clearing all the states at once.
     *
     * @return The bit state constant for Stencil Buffers
     */
    @Override
    public int getBufferBitMask()
    {
        return GL.GL_STENCIL_BUFFER_BIT;
    }

    /**
     * Check to see if this buffer should be cleared at the start of this run.
     * If it should be, add the bit mask from this state to the global list.
     *
     * @return true if the state should be cleared
     */
    @Override
    public boolean checkClearBufferState()
    {
        return clearState;
    }

    /**
     * Issue ogl commands needed for this buffer to set the initial state,
     * including the initial enabling.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void setBufferState(GL2 gl)
    {
        gl.glClearStencil(clearMask);
        gl.glEnable(GL.GL_STENCIL_TEST);
        gl.glStencilFunc(function, referenceValue, compareMask);
        gl.glStencilOp(failOp, depthFailOp, depthPassOp);
        gl.glStencilMask(writeMask);
    }

    /**
     * Issue ogl commands needed for this component to change the state,
     * assuming that it is already enabled.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void updateBufferState(GL2 gl)
    {
        gl.glClearStencil(clearMask);
        gl.glStencilFunc(function, referenceValue, compareMask);
        gl.glStencilOp(failOp, depthFailOp, depthPassOp);
        gl.glStencilMask(writeMask);
    }

    /**
     * Restore all state to the default values.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void clearBufferState(GL2 gl)
    {
        gl.glDisable(GL.GL_STENCIL_TEST);

        // Reset the mask so that the next time clear is called, it will
        // automatically clear all planes.
        gl.glStencilMask(~0);
        gl.glClearStencil(0);
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    @Override
    public int compareTo(Renderable o)
        throws ClassCastException
    {
        StencilBufferState sa = (StencilBufferState)o;
        return compareTo(sa);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof StencilBufferState))
            return false;
        else
            return equals((StencilBufferState)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the flag for whether the buffer state should be cleared when this
     * state object is executed.
     *
     * @param clear True if the buffer should be cleared when this is the first
     *   state to be used in the current operation
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setClearBufferState(boolean clear)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        clearState = clear;
    }

    /**
     * Set the bitmask used during the clear of the buffer. Note that the
     * the mask will only support the number of bits defined by the current
     * hardware. Flags above the number of supported bit planes are ignored.
     *
     * @param mask The bit mask of plane values to set during clear
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setClearMask(int mask)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        clearMask = mask;
    }

    /**
     * Get the current function comparison mask.
     *
     * @return An integer bit flag.
     */
    public int getClearMask()
    {
        return clearMask;
    }

    /**
     * Set the bitmask used for the stencil comparison operation. Note that the
     * the mask will only support the number of bits defined by the current
     * hardware. Flags above the number of supported bit planes are ignored.
     *
     * @param mask The bit mask of planes to compare
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setFunctionCompareMask(int mask)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        compareMask = mask;
    }

    /**
     * Get the current function comparison mask.
     *
     * @return An integer bit flag.
     */
    public int getFunctionCompareMask()
    {
        return compareMask;
    }

    /**
     * Set the bitmask used for the stencil write operation. Note that the
     * the mask will only support the number of bits defined by the current
     * hardware. Flags above the number of supported bit planes are ignored.
     *
     * @param mask The bit mask of planes to write when operations succeed
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setStencilWriteMask(int mask)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        writeMask = mask;
    }

    /**
     * Get the current write mask.
     *
     * @return An integer bit flag.
     */
    public int getStencilWriteMask()
    {
        return compareMask;
    }

    /**
     * Set the reference value used for the stencil comparison operation.
     *
     * @param value A value to compare for the depth testing
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setFunctionReferenceValue(int value)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        referenceValue = value;
    }

    /**
     * Get the current stencil reference value for use in the stencil function
     * setup.
     *
     * @return An integer bit flag.
     */
    public int getFunctionReferenceValue()
    {
        return referenceValue;
    }

    /**
     * Set the operation that should be performed if the stencil test fails.
     * This should be one of the STENCIL_ constants at the top of this class.
     *
     * @param op One of the STENCIL_ constants
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The operation value was not one of the
     *   valid types.
     */
    public void setStencilFailOperation(int op)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(op)
        {
            case STENCIL_INCREMENT:
            case STENCIL_DECREMENT:
            case STENCIL_KEEP:
            case STENCIL_INVERT:
            case STENCIL_ZERO:
            case STENCIL_REPLACE:
                failOp = op;
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_OP_PROP) + op;
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the current function comparison mask.
     *
     * @return An integer bit flag.
     */
    public int getStencilFailOperation()
    {
        return failOp;
    }

    /**
     * Set the operation that should be performed if the stencil test passes
     * but the depth test fails. This should be one of the STENCIL_ constants
     * at the top of this class.
     *
     * @param op One of the STENCIL_ constants
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The operation value was not one of the
     *   valid types.
     */
    public void setDepthFailOperation(int op)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(op)
        {
            case STENCIL_INCREMENT:
            case STENCIL_DECREMENT:
            case STENCIL_KEEP:
            case STENCIL_INVERT:
            case STENCIL_ZERO:
            case STENCIL_REPLACE:
                depthFailOp = op;
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_OP_PROP) + op;
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the current operation used when the stencil test passes, but depth
     * test fails.
     *
     * @return One of the STENCIL_ constants
     */
    public int getDepthFailOperation()
    {
        return depthFailOp;
    }

    /**
     * Set the operation that should be performed if the stencil and depth
     * tests pass. This should be one of the STENCIL_ constants at the top of
     * this class.
     *
     * @param op One of the STENCIL_ constants
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The operation value was not one of the
     *   valid types.
     */
    public void setDepthPassOperation(int op)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(op)
        {
            case STENCIL_INCREMENT:
            case STENCIL_DECREMENT:
            case STENCIL_KEEP:
            case STENCIL_INVERT:
            case STENCIL_ZERO:
            case STENCIL_REPLACE:
                depthPassOp = op;
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_OP_PROP) + op;
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the current operation used when the stencil and depth tests pass.
     *
     * @return One of the STENCIL_ constants
     */
    public int getDepthPassOperation()
    {
        return depthPassOp;
    }

    /**
     * Set the operation that should be performed if the stencil and depth
     * tests pass. This should be one of the FUNCTION_ constants at the top of
     * this class.
     *
     * @param func One of the FUNCTION_ constants
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The operation value was not one of the
     *   valid types.
     */
    public void setStencilFunction(int func)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(func)
        {
            case FUNCTION_NEVER:
            case FUNCTION_ALWAYS:
            case FUNCTION_LESS:
            case FUNCTION_LESS_OR_EQUAL:
            case FUNCTION_GREATER:
            case FUNCTION_GREATER_OR_EQUAL:
            case FUNCTION_EQUAL:
            case FUNCTION_NOTEQUAL:
                function = func;
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_FUNCTION_PROP) + func;
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the function type used when the stencil and depth tests pass.
     *
     * @return One of the FUNCTION constants
     */
    public int getStencilFunction()
    {
        return function;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sbs The attributes instance to be comsbsred
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(StencilBufferState sbs)
    {
        if(sbs == null)
            return 1;

        if(sbs == this)
            return 0;

        if(clearState != sbs.clearState)
            return clearState ? 1 : -1;

        if(compareMask != sbs.compareMask)
            return compareMask < sbs.compareMask ? -1 : 1;

        if(writeMask != sbs.writeMask)
            return writeMask < sbs.writeMask ? -1 : 1;

        if(clearMask != sbs.clearMask)
            return clearMask < sbs.clearMask ? -1 : 1;

        if(referenceValue != sbs.referenceValue)
            return referenceValue < sbs.referenceValue ? -1 : 1;

        if(failOp != sbs.failOp)
            return failOp < sbs.failOp ? -1 : 1;

        if(depthFailOp != sbs.depthFailOp)
            return depthFailOp < sbs.depthFailOp ? -1 : 1;

        if(depthPassOp != sbs.depthPassOp)
            return depthPassOp < sbs.depthPassOp ? -1 : 1;

        if(function != sbs.function)
            return function < sbs.function? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sbs The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(StencilBufferState sbs)
    {
        if(sbs == this)
            return true;

        return !((sbs == null) ||
                (clearState != sbs.clearState) ||
                (compareMask != sbs.compareMask) ||
                (writeMask != sbs.writeMask) ||
                (clearMask != sbs.clearMask) ||
                (referenceValue != sbs.referenceValue)||
                (failOp != sbs.failOp) ||
                (depthFailOp != sbs.depthFailOp) ||
                (depthPassOp != sbs.depthPassOp) ||
                (function != sbs.function));
    }
}

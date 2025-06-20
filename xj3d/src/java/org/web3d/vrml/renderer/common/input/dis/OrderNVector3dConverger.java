/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

import javax.vecmath.*;

/**
 * An order-N <code>Vector3dConverger</code>.
 *
 * @author Andrzej Kapolka
 * @version $Revision: 1.2 $
 */

public class OrderNVector3dConverger
{
    /**
     * The length of the convergence interval, in milliseconds.
     */
    private int convergenceInterval;

    /**
     * The differential equation solver module.
     */
    private Solver solver;

    /**
     * The primary variable.
     */
    private Vector3dVariable primaryVariable;

    /**
     * The secondary variable.
     */
    private Vector3dVariable secondaryVariable;

    /**
     * The tertiary variable variable.
     */
    private Vector3dVariable tertiaryVariable;

    /**
     * Whether or not this converger is converging.
     */
    private boolean converging;

    /**
     * The time of the beginning of the convergence interval.
     */
    private long convergenceStartTime;


    /**
     * Constructor.
     *
     * @param pOrder the order of the converger
     * @param pConvergenceInterval the length of the convergence interval, in milliseconds
     * @param pSolver the differential equation solver to use
     */
    public OrderNVector3dConverger(int pOrder, int pConvergenceInterval, Solver pSolver)
    {
        convergenceInterval = pConvergenceInterval;
        solver = pSolver;

        primaryVariable = new Vector3dVariable(pOrder, solver);
        secondaryVariable = new Vector3dVariable(pOrder, solver);
        tertiaryVariable = new Vector3dVariable(pOrder, solver);
    }

    /**
     * Sets the value and derivatives associated with this converger.  This method changes
     * the state of the converger immediately, without converging to the new values.
     *
     * @param value the value of the variable
     * @param derivatives an array containing the values of the variable's derivatives; the
     * value at index 0 is the first derivative, the value at index 1 is the second
     * derivative, and so on
     * @param referenceTime the time at which the values were valid
     */
    public void setValueAndDerivatives(Vector3d value, Vector3d[] derivatives, long referenceTime)
    {
        primaryVariable.set(value,derivatives,referenceTime);

        converging = false;
    }

    /**
     * Makes the value of this converger current.
     *
     * @param time the current time
     */
    private void makeCurrent(long time)
    {
        if(!converging)
        {
            primaryVariable.makeCurrent(time);
        }
        else if( (time-convergenceStartTime) > convergenceInterval )
        {
            converging = false;

            primaryVariable.set(tertiaryVariable);

            primaryVariable.makeCurrent(time);
        }
        else
        {
            double t = (double)(time-convergenceStartTime)/convergenceInterval,
                   alpha = -2.0*t*t*t + 3.0*t*t;

            primaryVariable.interpolate(secondaryVariable, tertiaryVariable, alpha, time);
        }
    }

    /**
     * Returns the current value of the variable.
     *
     * @param time the current time, in milliseconds since the epoch
     * @param result a <code>Vector3d</code> to hold the result
     */
    public void getValue(long time, Vector3d result)
    {
        makeCurrent(time);

        primaryVariable.getValue(result);
    }

    /**
     * Fills the given array with the current values of the variable's derivatives.
     * The value at index 0 will be set to the first derivative, the value at index
     * 1 will be set to the second derivative, and so on (up to the size of the
     * array).
     *
     * @param time the current time, in milliseconds since the epoch
     * @param result an array to contain the current values of the variable's derivatives
     */
    public void getDerivatives(long time, Vector3d[] result)
    {
        makeCurrent(time);

        primaryVariable.getDerivatives(result);
    }

    /**
     * Smoothly converges to the specified state.
     *
     * @param value the value of the variable
     * @param derivatives an array containing the values of the variable's derivatives; the
     * value at index 0 is the first derivative, the value at index 1 is the second
     * derivative, and so on
     * @param referenceTime the time at which the values were valid
     * @param currentTime the current time, in milliseconds since the epoch
     */
    public void convergeTo(Vector3d value, Vector3d[] derivatives, long referenceTime,
                           long currentTime)
    {
        makeCurrent(currentTime);

        secondaryVariable.set(primaryVariable);

        tertiaryVariable.set(value,derivatives,referenceTime);

        converging = true;
        convergenceStartTime = currentTime;
    }

    /**
     * Are the values still converging?
     *
     * @return Are we still converging.
     */
    public boolean isConverging()
    {
        return converging;
    }
}

package org.web3d.vrml.renderer.common.input.dis;

// External imports
import edu.nps.moves.dis7.pdus.DeadReckoningParameters;
import edu.nps.moves.dis7.pdus.EntityStatePdu;
import edu.nps.moves.dis7.pdus.EulerAngles;
import edu.nps.moves.dis7.pdus.Vector3Double;
import edu.nps.moves.dis7.pdus.Vector3Float;

/** Utility methods for handling dead reckoning calculations
 *
 * @author <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.vrml.renderer.common.input.dis.ProtocolHandlerUtils">Terry D. Norbraten</a>
 */
public class ProtocolHandlerUtils {
    
    /* Forget why, but had something to do with getting the Scan Eagle to work
       with DFDL conversion of KLV to DIS (TDN) 5/1/23
    */
    private static final float DR_ADJUSTMENT = 0.5f;
    
    /**
     * Calculate the dead reckoning position of a EntityStatePDU
     * 
     * @param espdu
     * @param dt
     * @param dRPosition
     */
    public static void DRPosition(EntityStatePdu espdu, float dt, float[] dRPosition) {
        float dtSq = dt * dt;

        Vector3Double location = espdu.getEntityLocation();
        Vector3Float linearVelocity = espdu.getEntityLinearVelocity();
        DeadReckoningParameters drp = espdu.getDeadReckoningParameters();
        Vector3Float linearAcceleration = drp.getEntityLinearAcceleration();

        dRPosition[0] = (float) (location.getX() +
                dt * linearVelocity.getX() +
                DR_ADJUSTMENT * dtSq * linearAcceleration.getX());

        dRPosition[1] = (float) (-location.getZ() -
                dt * linearVelocity.getZ() -
                DR_ADJUSTMENT * dtSq * linearAcceleration.getZ());

        dRPosition[2] = (float) (location.getY() +
                dt * linearVelocity.getY() +
                DR_ADJUSTMENT * dtSq * linearAcceleration.getY());
//System.out.println("DRPOS:");
//System.out.println("   Current Pos: " + espdu.getEntityLocationX() + " " + espdu.getEntityLocationY() + " " + espdu.getEntityLocationZ());
//System.out.println("   Velocity: " + espdu.getEntityLinearVelocityX() + " " + espdu.getEntityLinearVelocityY() + " " + espdu.getEntityLinearVelocityZ());
//System.out.println("   Calc Pos: " + dRPosition[0] + " " + dRPosition[1] + " " + dRPosition[2]);
    }

    /**
     * Calculate the dead reckoning orientation of a EntityStatePDU
     * 
     * @param espdu
     * @param dt
     * @param dROrientation
     */
    public static void DROrientation(EntityStatePdu espdu, float dt, float[] dROrientation) {
        float yaw, pitch, roll;

        EulerAngles eat = espdu.getEntityOrientation();
        DeadReckoningParameters drp = espdu.getDeadReckoningParameters();
        Vector3Float angularVelocity = drp.getEntityAngularVelocity();

        roll = eat.getPhi() + dt * angularVelocity.getX();
        pitch = eat.getTheta() + dt * angularVelocity.getY();
        yaw = eat.getPsi() + dt * angularVelocity.getZ();

        // note Kent's quaternion code has irregular ordering of Euler angles
        // which (by whatever method :) accomplishes the angle transformation
        // desired...   (results verified using NPS AUV)

        dROrientation[0] = -yaw;
        dROrientation[1] = roll;
        dROrientation[2] = pitch;
    }

    private void smooth3Floats(float[] drCurrent, float[] drPrevUpdate, float[] result,
            float currentTime, float averageUpdateTime) {
        /*
        // the ratio of how long since the most recent update time to the averageUpdateTime
        float factor = currentTime / averageUpdateTime;

        result[0] = drPrevUpdate[0] + ( drCurrent[0] - drPrevUpdate[0] ) * factor;
        result[1] = drPrevUpdate[1] + ( drCurrent[1] - drPrevUpdate[1] ) * factor;
        result[2] = drPrevUpdate[2] + ( drCurrent[2] - drPrevUpdate[2] ) * factor;
         */

        result[0] = (drPrevUpdate[0] + drCurrent[0]) / 2.0f;
        result[1] = (drPrevUpdate[1] + drCurrent[1]) / 2.0f;
        result[2] = (drPrevUpdate[2] + drCurrent[2]) / 2.f;

    }

    private float normalize2(float input_angle) {
        float angle = input_angle;
        float twoPI = (float) Math.PI * 2.0f;

        while (angle > Math.PI) {
            angle -= twoPI;
        }
        while (angle <= -Math.PI) {
            angle += twoPI;
        }
        return angle;
    }

    // returns: the magnitude and direction of the angle change.
    private void fixEulers(float[] goalOrientation, float[] currOrientation) {
        for (int idx = 0; idx < goalOrientation.length; idx++) {
            goalOrientation[idx] = currOrientation[idx] +
                    normalize2(normalize2(goalOrientation[idx]) - normalize2(currOrientation[idx]));
        }
    }

    /**
     * Sum of the squares of the diffs between two float arrays.
     * @param first
     * @param second
     * @return square of delta value between floats
     */
    private float SqrDeltaFloats(float[] first, float[] second) {
        float sumOfSqrs = 0.0f;

        for (int idx = 0; idx < first.length; idx++) {
            sumOfSqrs += (first[idx] - second[idx]) * (first[idx] - second[idx]);
        }

        return sumOfSqrs;
    }
    
}

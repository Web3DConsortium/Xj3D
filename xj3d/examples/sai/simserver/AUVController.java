
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

import edu.nps.moves.dis7.pdus.DeadReckoningParameters;
import edu.nps.moves.dis7.pdus.EntityID;
import edu.nps.moves.dis7.pdus.EntityStatePdu;
import edu.nps.moves.dis7.pdus.EulerAngles;
import edu.nps.moves.dis7.pdus.VariableParameter;
import edu.nps.moves.dis7.pdus.Vector3Double;
import edu.nps.moves.dis7.pdus.Vector3Float;

import edu.nps.moves.dis7.utilities.DisThreadedNetworkInterface;
import edu.nps.moves.dis7.utilities.DisTime;
import edu.nps.moves.dis7.utilities.PduFactory;

import java.nio.ByteBuffer;
import java.util.Random;

import java.util.StringTokenizer;

/**
 * Moves an AUV around a world.  Also moves a target around in random
 * places for the AUV to collide with.
 *
 * @author Alan Hudson
 * @author <a href="mailto:tdnorbra@nps.edu?subject=AUVController">Terry D. Norbraten</a>
 * @version 2 (modified for OpenDIS v7)
 */
public class AUVController {

    public static int numEntities = 1;
    public static int pauseTime = 100;
    public static final String DATA_LAYOUT =
      "# EntityXLocation EntityYLocation EntityZLocation velocityX velocityY velocityZ Psi Theta Phi AngVelX AngVelY AngVelZ\n"
    + " 4   14  1.5 3   0   0   0   -.1 0   0   0   0 \n"
    + " 7   14  1.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "10   14  2   3   0   .25 0   -.1 0   0   0   0 \n"
    + "13   14  2.25    3   0   .25 0   -.1 0   0   0   0 \n"
    + "16   14  2.5 3   0   .25 0   -.1 0   0   0   0 \n"
    + "19   14  2.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "22   14  3   3   0   .25 0   -.1 0   0   0   0 \n"
    + "25   14  3.25    3   0   .25 0   -.1 0   0   0   0 \n"
    + "28   14  3.5 3   0   .25 0   -.1 0   0   0   0 \n"
    + "31   14  3.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "34   14  4   1   1   .25 0.785   -.1 0   0   0   0 \n"
    + "35   15  4.25    0   1   .25 1.5708  -.1 0   0   0   0 \n"
    + "35   16  4.5 0   1   .25 1.5708  -.1 0   0   0   0 \n"
    + "35   17  4.75    -1  1   .25 2.355   -.1 0   0   0   0 \n"
    + "34   18  5   -3  0   0   3.1416  .1  0   0   0   0 \n"
    + "31   18  4.75    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "28   18  4.5 -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "25   18  4.25    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "22   18  4   -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "19   18  3.75    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "16   18  3.5 -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "13   18  3.25    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "10   18  3   -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "7    18  2.75    -3  0   -.25    3.9221  .1  0   0   0   0 \n"
    + "4    18  2.5 -1  -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    17  2.25    0   -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    16  2   0   -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    15  1.75    1   -1  -.25    5.497   .1  0   0   0   0 \n";

    private static final int PORT = 62040;
    private static final String GROUP = "224.2.181.145";
    private DisThreadedNetworkInterface writer;

    public static void main(String args[]) {
        if (args.length > 1) {
            numEntities = Integer.parseInt(args[0]);
            pauseTime = 1000 / Integer.parseInt(args[1]);
        }

        System.out.println("Number entities: " + numEntities + " pauseTime: " + pauseTime);

        AUVController controller = new AUVController();
        controller.test();
    }

    public void test() {
        System.out.println("Starting Controller");
        int sleepTime = 500 / numEntities;
        float offset = -10;

        writer = new DisThreadedNetworkInterface(GROUP, PORT);
        writer.setVerbose(false);

        for (int i = 0; i < numEntities; i++) {
            if (i % 10 == 0) {
                offset += 2.5;
            }
            launchEntity(DATA_LAYOUT, 0, 1, i, 0, 0, offset);

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {}
        }

        System.out.println("Launching Target");
        launchTarget(DATA_LAYOUT, 0, 1, numEntities, 0, 0, offset);
    }

    private void launchEntity(String path, int siteID, int appID, int entityID,
            float xoff, float yoff, float zoff) {

        EntityRunner r = new EntityRunner(writer, path, siteID, appID, entityID, pauseTime);
        r.setOffset(xoff, yoff, zoff);

        Thread thread = new Thread(r);
//        thread.setDaemon(true);
        thread.start();
    }

    private void launchTarget(String path, int siteID, int appID, int entityID,
            float xoff, float yoff, float zoff) {

        TargetRunner r = new TargetRunner(writer, path, siteID, appID, entityID, 500);
        r.setOffset(xoff, yoff, zoff);

        Thread thread = new Thread(r);
//        thread.setDaemon(true);
        thread.start();
    }
}

class EntityRunner implements Runnable {

    EntityID id;
    DisThreadedNetworkInterface writer;
    String data;
    EntityStatePdu espdu;
    float xoff;
    float yoff;
    float zoff;
    int entityID;
    int pauseTime;
    boolean sendingPDUs = true;
    StringTokenizer lineTokenizer;
    PduFactory fac;

    public EntityRunner(DisThreadedNetworkInterface writer, String data, int siteID, int appID, int entityID, int pauseTime) {
        this.writer = writer;
        this.data = data;
        this.entityID = entityID;
        this.pauseTime = pauseTime;

        /***** Begin DIS Enumerations *****/
        id = new EntityID();
        id.setSiteID(siteID);
        id.setApplicationID(appID);
        id.setEntityID(entityID);

        fac = new PduFactory();
        espdu = fac.makeEntityStatePdu();
        espdu.setEntityID(id);

        espdu.getVariableParameters().clear();

        if (entityID == 0) {
            VariableParameter p = new VariableParameter();
            p.setRecordSpecificFields(ByteBuffer.allocate(4).putFloat(10.0f).array());
            espdu.getVariableParameters().add(p);
        }
        /***** End DIS Enumerations *****/
    }

    public void setOffset(float x, float y, float z) {
        xoff = x;
        yoff = y;
        zoff = z;
    }

    @Override
    public void run() {

        String lineString;                   // one line from the string
        StringTokenizer itemTokenizer;
        float pduValues[];
        int valueCount;
        float value;
        String token;

        Vector3Double v3d = new Vector3Double();
        Vector3Float v3f = new Vector3Float();
        EulerAngles orient = new EulerAngles();
        DeadReckoningParameters dp;

        while (sendingPDUs) {

            // Check to see if we're beyond the time-out limit. If so, generate a fake event
            // that presses the "stop" button. This saves us having to write a bunch of
            // duplicate code.
            lineTokenizer = new StringTokenizer(data, "\r\n");

            // while we have more lines....
            while (lineTokenizer.hasMoreTokens() /*&& sendingPDUs*/) {
                pduValues = new float[12];  //holds x,y,z; dx,dy,dz; psi,theta,phi; angX,angY,angZ
                valueCount = 0;

                // get one line of input, then decode each token in that string
                lineString = lineTokenizer.nextToken();
                itemTokenizer = new StringTokenizer(lineString);

                while (itemTokenizer.hasMoreTokens()) {

                    token = itemTokenizer.nextToken();

                    // got a hash mark somewhere in the token; ignore all the rest
                    if (token.indexOf('#') != -1) {
                        break;
                    }

                    // Read the value into a float
                    value = Float.parseFloat(token);
                    valueCount++;

                    // prevents array out of bounds if extra values present
                    if (valueCount > 12) {
                        break;
                    }

                    pduValues[valueCount - 1] = value;
                }

                if (valueCount == 0) {
                    // got a blank line; skip it, and don't send out a zero PDU
                    continue;
                }

                // location
                v3d.setX(pduValues[0] + xoff);
                v3d.setY(pduValues[1] + yoff);
                v3d.setZ(pduValues[2] + zoff);
                espdu.setEntityLocation(v3d);

                // velocity
                v3f.setX(pduValues[3]);
                v3f.setY(pduValues[4]);
                v3f.setZ(pduValues[5]);
                espdu.setEntityLinearVelocity(v3f);

                // orientation
                orient.setPsi(pduValues[6]);   // h
                orient.setTheta(pduValues[7]); // p
                orient.setPhi(pduValues[8]);   // r
                espdu.setEntityOrientation(orient);

                // angular velocity
                dp = espdu.getDeadReckoningParameters();
                v3f = dp.getEntityAngularVelocity();
                v3f.setX(pduValues[9]);  // h
                v3f.setY(pduValues[10]); // p
                v3f.setZ(pduValues[11]); // r
//                dp.setEntityAngularVelocity(v3f);

                espdu.setTimestamp(DisTime.getCurrentDisTimestamp());
                writer.sendPDU(espdu);
                sleep(pauseTime);
            }
        }
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }
}

class TargetRunner implements Runnable {

    EntityID id;
    DisThreadedNetworkInterface writer;
    String data;
    EntityStatePdu espdu;
    float xoff;
    float yoff;
    float zoff;
    int entityID;
    int pauseTime;
    boolean sendingPDUs = true;
    StringTokenizer lineTokenizer;
    PduFactory fac;

    public TargetRunner(DisThreadedNetworkInterface writer, String data, int siteID, int appID, int entityID, int pauseTime) {
        this.writer = writer;
        this.data = data;
        this.entityID = entityID;
        this.pauseTime = pauseTime;

        id = new EntityID();
        id.setSiteID(siteID);
        id.setApplicationID(appID);
        id.setEntityID(entityID);

        fac = new PduFactory();
        espdu = fac.makeEntityStatePdu();
        espdu.setEntityID(id);

        espdu.getVariableParameters().clear();

        if (entityID == 0) {
            VariableParameter p = new VariableParameter();
            p.setRecordSpecificFields(ByteBuffer.allocate(4).putFloat(10.0f).array());
            espdu.getVariableParameters().add(p);
        }
    }

    public void setOffset(float x, float y, float z) {
        xoff = x;
        yoff = y;
        zoff = z;
    }

    @Override
    public void run() {

        String lineString;                   // one line from the string
        StringTokenizer itemTokenizer;
        float pduValues[];
        int valueCount;
        float value;
        String token;

        Vector3Double v3d = new Vector3Double();
        Vector3Float v3f = new Vector3Float();
        EulerAngles orient = new EulerAngles();
        DeadReckoningParameters dp;
        Random rand = new Random();

        while (sendingPDUs) {

            // Check to see if we're beyond the time-out limit. If so, generate a fake event
            // that presses the "stop" button. This saves us having to write a bunch of
            // duplicate code.
            lineTokenizer = new StringTokenizer(data, "\r\n");

            // while we have more lines....
            while (lineTokenizer.hasMoreTokens() /*&& sendingPDUs*/) {
                pduValues = new float[12];  //holds x,y,z; dx,dy,dz; psi,theta,phi; angX,angY,angZ
                valueCount = 0;

                // get one line of input, then decode each token in that string
                lineString = lineTokenizer.nextToken();
                itemTokenizer = new StringTokenizer(lineString);

                while (itemTokenizer.hasMoreTokens()) {

                    token = itemTokenizer.nextToken();

                    // got a hash mark somewhere in the token; ignore all the rest
                    if (token.indexOf('#') != -1) {
                        break;
                    }

                    // Read the value into a float
                    value = Float.parseFloat(token);
                    valueCount++;

                    // prevents array out of bounds if extra values present
                    if (valueCount > 12) {
                        break;
                    }

                    pduValues[valueCount - 1] = value;
                }

                if (valueCount == 0 || rand.nextFloat() > 0.1f) {     // got a blank line; skip it, and don't send out a zero PDU

                    sleep(pauseTime);
                    continue;
                }

                System.out.println("Moving target:" + entityID + " rand: " + rand);

                // location
                v3d.setX(pduValues[0] + xoff);
                v3d.setY(pduValues[1] + yoff);
                v3d.setZ(pduValues[2] + zoff);
                espdu.setEntityLocation(v3d);

                // velocity
                v3f.setX(pduValues[3]);
                v3f.setY(pduValues[4]);
                v3f.setZ(pduValues[5]);
                espdu.setEntityLinearVelocity(v3f);

                // orientation
                orient.setPsi(pduValues[6]);   // h
                orient.setTheta(pduValues[7]); // p
                orient.setPhi(pduValues[8]);   // r
                espdu.setEntityOrientation(orient);

                // angular velocity
                dp = espdu.getDeadReckoningParameters();
                v3f = dp.getEntityAngularVelocity();
                v3f.setX(pduValues[9]);  // h
                v3f.setY(pduValues[10]); // p
                v3f.setZ(pduValues[11]); // r
                dp.setEntityAngularVelocity(v3f);

                espdu.setTimestamp(DisTime.getCurrentDisTimestamp());
                writer.sendPDU(espdu);
                sleep(pauseTime);
            }
        }
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }
}

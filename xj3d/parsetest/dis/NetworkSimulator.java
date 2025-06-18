
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

import java.util.StringTokenizer;

public class NetworkSimulator {

    public static int numEntities = 50;
    public static int hz = 5;
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

    private static final int DATA_ITEMS = 28;
    private static float[][] DATA;
    private static final int PORT = 62040;
    private static final String GROUP = "224.2.181.145";
    private DisThreadedNetworkInterface writer;
    private static Simulator[] runners;

    public static void main(String args[]) {
        if (args.length > 1) {
            numEntities = Integer.parseInt(args[0]);
            hz = Integer.parseInt(args[1]);
        }

        System.out.println("Number entities: " + numEntities + " hz: " + hz);

        parseData();
        runners = new Simulator[numEntities];

        NetworkSimulator tester = new NetworkSimulator();
        tester.test();
    }

    public void test() {

        System.out.println("Starting Simulator");
        float offset = -10;
        
        writer = new DisThreadedNetworkInterface(GROUP, PORT);
        writer.setVerbose(false);

        for (int i = 0; i < numEntities; i++) {
            if (i % DATA_ITEMS == 0) {
                offset += 2.5;
            }
            launchEntity(0, 1, i, 0, 0, offset);
        }

        int clock;
        int localClock;
        long startTime;
        long cycleTime;
        long pauseTime;
        int cycleInterval = 1000 / hz;

        for (int i = 0; i < 50000; i++) {
            clock = i % DATA_ITEMS;
            startTime = System.currentTimeMillis();
            for (int j = 0; j < numEntities; j++) {
                localClock = clock + (j % DATA_ITEMS);
                if (localClock > DATA_ITEMS - 1) {
                    localClock = localClock - DATA_ITEMS;
                }
//System.out.println("j: " + j + " clock: " + localClock);
                runners[j].clock(localClock);
            }

            cycleTime = System.currentTimeMillis() - startTime;

            pauseTime = cycleInterval - cycleTime;
            if (pauseTime > 0) {
                try {
                    Thread.sleep(pauseTime);
                } catch (InterruptedException e) {}
            } else {
                pauseTime = 0;
            }

            System.out.println("processing Time: " + cycleTime + " pauseTime: " + pauseTime + " actual hz: " + (1000.0f / (cycleTime + pauseTime)));
        }
    }

    private void launchEntity(int siteID, int appID, int entityID,
            float xoff, float yoff, float zoff) {

        Simulator e1 = new Simulator(writer, DATA, siteID, appID, entityID, hz);
        e1.setOffset(xoff, yoff, zoff);

        runners[entityID] = e1;
    }

    private static void parseData() {
        DATA = new float[DATA_ITEMS][12];

        String lineString;                   // one line from the string
        StringTokenizer lineTokenizer;
        StringTokenizer itemTokenizer;

        lineTokenizer = new StringTokenizer(DATA_LAYOUT, "\r\n");
        int pdu = 0;
        int valueCount;
        float value;
        String token;

        // while we have more lines....
        while (lineTokenizer.hasMoreTokens()) {
            // get one line of input, then decode each token in that string
            lineString = lineTokenizer.nextToken();
            itemTokenizer = new StringTokenizer(lineString);
            valueCount = 0;

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

                DATA[pdu][valueCount - 1] = value;
            }

            if (valueCount == 0) // got a blank line; skip it, and don't send out a zero PDU
            {
                continue;
            }

            pdu++;
        }
    }
}

class Simulator {

    EntityID id;
    DisThreadedNetworkInterface writer;
    float[][] data;
    EntityStatePdu espdu;
    float xoff;
    float yoff;
    float zoff;
    int entityID;
    int hz;
    
    Vector3Double v3d = new Vector3Double();
    Vector3Float v3f = new Vector3Float();
    EulerAngles orient = new EulerAngles();
    DeadReckoningParameters dp;
    PduFactory fac;

    public Simulator(DisThreadedNetworkInterface writer, float[][] data, int siteID, int appID, int entityID, int hz) {
        this.writer = writer;
        this.data = data;
        this.entityID = entityID;
        this.hz = hz;
        
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
    }

    public void setOffset(float x, float y, float z) {
        xoff = x;
        yoff = y;
        zoff = z;
    }

    public void clock(int frame) {

        // location
        v3d.setX(data[frame][0] + xoff);
        v3d.setY(data[frame][1] + yoff);
        v3d.setZ(data[frame][2] + zoff);
        espdu.setEntityLocation(v3d);

        // velocity
        v3f.setX(data[frame][3]);
        v3f.setY(data[frame][4]);
        v3f.setZ(data[frame][5]);
        espdu.setEntityLinearVelocity(v3f);

        // orientation
        orient.setPsi(data[frame][6]);   // h
        orient.setTheta(data[frame][7]); // p
        orient.setPhi(data[frame][8]);   // r
        espdu.setEntityOrientation(orient);

        // angular velocity
        dp = espdu.getDeadReckoningParameters();
        v3f = dp.getEntityAngularVelocity();
        v3f.setX(data[frame][9]);  // h
        v3f.setY(data[frame][10]); // p
        v3f.setZ(data[frame][11]); // r
//                dp.setEntityAngularVelocity(v3f);

        espdu.setTimestamp(DisTime.getCurrentDisTimestamp());
        writer.sendPDU(espdu);
    }
}


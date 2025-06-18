/*
 * ****************************************************************************
 *  *                        Shapeways Copyright (c) 2015
 *  *                               Java Source
 *  *
 *  * This source is licensed under the GNU LGPL v2.1
 *  * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  *
 *  * This software comes with the standard NO WARRANTY disclaimer for any
 *  * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *  *
 *  ****************************************************************************
 */

/*****************************************************************************
 *                        Yumetech Copyright (c) 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

// External Imports
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Internal Imports
import xj3d.filter.filters.GlobalBoundsFilter;


/**
 * Validate global bounds.
 * <p>
 * Pass in arguments as in:  -compareBounds LT 1.0 1.0 1.0
 *
 * Operations supported:
 *    LT   <
 *    LTE  <=
 *    GT   >
 *    GTE  >=
 *    EQ   =
 *    NE   !=
 *
 * @author Alan Hudson
 * @version
 */
public class GlobalBoundsFilterValidator extends GlobalBoundsFilter {
    private static final boolean DEBUG = true;
    private static final double EPS = 1e-6;

    private static final Set<String> OPS;

    /** Operations */
    private List<String> ops;

    /** Values */
    private List<Vector3d> vals;


    static {
        OPS = new HashSet<>();
        OPS.add("LT");
        OPS.add("LTE");
        OPS.add("GT");
        OPS.add("GTE");
        OPS.add("EQ");
        OPS.add("NE");
    }

    public GlobalBoundsFilterValidator() {
        ops = new ArrayList<>();
        vals = new ArrayList<>();
    }

    @Override
    public void endDocument() {
        boolean fail = false;
        String msg = "Unknown failure";

        Vector3d size = new Vector3d();
        size.x = maxBound[0] - minBound[0];
        size.y = maxBound[1] - minBound[1];
        size.z = maxBound[2] - minBound[2];

        if (DEBUG) {
            System.out.printf("Bounds.  min: %7.4f,%7.4f,%7.4f max: %7.4f,%7.4f,%7.4f  size: %7.4f,%7.4f,%7.4f\n",minBound[0],minBound[1],minBound[2],maxBound[0],maxBound[1],maxBound[2],size.x,size.y,size.z);
        }

        for(int i=0; i < ops.size(); i++) {
            String op = ops.get(i);
            Vector3d val = vals.get(i);

            switch (op) {
                case "LTE":
                    if (!(val.x <= size.x && val.y <= size.y && val.z <= size.z)) {
                        fail = true;
                        msg = "bounds " + op + " " + val + " bounds was: " + size;
                    }   break;
                case "GTE":
                    if (!(val.x >= size.x && val.y >= size.y && val.z >= size.z)) {
                        fail = true;
                        msg = "bounds " + op + " " + val + " bounds was: " + size;
                    }   break;
                case "LT":
                    if (!(val.x < size.x && val.y < size.y && val.z < size.z)) {
                        fail = true;
                        msg = "bounds " + op + " " + val + " bounds was: " + size;
                    }   break;
                case "GT":
                    if (!(val.x > size.x && val.y > size.y && val.z > size.z)) {
                        fail = true;
                        msg = "bounds " + op + " " + val + " bounds was: " + size;
                    }   break;
                case "EQ":
                    if (DEBUG) System.out.printf("Checking bounds: %s vs %s\n",size,val);
                    if (!((Math.abs(val.x - size.x) < EPS) && (Math.abs(val.y - size.y) < EPS) && (Math.abs(val.z - size.z) < EPS))) {
                        fail = true;
                        msg = "bounds " + op + " " + val + " bounds was: " + size;
                    }   break;
                case "NE":
                    if (!(val.x != size.x && val.y != size.y && val.z != size.z)) {
                        fail = true;
                        msg = "bounds " + op + " " + val + " bounds was: " + size;
                    }   break;
                default:
                    msg = "Unknown operation: " + op;
                    fail = true;
                    break;
            }
        }

        if (fail) {
            System.out.println("Failure was: " + msg);
            lastErrorCode = -1;
            FilterProcessingException fpe = new FilterProcessingException(msg, -1);
            throw fpe;

            //throw new VRMLException("Told to fail");
        }
    }

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        ops = new ArrayList<>();
        vals = new ArrayList<>();

        if (DEBUG) {
            System.out.printf("Args to GlobalBoundsFilterValidator:\n");
        }
        String argument, st;
        String[] res;
        Vector3d vec;
        for( int i = 0; i< args.length; i++) {
            argument = args[i];
            if (DEBUG) System.out.printf("%s -> %s\n",argument,args[i+1]);
            if (argument.startsWith("-compareBounds")) {
                st = args[++i];
                res = st.split(" ");
                ops.add(res[0]);
                vec = new Vector3d();
                vec.x = Float.parseFloat(res[1]);
                vec.y = Float.parseFloat(res[2]);
                vec.z = Float.parseFloat(res[3]);
                vals.add(vec);
                if (DEBUG) System.out.printf("Adding op: %s --> %6.4f,%6.4f,%6.4f\n",res[0],vec.x,vec.y,vec.z);
            }
        }

        if (DEBUG) {
            for(int i=0; i < ops.size(); i++) {
                System.out.printf("op: %s  val: %s\n",ops.get(i),vals.get(i));
            }
        }
    }
}
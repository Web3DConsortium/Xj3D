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
import java.util.*;

// Internal Imports
// None

import xj3d.filter.filters.NodeCountInfoFilter;


/**
 * Validate Node counts.  All countNode operations must be true
 * <p>
 * Pass in arguments as in:  -countNode1 "Shape LT 1"  -countNode2 "Transform GTE 2"
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
public class CountFilterValidator extends NodeCountInfoFilter {
    private static final Set<String> OPS;

    /** Node names */
    private List<String> nodes;

    /** Operations */
    private List<String> ops;

    /** Values */
    private List<Integer> vals;


    static {
        OPS = new HashSet<>();
        OPS.add("LT");
        OPS.add("LTE");
        OPS.add("GT");
        OPS.add("GTE");
        OPS.add("EQ");
        OPS.add("NE");
    }

    public CountFilterValidator() {
        nodes = new ArrayList<>();
        ops = new ArrayList<>();
        vals = new ArrayList<>();
    }

    @Override
    public void endDocument() {
        boolean fail = false;
        String msg = "Unknown failure";

        String nodeName, op;
        Integer val, cnt;
        for (int i=0; i < nodes.size(); i++) {
            nodeName = nodes.get(i);
            op = ops.get(i);
            val = vals.get(i);

            cnt = counts.get(nodeName);

            if (cnt == null) {
                cnt = 0;
            }

System.out.println("Testing: " + nodeName + " op: " + op + " val: " + val + " against: " + cnt);
            switch (op) {
                case "LTE":
                    if (!(cnt <= val)) {
                        fail = true;
                        msg = nodeName + " " + op + " " + cnt + " count was: " + val;
                    }   break;
                case "GTE":
                    if (!(cnt >= val)) {
                        fail = true;
                        msg = nodeName + " " + op + " " + cnt + " count was: " + val;
                    }   break;
                case "LT":
                    if (!(cnt < val)) {
                        fail = true;
                        msg = nodeName + " " + op + " " + cnt + " count was: " + val;
                    }   break;
                case "GT":
                    if (!(cnt > val)) {
                        fail = true;
                        msg = nodeName + " " + op + " " + cnt + " count was: " + val;
                    }   break;
                case "EQ":
                    if (!(val.intValue() == cnt)) {
                        fail = true;
                        msg = nodeName + " " + op + " " + cnt + " count was: " + val;
                    }   break;
                case "NE":
                    if (!(val.intValue() != cnt)) {
                        fail = true;
                        msg = nodeName + " " + op + " " + cnt + " count was: " + val;
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

        nodes = new ArrayList<>();
        ops = new ArrayList<>();
        vals = new ArrayList<>();

        String st;
        String[] res;
        for(int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-countNode")) {
                st = args[++i];
                res = st.split(" ");
                nodes.add(res[0]);
                ops.add(res[1]);
                vals.add(Integer.valueOf(res[2]));
            }
        }
    }
}
/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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

// Local imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;

/**
 * Shorten's DEF names.  Assigns a small name to each one.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class ShortenDEFFilter extends AbstractFilter {

    /** A list of original DEF names and new ones */
    private Map<String, String> defNames;

    /** The index of the last DEF */
    private int defNum;

    /**
     * Create a new default instance of the filter.
     */
    public ShortenDEFFilter() {
        defNum = 1;
        defNames = new HashMap<>();
    }

    //----------------------------------------------------------
    // ContentHandler methods
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {
        super.endDocument();

        defNames.clear();
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (defName != null) {
            String newName = "D" + defNum++;

            defNames.put(defName, newName);

            defName = newName;
        }

        super.startNode(name, defName);
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {

        String new_name = defNames.get(defName);

        super.useDecl(new_name);
    }

    //-----------------------------------------------------------------------
    //Methods for interface RouteHandler
    //-----------------------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNode The name of the DEF of the source node
     * @param srcField The name of the field to route values from
     * @param destNode The name of the DEF of the destination node
     * @param destField The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void routeDecl(String srcNode,
                          String srcField,
                          String destNode,
                          String destField)
        throws SAVException, VRMLException {

        String new_src = defNames.get(srcNode);
        String new_dest = defNames.get(destNode);

        super.routeDecl(new_src, srcField, new_dest, destField);
    }
}

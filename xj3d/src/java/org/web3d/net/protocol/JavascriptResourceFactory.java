/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.net.protocol;

// Standard imports
import org.ietf.uri.URIResourceStream;
import org.ietf.uri.URIResourceStreamFactory;

// Application specific imports
import org.j3d.util.HashSet;

/**
 *  <p>
 * A factory for producing resources specific to VRML97/X3D.
 *
 * The current factory supports handlers for the javascript and ecmascript
 * protocol types.
 * </p>
 *
 * <p>
 * This software is released under the
 * <a href="http://www.gnu.org/copyleft/lgpl.html">GNU LGPL</a>
 * </p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class JavascriptResourceFactory implements URIResourceStreamFactory {

    /** Set containing the supported protocol types */
    private static final HashSet<String> supportedTypes;

    /** Reference to the next factory to delegate too if needed */
    private URIResourceStreamFactory nestedFactory;

    /**
     * Static initialiser to set up the supported types.
     */
    static {
        supportedTypes = new HashSet<>();
        supportedTypes.add("ecmascript");
        supportedTypes.add("javascript");
    }

    /**
     * Create a new instance of the factory that uses the nested factory
     * for anything this instance cannot support. Use a value of null if
     * not used.
     *
     * @param fac The factory instance to be used
     */
    public JavascriptResourceFactory(URIResourceStreamFactory fac) {
        nestedFactory = fac;
    }

    /**
     * Create a new resource stream for the given protocol. If none of the
     * factories support it, return null.
     *
     * @param protocol The protocol handler
     * @return A new resource stream as needed or null
     */
    @Override
    public URIResourceStream createURIResourceStream(String protocol) {

        URIResourceStream ret_val = null;

        // check if it is one of our local types
        if(supportedTypes.contains(protocol)) {
            boolean ecma = protocol.equals("ecmascript");
            ret_val = new JavascriptResourceStream(ecma);
        } else if(nestedFactory != null) {
            // no? check the nested factory
            ret_val = nestedFactory.createURIResourceStream(protocol);
        }

        return ret_val;
    }
}

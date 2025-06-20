/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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

// External imports
import org.ietf.uri.URIResourceStream;
import org.ietf.uri.URIResourceStreamFactory;
import org.j3d.util.HashSet;

// Local imports
// None

/**
 * <p>
 * A factory for producing resources specific to Web3D-only.
 *
 * The current factory supports handlers for the ecmascript and jar
 * protocol types.
 * </p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.4 $
 */
public class Web3DResourceFactory implements URIResourceStreamFactory {

    /** Set containing the supported protocol types */
    private static final HashSet<String> supportedTypes;

    /** Reference to the next factory to delegate too if needed */
    private URIResourceStreamFactory nestedFactory;

    /**
     * Static initialiser to set up the supported types.
     */
    static {
        supportedTypes = new HashSet<>();
        supportedTypes.add("jar");
        supportedTypes.add("ecmascript");
        supportedTypes.add("javascript");
        supportedTypes.add("file");
    }

    /**
     * Create a new instance of the factory that uses the nested factory
     * for anything this instance cannot support. Use a value of null if
     * not used.
     *
     * @param fac The factory instance to be used
     */
    public Web3DResourceFactory(URIResourceStreamFactory fac) {
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
            switch(protocol.charAt(2)) {
                case 'r':
                    ret_val = new JarResourceStream();
                    break;
                case 'm':
                    ret_val = new JavascriptResourceStream(true);
                    break;
                case 'v':
                    ret_val = new JavascriptResourceStream(false);
                    break;
                case 'l':
                    ret_val = new FileResourceStream();
                    break;
            }
        } else if(nestedFactory != null) {
            // no? check the nested factory
            ret_val = nestedFactory.createURIResourceStream(protocol);
        }

        return ret_val;
    }
}

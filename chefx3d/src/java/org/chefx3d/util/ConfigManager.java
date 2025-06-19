/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.util;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ConfigManager {

    private ConfigManager() {}

    /**
     * A convenience method to go locate a file within the classpath and
     * return a stream to it.
     *
     * @param filename The name of the file to look for
     * @return A stream pointing to it or null if not found
     */
    public static InputStream getConfigFile(final String filename) {

        InputStream is = (InputStream)AccessController.doPrivileged(
                    new PrivilegedAction() {
                        @Override
                        public Object run() {
                            return ClassLoader.getSystemResourceAsStream(filename);
                        }
                    }
                );

        if (is == null) {

            // Fallback mechanism for WebStart
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        }

        return is;

    }

}

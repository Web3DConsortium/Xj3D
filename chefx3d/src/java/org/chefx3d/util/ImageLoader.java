/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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

// External imports
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.WeakHashMap;

/**
 * A convenience class that loads Icons and images for Xj3D's internal uses and
 * provides caching mechanisms.
 * <p>
 *
 * @author Russell Dodds
 * @version $Revision: 1.3 $
 */
public class ImageLoader {
    /** Message when we fail to find an icon */
    private static final String IMAGE_FAIL_MSG = "Unable to locate the required image file ";

    /** The default size of the map = roughly the number of default nodes */
    private static final int DEFAULT_SIZE = 100;

    /**
     * A hashmap of the loaded image instances. Weak so that we can discard them
     * if if needed because we're running out of memory.
     */
    private static WeakHashMap<String, Image> loadedImages;

    /**
     * Static initialiser to get all the bits set up as needed.
     */
    static {
        loadedImages = new WeakHashMap<>(DEFAULT_SIZE);
    }

    /**
     * Load an image for the named image file. Looks in the classpath for the
     * image so the path provided must be fully qualified relative to the
     * classpath.
     *
     * @param name the name to load the icon for. If not found, no image is
     *        loaded.
     * @return An image for the named path.
     */
    public static Image loadImage(String name) {

        // Check the map for an instance first
        Image ret_val = loadedImages.get(name);

        if (name == null) {
            return null;
        }

        URL url = ClassLoader.getSystemResource(name);
        Toolkit tk = Toolkit.getDefaultToolkit();

        if (url != null) {
            ret_val = tk.createImage(url);
        }

        // Fallback for WebStart
        if (ret_val == null) {
            url = ImageLoader.class.getClassLoader().getResource(name);

            if (url != null) {
                ret_val = tk.createImage(url);
            }
        }

        if (ret_val == null) {
            System.out.println(IMAGE_FAIL_MSG + name);
        } else {
            loadedImages.put(name, ret_val);
        }

        return ret_val;
    }

}
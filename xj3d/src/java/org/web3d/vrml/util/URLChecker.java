/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.util;

// Standard imports
import java.io.File;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

import org.ietf.uri.URIUtils;

// Application specific imports
import org.j3d.util.ObjectArray;

/**
 * A checker of URLs to update them and include a root URL if needed.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class URLChecker {

    public static final String FILE_COLON = "file:";
    public static final String FILE_URI_SCHEME_NO_HOST = "file:///";

    public static final String OS = System.getProperty("os.name");
    public static final boolean IS_WIN = OS.toLowerCase().contains("win");

    /**
     * Check the given array of URLs for relative references and return a new
     * array with the resulting urls. If found, add the base URL to it to make
     * them all fully qualified. This will also set the urlRelativeCheck flag to
     * true.
     *
     * @param worldURL the root URL to apply to the urls
     * @param urls The array of URLs to check
     * @param anchor true if we should apply extra checks for anchor URLs.
     * @return An array of updated URLs.
     */
    public static String[] checkURLs(String worldURL, String[] urls, boolean anchor) {

        int len = urls.length;
        String[] ret_val = new String[len];
        System.arraycopy(urls, 0, ret_val, 0, len);
        checkURLsInPlace(worldURL, ret_val, anchor);
        return (ret_val);
    }

    /** Ensure that the proper URI scheme of file:/// is prepended to the
     * given url if missing, or incomplete from a local file source. Return URLs
     * unchanged if the scheme is http:// or https://.
     *
     * @param url the url to prepend the proper URI scheme to
     * @return a properly prepended URI scheme to a url
     */
    public static String prependFileScheme(String url) {

        if (url == null)
            return url;

        if (url.contains("http"))
            return url;

        // Handle case of unix root scheme: /some.file to prepend file:
        if (url.startsWith("/"))
            return FILE_COLON + url;

        // Handle Win drive letters in URL
        if (IS_WIN) {
            url = url.replace('\\', '/');

            if (Character.isLetter(url.charAt(0)) && !url.contains(FILE_COLON))
                url = FILE_COLON + "/" + url;
        }

        // Now we're at this point b/c of InputSource.resolveUrl()
        String ret_val = null;
        if (url.startsWith(FILE_COLON)) {

            // Change file:/ to file:///
            if (Character.isLetter(url.charAt(6)) && IS_WIN) // allow windows drive, e.g. file:/C:/
                ret_val = FILE_URI_SCHEME_NO_HOST + url.substring(6);
            else if (url.charAt(6) != '/')
                ret_val = FILE_URI_SCHEME_NO_HOST + url.substring(6);

            // Check for file extensions and trailing slash
            File ext = new File(url);
            if (!ext.getName().contains(".") && !url.endsWith("/"))
                ret_val = url + "/";
        }
        return ret_val == null ? url : ret_val;
    }

    /**
     * Check the given array of URLs for relative references - in place. If
     * found, add the base URL to it to make them all fully qualified. This will
     * also set the urlRelativeCheck flag to true.
     *
     * @param worldURL the root URL to apply to the urls
     * @param urls The array of URLs to check and update if necessary.
     * @param anchor true if we should apply extra checks for anchor URLs.
     */
    public static void checkURLsInPlace(String worldURL, String[] urls, boolean anchor) {

        worldURL = prependFileScheme(worldURL);

        if (anchor) {
            String tmp;

            for (int i = 0; i < urls.length; i++) {
                tmp = urls[i];

                if ((tmp.charAt(0) != '#') && (tmp.indexOf(':') == -1)) {
                    urls[i] = buildURL(worldURL, tmp);
                }
            }
        } else {
            for (int i = 0; i < urls.length; i++) {
                if (urls[i].indexOf(':') == -1) {
                    urls[i] = buildURL(worldURL, urls[i]);
                }
            }
        }
    }

    /**
     * Join the world URL and the relative URL together to trim the relative
     * path if it contains "../" and "./" entries.
     *
     * @param worldURL the root URL to apply to the urls
     * @param url The relative URL to check
     * @return The completed URL string
     */
    private static String buildURL(String worldURL, String url) {

        if (url == null || url.length() < 1) {
            return worldURL;
        }

        char first_char = url.charAt(0);
        String ret_val = null;

        try {
            switch (first_char) {
                case '.':
                    {
                        String scheme = URIUtils.getScheme(worldURL);
                        String[] host_bits = URIUtils.getHostAndPortFromUrl(worldURL);
                        StringBuilder buf = new StringBuilder(scheme);
                        buf.append("://");
                        if (host_bits != null) {
                            if (host_bits[0] != null) {
                                buf.append(host_bits[0]);
                            }

                            if (host_bits[1] != null) {
                                buf.append(':');
                                buf.append(host_bits[1]);
                            }
                        }
                        String world_path = URIUtils.getPathFromUrlString(worldURL);
                        StringTokenizer strtok = new StringTokenizer(world_path, "/");
                        ObjectArray path_stack = new ObjectArray();

                        while (strtok.hasMoreTokens()) {
                            path_stack.add(strtok.nextToken());
                        }

                        // Tokenize the main string now.
                        String[] path_bits = URIUtils.stripFile(url);
                        if (path_bits[0] != null) {
                            strtok = new StringTokenizer(path_bits[0], "/");
                            while (strtok.hasMoreTokens()) {
                                String str = strtok.nextToken();

                                switch (str.length()) {
                                    case 2:
                                        if (str.charAt(0) != '.') {
                                            path_stack.add(str);
                                        } else {
                                            path_stack.remove(path_stack.size() - 1);
                                        }
                                        break;

                                    case 3:
                                        if ((str.charAt(0) != '.') && (str.charAt(1) == '.')) {
                                            if (path_stack.size() == 0) {
                                                System.out.println("Invalid relative path " + url);
                                                return null;
                                            }
                                            path_stack.remove(path_stack.size() - 1);
                                        } else {
                                            path_stack.add(str);
                                        }

                                        break;
                                    default:
                                        path_stack.add(str);
                                }
                            }
                        }

                        // Now build all the bits back up again into a single path
                        int num_items = path_stack.size();
                        for (int i = 0; i < num_items; i++) {
                            String dir = (String) path_stack.get(i);
                            buf.append('/');
                            buf.append(dir);
                        }       if (path_bits[1] != null) {
                            buf.append('?');
                            buf.append(path_bits[1]);
                        }       if (path_bits[2] != null) {
                            buf.append('#');
                            buf.append(path_bits[2]);
                        }       ret_val = buf.toString();
                        break;
                    }
                case '/':
                    {
                        // The URL is placing itself at the root of the source, so grab
                        // that from the worldURL. This currently does not check for the
                        // user using relative paths within the url string like this:
                        //   /root/dir/../other/dir/something.jpg
                        String scheme = URIUtils.getScheme(worldURL);
                        String[] host_bits = URIUtils.getHostAndPortFromUrl(worldURL);
                        StringBuilder buf = new StringBuilder(scheme);
                        buf.append("://");
                        if (host_bits != null) {
                            if (host_bits[0] != null) {
                                buf.append(host_bits[0]);
                            }

                            if (host_bits[1] != null) {
                                buf.append(':');
                                buf.append(host_bits[1]);
                            }
                        }

                        // There may be an issue here in that we ignore the authority part,
                        // like name and password that http and ftp urls may include.
                        buf.append(url);
                        ret_val = buf.toString();
                        break;
                    }
                default:

                    // Allow for URLs like jar:file:/...
                    // TODO: This could be a better check for the jar: scheme
                    if (!worldURL.contains(":/")) {
                        ret_val = prependFileScheme(worldURL) + url;
                    } else {
                        ret_val = worldURL + url;
                    }
                    break;
            }
        } catch (MalformedURLException mue) {
            System.err.println("Danger! Malformed URL in " + URLChecker.class.getName());
            mue.printStackTrace(System.err);
        }

        return ret_val;
    }
}

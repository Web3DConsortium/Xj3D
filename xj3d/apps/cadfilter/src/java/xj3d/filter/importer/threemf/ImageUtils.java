/*****************************************************************************
 *                        Shapeways Copyright (c) 2018
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/
package xj3d.filter.importer.threemf;

// External Imports
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.process.ProcessStarter;

// Internal Imports
// none

/**
 * Handle image tasks
 *
 * @author Alan Hudson
 */
public class ImageUtils {
    private static final boolean DEBUG = false;
    private String imageMagicLocation = "/usr/bin";

    public ImageUtils() {
        String osName = System.getProperty("os.name");

        if (osName.contains("Windows")) {
            imageMagicLocation = "C:\\InstalledPrograms\\ImageMagick-6.9.8-Q16";

        }
        ProcessStarter.setGlobalSearchPath(imageMagicLocation);

        if (DEBUG) System.out.printf("Using imagemagick: %s\n",imageMagicLocation);
    }

    public void setInstallLocation(String loc) {
        imageMagicLocation = loc;
        ProcessStarter.setGlobalSearchPath(imageMagicLocation);
    }

    public void rescaleFile(String input, String dest, int width, int height) throws Exception {
        float desiredAspectRatio = (float)width / height;
        float AR_EPS = 0.2f;
        boolean goodAspect = true;
        boolean trimImage = false;

        Info imageInfo = new Info(input,true);
        float ar = (float)imageInfo.getImageWidth() / imageInfo.getImageHeight();

        if ((Math.abs(ar - desiredAspectRatio) > AR_EPS)) {
            goodAspect = false;
        }

        if (!goodAspect) trimImage = true;

        ConvertCmd cmd = new ConvertCmd();

        IMOperation op = new IMOperation();
        op.addImage(input);

        op = new IMOperation();
        op.addImage(input);
        if (trimImage) op.trim();
        op.resize(width,height,"!>");

        op.addImage(dest);
        cmd.run(op);
    }

    /**
     * Convert a file to png32 format
     * @param input
     * @param dest
     * @throws java.lang.Exception
     */
    public void convToPng32(String input, String dest) throws Exception {
        ConvertCmd cmd = new ConvertCmd();

        IMOperation op = new IMOperation();
        op.addImage(input);

        op = new IMOperation();
        op.addImage(input);

        op.addImage("PNG32:" + dest);
        cmd.run(op);
    }
}
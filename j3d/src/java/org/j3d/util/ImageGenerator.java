/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.util;

// External imports
import java.awt.image.*;

import java.util.Hashtable;

// Local imports
// none

/**
 * The conversion class that is an image consumer and creates a buffered
 * image as the output.
 * <p>
 *
 * The generator only works on a single image instance at a time. The
 * consumer can be reset if needed to work on another image. If the image
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
class ImageGenerator implements ImageConsumer
{
    /** Semaphore to block return of a loader until the image is complete */
    private final Object holder;

    /** Colour model given to us by the consumer */
    private ColorModel colorModel;

    /** List of properties from the original image */
    private Hashtable properties;

    /** Raster used to put the image values in to as the pixels are read */
    private WritableRaster raster;

    /** Width of the image in pixels */
    private int width;

    /** Height of the image in pixels */
    private int height;

    private BufferedImage image;
    private int[] intBuffer;
    private boolean loadComplete;

    ImageGenerator()
    {
        holder = new Object();
        width = -1;
        height = -1;
        loadComplete = false;
    }

    //------------------------------------------------------------------------
    // Methods for ImageConsumer events
    //------------------------------------------------------------------------

    /**
     * Notification of the image producer completing the source image.
     * Completion may be due to an error or other form of invalid data.
     *
     * @param status The status of the completion
     */
    @Override
    public void imageComplete(int status)
    {
        if(status == STATICIMAGEDONE ||
            status == IMAGEABORTED ||
            status == IMAGEERROR ||
            status == SINGLEFRAMEDONE)
        {
            synchronized(holder)
            {
                loadComplete = true;
                holder.notify();
            }
        }
        else
            System.err.println("Some other value passed to complete");
    }

    /**
     * Set the color model to use for the new image based on the model used
     * by the source image.
     *
     * @param model The model to use
     */
    @Override
    public void setColorModel(ColorModel model)
    {
        colorModel = model;
        createImage();
    }

    /**
     * Notification of the dimensions of the source image.
     *
     * @param w The width of the source image
     * @param h The height of the source image
     */
    @Override
    public void setDimensions(int w, int h)
    {
        width = w;
        height = h;
        createImage();
    }

    /**
     * Notification of load hints that may be useful. Not used in this
     * implementation.
     *
     * @param flags The hints
     */
    @Override
    public void setHints(int flags)
    {
    }

    /**
     * Notification of a bunch of pixel values in byte form. Used for
     * 256 color or less images (eg GIF, greyscale etc).
     *
     * @param x The starting x position of the pixels
     * @param y The starting y position of the pixels
     * @param w The number of pixels in the width
     * @param h The number of pixels in the height
     * @param model The color model used with these pixel values
     * @param offset The offset into the source array to copy from
     * @param scansize The number of pixel values between rows
     */
    @Override
    public void setPixels(int x,
        int y,
        int w,
        int h,
        ColorModel model,
        byte[] pixels,
        int offset,
        int scansize)
    {
        if (loadComplete)
            return;

        if((intBuffer == null) || (pixels.length > intBuffer.length))
            intBuffer = new int[pixels.length];

        for(int i = pixels.length; --i >= 0 ; )
            intBuffer[i] = (int)pixels[i] & 0xFF;

        raster.setPixels(x, y, w, h, intBuffer);
    }

    /**
     * Notification of a bunch of pixel values as ints. These will be
     * full 3 or 4 component images.
     *
     * @param x The starting x position of the pixels
     * @param y The starting y position of the pixels
     * @param w The number of pixels in the width
     * @param h The number of pixels in the height
     * @param model The color model used with these pixel values
     * @param offset The offset into the source array to copy from
     * @param scansize The number of pixel values between rows
     */
    @Override
    public void setPixels(int x,
        int y,
        int w,
        int h,
        ColorModel model,
        int[] pixels,
        int offset,
        int scansize)
    {
        if (loadComplete)
            return;

        image.setRGB(x, y, w, h, pixels, offset, scansize);
    }

    /**
     * Notification of the properties of the image to use.
     *
     * @param props The map of properties for this image
     */
    @Override
    public void setProperties(Hashtable props)
    {
        properties = props;
        createImage();
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------

    /**
     * Fetch the image. This image is not necessarily completely rendered
     * although we do try to guarantee it.
     *
     * @return The image that has been created for the current input
     */
    BufferedImage getImage()
    {
        synchronized(holder)
        {
            if(!loadComplete)
            {
                try
                {
                    holder.wait();
                }
                catch(InterruptedException ie)
                {
                }
            }
        }

        return image;
    }

    /**
     * Reset the converter to work with a new image source. If an image is
     * currently loading then the results are indeterminate.
     */
    void reset()
    {
        synchronized(holder)
        {
            holder.notify();
        }

        loadComplete = false;
        colorModel = null;
        raster = null;
        properties = null;
        image = null;
        width = -1;
        height = -1;
    }

    /**
     * Convenience method used to create the output image based on the data
     * that has been given to us so far. Will not create the image until all
     * the necessary information is given, and once created, will not overwrite
     * the current image.
     */
    private void createImage()
    {
        // meet the preconditions first.
        if((image != null) ||
            (width == -1) ||
            (colorModel == null) || 
            loadComplete)
            return;

        raster = colorModel.createCompatibleWritableRaster(width, height);

        boolean premult = colorModel.isAlphaPremultiplied();
        image = new BufferedImage(colorModel, raster, premult, properties);
    }
}

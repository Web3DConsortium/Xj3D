/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.image;

// External imports
import java.nio.ByteBuffer;

// Local imports
// none

/**
 * A representation of an image contained in a <code>ByteBuffer</code>.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class NIOBufferImage {

    /**
     * Invalid width error message
     */
    private static final String INVALID_WIDTH_PARAMETER
            = "image width must be a positive integer";

    /**
     * Invalid height error message
     */
    private static final String INVALID_HEIGHT_PARAMETER
            = "image height must be a positive integer";

    /**
     * Invalid type error message, null
     */
    private static final String TYPE_IS_NULL
            = "image type must be non-null";

    /**
     * Invalid buffer error message, null
     */
    private static final String BUFFER_IS_NULL
            = "image buffer must be non-null";

    /**
     * Invalid buffer error message, insufficient size
     */
    private static final String BUFFER_INSUFFICIENT
            = "image buffer must be sufficiently sized to contain image";

    /**
     * The image width
     */
    private int width;

    /**
     * The image height
     */
    private int height;

    /**
     * The image format type
     */
    private NIOBufferImageType type;

    /**
     * The image buffer
     */
    private ByteBuffer[] buffer;

    /**
     * Flag indicating that the image should be treated as grayscale, regardless
     * of the actual number of components
     */
    private boolean isGrayScale;

    /**
     * Flag indicating that the image includes alpha (1 - transparency) components
     */
    private boolean hasTransparency;

    /**
     * Constructor
     *
     * @param width The image width
     * @param height The image height
     * @param type The image format type
     * @throws IllegalArgumentException if either the width or height arguments
     * are not positive integers
     * @throws NullPointerException if the type argument is <code>null</code>
     */
    public NIOBufferImage(int width, int height, NIOBufferImageType type) {
        this(width,
             height,
             type,
             ByteBuffer.allocateDirect(width * height * type.size));
    }

    /**
     * Constructor
     *
     * @param width The image width
     * @param height The image height
     * @param type The image format type
     * @param buffer The image data
     * @throws IllegalArgumentException if either the width or height arguments
     * are not positive integers
     * @throws NullPointerException if either the type or buffer argument are
     * <code>null</code>
     * @throws IllegalArgumentException if the buffer is insufficiently sized
     */
    public NIOBufferImage(int width, int height, NIOBufferImageType type, ByteBuffer buffer) {
        this(width,
             height,
             type,
             (type == NIOBufferImageType.INTENSITY) | (type == NIOBufferImageType.INTENSITY_ALPHA),
             buffer);
    }

    /**
     * Constructor
     *
     * @param width The image width
     * @param height The image height
     * @param type The image format type
     * @param isGrayScale Flag indicating that the image should be treated as
     * grayscale, regardless of the format
     * @param buffer The image data
     * @throws IllegalArgumentException if either the width or height arguments
     * are not positive integers
     * @throws NullPointerException if either the type or buffer argument are
     * <code>null</code>
     * @throws IllegalArgumentException if the buffer is insufficiently sized
     */
    public NIOBufferImage(int width, int height, NIOBufferImageType type,
            boolean isGrayScale, ByteBuffer buffer) {

        if (width < 1) {
            throw new IllegalArgumentException(INVALID_WIDTH_PARAMETER);
        } else if (height < 1) {
            throw new IllegalArgumentException(INVALID_HEIGHT_PARAMETER);
        } else if (type == null) {
            throw new NullPointerException(TYPE_IS_NULL);
        } else if (buffer == null) {
            throw new NullPointerException(BUFFER_IS_NULL);
        } else if (buffer.limit() != width * height * type.size) {
            throw new IllegalArgumentException(BUFFER_INSUFFICIENT);
        }
        this.width = width;
        this.height = height;
        this.type = type;
        this.isGrayScale = isGrayScale;
        this.hasTransparency = (type == NIOBufferImageType.INTENSITY_ALPHA) | (type == NIOBufferImageType.RGBA);
        this.buffer = new ByteBuffer[]{buffer};
    }

    /**
     * Return the image width
     *
     * @return The image width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Return the image height
     *
     * @return The image height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Return the image format type
     *
     * @return The image format type
     */
    public NIOBufferImageType getType() {
        return type;
    }

    /**
     * Return whether the image should be treated as grayscale
     *
     * @return whether the image should be treated as grayscale
     */
    public boolean isGrayScale() {
        return isGrayScale;
    }

    /**
     * Return whether the image includes alpha (1 - transparency) components
     *
     * @return whether the image should be treated as having alpha (1 - transparency) components
     */
    public boolean hasTransparency() {
        return hasTransparency;
    }

    /**
     * Return the number of image levels
     *
     * @return The number of image levels
     */
    public int getLevels() {
        return buffer.length;
    }

    /**
     * Return the image buffer
     *
     * @return The image buffer
     */
    public ByteBuffer getBuffer() {
        buffer[0].rewind();
        return buffer[0];
    }

    /**
     * Return the image buffer array
     *
     * @param ret_buf
     * @return The image buffer array
     */
    public ByteBuffer[] getBuffer(ByteBuffer[] ret_buf) {
        int size = buffer.length;
        if ((ret_buf == null) || (ret_buf.length < size)) {
            ret_buf = new ByteBuffer[size];
        }
        for (int i = 0; i < size; i++) {
            buffer[i].rewind();
            ret_buf[i] = buffer[i];
        }
        return ret_buf;
    }

    /**
     * Set the image buffer
     *
     * @param buffer The image buffer
     * @throws NullPointerException if the argument buffer is <code>null</code>
     * @throws IllegalArgumentException if the buffer is insufficiently sized
     */
    public void setBuffer(ByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException(BUFFER_IS_NULL);
        } else if (buffer.limit() != width * height * type.size) {
            throw new IllegalArgumentException(BUFFER_INSUFFICIENT);
        }
        this.buffer = new ByteBuffer[]{buffer};
    }

    /**
     * Set the image buffer array
     *
     * @param buffer The image buffer
     * @throws NullPointerException if the argument buffer is <code>null</code>
     * @throws IllegalArgumentException if the buffer is insufficiently sized
     */
    public void setBuffer(ByteBuffer[] buffer) {
        if (buffer == null) {
            throw new NullPointerException(BUFFER_IS_NULL);
        } else if (buffer[0].limit() != width * height * type.size) {
            throw new IllegalArgumentException(BUFFER_INSUFFICIENT);
        }
        int size = buffer.length;
        this.buffer = new ByteBuffer[size];
        System.arraycopy(buffer, 0, this.buffer, 0, size);
    }

    /**
     * Return a description of the image
     *
     * @return a description of the image
     */
    @Override
    public String toString() {
        return ("NIOBufferImage: type = " + type.name
                + ", width = " + width
                + ", height = " + height);
    }
}

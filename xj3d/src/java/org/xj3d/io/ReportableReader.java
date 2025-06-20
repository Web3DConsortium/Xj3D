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

package org.xj3d.io;

// External imports
import java.io.*;

// Local imports

/**
 * Listens for updates to read status on a stream.
 * <p>
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class ReportableReader extends FilterReader {

    /** The number of bytes read since last update*/
    private int bytesRead;

    /** The total number of bytes read */
    private int totalBytesRead;

    /** Whether to report relative or absolute values */
    private boolean relative;

    /** The frequency(bytes) to report updates */
    private int updateSize;

    /** The Progress Listener */
    private ReadProgressListener listener;

    /**
     * Constructor.
     *
     * @param relative Whether the update size is relative or absolute.
     * @param updateSize The size in bytes to issue updates.
     *        This will be approximately honored.
     * @param listener The progress listener
     * @param in The input source
     */
    public ReportableReader(boolean relative, int updateSize,
        ReadProgressListener listener, Reader in) {

        super(in);

        bytesRead = 0;
        this.relative = relative;
        this.updateSize = updateSize;
        this.listener = listener;
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (listener != null)
            listener.streamClosed();
    }


    @Override
    public int read() throws IOException {
        int i = super.read();

        if(i != -1) {
            bytesRead++;
        }

        if (bytesRead >= updateSize) {
            totalBytesRead += bytesRead;

            if (relative)
                listener.progressUpdate(bytesRead);
            else
                listener.progressUpdate(totalBytesRead);

            bytesRead = 0;
        }

        return i;
    }

    @Override
    public int read(char[] b) throws IOException {
        int i = super.read(b,0,b.length);

        if(i!=-1) {
            bytesRead += i;
        }

        if (bytesRead >= updateSize) {
            totalBytesRead += bytesRead;

            if (relative)
                listener.progressUpdate(bytesRead);
            else
                listener.progressUpdate(totalBytesRead);

            bytesRead = 0;
        }

        return i;
    }

    @Override
    public int read(char[] b, int off, int len) throws IOException {
        int i = super.read(b,off,len);

        if(i != -1) {
            bytesRead += i;
        }

        if (bytesRead >= updateSize) {
            totalBytesRead += bytesRead;

            if (relative)
                listener.progressUpdate(bytesRead);
            else
                listener.progressUpdate(totalBytesRead);

            bytesRead = 0;
        }

        return i;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        totalBytesRead = 0;
        bytesRead = 0;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = skip(n);

        bytesRead += n;

        if (bytesRead >= updateSize) {
            totalBytesRead += bytesRead;

            if (relative)
                listener.progressUpdate(bytesRead);
            else
                listener.progressUpdate(totalBytesRead);

            bytesRead = 0;
        }

        return result;
    }
}

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

package org.web3d.net.content;

// External imports
import com.jogamp.openal.ALConstants;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.ietf.uri.ContentHandler;
import org.ietf.uri.ResourceConnection;

// Local imports
import org.xj3d.io.StreamContentContainer;

/**
 * Content handler implementation for loading VRML Audio content from a URI
 * resource connection.
 *  <p>
 *
 * The returned object type for this loader is an InputStream
 *  <p>
 *
 * @author  Guy Carpenter
 * @version $Revision: 1.5 $
 */
class AudioContentHandler extends ContentHandler {

    /**
     * Construct a new instance of the content handler.
     *
     */
    AudioContentHandler() {
    }

    /**
     * Given a fresh stream from a ResourceConnection,
     * read and create an object instance.
     *
     * @param resc The resource connection to read the data from
     * @return The object read in by the content handler
     * @exception IOException The connection stuffed up.
     */
    @Override
    public Object getContent(ResourceConnection resc)
        throws IOException {

        InputStream inputStream = resc.getInputStream();
        int format;
        int freq;
        int length = resc.getContentLength();

        InputStream bis = new BufferedInputStream(inputStream);

        try {
            AudioFormat fmt = AudioSystem.getAudioFileFormat(bis).getFormat();

            int numChannels = fmt.getChannels();
            int bits = fmt.getSampleSizeInBits();
            format = ALConstants.AL_FORMAT_MONO8;

            if ((bits == 8) && (numChannels == 1)) {
                format = ALConstants.AL_FORMAT_MONO8;
            } else if ((bits == 16) && (numChannels == 1)) {
                format = ALConstants.AL_FORMAT_MONO16;
            } else if ((bits == 8) && (numChannels == 2)) {
                format = ALConstants.AL_FORMAT_STEREO8;
            } else if ((bits == 16) && (numChannels == 2)) {
                format = ALConstants.AL_FORMAT_STEREO16;
            }

            freq = Math.round(fmt.getSampleRate());
        } catch(UnsupportedAudioFileException | IOException e) {
            e.printStackTrace(System.err);
            return null;
        }

        return new StreamContentContainer(bis, length, format, freq);
    }
}

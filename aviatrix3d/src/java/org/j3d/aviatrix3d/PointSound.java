/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
import com.jogamp.openal.AL;

import java.text.MessageFormat;
import java.util.Locale;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * A PointSound class which emits sound in all directions from a point.
 * <p>
 * The sound will attenuate by distance based on the refDistance
 * and maxDistance parameters.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>sourceCreateFailedMsg: Error message when the something when wrong
 *     creating the underlying OpenAL buffers</li>
 * <li>sourceParamsFailedMsg: Error message when setting up the source
 *     buffer params failed.</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class PointSound extends Sound
{
    /** Message when OpenAL buffer creation failed */
    protected static final String BUFFER_CREATE_PROP =
        "org.j3d.aviatrix3d.PointSound.sourceCreateFailedMsg";

    /** Message when OpenAL buffer creation failed */
    protected static final String BUFFER_PARAMS_PROP =
        "org.j3d.aviatrix3d.PointSound.sourceParamsFailedMsg";

    /** Scratch source position */
    protected Point3f tmpSourcePosition;

    /** The distance when gain rolloff starts */
    protected float refDistance;

    /** Maximum distance of a source.  Used to clamp distance attenuation */
    protected float maxDistance;

    /** How quickly does the sound decrease over distance. */
    protected float rolloffFactor;

    /**
     * Creates a sound.
     */
    public PointSound()
    {
        super();

        tmpSourcePosition = new Point3f();

        // bugfix 609
        refDistance = 1.0f;
        maxDistance = Float.MAX_VALUE;
        rolloffFactor = 1f;
    }

    //----------------------------------------------------------
    // Methods defined by AudioRenderable
    //----------------------------------------------------------

    @Override
    public void render(AL al, Matrix4f transform)
    {
        transform.transform(sourcePosition,tmpSourcePosition);

        if (dataChanged)
        {
            buffer = soundSource.getBufferId(al, seq);
            if (buffer == -1)
                return;

            dataChanged = false;
            playChanged = true;

            // Bind buffer with a source.
            values.rewind();

            // Bind buffer with a source.
            al.alGenSources(1, values);
            source = values.get(0);

            int error = al.alGetError();
            if (error != AL.AL_NO_ERROR)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                Locale lcl = intl_mgr.getFoundLocale();
                String msg_pattern = intl_mgr.getString(BUFFER_CREATE_PROP);

                Object[] msg_args = { error };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);

                System.out.println(msg);
            }
            else
            {
                al.alSourcei(source, AL.AL_BUFFER, buffer);
                al.alSourcef(source, AL.AL_PITCH, pitch);
                al.alSourcef(source, AL.AL_GAIN, 1.0f);
                al.alSource3f(source, AL.AL_POSITION, tmpSourcePosition.x, tmpSourcePosition.y,tmpSourcePosition.z);
                al.alSource3f(source, AL.AL_VELOCITY, sourceVelocity.x, sourceVelocity.y, sourceVelocity.z);
                al.alSourcei(source, AL.AL_LOOPING, loop ? 1 : 0);

                al.alSourcef(source, AL.AL_REFERENCE_DISTANCE, refDistance);
                al.alSourcef(source, AL.AL_MAX_DISTANCE, maxDistance);
                al.alSourcef(source, AL.AL_ROLLOFF_FACTOR, rolloffFactor);

                error = al.alGetError();
                if (error != AL.AL_NO_ERROR)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    Locale lcl = intl_mgr.getFoundLocale();
                    String msg_pattern = intl_mgr.getString(BUFFER_PARAMS_PROP);

                    Object[] msg_args = { error };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    String msg = msg_fmt.format(msg_args);

                    System.out.println(msg);
                }
            }
        }

        if (paramsChanged)
        {
            al.alSourcef(source, AL.AL_PITCH, pitch);
            al.alSourcei(source, AL.AL_LOOPING, loop ? 1 : 0);

            paramsChanged = false;
        }

        al.alSource3f(source, AL.AL_POSITION, tmpSourcePosition.x, tmpSourcePosition.y,tmpSourcePosition.z);

        if (playChanged)
        {
            if (playing && paused)
            {
                al.alSourcePlay(source);
            }

            if (playing)
            {
                if (paused)
                    al.alSourcePause(source);
                else
                    al.alSourcePlay(source);
            }
            else
                al.alSourceStop(source);

            playChanged = false;
        }
        else if (playing)
        {
            // Bind buffer with a source.
            values.rewind();

            al.alGetSourcei(source, AL.AL_SOURCE_STATE, values);
            if (values.get(0) == AL.AL_STOPPED)
            {
                playing = false;
            }
        }
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param al The al context to draw with
     */
    @Override
    public void postRender(AL al)
    {
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    @Override
    public int compareTo(Renderable o)
        throws ClassCastException
    {
        Sound app = (Sound)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof PointSound))
            return false;
        else
            return equals((PointSound)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the distance when gain rolloff starts.
     *
     * @param distance The distance in units
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void setRefDistance(float distance)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        refDistance = distance;
    }

    /**
     * Set the distance when gain rolloff reaches zero.
     *
     * @param distance The distance in units
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void setMaxDistance(float distance)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        maxDistance = distance;
    }

    /**
     * Set the rollloffFactor.
     *
     * @param factor
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void setRolloffFactor(float factor)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        rolloffFactor = factor;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ps The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(PointSound ps)
    {
        if(ps == null)
            return 1;

        if(ps == this)
            return 0;

        return super.compareTo(ps);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ps The background instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(PointSound ps)
    {
        if(ps == this)
            return true;

        if(ps == null)
            return false;

        return super.equals(ps);
    }
}

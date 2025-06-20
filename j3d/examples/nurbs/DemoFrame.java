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

// Standard imports
import java.awt.*;
import java.awt.event.*;
import javax.media.j3d.*;

// Application Specific imports
// none

/**
 * Demonstration of a mouse navigation in a world.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DemoFrame extends Frame implements WindowListener
{
    /** The canvas supplied by this frame */
    protected Canvas3D canvas;

    public DemoFrame(String title)
    {
        super(title);

        canvas = createCanvas();
        add(canvas, BorderLayout.CENTER);

        setSize(600, 400);
        setLocation(40, 40);
        addWindowListener(this);
    }

    /**
     * Create a new demo frame that has the option of creating a
     * canvas for use. If it does, it is assigned to the center of the
     * frame.
     */
    public DemoFrame(String title, boolean useCanvas)
    {
        super(title);

        if(useCanvas)
        {
            canvas = createCanvas();
            add(canvas, BorderLayout.CENTER);
        }

        setSize(400, 400);
        setLocation(40, 40);
        addWindowListener(this);
    }

    /**
     * Create a 3D canvas for us to use.
     *
     * @return A new canvas to use
     */
    protected Canvas3D createCanvas()
    {
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        Canvas3D cv = new Canvas3D(dev.getBestConfiguration(template));
        cv.setStereoEnable(false);
        cv.setDoubleBufferEnable(true);

        return cv;
    }

    /**
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowOpened(WindowEvent evt)
    {
    }
}

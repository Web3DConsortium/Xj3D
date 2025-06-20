
import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 * TextureDemo1
 *
 * Create your very own animated texture by modifying it every frame.
 */
public class SAIPositionInterpolator {

    static X3DNode timer;

    static X3DNode trans;

    public static void main(String[] args) {
        // Step One: Create the browser component
        ExternalBrowser browser = SAITestFactory.getBrowser();
        // Step Two: Initialize your scene
        browser.addBrowserListener(new GenericSAIBrowserListener());
        X3DScene s = browser.createX3DFromString(
                "PROFILE Interchange\n"
                + "Viewpoint {}\n"
                + "TimeSensor { loop TRUE enabled TRUE }\n"
                + "Transform { children Shape { geometry Box {} appearance Appearance { material Material { diffuseColor 1 0 0}} }}"
        );
        trans = s.getRootNodes()[2];
        timer = s.getRootNodes()[1];
        timer.getField("time").addX3DEventListener(new SAIPositionInterpolatorOne(
                (SFVec3f) trans.getField("translation"))
        );
        browser.addBrowserListener((BrowserEvent evt) -> {
            System.out.println("Finishing the stuff.");
            ((SFTime) timer.getField("startTime")).setValue(System.currentTimeMillis());
        });
        browser.replaceWorld(s);
    }
}

/**
 * A simple demo recreating the functionality of an interpolator node by
 * listening to a time field on a time sensor and modifying a position field as
 * appropriate.
 */
class SAIPositionInterpolatorOne implements X3DFieldEventListener {

    float startPosition[] = new float[]{-5, 0, 0};

    float endPosition[] = new float[]{5, 0, 0};

    SFVec3f destination;

    /**
     * Buffer for setting position field.
     */
    float scratchData[] = new float[3];

    public SAIPositionInterpolatorOne(SFVec3f dest) {
        destination = dest;
    }

    /**
     * Respond to the field changing. This demo depends on the fact that it can
     * modify the field that it is receiving an event from. This is not always
     * the case
     */
    @Override
    public void readableFieldChanged(X3DFieldEvent evt) {
        System.out.println("Time event " + evt.getTime());
        scratchData[0] = (float) (evt.getTime() % 5.0) - 3.0f;
        System.out.println(scratchData[0]);
        scratchData[1] = 0;
        scratchData[2] = 0;
        destination.setValue(scratchData);
    }
}

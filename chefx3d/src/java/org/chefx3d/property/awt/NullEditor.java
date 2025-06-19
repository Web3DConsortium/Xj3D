/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.property.awt;

// External Imports
import java.awt.BorderLayout;
import javax.swing.*;

// Internal Imports
import org.chefx3d.property.DataEditor;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * An editor that allows selection from a combo box
 *
 * @author Russell Dodds
 * @version $Revision: 1.2 $
 */
public class NullEditor extends DataEditor {

    /** The component to return to the editor */
    private JPanel component;

    /**
     * Create a new null editor
     *
     */
    public NullEditor() {

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        initUI();

    }

    // ----------------------------------------------------------
    // Methods overriding Object
    // ----------------------------------------------------------
    /**
     * Make a clone of this object.
     *
     * @return A clone of the object
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        NullEditor o = (NullEditor) super.clone();

        o.initUI();

        return o;
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     *
     * @return
     */
    @Override
    public String getValue() {
        return null;
    }

    @Override
    public void setValue(String value) {
    }

    /**
     * Set the component state
     *
     * @param isEnabled The new state
     */
    @Override
    public void setEnabled(boolean isEnabled) {
    }

    /**
     *
     * @return
     */
    @Override
    public JPanel getComponent() {
        //System.out.println("NullEditor.getComponent()");
        return component;
    }

    /**
     * Initialize the GUI.
     */
    private void initUI() {
        // Create the panel
        component = new JPanel(new BorderLayout());
    }

}
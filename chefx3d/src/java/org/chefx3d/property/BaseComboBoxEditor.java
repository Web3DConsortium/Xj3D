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

package org.chefx3d.property;

// External Imports
import java.util.*;

// Internal Imports
import org.chefx3d.util.DefaultErrorReporter;

/**
 * An editor that allows selection from a combo box
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class BaseComboBoxEditor extends DataEditor {

    /** The tooltip for the add button */
    protected String tooltip;

    /** The valid values */
    protected String[] validValues;

    /** The mapped values */
    protected Map<String, String> mappedValues;

    /** The initial value */
    protected String initialValue;

    /** The editor updates what type of value */
    protected DataEditor.Types type;

    /**
     * Create a ComboBoxEditor
     *
     * @param tooltip The icon tooltip
     * @param validValues The valid values
     * @param initialValue The initial value
     * @param type
     */
    public BaseComboBoxEditor(String tooltip, String[] validValues,
            String initialValue, DataEditor.Types type) {

        this.validValues = validValues;
        this.tooltip = tooltip;
        this.initialValue = initialValue;
        this.type = type;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

    }

    /**
     * Create a ComboBoxEditor.
     *
     * @param tooltip The icon tooltip
     * @param validValues The valid values
     * @param mappedValues The values to actually return
     * @param initialValue The initial value
     * @param type
     */
    public BaseComboBoxEditor(String tooltip, String[] validValues,
            Map<String, String> mappedValues, String initialValue,
            DataEditor.Types type) {

        this.validValues = validValues;
        this.tooltip = tooltip;
        this.mappedValues = mappedValues;
        this.initialValue = initialValue;
        this.type = type;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

    }

    @Override
    public Object getComponent() {
        return null;
    }

    /**
     * Set the component value
     *
     * @param value The new value
     */
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

    public void setFocus() {
    }

    @Override
    public String getValue() {
        return initialValue;
    }

}
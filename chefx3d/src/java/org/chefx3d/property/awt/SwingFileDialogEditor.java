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
import javax.swing.*;

import java.io.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.w3c.dom.UserDataHandler;

// Internal Imports
import org.chefx3d.property.DataEditor;
import org.chefx3d.model.*;
import org.chefx3d.util.DefaultErrorReporter;

/**
 * A swing based file dialog editor.
 *
 * @author Alan Hudson
 * @version
 */
public class SwingFileDialogEditor extends DataEditor implements
        UserDataHandler, ActionListener {

    /** The component to return to the editor */
    private JPanel component;

    /** The max columns to display */
    private int maxColumns;

    /** The initial value */
    private String initialValue;

    /** The chooser for saving files */
    private JFileChooser fileChooser;

    /** The text field */
    private JTextField textfield;

    /** The value of this editor */
    private String value;

    /** The parent component */
    private Component parent;

    /** The default directory to save files */
    private String defaultDir;

    /**
     * Create a new Swing Dialog Editor.
     *
     * @param defaultDir
     * @param allowedExtensions The allowed extensions
     * @param descriptions The descriptions of the extensions
     * @param initialValue
     * @param maxColumns
     */
    public SwingFileDialogEditor(String defaultDir, String[] allowedExtensions,
            String[] descriptions, String initialValue, int maxColumns) {

        value = initialValue;
        this.initialValue = initialValue;
        this.defaultDir = defaultDir;
        this.maxColumns = maxColumns;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        initUI();
    }

    // ----------------------------------------------------------
    // Methods required by the ActionListener interface
    // ----------------------------------------------------------

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (fileChooser == null) {
            fileChooser = initFileChooser();
        }

        int ret = fileChooser.showSaveDialog(component);
        if (ret != JFileChooser.APPROVE_OPTION)
            return;

        File file = fileChooser.getSelectedFile();

        value = processValue(file.getPath());

        textfield.setText(value);

        processNewValue();
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
        SwingFileDialogEditor o = (SwingFileDialogEditor) super.clone();

        o.initUI();

        return o;
    }

    /**
     *
     * @return The UI component to extract a value from
     */
    @Override
    public String getValue() {

        return processValue(textfield.getText());

    }

    @Override
    public void setValue(String value) {

        textfield.setText(value);

    }

    /**
     * Set the component state
     *
     * @param isEnabled The new state
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        textfield.setEnabled(isEnabled);
    }

    /**
     *
     * @return
     */
    @Override
    public JPanel getComponent() {
        return component;
    }

    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------
    /**
     * Process a new value.
     */
    protected void processNewValue() {
        field.setNodeValue(value);

        Entity entity = model.getEntity(entityID);

        ChangePropertyCommand cmd = new ChangePropertyCommand(entity,
                sheet, name, "", value);
        model.applyCommand(cmd);
    }

    /**
     * Initialize the GUI.
     */
    private void initUI() {

        // Create the panel
        component = new JPanel(new BorderLayout());

        textfield = new JTextField(initialValue, maxColumns);

        JButton butt = new JButton("Open");
        butt.addActionListener(this);

        component.add(textfield, BorderLayout.WEST);
        component.add(butt, BorderLayout.EAST);

    }

    /**
     * Setup the file chooser.
     *
     * @return The configured chooser
     */
    private JFileChooser initFileChooser() {
        JFileChooser chooser = new JFileChooser(defaultDir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        chooser.setDialogTitle("Open");
        /*
         * FileFilterEx[] filter = { new FileFilterEx(".smal", "SMAL File
         * (*.smal)", true) };
         *
         * for (int i = 0; i < filter.length; i++)
         * chooser.addChoosableFileFilter(filter[i]);
         */
        // chooser.setFileView(new IconFileView(".xml", new
        // ImageIcon(AUVW.images+"xmlicon.png")));
        return chooser;
    }
}
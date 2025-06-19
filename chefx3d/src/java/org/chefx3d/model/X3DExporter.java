/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.chefx3d.model;

// External Imports
import java.io.*;
import javax.vecmath.Vector3f;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.*;

// Internal Imports
import org.chefx3d.util.*;

/**
 * Export a world model into the X3D format.
 *
 * @author Alan Hudson
 * @version $Revision: 1.37 $
 */
public class X3DExporter extends Exporter {
    // Scratch vars
    private double[] pos;

    private float[] rot;

    private Vector3f tmpVec;

    /** Default direction for segments */
    private Vector3f segmentDirection;

    /** The header string to add. */
    private String header;

    /**
     * Constructor.
     *
     * @param version The spec major and minor version number
     * @param profile The profile to use when exporting
     * @param components The components to add to the profile. Null means none.
     * @param levels The components levels
     */
    public X3DExporter(String version, String profile, String[] components,
            int[] levels) {
        pos = new double[3];
        rot = new float[4];

        segmentDirection = new Vector3f(1, 0, 0);
        tmpVec = new Vector3f();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        // Good to have DOCTYPE inserted (TDN)
        sb.append("<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D ").append(version).append("//EN\" \"http://www.web3d.org/specifications/x3d-").append(version).append(".dtd\">\n");
        sb.append("<X3D profile=\"");
        sb.append(profile);
        sb.append("\" version=\"");
        sb.append(version);
        sb.append("\">\n");
        sb.append("<head>\n");

        if (components != null && levels != null) {
            int len = Math.min(components.length, levels.length);

            for (int i = 0; i < len; i++) {
                sb.append("\t<component name=\"");
                sb.append(components[i]);
                sb.append("\" level=\"");
                sb.append(levels[i]);
                sb.append("\" />\n");
            }
        }

        sb.append("</head>\n");
        sb.append("<Scene>\n");
        sb.append("\t<WorldInfo info=\"Created with ChefX3D.  www.chefx3d.org\" />\n");

        header = sb.toString();

        errorReporter = DefaultErrorReporter.getDefaultReporter();

    }

    /**
     * Output a specific entity to the specified stream.
     *
     * @param model The world model to export
     * @param entityID The entity to export
     * @param substyle The stylesheet version to use. Appends to the normal
     *        "x3d" version.
     * @param fw The stream to write to
     */
    @Override
    public void export(WorldModel model, int entityID, String substyle,
            Writer fw) {

        super.export(model, entityID, substyle, fw);

        Entity[] toolValues = model.getModelData();

        // int len = toolValues.length;
        // HashMap styles;

        Entity td = toolValues[entityID];

        if (td == null) {
            errorReporter.messageReport("Cannot find model to export: " + entityID);
            return;
        }

        if (td.isController()) {
            return;
        }

        try {
            try (StringWriter sw = new StringWriter(1_024)) {
                Result result = new StreamResult(sw);

                fw.write(header);

                // TODO: This won't handle associations right
                //addParams(td);

                writeEntity(td, substyle, result);

                String input = sw.toString();

                String modString = removeXMLHeader(input);

                fw.write(modString);
                fw.write("</Scene>\n");
                fw.write("</X3D>\n");
            }
            fw.close();
        } catch (IOException ioe) {
            errorReporter.errorReport("IO Error.", ioe);
        }
    }

    /**
     * Output the World Model to the specified stream.
     *
     * @param model The world model to export
     * @param fw The stream to write to
     */
    @Override
    public void export(WorldModel model, Writer fw) {
        super.export(model, fw);

        Entity[] toolValues = model.getModelData();

        int len = toolValues.length;

        try {
            try (StringWriter sw = new StringWriter(1_024)) {
                Result result = new StreamResult(sw);

                fw.write(header);

                // boolean useDefault = false;

                Set<String> globals = new HashSet<>();
                String name;

                // Write out all global information
                for (int i = 0; i < len; i++) {
                    Entity td = toolValues[i];

                    if (td == null) {
                        // Its expected we will have gaps
                        continue;
                    }

                    if (td.isController()) {
                        continue;
                    }

                    name = td.getName();

                    if (!globals.contains(name)) {
                        writeEntityGlobals(td, result);
                        globals.add(name);
                    }
                }

                //errorReporter.messageReport("Entities to write: " + len);
                for (int i = 0; i < len; i++) {
                    Entity td = toolValues[i];

                    if (td == null) {
                        // Its expected we will have gaps
                        continue;
                    }

                    if (td.isController()) {
                        continue;
                    }

                    //errorReporter.messageReport("Writing entity: " + td.getTool().getName());
                    sw.write("<!-- Begin entity: " + td.getEntityID());
                    sw.write("-->\n");
                    writeEntity(td, null, result);
                    sw.write("<!-- End entity: " + td.getEntityID());
                    sw.write("-->\n");
                }

                String input = sw.toString();

                String modString = removeXMLHeader(input);

                fw.write(modString);
                fw.write("</Scene>\n");
                fw.write("</X3D>\n");
            }
            fw.close();
        } catch (IOException ioe) {
            errorReporter.errorReport("IO Error.", ioe);
        }
    }

    /**
     * Write out an entity.
     *
     * @param td The tool data
     * @param substyle The stylesheet version to use. Appends to the normal
     *        "x3d" version.
     * @param result The result to write to
     */
    private void writeEntity(Entity td, String substyle, Result result) {

        if (td instanceof PositionableEntity) {
            ((PositionableEntity)td).getPosition(pos);
            ((PositionableEntity)td).getRotation(rot);
        } else {
            pos = new double[] {0, 0, 0};
            rot = new float[] {0, 0, 0, 0};
        }

        // Element tElement = null;

        Map<String, String> styles = td.getStyleSheets();
        String styleName = "x3d";

        if (substyle != null)
            styleName = styleName + "_" + substyle;

        String ss = styles.get(styleName);

        if (ss == null) {
            if (substyle == null) {
                errorReporter.messageReport("Stylesheet not defined for x3d: "
                        + td.getName());
                return;
            } else if (substyle.equals("global")) {
                // Ignore globals not being here
                return;
            } else if (substyle.equals("view")) {
                ss = styles.get("x3d");
            }
        }

        if (ss != null && ss.equals("NONE")) {
            return;
        }

        File fil = new File(ss);
        if (!fil.exists()) {
            errorReporter.messageReport("Stylesheet does not exist: " + ss + " for "
                    + td.getName());
            return;
        }

        Iterator itr = td.getProperties().entrySet().iterator();
        Map.Entry entry;

        try {
            //errorReporter.messageReport("Using stylesheet: " + ss);
            Transformer trans = StylesheetCache.newTransformer(ss);

            while(itr.hasNext()) {
                entry = (Map.Entry) itr.next();
                Document props = (Document) entry.getValue();

            //System.out.println("Printing X3D DOM");
            //DOMUtils.print(props);
            //System.out.println("Done");
            //System.out.flush();

                DOMSource ds = new DOMSource(props);

                trans.transform(ds, result);
            }
        } catch (TransformerException e) {
            errorReporter.errorReport("Error exporting: " + td.getName(), e);
        }
    }

    /**
     * Write out an entities global state.
     *
     * @param td The tool data
     * @param result The result to write to
     */
    private void writeEntityGlobals(Entity td, Result result) {

        if (td instanceof PositionableEntity) {
            ((PositionableEntity)td).getPosition(pos);
            ((PositionableEntity)td).getRotation(rot);
        } else {
            pos = new double[] {0, 0, 0};
            rot = new float[] {0, 0, 0, 0};
        }
        // Element tElement = null;

        Map<String, String> styles = td.getStyleSheets();
        String ss = styles.get("x3d_global");

        if (ss == null)
            return;

        File fil = new File(ss);
        if (!fil.exists()) {
            return;
        }

        try {
            Transformer trans = StylesheetCache.newTransformer(ss);

            Map<String, Document> properties = td.getProperties();

            // iterate through each property sheet
            Iterator<Map.Entry<String, Document>> index = properties.entrySet()
                    .iterator();
            while (index.hasNext()) {
                Map.Entry<String, Document> mapEntry = index.next();

                // get the key, value pairing
                // String sheetName = mapEntry.getKey();
                Document sheetProperties = mapEntry.getValue();

                // System.out.println("Printing DOM");
                // DOMUtils.print(props);

                DOMSource ds = new DOMSource(sheetProperties);
                trans.transform(ds, result);

            }

        } catch (TransformerException e) {
            errorReporter.errorReport("Error exporting: " + td.getName(), e);
        }
    }

}
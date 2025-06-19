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
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.DOMSource;

// Internal Imports

/**
 * Export a world model into the SMAL format.
 *
 * TODO: Right now this is not exactly SMAL. Includes tool params and segments.
 * Not positive that SMAL is actually the right choice for persistence.
 *
 * @author Alan Hudson
 * @version $Revision: 1.19 $
 */
public class SMALExporter extends Exporter {

    /**
     * Save the current world to the specified file as a SMAL file.
     */
    @Override
    public void export(WorldModel model, Writer fw) {
        super.export(model, fw);

        Entity[] toolValues = model.getModelData();

        try {

            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<SMAL>\n");
            //errorReporter.messageReport("SMAL Models to write: " + toolValues.length);

            for (Entity td : toolValues) {

                if (td != null) {
                    exportEntity(model, td.getEntityID(), "", fw);
                }

            }

            fw.write("</SMAL>\n");
        } catch (IOException e) {
            errorReporter.errorReport(e.getMessage(), e);
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException e) {}
        }
    }

    /**
     * Output a specific entity to the specified stream.
     *
     * @param model The world model to export
     * @param entityID The entity to export
     * @param substyle The stylesheet version to use. Appends to the normal
     *        version.
     * @param fw The stream to write to
     */
    @Override
    public void export(WorldModel model, int entityID, String substyle,
            Writer fw) {

        super.export(model, entityID, substyle, fw);

        try {

            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<SMAL>\n");

            exportEntity(model, entityID, substyle, fw);

            fw.write("</SMAL>\n");
        } catch (IOException e) {
            errorReporter.errorReport(e.getMessage(), e);
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException e) {}
        }
    }

    /**
     * Exports an entity.  No surrounding file format, just the entity data.
     *
     * @param model The world model to export
     * @param entityID The entity to export
     * @param fw The stream to write to
     */
    private void exportEntity(WorldModel model, int entityID, String substyle,
            Writer fw) throws IOException {

        Entity td = model.getEntity(entityID);
        if (td == null) {return;}

        try (StringWriter sw = new StringWriter(1_024)) {
            Result result = new StreamResult(sw);
            Transformer trans = null;
            try {
                TransformerFactory tFactory = TransformerFactory.newInstance();
                trans = tFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                errorReporter.errorReport(e.getMessage(), e);
            }

            sw.write("<!-- Starting entity: ");
            sw.write(td.getEntityID() + " -->\n");
            System.out.println("Exporting entity: " + entityID);
            try {
                Document combined_props = combinePropertySheets(td);
                DOMSource ds = new DOMSource(combined_props);
                trans.transform(ds, result);

                sw.write("<!-- Ending entity: ");
                sw.write(td.getEntityID() + " -->\n");
            } catch (TransformerException e) {
                errorReporter.errorReport("SMAL Export Error!", e);
            }
            String input = sw.toString();
            String modString = removeXMLHeader(input);
            fw.write(modString);
        }
    }
}

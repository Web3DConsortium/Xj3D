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

package org.web3d.parser.x3d;

// External imports
import java.io.*;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.j3d.util.ErrorReporter;

import org.xml.sax.SAXException;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.BaseReader;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.parser.VRMLParserUtils;

import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.x3d.jaxp.X3DErrorHandler;
import org.web3d.x3d.jaxp.X3DSAVAdapter;

/**
 * Implementation of an X3D reader that can be either strict or lenient in
 * the parsing and supports both VRML and XML encoded files.
 * <p>
 * When requested to parse, the reader will open the stream and check to see
 * that we have the right sort of parser.
 * <p>
 * This parser supports the following properties:
 * <ul>
 * <li>"conformance": ["weak", "strict"]. String values. Defaults to weak</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class X3DReader extends BaseReader {

    /** Name of the property specifying the namespace awareness */
    private static final String NAMESPACE_PROP = "useNamespaces";

    /** Name of the property to set the lexical handler in the XMLReader */
    private static final String LEXICAL_HANDLER_PROP =
    "http://xml.org/sax/properties/lexical-handler";

    /** The local relaxed parser instance */
    private X3DRelaxedParser relaxedParser;

    /** The local binary X3D parser instance */
    private X3DBinaryParser binaryParser;

    // private X3DStrictParser strictParser;

    /** The factory to generate SAX parser instances */
    private SAXParserFactory parserFactory;

    /** Adapter used to munge between SAX and SAV calls */
    private X3DSAVAdapter adapter;

    /** Common entity resolver instance */
    private X3DEntityResolver resolver;

    /** SAX Error handler for the system */
    private X3DErrorHandler x3dErrors;

    /**
     * Create a new instance of the reader. Does not initialise anything until
     * we know what sort of input file we have.
     */
    public X3DReader() {
        properties.put(NAMESPACE_PROP, Boolean.FALSE);

        try {
            parserFactory = SAXParserFactory.newInstance();
        } catch(javax.xml.parsers.FactoryConfigurationError fce) {
            throw new FactoryConfigurationError("No SAX parser defined");
        }

        adapter = new X3DSAVAdapter();
        x3dErrors = new X3DErrorHandler();
        resolver = new X3DEntityResolver();
    }

    //---------------------------------------------------------------
    // Methods required by BaseReader
    //---------------------------------------------------------------

    /**
     * Parse a VRML document represented by the given input source. This
     * method should not be called while currently processing a stream. If
     * multiple concurrent streams need to be parsed then multiple instances
     * of this interface should be used.
     *
     * @param input is The input source to be used
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    @Override
    public void parse(final InputSource input)
        throws IOException, VRMLException {

        boolean type_found = false;
        boolean xml_parsing = false;
        boolean useFastInfoSet = false;
        PushbackInputStream pbis = null;

        String realURL = input.getURL();
        String contentType = null;

        if (realURL != null && realURL.endsWith(".x3db")) {
            contentType = "model/x3d+binary";
        }

        // We need to first sniff the stream to work out what is being
        // given to us. The simplest way to do this is to look for the
        // first non-whitespace character. If it is '<' then we know we
        // have an XML file. If it is anything else, treat it as VRML
        // encoded. Do a quick check of the first character of the stream
        // as we're most likely to have the value there. Only on rare
        // occasions should we have any whitespace.

        try {

            InputStream is = AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>) () -> input.getByteStream());

            pbis = new PushbackInputStream(is);

        } catch (PrivilegedActionException pae) {

            String msg = "IO Error while attempting to access file: "
                    + input.getURL();

            throw new IOException(msg, pae.getException());
        }

        if (contentType != null && contentType.equals("model/x3d+binary")) {
            useFastInfoSet = true;
        } else {

            int ch = 0;

            while(!type_found) {
                ch = pbis.read();

                switch(ch) {
                    case '\u00E0':
                        useFastInfoSet = true;
                        type_found = true;
                        break;

                    case '<':
                        xml_parsing = true;
                        type_found = true;
                        break;

                    case '#':
                        xml_parsing = false;
                        type_found = true;
                        break;

                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        break;

                    default:
                        // anything other than whitespace must be the start of the
                        // stream, so we've found something to parse.
                        type_found = true;
                        break;
                }
            }

            // push back the last read character.
            if (ch!=-1)
                pbis.unread(ch);
        }

        String conformance = (String)properties.get(CONFORMANCE_PROP);

        if(xml_parsing) {
            boolean validate = conformance.equals(WEAK_CONFORMANCE);
            Boolean namespace = (Boolean)properties.get(NAMESPACE_PROP);

            parserFactory.setValidating(!validate);
            parserFactory.setNamespaceAware(namespace);

            org.xml.sax.XMLReader reader = null;

            try {
                SAXParser parser = parserFactory.newSAXParser();

                adapter.setLoadState(input.getBaseURL(), input.getURL(), false);

                reader = parser.getXMLReader();
                reader.setContentHandler(adapter);
                reader.setProperty(LEXICAL_HANDLER_PROP, adapter);
                reader.setErrorHandler(x3dErrors);
                reader.setEntityResolver(resolver);
            } catch(ParserConfigurationException | SAXException e) {
                throw new IOException("Unable to configure factory as required");
            }

            // Convert our InputSource, to their InputSource....
            org.xml.sax.InputSource xis = new org.xml.sax.InputSource();
            xis.setByteStream(pbis);

            xis.setEncoding(input.getEncoding());

            try {
                reader.parse(xis);
            } catch(SAXException se) {
                Exception e = se.getException();

                if(e != null)
                    errorReporter.errorReport("Error parsing XML", e);
                else
                    errorReporter.errorReport("Error parsing XML", se);
            } catch(VRMLException ve) {
                errorReporter.errorReport("Error parsing XML", ve);
            }
        } else {

            String base_url = input.getBaseURL();
            String real_url = input.getURL();

            if(real_url == null)
                real_url = "Unknown URL source. Base URL is " + base_url;

            switch (conformance) {
                case WEAK_CONFORMANCE:
                    parseVRMLWeakly(pbis, useFastInfoSet, base_url, real_url);
                    break;
                case STRICT_CONFORMANCE:
                    parseVRMLStrictly(pbis, useFastInfoSet, base_url, real_url);
                    break;
            }
        }
    }

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    @Override
    public void setErrorReporter(ErrorReporter eh) {
        super.setErrorReporter(eh);
        x3dErrors.setErrorReporter(eh);
    }

    /**
     * Set the node handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ch The content handler instance to use
     */
    @Override
    public void setContentHandler(ContentHandler ch) {
        super.setContentHandler(ch);
        adapter.setContentHandler(ch);
    }

    /**
     * Set the script handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param sh The script handler instance to use
     */
    @Override
    public void setScriptHandler(ScriptHandler sh) {
        super.setScriptHandler(sh);
        adapter.setScriptHandler(sh);
    }

    /**
     * Set the proto handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ph The proto handler instance to use
     */
    @Override
    public void setProtoHandler(ProtoHandler ph) {
        super.setProtoHandler(ph);
        adapter.setProtoHandler(ph);
    }

    /**
     * Set the route handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param rh The route handler instance to use
     */
    @Override
    public void setRouteHandler(RouteHandler rh) {
        super.setRouteHandler(rh);
        adapter.setRouteHandler(rh);
    }

    /**
     * Convenience method to build and organise a weak parser.
     *
     * @param is The input source to be used
     * @param baseURL The URL to the base directory of this stream
     * @param realURL the fully qualified URL to the stream
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not X3D UTF8
     *    encoded.
     */
    private void parseVRMLWeakly(InputStream is,
                                 boolean useFastInfoSet,
                                 String baseURL,
                                 String realURL)
        throws IOException, VRMLException {

        // Before we do anything, we'll sanity check the headers if we are
        // supposed to use them
        String[] header = null;
        boolean binary = false;

        if(useFastInfoSet) {
            binary = true;
        } else if(!ignoreHeader) {
            header = VRMLParserUtils.parseFileHeader(is);
            if(header.length != 4)
                throw new VRMLParseException(1, 1, "Header missing #X3D statement.  File: " + realURL);

            // Now check the values of each item
            if(!header[0].equals("#X3D"))
                throw new VRMLParseException(1, 1, "Invalid header. Not a X3D file.  File: " + realURL);

            if((header[1] == null) || !(header[1].equals("V3.0") || header[1].equals("V3.1") || header[1].equals("V3.2") || header[1].equals("V3.3") || header[1].equals("V4.0")))
                throw new VRMLParseException(1, 1, "Unsupported version " + header[1]);

            if((header[2] == null) || !header[2].equals("utf8"))
                throw new VRMLParseException(1, 1, "Unsupported encoding " + header[2]);
        } else {
            header = new String[4];
            header[0] = "#X3D";
            header[1] = "V3.3";
            header[2] = "utf8";
        }

        DocumentLocator loc;

        if (binary) {
            if(binaryParser == null) {
                binaryParser = new X3DBinaryParser(is);
                binaryParser.initialize();
            } else
                binaryParser.ReInit(is);

            binaryParser.setContentHandler(contentHandler);
            binaryParser.setRouteHandler(routeHandler);
            binaryParser.setScriptHandler(scriptHandler);

            binaryParser.setProtoHandler(protoHandler);
            binaryParser.setErrorReporter(errorReporter);
            binaryParser.setDocumentUrl(realURL, baseURL);

            loc = binaryParser.getDocumentLocator();
        } else {
            if(relaxedParser == null) {
                relaxedParser = new X3DRelaxedParser(is);
                relaxedParser.initialize();
            } else
                relaxedParser.ReInit(is);

            relaxedParser.setContentHandler(contentHandler);
            relaxedParser.setRouteHandler(routeHandler);
            relaxedParser.setScriptHandler(scriptHandler);
            relaxedParser.setProtoHandler(protoHandler);
            relaxedParser.setErrorReporter(errorReporter);
            relaxedParser.setDocumentUrl(realURL);

            loc = relaxedParser.getDocumentLocator();
        }

        try {
            if (binary) {
                binaryParser.Scene();
            } else {
                // VRML Encoded files need explicit start and end document calls
                // because the scene parsing process doesn't have them implicit
                // in the document structure like XML has.
                if(contentHandler != null) {
                    contentHandler.setDocumentLocator(loc);

                    // Start document needs to reconstruct the header line to give
                    contentHandler.startDocument(realURL,
                                                 baseURL,
                                                 header[2],
                                                 header[0],
                                                 header[1],
                                                 header[3]);
                }

                relaxedParser.Scene();

                if(contentHandler != null)
                    contentHandler.endDocument();
            }
        } catch(org.web3d.parser.x3d.ParseException pe2) {
            VRMLParseException vpe = new VRMLParseException(loc.getLineNumber(),
                                         loc.getColumnNumber(),
                                         "Error in file: " + realURL + "\n" + pe2.getMessage());


            throw vpe;
        }
    }

    /**
     * Convenience method to build and organise a strict parser.
     *
     * @param is The input source to be used
     * @param baseURL The URL to the base directory of this stream
     * @param realURL the fully qualified URL to the stream
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not X3D UTF8
     *    encoded.
     */
    private void parseVRMLStrictly(InputStream is,
                                   boolean useFastInfoSet,
                                   String baseURL,
                                   String realURL)
        throws IOException, SAVException {

        throw new SAVNotSupportedException("Strict parsing not implemented yet");
    }
}

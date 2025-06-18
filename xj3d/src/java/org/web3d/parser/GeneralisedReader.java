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

package org.web3d.parser;

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

import org.web3d.parser.vrml97.VRML97RelaxedParser;
import org.web3d.parser.x3d.X3DRelaxedParser;
import org.web3d.parser.x3d.X3DBinaryParser;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.parser.BaseReader;
import org.web3d.vrml.parser.VRMLParserUtils;

import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.x3d.jaxp.X3DErrorHandler;
import org.web3d.x3d.jaxp.X3DSAVAdapter;

/**
 * Implementation of a VRML97 reader that can be either strict or lenient in
 * the parsing.
 * <p>
 * When requested to parse, the reader will open the stream and check to see
 * that we have the right sort of parser. If the header does not contain
 * "#VRML V2.0 utf8" then it will generate an exception.
 * </p>
 * This parser supports the following properties:
 * <ul>
 * <li>"conformance": ["weak", "strict"]. String values. Defaults to weak</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.29 $
 */
class GeneralisedReader extends BaseReader {

    /** Name of the property specifying the namespace awareness */
    private static final String NAMESPACE_PROPERTY = "useNamespaces";

    /** Name of the property to set the lexical handler in the XMLReader */
    private static final String LEXICAL_HANDLER_PROPERTY =
    "http://xml.org/sax/properties/lexical-handler";


    /** Message when the header is completely missing */
    private static final String NO_HEADER_MESSAGE =
        "Header missing #VRML or #X3D statement";

    /** The local relaxed VRML97 parser instance */
    private VRML97RelaxedParser vrml97RelaxedParser;

    // private VRML97StrictParser strict97;

    /** The local relaxed X3D parser instance */
    private X3DRelaxedParser x3dRelaxedParser;

    /** The local binary X3D parser instance */
    private X3DBinaryParser x3dBinaryParser;

    // private X3DStrictParser strict03;

    /** The factory to generate SAX parser instances */
    private SAXParserFactory saxParserFactory;

    /** Adapter used to munge between SAX and SAV calls */
    private final X3DSAVAdapter x3dSavAdapter;

    /** Common entity resolver instance */
    private final X3DEntityResolver x3dEntityResolver;

    /** SAX Error handler for the system */
    private final X3DErrorHandler x3dErrorHandler;
    
    /** The assigned SAVAdapter contentHandler */
//    private ContentHandler savContentHandler;

    /**
     * Create a new instance of the reader. Does not initialise anything until
     * we know what sort of input file we have.
     */
    GeneralisedReader() 
    {
        properties.put(NAMESPACE_PROPERTY, Boolean.FALSE);

        try
        {
            saxParserFactory = SAXParserFactory.newInstance();
        } 
        catch(javax.xml.parsers.FactoryConfigurationError fce) 
        {
            throw new FactoryConfigurationError("No SAX parser defined");
        }
        x3dSavAdapter     = new X3DSAVAdapter();
        x3dErrorHandler   = new X3DErrorHandler();
        x3dEntityResolver = new X3DEntityResolver();
    }

    //---------------------------------------------------------------
    // Methods required by BaseReader
    //---------------------------------------------------------------

    @Override
    public void parse (final InputSource inputSource) throws IOException, VRMLException
    {
        boolean type_found     = false;
        boolean xml_parsing    = false;
        boolean useFastInfoSet = false;
        PushbackInputStream pushbackInputStream = null;

        String realURL = inputSource.getURL();
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

        try 
        {
            // TODO fix deprecation
            InputStream inputStream = AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>) () -> inputSource.getByteStream());
            pushbackInputStream = new PushbackInputStream(inputStream);
        } 
        catch (PrivilegedActionException pae) 
        {
            String message = "IO Error while attempting to access file: " + inputSource.getURL();
            throw new IOException(message, pae.getException());
        }

        if (contentType != null && contentType.equals("model/x3d+binary")) 
        {
            useFastInfoSet = true;
        }
        else 
        {
            int ch = 0;

            while (!type_found)
            {
                ch = pushbackInputStream.read();

                switch(ch) {
                    case '\u00E0':
                        useFastInfoSet = true;
                        type_found = true;
                        break;

                    case '<':
                        xml_parsing = true;
                        type_found = true;
                        break;

                    case '#':                // parsing VRML syntax
                        xml_parsing = false;
                        type_found = true;
                        break;

                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        break; // skip through initial whitespace, repeating until first non-whitespace character found

                    default:
                        // anything other than whitespace must be the start of the
                        // stream, so we've found something else to parse.
                        type_found = true;
                        break;
                }
            }

            // push back the previously read non-whitespace character.
            if (ch != -1)
                pushbackInputStream.unread(ch);
        }

        String conformance = (String)properties.get(CONFORMANCE_PROP);

        if (xml_parsing) 
        {
            boolean validate = !conformance.equals(WEAK_CONFORMANCE);

            Boolean namespace = (Boolean)properties.get(NAMESPACE_PROPERTY);

            saxParserFactory.setValidating(validate);
            saxParserFactory.setNamespaceAware(namespace);

            org.xml.sax.XMLReader saxXMLreader = null;

            try
            {
                SAXParser saxParser = saxParserFactory.newSAXParser();

                x3dSavAdapter.setLoadState(inputSource.getBaseURL(), inputSource.getURL(), false);

                saxXMLreader = saxParser.getXMLReader();
                saxXMLreader.setContentHandler(x3dSavAdapter);
                saxXMLreader.setProperty(LEXICAL_HANDLER_PROPERTY, x3dSavAdapter);
                saxXMLreader.setErrorHandler(x3dErrorHandler);
                saxXMLreader.setEntityResolver(x3dEntityResolver);
            } 
            catch (ParserConfigurationException | SAXException e) 
            {
                e.printStackTrace(System.err);
                throw new IOException("Unable to configure factory as required");
            }
            // Convert our InputSource, to their InputSource....
            org.xml.sax.InputSource saxInputSource = new org.xml.sax.InputSource();
            saxInputSource.setByteStream(pushbackInputStream);
            // TODO warning, input.getEncoding() is null
            // saxInputSource.setEncoding(inputSource.getEncoding());
            saxInputSource.setEncoding("UTF-8");

            try
            {
                // TODO HelloCostaRica 3.3 succeeds, HelloPortugal 4.0 fails due to incorrectly parsing as VRML vice XML
                saxXMLreader.parse(saxInputSource);
            } 
            catch(SAXException se) 
            {
                Exception e = se.getException();
                if (e != null)
                {
                    errorReporter.errorReport("Error parsing XML", e);
                    throw new VRMLException("Failed to parse file");
                } 
                else
                {
                    errorReporter.errorReport("Error parsing XML", se);
                    throw new VRMLException("Failed to parse file");
                }
            }
        }
        else // VRML parsing
        {
            String baseUrl = inputSource.getBaseURL();
            String realUrl = inputSource.getURL();

            if (realUrl == null)
                realUrl = "Unknown URL source. Base URL is " + baseUrl;

            switch (conformance) 
            {
                case WEAK_CONFORMANCE:
                    parseVRMLWeakly(pushbackInputStream, useFastInfoSet, baseUrl, realUrl);
                    break;
                case STRICT_CONFORMANCE:
                    parseVRMLStrictly(pushbackInputStream, baseUrl, realUrl);
                    break;
            }
        }
        pushbackInputStream.close();
    }

    @Override
    public void setErrorReporter(ErrorReporter er) 
    {
                  super.setErrorReporter(er);
        x3dErrorHandler.setErrorReporter(er);
          x3dSavAdapter.setErrorReporter(er);
    }

    @Override
    public void setContentHandler(ContentHandler ch)
    {
                super.setContentHandler(ch);
        x3dSavAdapter.setContentHandler(ch);
    }

    @Override
    public void setScriptHandler(ScriptHandler sh)
    {
                super.setScriptHandler(sh);
        x3dSavAdapter.setScriptHandler(sh);
    }

    @Override
    public void setProtoHandler(ProtoHandler ph)
    {
                super.setProtoHandler(ph);
        x3dSavAdapter.setProtoHandler(ph);
    }

    @Override
    public void setRouteHandler(RouteHandler rh)
    {
                super.setRouteHandler(rh);
        x3dSavAdapter.setRouteHandler(rh);
    }

    /**
     * Convenience method to build and organize a weak parser.
     *
     * @param rdr The input source to be used
     * @param baseURL The URL to the base directory of this stream
     * @param realURL the fully qualified URL to the stream
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    private void parseVRMLWeakly(InputStream inputStream, boolean useFastInfoSet, String baseURL, String realURL)
        throws IOException, VRMLException
    {
        // Before we do anything, we'll sanity check the headers if we are
        // supposed to use them
        String[] header = null;
        boolean vrml_97 = false;
        boolean binary  = false;

        // TODO: It seems like we should be able to sniff the FastInfoset header, but
        //       what should we do when the rdr is null?  Also the underlying FI libraries
        //       expect that header so we need to reintroduce to the stream.

        if (useFastInfoSet)
        {
            binary = true;
        }
        else if (!ignoreHeader) // parseVRMLWeakly
        {
            header = VRMLParserUtils.parseFileHeader(inputStream);

            if (header.length != 4)
                throw new VRMLParseException(1, 1, NO_HEADER_MESSAGE);

            // Now check the values of each item
            if (!header[0].equals("#VRML") && !header[0].equals("#X3D"))
                throw new VRMLParseException(1, 1, "Invalid header. Not a VRML97 or a ClassicVRML file");

            vrml_97 = header[0].equals("#VRML"); // otherwise ClassicVRML

            if ((header[1] == null) || (vrml_97 && !header[1].equals("V2.0")))
                throw new VRMLParseException(1, 5, "Unsupported VRML version " + header[1]);
            if ((header[2] == null) || (!header[2].equals("utf8") && !header[2].equals("binary")))
                throw new VRMLParseException(1, 10, "Unsupported encoding " + header[2]);
        }
        else 
        {
            // if we are going header-less then assume X3D 3.3 format; TODO 4.0
            vrml_97 = false;
            header = new String[4];
            header[0] = "#X3D";
            header[1] = "V3.3"; // TODO is this forcing a default major/minor version?
            header[2] = "utf8";
        }

        DocumentLocator documentLocator;

        if (vrml_97) 
        {
            if  (vrml97RelaxedParser == null)
            {
                 vrml97RelaxedParser = new VRML97RelaxedParser(inputStream);
                 vrml97RelaxedParser.initialize();
            }
            else vrml97RelaxedParser.ReInit(inputStream);

            vrml97RelaxedParser.setContentHandler(contentHandler);
            vrml97RelaxedParser.setRouteHandler(routeHandler);
            vrml97RelaxedParser.setScriptHandler(scriptHandler);
            vrml97RelaxedParser.setProtoHandler(protoHandler);
            vrml97RelaxedParser.setErrorReporter(errorReporter);
            vrml97RelaxedParser.setDocumentUrl(realURL);

            documentLocator = vrml97RelaxedParser.getDocumentLocator();
        }
        else if (binary)
        {
            if  (x3dBinaryParser == null)
            {
                 x3dBinaryParser = new X3DBinaryParser(inputStream);
                 x3dBinaryParser.initialize();
            } 
            else x3dBinaryParser.ReInit(inputStream);

            x3dBinaryParser.setContentHandler(contentHandler);
            x3dBinaryParser.setRouteHandler(routeHandler);
            x3dBinaryParser.setScriptHandler(scriptHandler);
            x3dBinaryParser.setProtoHandler(protoHandler);
            x3dBinaryParser.setErrorReporter(errorReporter);
            x3dBinaryParser.setDocumentUrl(realURL, baseURL);

            documentLocator = x3dBinaryParser.getDocumentLocator();
        }
        else // ClassicVRML
        {
            if  (x3dRelaxedParser == null) 
            {
                 x3dRelaxedParser = new X3DRelaxedParser(inputStream);
                 x3dRelaxedParser.initialize();
            } 
            else x3dRelaxedParser.ReInit(inputStream);

            x3dRelaxedParser.setContentHandler(contentHandler);
            x3dRelaxedParser.setRouteHandler(routeHandler);
            x3dRelaxedParser.setScriptHandler(scriptHandler);
            x3dRelaxedParser.setProtoHandler(protoHandler);
            x3dRelaxedParser.setErrorReporter(errorReporter);
            x3dRelaxedParser.setDocumentUrl(realURL);

            documentLocator = x3dRelaxedParser.getDocumentLocator();
        }

        try {
            // VRML Encoded files need explicit start and end document calls
            // because the scene parsing process doesn't have them implicit
            // in the document structure like XML has.

            if (binary) {
                x3dBinaryParser.Scene();
            } 
            else if (vrml_97) 
            {
                if (contentHandler != null) {
                    contentHandler.setDocumentLocator(documentLocator);

                    // Start document needs to reconstruct the header line to give
                    contentHandler.startDocument(realURL,
                                                 baseURL,
                                                 header[2],
                                                 header[0],
                                                 header[1],
                                                 header[3]);
                }
                try {
                    vrml97RelaxedParser.Scene();
                } 
                catch (org.web3d.parser.vrml97.TokenMgrError tme1)
                {
                    String msg = tme1.getMessage();
                    if (msg.contains("<EOF> after"))
                    {
                        if (contentHandler != null)
                            contentHandler.endDocument();
                        return;
                    }
                    throw tme1;
                }
                if (contentHandler != null)
                    contentHandler.endDocument();
            } 
            else // ClassicVRML
            {
                if (contentHandler != null) {
                    contentHandler.setDocumentLocator(documentLocator);

                    // Start document needs to reconstruct the header line to give
                    contentHandler.startDocument(realURL,
                                                 baseURL,
                                                 header[2],
                                                 header[0],
                                                 header[1],
                                                 header[3]);
                }
                try {
                    x3dRelaxedParser.Scene();
                }
                catch (org.web3d.parser.x3d.TokenMgrError tme1)
                {
                    String msg = tme1.getMessage();
                    if (msg.contains("<EOF> after")) {
                        if (contentHandler != null)
                            contentHandler.endDocument();

                        return;
                    }
                    throw tme1;
                }
                if (contentHandler != null)
                    contentHandler.endDocument();
            }
        } 
        catch(org.web3d.parser.vrml97.ParseException | org.web3d.parser.x3d.ParseException e) 
        {
            VRMLParseException vpe = new VRMLParseException(documentLocator.getLineNumber(),
                                         documentLocator.getColumnNumber(),
                                         "Error in file: " + realURL + "\n" + e.getMessage());
            throw vpe;
        }
    }

    /**
     * Convenience method to build and organize a strict parser.
     *
     * @param inputSource The input source to be used
     * @param baseURL The URL to the base directory of this stream
     * @param realURL the fully qualified URL to the stream
     * @throws IOException An I/O error while reading the stream
     * @throws SAVException The input file is not VRML97 UTF8 encoded.
     */
    protected void parseVRMLStrictly (InputStream inputSource, String baseURL, String realURL)
        throws IOException, SAVException
    {
        throw new SAVNotSupportedException("Strict VRML parsing not implemented yet");
    }
}

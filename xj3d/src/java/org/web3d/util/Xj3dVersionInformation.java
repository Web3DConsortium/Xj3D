/*
Copyright (c) 1995-2025 held by the author(s).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Web3D Consortium (https://www.web3d.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.web3d.util;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Creates the version stamp information when built via regex updates from Ant
 * @version $Id: Xj3dVersionInformation.java 12564M 2019-12-13 20:12:38Z (local) $
 * @author <a href="mailto:terry.norbraten@gmail.com?subject=org.web3d.util.Xj3dVersionInformation">Terry Norbraten</a>
 */
@SuppressWarnings("StaticNonFinalUsedInInitialization")
public class Xj3dVersionInformation {

    /** These must be manually changed by the developer in build.properties */
    public static final String BUILD_MAJOR_VERSION = getProjectProperties().getString("product.version.major");          // TODO update
    public static final String BUILD_MINOR_VERSION = getProjectProperties().getString("product.version.level") + "-nps"; // TODO -web3d
    public static final String JAVA_VERSION        = System.getProperty("java.version");

    /** These strings are set by the timestamp task in the build file
     * so need not be modified here
     */
    public static final String BUILD_DSTAMP = "20251223";
    public static final String BUILD_TSTAMP = "2300";
    public static final String BUILD_TODAY  = "23 December 2025";

    public static final String SP = " ";
    public static final String PERIOD = ".";

    /** Property in xj3d.properties */
    private static final String DIS_VERSION = getProjectProperties().getString("dis.version");
    public  static final String OPEN_DIS_VERSION = "opendis" + DIS_VERSION + "-java library for IEEE DIS version " + DIS_VERSION;

    /** Customizable message to be displayed, must first be initialized by class constructor */
    public static String DEVELOPER_CUSTOM_MESSAGE;

    /** Customizable message to be displayed, must first be initialized by class constructor */
    public static String AVIATRIX3D_VERSION;

    /** Customizable message to be displayed, must first be initialized by class constructor */
    public static String JOGL_VERSION;

    // https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
    // https://stackoverflow.com/questions/5103121/how-to-find-the-jvm-version-from-a-program
    // https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/lang/System.html#getProperties()
    public static final String OS_JAVA_VERSION_MESSAGE  = "   Operating system: " + System.getProperty("os.name") + SP + System.getProperty("os.version") + "\n" +
                                                          "   Java environment: " + System.getProperty("java.runtime.name") + SP + JAVA_VERSION;
    /**
     * The release version, must first be initialized by class constructor. Milestone format will be
     * <code>M<i>MainVersion</i>_<i>DevRelease#</i></code>
     */
    public static       String XJ3D_VERSION;

    /**
     * Static constructor block to initialize exception-prone constants
     * https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     * https://stackoverflow.com/questions/2420389/static-initialization-blocks
     * with jogamp code available at
     * https://jogamp.org/deployment/jogamp-next/fat
     */
    static
    {
        org.j3d.aviatrix3d.Aviatrix3dVersion aviatrix3dVersion = org.j3d.aviatrix3d.Aviatrix3dVersion.getInstance();
        if  (aviatrix3dVersion != null)
        {
            AVIATRIX3D_VERSION = 
                aviatrix3dVersion.getExtensionName() + ", " + 
                aviatrix3dVersion.getSpecificationTitle() + ", " + 
                aviatrix3dVersion.getSpecificationVendor() + ", version " + 
                aviatrix3dVersion.getSpecificationVersion();
        }
//           AVIATRIX3D_VERSION = org.j3d.aviatrix3d.Aviatrix3dVersion.getInstance().toString(); // includes many null values
        else AVIATRIX3D_VERSION = "(*** error, org.j3d.aviatrix3d.Aviatrix3dVersion not found)";

        com.jogamp.opengl.JoglVersion joglVersion = com.jogamp.opengl.JoglVersion.getInstance();
        if  (joglVersion != null)
        {
            JOGL_VERSION = 
                joglVersion.getExtensionName() + ", " + 
                joglVersion.getSpecificationTitle() + ", " + 
                joglVersion.getSpecificationVendor() + ", version " + 
                joglVersion.getSpecificationVersion();
        }
//           JOGL_VERSION =  com.jogamp.opengl.JoglVersion.getInstance().toString(); // includes many null values
        else JOGL_VERSION = "(*** error, com.jogamp.opengl.JoglVersion not found)";

        DEVELOPER_CUSTOM_MESSAGE = "utilizing " + 
            "\n   " + OPEN_DIS_VERSION + 
            "\n   " + AVIATRIX3D_VERSION +
            "\n   " + JOGL_VERSION;

        XJ3D_VERSION = "v" + BUILD_MAJOR_VERSION + PERIOD +
            BUILD_MINOR_VERSION + SP +
            DEVELOPER_CUSTOM_MESSAGE + "\n" +
	    OS_JAVA_VERSION_MESSAGE  + "\n" +
            "   " + "BuildStamp time and date:" + SP + BUILD_TSTAMP + SP + "on" + SP + BUILD_TODAY;
    }
    
    /**
     * <p>Project specific Project properties resourced from
     * config/xj3d.properties. These are not expected to dynamically
     * change during runtime.</p>
     * @return specific Project properties resourced from config/xj3d.properties
     */
    public static ResourceBundle getProjectProperties() {
        return PropertyResourceBundle.getBundle("config.xj3d");
    }

    /**
     * Command line entry point for this class
     * @param args command line arguments if any
     */
    public static void main(String args[]) {
        System.out.println("Xj3D" + SP + XJ3D_VERSION);
    }

} // end class file Xj3dVersionInformation.java

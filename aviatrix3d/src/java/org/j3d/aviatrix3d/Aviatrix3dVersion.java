/* Aviatrix3D Licensing information

This codebase contains a lot of code, licensed under a number of different
licenses, dependent on the intended use. The text for each license may be
found in the same directory as this file is located.

Each directory is given relative to the root of the tree as checked out
from CVS.

GNU LGPL (LGPL.txt):
  All code under the /src directory
  All code under the /make directory
  All code under the /test directory
  Most of the pre-compiled libraries contained under /lib
    (need more specific detail on this)

BSD (BSD.txt):
  All code under /examples
  All code under /bin

Documents under /docs are not currently assigned a license, thus
redistributable under standard copyright terms.

Files in /contrib each have their own license. Please consult the source
to find out in each specific case.

This code makes reference to and includes a collection of prebuilt code 
from other j3d.org projects. Specifically the SWT OpenGL bindings. The 
source for that can be found at http://opengl.j3d.org/swt/ and is licensed
under the BSD License. That in turn is derived from Sun's BSD licensed
JSR 231 reference implementation. 
*/
package org.j3d.aviatrix3d;

import com.jogamp.common.util.JogampVersion;
import com.jogamp.common.util.VersionUtil;

import java.util.jar.Manifest;

/** Show the jar manifest contents to the caller.
 * 
 * Borrowed from com.jogamp.opengl.JoglVersion
 *
 * @author <a href="mailto:tdnorbra@nps.edu">Terry D. Norbraten</a>
 */
public class Aviatrix3dVersion extends JogampVersion {
    
    protected static volatile Aviatrix3dVersion aviatrix3dVersionInfo;
    
    protected Aviatrix3dVersion(final String packageName, final Manifest mf) {
        super(packageName, mf);
    }
    
    public static Aviatrix3dVersion getInstance() {
        if(null == aviatrix3dVersionInfo) { // volatile: ok
            synchronized(Aviatrix3dVersion.class) {
                if( null == aviatrix3dVersionInfo ) {
                    final String packageName = "org.j3d.aviatrix3d";
                    final Manifest mf = VersionUtil.getManifest(Aviatrix3dVersion.class.getClassLoader(), packageName);
                    aviatrix3dVersionInfo = new Aviatrix3dVersion(packageName, mf);
                }
            }
        }
        return aviatrix3dVersionInfo;
    }
    
    public static void main(final String args[]) {
//        System.err.println(VersionUtil.getPlatformInfo());
        System.err.println(Aviatrix3dVersion.getInstance());
    }
    
}

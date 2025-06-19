                      Running the examples

To compile these examples first have the main codebase compiled and the
ChefX3D JAR file built. Put that JAR file into the lib directory (../lib
from here). Now have your classpath point to the chefx3d_<version>.jar
file during the compilation step. That JAR file automatically pulls in
everything else.

Once you have the local classes compiled, running the code just needs the
following commandline

java -classpath "../chefx3d.2.0.0.jar;." SimpleExample.jar

The classpath separator character will need to change depending on if you
are a unix-derived commandline or MS Windows commandline, but otherwise
should be no different between the two systems.


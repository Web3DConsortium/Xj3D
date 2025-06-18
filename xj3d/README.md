                         Welcome to NPS Xj3D codebase!

* For project overview and links, please see [Savage Developers Guide: NPS Xj3D](https://savage.nps.edu/Savage/developers.html#Xj3D)

* All code in this is Open Source.  Please see [licensing information](docs/LICENSE) for details.

If you have arrived at this file as a result of directly downloading the SVN
archive, we suggest looking at the HowToInstall.html file.  This will
take you through getting the proper packages needed for installation and
compiling the codebase.

If you have arrived here through a pre-built installer for the binaries,
ignore the installation instructions.

This is a simple roadmap to the entire codebase as found in SVN:
<pre>
Directory        Description
---------        -----------
apps             Standalone applications developed with Xj3D
bin              Convenience scripts.
contrib          Externally contributed code. Not maintained as part of
                 the mainline codebase.
   legacy        Support files used by make (deprecated)
docs             Documentation for this project
   arch          Architecture documentation about how the code is put together
   setup         Files that might make your personal setup easier (outdated)
   javadoc       Automatically created when you want to create javadoc
examples         Small example projects
    browser      Examples of how to assemble a customised browser
                 using the internal APIs of Xj3D.
    loader       Examples of using Xj3D as a Java3D Loader (deprecated)
    sai          Examples showing how to use the X3D external SAI
    spec         Examples directly transcribed from the X3D specifications
lib              3rd-party libraries needed to compile and run Xj3D.
natives          Dynamic runtime binaries for interface with native code
nbproject        NetBeans IDE specific project files
parsetest        Files used to test all aspects of the system
src              Source code used to build Xj3D and associated applications,
                 such as the installers. Separate subdirectories by language.
test             JUnit tests for the main codebase
</pre>

Depending on how you got here, some of these directories may not be present
on your machine.

Documentation and Support
--------------------------

The majority of documentation about Xj3D is on a public website. This can
be accessed at

* http://www.xj3d.org

There you will find some tutorials, a FAQ and general documentation about
the project, contributors etc.

If you have problems with Xj3D, please join up and send email to the Web3D
Consortium's Source mailing list. Details on how to sign up can be found
on this page:

* http://www.web3d.org/x3d/workgroups/source.html

If you have found bugs in Xj3D, please check out our bug tracker at

* http://bugzilla.xj3d.org (prior to August 2014)

* https://www.movesinstitute.org/bugzilla (after August 2014, sadly defunct)

* https://gitlab.nps.edu/Savage/xj3d/issues (after December 2019)

to see if someone has already found the same bug as yours.

NPS MOVES is a federally
funded research institute dedicated to advancing best
practices for modeling and simulation, to include open source 3D rendering.
Priority of response on the bugs
may or may not be fast, depending on what the various developers are currently
working on. If you require support, please contact the Naval Postgraduate
School (NPS) Modeling, Virtual Environments and Simulation (MOVES) Institute.

* https://my.nps.edu/web/moves

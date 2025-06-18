#!/usr/bin/env zsh

# Shell file to run Xj3D on macOS
# Usage: sh browser.sh.command -help

SCRIPTDIR=`dirname $0`
cd $SCRIPTDIR

LIB=$SCRIPTDIR/jars
CLASSPATH=.
for i in $LIB/*jar ; do
    CLASSPATH=$CLASSPATH:$i
done
echo CLASSPATH: $CLASSPATH

os=`uname -s`
if [ "$os" = Darwin ]; then
  OS="Mac OS X"
else
  OS="Linux"
fi

MACH=`uname -m`
NATIVES_PATH=./natives/$OS/$MACH
echo NATIVES_PATH: $NATIVES_PATH

# In case JAVA_HOME is not set on the PATH
JAVA_CMD=$(which java)

$JAVA_CMD \
  -Xmx1g \
  -Dorg.web3d.vrml.renderer.common.nodes.shape.useTextureCache=true \
  -Dswing.aatext=true \
  -Dawt.useSystemAAFontSettings=gasp \
  -Dsun.java2d.dpiaware=true \
  -Djava.net.preferIPv4Stack=true \
  -Djava.library.path="$NATIVES_PATH" \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.desktop/sun.awt=ALL-UNNAMED \
  --add-opens java.desktop/sun.java2d=ALL-UNNAMED \
  --add-opens java.base/java.net=ALL-UNNAMED \
  -Dsun.java2d.opengl=true \
  -Dsun.java2d.opengl.fbobject=true \
  -Dapple.awt.graphics.UseQuartz=true \
  -Dapple.laf.useScreenMenuBar=true \
  -Dapple.awt.brushMetalLook=true \
  -cp $CLASSPATH \
  xj3d.filter.CDFFilter Identity $1 $2 $3 $4 $5 $6
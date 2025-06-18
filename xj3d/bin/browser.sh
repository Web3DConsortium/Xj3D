#!/bin/sh

# Shell file to run Xj3D using Apache Ant

SCRIPTDIR=`dirname $0`
cd $SCRIPTDIR

echo browser.args $1
ant -f build.xml -Dargs=$1 run

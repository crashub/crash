#!/bin/sh
#

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set CRASH_HOME if not already set
[ -z "$CRASH_HOME" ] && CRASH_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

for JAR in $CRASH_HOME/lib/*.jar; do
  if [ -z EXT_JARS ]
  then
    EXT_JARS="$JAR"
  else
    EXT_JARS="$EXT_JARS:$JAR"
  fi
done

# Create tmp dir if it does not exist
mkdir -p $CRASH_HOME/tmp

# Hotspot and OpenJDK requires tools.jar in CLASSPATH for VirtualMachine
if [ -z "$JAVA_HOME" ]; then
  JAVA_BIN_PATH=`which java`
  if [ ! -z "$JAVA_BIN_PATH" ]; then
    TOOLS_DIR=`dirname $JAVA_BIN_PATH`
    TOOLS_DIR=$TOOLS_DIR/../lib/
  fi
else
   TOOLS_DIR=$JAVA_HOME/lib
fi

if [ -n $TOOLS_DIR/tools.jar ]; then
  TOOLS_JAR=$TOOLS_DIR/tools.jar
  CLASSPATH=$TOOLS_JAR
  BOOTCP=-Xbootclasspath/a:$TOOLS_JAR
else
  echo "warning tools.jar can't be find, please ensure JAVA_HOME is set accrordingly"
fi

export CLASSPATH=$CLASSPATH:$CRASH_HOME/bin/crsh.cli-${project.version}.jar:$EXT_JARS

java $BOOTCP -Djava.util.logging.config.file=$CRASH_HOME/conf/logging.properties org.crsh.cli.impl.bootstrap.Main --conf $CRASH_HOME/conf --cmd $CRASH_HOME/cmd "$@"

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
  EXT_JARS="$EXT_JARS --jar $JAR"
done

# Create tmp dir if it does not exist
mkdir -p $CRASH_HOME/tmp

# Java 7 and 8 require tools.jar in CLASSPATH
[ -z "$JAVA_HOME] && CLASSPATH=$JAVA_HOME/lib/tools.jar

export CLASSPATH=$CLASSPATH:$CRASH_HOME/bin/crsh.shell.core-1.0.0-cr2-standalone.jar

java -Djava.util.logging.config.file=$CRASH_HOME/conf/logging.properties org.crsh.standalone.CRaSH $EXT_JARS --cmd $CRASH_HOME/cmd --property crash.vfs.refresh_period=1 $@

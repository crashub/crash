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

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           fi
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
           fi
           ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# For Migwn, ensure paths are in UNIX format before anything is touched
if $mingw ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME="`(cd "$JAVA_HOME"; pwd)`"
  # TODO classpath?
fi

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="`which javac`"
  if [ -n "$javaExecutable" -a ! "`expr \"$javaExecutable\" : '\([^ ]*\)'`" = "no" ]; then
    # readlink(1) is not available as standard on Solaris 10.
    readLink=`which readlink`
    if [ ! `expr "$readLink" : '\([^ ]*\)'` = "no" ]; then
      javaExecutable="`readlink -f \"$javaExecutable\"`"
      javaHome="`dirname \"$javaExecutable\"`"
      javaHome=`expr "$javaHome" : '\(.*\)/bin'`
      JAVA_HOME="$javaHome"
      export JAVA_HOME
    fi
  fi
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="`which java`"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

# Locate tools.jar 
TOOLS_JAR=$JAVA_HOME/lib/tools.jar
if [ -n $JAVA_HOME/lib/tools.jar ]; then
  CLASSPATH=$TOOLS_JAR  
  BOOTCP=-Xbootclasspath/a:$TOOLS_JAR
else
  echo "Warning: tools.jar not found, crash won't works without it."
fi

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

export CLASSPATH=$CLASSPATH:$CRASH_HOME/bin/crash.cli-${project.version}.jar:$EXT_JARS

exec $JAVACMD $BOOTCP \
     -Djava.util.logging.config.file=$CRASH_HOME/conf/logging.properties \
     org.crsh.cli.impl.bootstrap.Main --conf-folder $CRASH_HOME/conf --cmd-folder $CRASH_HOME/cmd  "$@"

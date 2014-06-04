import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.text.Color

import java.util.logging.LogManager
import java.util.logging.LogRecord
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean
import java.util.logging.StreamHandler;
import java.util.regex.Pattern;
import javax.management.ObjectName;
import org.crsh.cli.spi.Completer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy
import org.crsh.cli.Usage
import org.crsh.cli.Option
import org.crsh.cli.Required
import org.crsh.cli.Argument
import org.crsh.cli.Man
import org.crsh.cli.Command
import org.crsh.command.InvocationContext

import org.crsh.cli.completers.EnumCompleter
import org.crsh.cli.spi.Completion
import org.crsh.command.Pipe;

@Usage("java.util.logging commands")
public class jul {

  @Usage("send a message to a jul logger")
  @Man("""\
The send command log one or several loggers with a specified message. For instance the
following impersonates the javax.management.mbeanserver class and send a message on its own
logger.

#% jul send -m hello javax.management.mbeanserver

Send is a <Logger, Void> command, it can log messages to consumed log objects:

% jul ls | jul send -m hello -l warn""")
  @Command
  public Pipe<Logger, Object> send(@MsgOpt String msg, @LoggerArg String name, @LevelOpt Level level) {
    level = level ?: Level.info;
    return new Pipe<Logger, Object>() {
      @Override
      void open() {
        if (name != null) {
          def logger = Logger.getLogger(name);
          level.log(logger, msg);
        }
      }
      @Override
      void provide(Logger element) {
        level.log(element, msg);
      }
    }
  }

  static Collection<String> getLoggers() {
    def names = [] as Set;

    // JDK
    LogManager mgr = LogManager.logManager;
    LoggingMXBean mbean = mgr.loggingMXBean;

    // Add the known names
    names.addAll(mbean.loggerNames);

    // This is a trick to get the logger names per web application in Tomcat environment
    try {
      def registryClass = Thread.currentThread().contextClassLoader.loadClass("org.apache.tomcat.util.modeler.Registry");
      def getRegistry = registryClass.getMethod("getRegistry");
      def registry = getRegistry.invoke(null);
      def server = registry.MBeanServer;
      ObjectName on = new ObjectName("*:j2eeType=WebModule,*");
      def res = server.queryNames(on, null).each {
        def loader = server.getAttribute(it, "loader");
        def oldCL = Thread.currentThread().contextClassLoader;
        try {
          Thread.currentThread().contextClassLoader = loader.classLoader;
          names.addAll(mbean.loggerNames);
        } finally {
          Thread.currentThread().contextClassLoader = oldCL;
        }
      }
    } catch (Exception ignore) {
    }

    //
    return names;
  }

  @Usage("list the available loggers")
  @Man("""\
The jul ls command list all the available loggers, for instance:

% jul ls
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/].[default]
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/eXoGadgetServer].[concat]
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/dashboard].[jsp]
...

The -f switch provides filtering with a Java regular expression

% jul ls -f javax.*
javax.management.mbeanserver
javax.management.modelmbean

The jul ls command is a <Void,Logger> command, therefore any logger produced can be
consumed.""")
  @Command
  public void ls(InvocationContext<Logger> context, @FilterOpt String filter) {

    // Regex filter
    def pattern = Pattern.compile(filter ?: ".*");

    //
    loggers.each {
       def matcher = it =~ pattern;
       if (matcher.matches()) {
         def logger = Logger.getLogger(it);
         context.provide(logger);
       }
    }
  }

  @Usage("create one or several loggers")
  @Command
  public void add(InvocationContext<Logger> context, @LoggerArg List<String> names) {
    names.each {
      if (it.length() > 0) {
        Logger logger = Logger.getLogger(it);
        if (logger != null) {
          context.provide(logger);
        }
      }
    }
  }

  @Man("""\
The set command sets the level of a logger. One or several logger names can be specified as
arguments and the -l option specify the level among the finest, finer, fine, info, warn and
severe levels. When no level is specified, the level is cleared and the level will be
inherited from its ancestors.

% jul set -l trace foo
% jul set foo

The logger name can be omitted and instead stream of logger can be consumed as it is a
<Logger,Void> command. The following set the level warn on all the available loggers:

% jul ls | jul set -l warn""")
  @Usage("configures the level of one of several loggers")
  @Command
  public Pipe<Logger, Object> set(@LoggerArg List<String> names, @LevelOpt @Required Level level) {

    //
    return new Pipe<Logger, Object>() {
      @Override
      void open() {
        names.each() {
          def logger = Logger.getLogger(it);
          level.setLevel(logger)
        }
      }
      @Override
      void provide(Logger element) {
        level.setLevel(element);
      }
    };
  }

  @Man("""\
The tail command provides a tail view of a list of loggers. One or several logger names can
be specified as argument and the -l option configures the level threshold. When no logger
name is specified, the root logger will be tailed, when no level is specified, the info
level will be used:

% jul tail
Feb 10, 2014 1:50:36 PM java_util_logging_Logger\$log call
INFO: HELLO

The tail process will end upon interruption (ctrl-c).""")
  @Usage("tail loggers")
  @Command
  public void tail(
      @Usage("the level treshold")
      @LevelOpt Level level,
      @Usage("the logger names to tail or empty for the root logger")
      @LoggerArg List<String> names,
      InvocationContext<LogRecord> context) {
    if (level == null) {
      level = Level.info;
    }
    def loggers = []
    if (names != null && names.size() > 0) {
      names.each { loggers << Logger.getLogger(it)  }
    } else {
      loggers = [Logger.getLogger("")]
    }
    def handler = new StreamHandler() {
      @Override
      synchronized void publish(LogRecord record) {
        if (record.level.intValue() >= level.value.intValue()) {
          context.provide(record);
          context.flush();
        }
      }
      @Override
      synchronized void flush() {
        context.flush();
      }
      @Override
      void close() throws SecurityException {
        // ?
      }
    };
    loggers.each { it.addHandler(handler); }
    def lock = new Object();
    try {
      synchronized (lock) {
        // Wait until ctrl-c
        lock.wait();
      }
    } finally {
      loggers.each { it.removeHandler(handler); }
    }
  }
}

enum Level {
  finest( java.util.logging.Level.FINEST,  Color.blue),
  finer(  java.util.logging.Level.FINER,   Color.blue),
  fine(   java.util.logging.Level.FINE,    Color.blue),
  info(   java.util.logging.Level.INFO,    Color.white),
  warning(java.util.logging.Level.WARNING, Color.yellow),
  severe( java.util.logging.Level.SEVERE,  Color.red);
  static Level valueOf(java.util.logging.Level level) {
    switch (level.intValue()) {
      case 300:
        return finest;
      case 400:
        return finer;
      case 500:
        return fine;
      case 800:
        return info;
      case 900:
        return warning;
      case 1000:
        return severe;
      default:
        return null;
    }
  }
  final Color color;
  final java.util.logging.Level value;
  Level(java.util.logging.Level value, Color color) {
    this.value = value;
    this.color = color;
  }
  void log(Logger logger, String msg) {
    logger.log(value, msg);
  }
  void setLevel(Logger logger) {
    logger.level = value;
  }
}

class LoggerCompleter implements Completer {

  Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    def builder = new Completion.Builder(prefix);
    log.loggers.each() {
      if (it.startsWith(prefix)) {
        builder.add(it.substring(prefix.length()), true);
      }
    }
    return builder.build();
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the logger level")
@Man("The logger level to assign among {trace, debug, info, warn, error}")
@Option(names=["l","level"],completer=EnumCompleter)
@interface LevelOpt { }

@Retention(RetentionPolicy.RUNTIME)
@Usage("the message")
@Man("The message to log")
@Option(names=["m","message"])
@Required
@interface MsgOpt { }

@Retention(RetentionPolicy.RUNTIME)
@Usage("the logger name")
@Man("The name of the logger")
@Argument(name = "name", completer = LoggerCompleter.class)
@interface LoggerArg { }

@Retention(RetentionPolicy.RUNTIME)
@Usage("a regexp filter")
@Man("A regular expressions used to filter the loggers")
@Option(names=["f","filter"])
@interface FilterOpt { }

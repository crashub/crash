import org.crsh.cli.descriptor.ParameterDescriptor

import java.util.logging.LogManager
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;
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
import org.crsh.command.PipeCommand;

@Usage("logging commands")
public class log {

  @Usage("send a message to a logger")
  @Man("""\
The send command log one or several loggers with a specified message. For instance the following impersonates
the javax.management.mbeanserver class and send a message on its own logger.

#% log send -m hello javax.management.mbeanserver

Send is a <Logger, Void> command, it can log messages to consumed log objects:

% log ls | log send -m hello -l warn""")
  @Command
  public PipeCommand<Logger, Object> send(@MsgOpt String msg, @LoggerArg String name, @LevelOpt Level level) {
    level = level ?: Level.info;
    return new PipeCommand<Logger, Object>() {
      @Override
      void open() {
        if (!isPiped()) {
          if (name != null) {
            def logger = Logger.getLogger(name);
            level.log(logger, msg);
          }
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
The logls command list all the available loggers., for instance:

% logls
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/].[default]
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/eXoGadgetServer].[concat]
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/dashboard].[jsp]
...

The -f switch provides filtering with a Java regular expression

% logls -f javax.*
javax.management.mbeanserver
javax.management.modelmbean

The logls command is a <Void,Logger> command, therefore any logger produced can be consumed.""")

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
The set command sets the level of a logger. One or several logger names can be specified as arguments
and the -l option specify the level among the trace, debug, info, warn and error levels. When no level is
specified, the level is cleared and the level will be inherited from its ancestors.

% logset -l trace foo
% logset foo

The logger name can be omitted and instead stream of logger can be consumed as it is a <Logger,Void> command.
The following set the level warn on all the available loggers:

% log ls | log set -l warn""")
  @Usage("configures the level of one of several loggers")
  @Command
  public PipeCommand<Logger, Object> set(@LoggerArg List<String> names, @LevelOpt @Required Level level) {

    //
    return new PipeCommand<Logger, Object>() {
      @Override
      void open() {
        if (!isPiped()) {
          names.each() {
            def logger = Logger.getLogger(it);
            level.setLevel(logger)
          }
        }
      }
      @Override
      void provide(Logger element) {
        level.setLevel(element);
      }
    };
  }
}

enum Level {
  trace(java.util.logging.Level.FINEST),
  debug(java.util.logging.Level.FINER),
  info(java.util.logging.Level.INFO),
  warn(java.util.logging.Level.WARNING),
  error(java.util.logging.Level.SEVERE) ;
  final java.util.logging.Level value;
  Level(java.util.logging.Level value) {
    this.value = value;
  }
  void log(Logger logger, String msg) {
    logger."${name()}"(msg);
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

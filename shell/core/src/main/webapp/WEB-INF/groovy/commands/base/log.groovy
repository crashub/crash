import org.crsh.cmdline.Argument;
import org.crsh.cmdline.Option;
import org.crsh.cmdline.Command;
import org.crsh.command.ScriptException;
import org.crsh.command.CommandContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.management.ObjectName;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class log extends org.crsh.command.CRaSHCommand implements org.crsh.cmdline.spi.Completer {

  /** The logger methods.*/
  private static final Set<String> methods = ["trace", "debug", "info", "warn", "error"];

  @Command(description="Send a message to a logger")
  public void send(
    CommandContext<Logger, Void> context,
    @Message String msg,
    @LoggerName String name,
    @Level String levelName) throws ScriptException {

    //
    if (levelName == null)
      levelName = "info";

    //
    if (context.piped) {
      context.consume().each() {
        doLog(it, levelName, msg);
      }
    } else {
      if (name != null) {
        def logger = LoggerFactory.getLogger(name);
        doLog(logger, levelName, msg);
      }
    }
  }

  private void doLog(Logger logger, String levelName, String msg) {
    String methodName = levelName.toLowerCase();
    if (!methods.contains(levelName))
      throw new ScriptException("Unrecognized log level $levelName");
    logger."$methodName"(msg);
  }

  private Collection<String> getLoggers() {
    def names = [] as Set;
    def factory = LoggerFactory.ILoggerFactory;
    def factoryName = factory.class.simpleName;
    if (factoryName.equals("JDK14LoggerFactory")) {
      // JDK
      LogManager mgr = LogManager.logManager;
      LoggingMXBean mbean = mgr.loggingMXBean;

      // Add the known names
      names.addAll(mbean.loggerNames);

      // This is a trick to get the logger names per web application in Tomcat environment
      def server = org.apache.tomcat.util.modeler.Registry.registry.MBeanServer;
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
    } else if (factoryName.equals("JBossLoggerFactory")) {
      // JBoss AS
      def f = factory.class.getDeclaredField("loggerMap");
      f.accessible = true;
      def loggers = f.get(factory);
      names.addAll(loggers.keySet());
    } else {
      System.out.println("Implement log lister for implementation " + factory.getClass().getName());
    }

    //
    return names;
  }

  @Command(description="List the available loggers")
  public void ls(CommandContext<Void, Logger> context, @Filter String filter) throws ScriptException {

    // Regex filter
    def pattern = Pattern.compile(filter != null ? filter : ".*");

    //
    def names = loggers;

    //
    names.each {
       def matcher = it =~ pattern;
       if (matcher.matches()) {
         def logger = LoggerFactory.getLogger(it);
         context.produce(logger);
         context.writer.println(it);
       }
    }
  }

  @Command(description="Create one or several loggers")
  public void add(
    CommandContext<Void, Logger> context,
    @Argument(description="The logger names to add") List<String> names) throws ScriptException {
    names.each {
      if (it.length() > 0) {
        Logger logger = LoggerFactory.getLogger(it);
        context.produce(logger);
        context.writer.println(it);
      }
    }
  }

  /** All levels. */
  private static final List<String> levels = ["trace","debug","info","warn","error"];

  @Command(description="Give info about a logger")
  public void info(CommandContext<Logger, Void> context, @LoggerName List<String> names) throws ScriptException {
    if (context.piped) {
      context.consume().each() {
        info(context.writer, it);
      }
    } else {
      names.each() {
        def logger = LoggerFactory.getLogger(it);
        info(context.writer, logger);
      }
    }
  }

  private void info(PrintWriter writer, Logger logger) {
    if (logger != null) {
      for (String level : levels) {
        if (logger[level + "Enabled"]) {
          writer.println(logger.name + "<" + level.toUpperCase() + ">");
          break;
        }
      }
    }
  }

  /** . */
  private static final Set<String> plugins = ["jdk","log4j"];

  /** . */
  private static final Map<String,java.util.logging.Level> julLevels = [
    "trace":"FINEST",
    "debug":"FINER",
    "info":"INFO",
    "warn":"WARNING",
    "error":"SEVERE"
    ];

  /** . */
  private static final Map<String,java.util.logging.Level> log4jLevels = [
    "trace":"TRACE",
    "debug":"DEBUG",
    "info":"INFO",
    "warn":"WARNING",
    "error":"ERROR"
    ];


  @Command(description="Set the level of one of several loggers")
  public void set(
    CommandContext<Logger, Void> context,
    @LoggerName List<String> names,
    @Level String levelName,
    @Plugin String plugin) throws ScriptException {

    //
    def cl = Thread.currentThread().contextClassLoader;

    //
    if (plugin != null) {
      plugin = plugin.toLowerCase();
      if (plugins[plugin] == null)
        throw new ScriptException("Plugin $plugin not supported");
    } else {
      // Auto detect plugin from SLF4J
      def ilfName = LoggerFactory.ILoggerFactory.class.simpleName;
      switch (ilfName) {
        case "JDK14LoggerFactory":
          plugin = "jdk";
          break;
        case "Log4jLoggerFactory":
          plugin = "log4j";
          break;
        case "JBossLoggerFactory":
          // Here we see if we have log4j in the classpath and we use it
          try {
            logManager = cl.loadClass("org.apache.log4j.LogManager");
            plugin = "log4j";
          }
          catch (ClassNotFoundException nf) {
          }
       }
    }

    //
    if (plugin == null)
      throw new ScriptException("No usable plugin");

    // The log invoker
    def invoker;

    // The non portable part
    switch (plugin) {
      case "jdk":
        def f = cl.loadClass("org.slf4j.impl.JDK14LoggerAdapter").getDeclaredField("logger");
        f.accessible = true;
        invoker = { Logger logger ->
          def julLogger = f.get(logger);
          def level = null;
          if (levelName != null) {
            def julLevelName = julLevels[levelName.toLowerCase()];
            if (julLevelName == null)
              throw new ScriptException("Unrecognized level $levelName");
            level = java.util.logging.Level[julLevelName];
          }
          julLogger.level = level;
        };
        break;
      case "log4j":
        invoker = { Logger logger ->
          def l = cl.loadClass("org.apache.log4j.Logger");
          def log4jLogger = l.getLogger(logger.name);
          context.writer.println(log4jLogger);
          def level = null;
          if (levelName != null) {
            def log4jLevelName = log4jLevels[levelName];
            if (log4jLevelName == null)
              throw new ScriptException("Unrecognized level name $levelName");
            level = cl.loadClass("org.apache.log4j.Level")[log4jLevelName];
          }
          log4jLogger.level = level;
        }
        break;
    }

    //
    if (context.piped) {
      context.consume().each() {
        invoker(it);
      }
    } else {
      names.each() {
        def logger = LoggerFactory.getLogger(it);
        invoker(logger);
      }
    }
  }

  public List<String> complete(org.crsh.cmdline.ParameterDescriptor<?> parameter, String prefix) {
    System.out.println("Want completion of parameter " + parameter.annotation);
    if (parameter.annotation instanceof Level) {
      def c = [];
      methods.each() {
        if (it.startsWith(prefix)) {
          c.add(it.substring(prefix.length()));
        }
      }
      return c;
    } else if (parameter.annotation instanceof LoggerName) {
      def c = [];
      loggers.each() {
        if (it.startsWith(prefix)) {
          c.add(it.substring(prefix.length()));
        }
      }
      return c;
    }
    return [];
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Option(names=["l","level"],description="The logger level to assign among {trace, debug, info, warn, error}")
@interface Level { }

@Retention(RetentionPolicy.RUNTIME)
@Option(names=["m","message"],required=true,description="The message to log")
@interface Message { }

@Retention(RetentionPolicy.RUNTIME)
@Argument(description="The logger name")
@interface LoggerName { }

@Retention(RetentionPolicy.RUNTIME)
@Option(names=["f","filter"],description="Filter the logger with a regular expression")
@interface Filter { }

@Retention(RetentionPolicy.RUNTIME)
@Option(names=["p","plugin"],description="Force the plugin implementation")
@interface Plugin { }

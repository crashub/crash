import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.management.ObjectName;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.spi.Completer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class log extends CRaSHCommand implements Completer {

  @Description(display="Send a message to a logger")
  @Command
  public void send(InvocationContext<Logger, Void> context, @MsgOpt String msg, @LoggerArg String name, @LevelOpt Level level) throws ScriptException {
    level = level ?: Level.info;
    if (context.piped) {
      context.consume().each() {
        level.log(it, msg);
      }
    } else {
      if (name != null) {
        def logger = LoggerFactory.getLogger(name);
        level.log(logger, msg);
      }
    }
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
      try {
        def registryClass = Thread.currentThread().contextClassLoader.loadClass("org.apache.tomcat.util.modeler.Registry");
        def getRegistry = registry.getMethod("getRegistry");
        def registry = registry.invoke(null);
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

  @Description("List the available loggers")
  @Command
  public void ls(InvocationContext<Void, Logger> context, @FilterOpt String filter) throws ScriptException {

    // Regex filter
    def pattern = Pattern.compile(filter ?: ".*");

    //
    loggers.each {
       def matcher = it =~ pattern;
       if (matcher.matches()) {
         def logger = LoggerFactory.getLogger(it);
         context.produce(logger);
         context.writer.println(it);
       }
    }
  }

  @Description("Create one or several loggers")
  @Command
  public void add(InvocationContext<Void, Logger> context, @LoggerArg List<String> names) throws ScriptException {
    names.each {
      if (it.length() > 0) {
        Logger logger = LoggerFactory.getLogger(it);
        context.produce(logger);
        context.writer.println(it);
      }
    }
  }

  @Description("Give info about a logger")
  @Command
  public void info(InvocationContext<Logger, Void> context, @LoggerArg List<String> names) throws ScriptException {
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
      for (Level level : Level.values()) {
        if (logger[level.name() + "Enabled"]) {
          writer.println(logger.name + "<" + level.name().toUpperCase() + ">");
          break;
        }
      }
    }
  }

  @Description("Set the level of one of several loggers")
  @Command
  public void set(
    InvocationContext<Logger, Void> context,
    @LoggerArg List<String> names,
    @LevelOpt Level level,
    @PluginOpt Plugin plugin) throws ScriptException {

    //
    plugin = plugin ?: Plugin.autoDetect();
    if (plugin == null)
      throw new ScriptException("No usable plugin");

    //
    if (context.piped) {
      context.consume().each() {
        plugin.setLevel(it, level);
      }
    } else {
      names.each() {
        def logger = LoggerFactory.getLogger(it);
        plugin.setLevel(logger, level);
      }
    }
  }

  public Map<String, String> complete(org.crsh.cmdline.ParameterDescriptor<?> parameter, String prefix) {
    def c = [:];
    if (parameter.annotation instanceof LoggerArg) {
      loggers.each() {
        if (it.startsWith(prefix)) {
          c.put(it.substring(prefix.length()), true);
        }
      }
    }
    return c;
  }
}

enum Plugin {
  jdk , log4j ;

  public static Plugin autoDetect() {
    // Auto detect plugin from SLF4J
    def ilfName = LoggerFactory.ILoggerFactory.class.simpleName;
    switch (ilfName) {
      case "JDK14LoggerFactory":
        return Plugin.jdk;
        break;
      case "Log4jLoggerFactory":
        return Plugin.log4j;
        break;
      case "JBossLoggerFactory":
        // Here we see if we have log4j in the classpath and we use it
        try {
          logManager = cl.loadClass("org.apache.log4j.LogManager");
          return Plugin.log4j;
        }
        catch (ClassNotFoundException nf) {
        }
    }
    return null;
  }

  public void setLevel(Logger logger, Level level) {
    switch (name()) {
      case "jdk":
      def f = Thread.currentThread().getContextClassLoader().loadClass("org.slf4j.impl.JDK14LoggerAdapter").getDeclaredField("logger");
      f.accessible = true;
        def julLogger = f.get(logger);
        julLogger.level = level.jdkObject;
      case "log4j":
        def l = Thread.currentThread().getContextClassLoader().loadClass("org.apache.log4j.Logger");
        def log4jLogger = l.getLogger(logger.name);
        log4jLogger.level = level.log4jObject;
    }
  }
}

enum Level { trace("FINEST","TRACE"), debug("FINER","DEBUG"), info("INFO","INFO"), warn("WARNING","WARNING"), error("SEVERE","ERROR") ;
  final String jdk;
  final String log4j;
  Level(String jdk, String log4j) {
    this.jdk = jdk;
    this.log4j=log4j;
  }
  Object getLog4jObject() {
    return Thread.currentThread().getContextClassLoader().loadClass("org.apache.log4j.Level")[log4j];
  }
  Object getJdkObject() {
    return java.util.logging.Level[jdk];
  }
  void log(Logger logger, String msg) {
    logger."${name()}"(msg);
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Description("The logger level to assign among {trace, debug, info, warn, error}")
@Option(names=["l","level"],completer=org.crsh.cmdline.EnumCompleter)
@interface LevelOpt { }

@Retention(RetentionPolicy.RUNTIME)
@Description("The message to log")
@Option(names=["m","message"],required=true)
@interface MsgOpt { }

@Retention(RetentionPolicy.RUNTIME)
@Description("The logger name")
@Argument
@interface LoggerArg { }

@Retention(RetentionPolicy.RUNTIME)
@Description("Filter the logger with a regular expression")
@Option(names=["f","filter"])
@interface FilterOpt { }

@Retention(RetentionPolicy.RUNTIME)
@Description("Force the plugin implementation")
@Option(names=["p","plugin"])
@interface PluginOpt { }

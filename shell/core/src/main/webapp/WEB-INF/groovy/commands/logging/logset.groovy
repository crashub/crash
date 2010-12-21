import org.crsh.cmdline.Argument;
import org.crsh.cmdline.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Description("Set the level of one of several loggers")
public class logset extends org.crsh.command.CRaSHCommand<Logger, Void> {

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

  @Description("The logger names")
  @Argument
  def List<String> names;

  @Description("The logger level to assign among {trace, debug, info, warn, error}")
  @Option(names=["l","level"])
  def String levelName;

  @Description("Force the plugin implementation")
  @Option(names=["p","plugin"])
  def String plugin;

  /** The log invoker.*/
  def invoker;

  public void execute(CommandContext<Logger, Void> context) throws ScriptException {

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
}


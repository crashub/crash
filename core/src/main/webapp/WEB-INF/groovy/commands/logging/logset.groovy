import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Description("Set the level of one of several loggers")
public class logset extends org.crsh.command.BaseCommand<Logger, Void> {

  private static final Map<String,java.util.logging.Level> julLevels = [
    "trace":java.util.logging.Level.FINEST,
    "debug":java.util.logging.Level.FINER,
    "info":java.util.logging.Level.INFO,
    "warn":java.util.logging.Level.WARNING,
    "error":java.util.logging.Level.SEVERE
    ];

  @Argument(required=false,index=0,usage="The logger names")
  def List<String> names;

  @Option(name="-l",aliases=["--level"],usage="The logger level to assign")
  def String levelName;

  public void execute(CommandContext<Logger, Void> context) throws ScriptException {
    if (context.piped) {
      context.consume().each() {
        configure(it);
      }
    } else {
      names.each() {
        def logger = LoggerFactory.getLogger(it);
        configure(logger);
      }
    }
  }

  private void configure(Logger logger) {
    def simpleName = logger.getClass().getSimpleName();
    if (simpleName.equals("JDK14LoggerAdapter")) {
      def f = logger.getClass().getDeclaredField("logger");
      f.accessible = true;
      def julLogger = f.get(logger);
      def level = null;
      if (levelName != null) {
        level = julLevels[levelName.toLowerCase()];
        if (level == null)
          throw new ScriptException("Unrecognized level $levelName");
      }
      julLogger.level = level;
    } else {
      System.out.println("Implement log set for implementation " + simpleName);
    }
  }
}


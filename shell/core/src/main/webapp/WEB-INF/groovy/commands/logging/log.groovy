import org.crsh.cmdline.Argument;
import org.crsh.cmdline.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Description("Log a message to a logger")
public class log extends org.crsh.command.BaseCommand<Logger, Void> {

  /** The logger methods.*/
  private static final Set<String> methods = ["trace", "debug", "info", "warn", "trace"];

  @Description("The logger name")
  @Argument
  def String name;

  @Description("The logger level (default info)")
  @Option(names=["l","level"])
  def String levelName;

  @Description("The message to log")
  @Option(names=["m","message"],required=true)
  def String msg;

  public void execute(CommandContext<Logger, Void> context) throws ScriptException {

    //
    if (levelName == null)
      levelName = "info";

    //
    if (context.piped) {
      context.consume().each() {
        doLog(it);
      }
    } else {
      if (name != null) {
        def logger = LoggerFactory.getLogger(name);
        doLog(logger);
      }
    }
  }

  private void doLog(Logger logger) {
    String methodName = levelName.toLowerCase();
    if (!methods.contains(levelName))
      throw new ScriptException("Unrecognized log level $levelName");
    logger."$methodName"(msg);
  }
}

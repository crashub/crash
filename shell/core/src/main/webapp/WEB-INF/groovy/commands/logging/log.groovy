import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Description("Log a message to a logger")
public class log extends org.crsh.command.BaseCommand<Logger, Void> {

  /** The logger methods.*/
  private static final Set<String> methods = ["trace", "debug", "info", "warn", "trace"];

  @Argument(required=false,index=0,usage="The logger name")
  def String name;

  @Option(name="-l",aliases=["--level"],usage="The logger level (default info)")
  def String levelName;

  @Option(name="-m",aliases=["--message"],usage="The message to log",required=true)
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

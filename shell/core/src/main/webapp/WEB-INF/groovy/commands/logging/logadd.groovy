import org.crsh.cmdline.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Description("Create one or several loggers")
public class logadd extends org.crsh.command.CRaSHCommand<Void, Logger> {

  @Description("The logger names to add")
  @Argument
  def List<String> names;

  public void execute(CommandContext<Void, Logger> context) throws ScriptException {
    names.each {
      if (it.length() > 0) {
        Logger logger = LoggerFactory.getLogger(it);
        context.produce(logger);
        context.writer.println(it);
      }
    }
  }
}


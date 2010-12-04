import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Description("Create one or several loggers")
public class logadd extends org.crsh.command.BaseCommand<Void, Logger> {

  @Argument(required=false,index=0,usage="The logger names to add")
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


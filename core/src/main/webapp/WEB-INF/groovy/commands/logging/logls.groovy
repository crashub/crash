import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.Collections;

@Description("List the available loggers")
public class logls extends org.crsh.command.BaseCommand<Void, Logger> {

  public void execute(CommandContext<Void, Logger> context) throws ScriptException {

    //
    def names = [];
    def factory = LoggerFactory.ILoggerFactory;
    if (factory.getClass().getSimpleName().equals("JDK14LoggerFactory")) {
      // JDK
      LogManager mgr = LogManager.getLogManager();
      LoggingMXBean mbean = mgr.getLoggingMXBean();
      names = mbean.loggerNames;
    } else {
      System.out.println("Implement log lister for implementation " + factory.getClass().getName());
    }

    //
    names.each {
       def logger = LoggerFactory.getLogger(it);
       context.produce(logger);
       context.writer.println(it);
    }
  }
}


import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.Collections;
import java.util.regex.Pattern;
import java.lang.management.ManagementFactory;
import javax.management.*;

@Description("List the available loggers")
public class logls extends org.crsh.command.BaseCommand<Void, Logger> {

  @Option(name="-f",aliases=["--filter"],usage="Filter the logger with a regular expression", required=false)
  def String filter;

  public void execute(CommandContext<Void, Logger> context) throws ScriptException {

    // Regex filter
    def pattern = Pattern.compile(filter != null ? filter : ".*");

    //
    def names = [] as Set;
    def factory = LoggerFactory.ILoggerFactory;
    if (factory.class.simpleName.equals("JDK14LoggerFactory")) {
      // JDK
      LogManager mgr = LogManager.logManager;
      LoggingMXBean mbean = mgr.loggingMXBean;

      // Add the known names
      names.addAll(mbean.loggerNames);

      // This is a trick to get the logger names per web application in Tomcat environment
      def server = ManagementFactory.platformMBeanServer;
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
    } else {
      System.out.println("Implement log lister for implementation " + factory.getClass().getName());
    }

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
}


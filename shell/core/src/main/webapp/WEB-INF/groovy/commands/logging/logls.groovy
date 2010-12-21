import org.crsh.cmdline.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.management.ObjectName;

@Description("List the available loggers")
public class logls extends org.crsh.command.CRaSHCommand<Void, Logger> {

  @Option(names=["f","filter"])
  @Description("Filter the logger with a regular expression")
  def String filter;

  public void execute(CommandContext<Void, Logger> context) throws ScriptException {

    // Regex filter
    def pattern = Pattern.compile(filter != null ? filter : ".*");

    //
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


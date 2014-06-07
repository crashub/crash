package crash.commands.base;

import org.crsh.cli.Argument;
import org.crsh.cli.Man;
import org.crsh.cli.Command;
import org.crsh.cli.Named;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;
import org.crsh.cli.Required;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.command.Pipe;
import org.crsh.command.ScriptException;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author Julien Viet */
@Usage("Java Management Extensions")
@Man("The jmx commands interracts with the JMX registry allowing several kind JMX operations.")
public class jmx extends BaseCommand {

  @Usage("query mbeans")
  @Man(
      "Create a stream of managed beans, by default the stream will contain all the registered managed beans:\n" +
      "% jmx query\n" +
      "...\n" +
      "The stream can be filtered with the pattern option:\n" +
      "% jmx query -p java.lang:*\n" +
      "..."
  )
  @Command
  public void query(
      InvocationContext<ObjectName> context,
      @Usage("the object name pattern for the query")
      @Argument
      String pattern) throws Exception {

    //
    ObjectName patternName = pattern != null ? ObjectName.getInstance(pattern) : null;
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectInstance> instances = server.queryMBeans(patternName, null);
    for (ObjectInstance instance : instances) {
      context.provide(instance.getObjectName());
    }
  }

  @Usage("provide the mbean info of a managed bean")
  @Man(
      "Provide the mbean info for a managed bean:\n" +
      "% jmx info java.lang:type=ClassLoading \n" +
      "sun.management.ClassLoadingImpl\n" +
      "+- ClassName   sun.management.ClassLoadingImpl\n" +
      "|  Description Information on the management interface of the MBean\n" +
      "+-Descriptor\n" +
      "| +-immutableInfo      true\n" +
      "|   interfaceClassName java.lang.management.ClassLoadingMXBean\n" +
      "|   mxbean             true\n" +
      "+-Attributes\n" +
      "| +-NAME                  TYPE                        DESCRIPTION\n" +
      "|   Verbose               boolean                     Verbose\n" +
      "|   TotalLoadedClassCount long                        TotalLoadedClassCount\n" +
      "|   LoadedClassCount      int                         LoadedClassCount\n" +
      "|   UnloadedClassCount    long                        UnloadedClassCount\n" +
      "|   ObjectName            javax.management.ObjectName ObjectName\n" +
      "+-Operations\n"
  )
  @Command
  @Named("info")
  public MBeanInfo info(@Required @Argument @Usage("a managed bean object name") ObjectName mbean) {
    try {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      return server.getMBeanInfo(mbean);
    }
    catch (JMException e) {
      throw new ScriptException("Could not retrieve mbean " + mbean + "info", e);
    }
  }

  @Usage("get attributes of a managed bean")
  @Man(
      "Retrieves the attributes of a stream of managed beans, this command can be used " +
      "by specifying managed bean arguments\n" +
      "% jmx get java.lang:type=ClassLoading\n" +
      "It also accepts a managed bean stream:\n" +
      "% jmx query -p java.lang:* | jmx get\n" +
      "By default all managed bean attributes will be retrieved, the attributes option allow to " +
      "use a list of attributes:\n" +
      "% jmx query -p java.lang:* | jmx get -a TotalSwapSpaceSize\n"
  )
  @Command
  public Pipe<ObjectName, Map> get(
      @Usage("silent mode ignores any attribute runtime failures")
      @Option(names = {"s","silent"})
      final Boolean silent,
      @Usage("add a column  with the option value with the managed bean name")
      @Option(names = {"n","name"})
      final String name,
      @Usage("specifies a managed bean attribute name")
      @Option(names = {"a","attributes"}) final List<String> attributes,
      @Usage("a managed bean object name")
      @Argument(name = "mbean") final List<ObjectName> mbeans
  ) {

    //
    return new Pipe<ObjectName, Map>() {

      /** . */
      private MBeanServer server;

      /** . */
      private List<ObjectName> buffer;

      @Override
      public void open() throws ScriptException {
        this.server = ManagementFactory.getPlatformMBeanServer();
        this.buffer = new ArrayList<ObjectName>();

        //
        if (mbeans != null) {
          buffer.addAll(mbeans);
        }
      }

      @Override
      public void provide(ObjectName name) throws IOException {
        buffer.add(name);
      }

      @Override
      public void close() throws Exception, IOException {

        // Determine attribute names
        String[] names;
        if (attributes == null) {
          LinkedHashSet<String> tmp = new LinkedHashSet<String>();
          for (ObjectName mbean : buffer) {
            MBeanInfo mbeanInfo;
            try {
              mbeanInfo = server.getMBeanInfo(mbean);
            }
            catch (JMException e) {
              throw new ScriptException(e);
            }
            for (MBeanAttributeInfo attributeInfo : mbeanInfo.getAttributes()) {
              if (attributeInfo.isReadable()) {
                tmp.add(attributeInfo.getName());
              }
            }
          }
          names = tmp.toArray(new String[tmp.size()]);
        } else {
          names = attributes.toArray(new String[attributes.size()]);
        }

        // Produce the output
        for (ObjectName mbean : buffer) {
          LinkedHashMap<String, Object> tuple = new LinkedHashMap<String, Object>();
          if (name != null) {
            tuple.put(name, mbean);
          }
          for (String name : names) {
            Object value;
            try {
              value = server.getAttribute(mbean, name);
            }
            catch (RuntimeMBeanException runtime) {
              if (Boolean.TRUE.equals(silent)) {
                throw new ScriptException(runtime.getCause());
              } else {
                value = null;
              }
            }
            catch (AttributeNotFoundException e) {
              value = null;
            }
            catch (JMException e) {
              throw new ScriptException(e);
            }
            tuple.put(name, value);
          }
          context.provide(tuple);
        }
      }
    };
  }
}

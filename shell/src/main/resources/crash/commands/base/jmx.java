package crash.commands.base;

import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;
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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author Julien Viet */
@Usage("Java Management Extensions")
public class jmx extends BaseCommand {

  @Usage("find mbeans")
  @Command
  public void find(
      InvocationContext<ObjectName> context,
      @Usage("The object name pattern")
      @Option(names = {"p", "pattern"})
      String pattern) throws Exception {

    //
    ObjectName patternName = pattern != null ? ObjectName.getInstance(pattern) : null;
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectInstance> instances = server.queryMBeans(patternName, null);
    for (ObjectInstance instance : instances) {
      context.provide(instance.getObjectName());
    }
  }

  @Usage("get attributes of an MBean")
  @Command
  public Pipe<ObjectName, Map> attributes(@Argument final List<String> attributes) {

    //
    return new Pipe<ObjectName, Map>() {

      /** . */
      private MBeanServer server;

      /** . */
      private List<ObjectName> mbeans;

      @Override
      public void open() throws ScriptException {
        server = ManagementFactory.getPlatformMBeanServer();
        mbeans = new ArrayList<ObjectName>();
      }

      @Override
      public void provide(ObjectName name) throws IOException {
        mbeans.add(name);
      }

      @Override
      public void close() throws ScriptException, IOException {

        // Determine attribute names
        String[] names;
        if (attributes == null) {
          LinkedHashSet<String> tmp = new LinkedHashSet<String>();
          for (ObjectName mbean : mbeans) {
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
        for (ObjectName mbean : mbeans) {
          LinkedHashMap<String, Object> tuple = new LinkedHashMap<String, Object>();
          tuple.put("MBean", mbean);
          for (String name : names) {
            Object value;
            try {
              value = server.getAttribute(mbean, name);
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

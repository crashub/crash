/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package crash.commands.base

import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.CRaSHCommand
import org.crsh.text.ui.UIBuilder
import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.ObjectInstance
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Option
import javax.management.MBeanInfo
import org.crsh.cmdline.annotations.Argument
import javax.management.ObjectName
import org.crsh.command.PipeCommand

@Usage("Java Management Extensions")
class jmx extends CRaSHCommand {

  @Usage("todo")
  @Command
  void ls(
      InvocationContext<ObjectName> context,
       @Usage("The object name pattern")
       @Option(names=["p","pattern"])
       String pattern) {

    //
    ObjectName patternName = pattern != null ? ObjectName.getInstance(pattern) : null;;
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectInstance> instances = server.queryMBeans(patternName, null);
    instances.each { instance ->
      context.provide(instance.objectName)
    }
/*
    if (context.piped) {
    } else {
      UIBuilder ui = new UIBuilder()
      ui.table(columns: [1,3]) {
        row(bold: true, fg: black, bg: white) {
          label("CLASS NAME"); label("OBJECT NAME")
        }
        instances.each { instance ->
          row() {
            label(foreground: red, instance.getClassName()); label(instance.objectName)
          }
        }
      }
      out << ui;
    }
*/
  }

  @Usage("todo")
  @Command
  PipeCommand<ObjectName, Map> get(InvocationContext<Map> context, @Option(names=['a','attributes']) List<String> attributes) {

    // Determine common attributes from all names
    if (attributes == null || attributes.isEmpty()) {
      HashSet<String> tmp = [] as HashSet;
      attributes.each { name ->
        ObjectName on = ObjectName.getInstance(name);
        MBeanInfo info = server.getMBeanInfo(on);
        info.attributes.each { attribute ->
          tmp.add(attribute.name);
        }
      }
      attributes = new ArrayList<String>(tmp);
    }

    //
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    //
    return new PipeCommand<ObjectName, Map>() {
      @Override
      void provide(ObjectName name) {
          def tuple = [:];
          def foo = server.getMBeanInfo(name)
          attributes.each { attribute ->
          String prop = name.getKeyProperty(attribute);
          if (prop != null) {
            tuple[attribute] = prop;
          } else {
            tuple[attribute] = server.getAttribute(name, attribute);
          }
        }
        context.provide(tuple);
      }
   };
  }

}
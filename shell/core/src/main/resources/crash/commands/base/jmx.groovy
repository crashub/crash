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
import org.crsh.shell.ui.UIBuilder
import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.ObjectInstance
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Option
import javax.management.MBeanInfo
import org.crsh.cmdline.annotations.Argument
import javax.management.ObjectName

@Usage("Java Management Extensions")
class jmx extends CRaSHCommand {

  @Usage("todo")
  @Command
  void ls(InvocationContext<Void, ObjectName> context) {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectInstance> instances = server.queryMBeans(null, null);
    if (context.piped) {
      instances.each { instance ->
        context.produce(instance.objectName)
      }
    } else {
      UIBuilder ui = new UIBuilder()
      ui.table() {
        row(decoration: bold, foreground: black, background: white) {
          label("CLASS NAME"); label("OBJECT NAME")
        }
        instances.each { instance ->
          row() {
            label(value: instance.className, foreground: red); label(instance.objectName)
          }
        }
      }
      out << ui;
    }
  }

  @Usage("todo")
  @Command
  void get(
      @Option(names=['a','attributes']) List<String> attributes,
      @Argument List<String> names) {

    MBeanServer server = ManagementFactory.getPlatformMBeanServer();


    // Determine attributes from names
    if (attributes == null) {
      HashSet<String> tmp = [] as HashSet;
      names.each { name ->
        ObjectName on = ObjectName.getInstance(name);
        MBeanInfo info = server.getMBeanInfo(on);
        info.attributes.each { attribute ->
          tmp.add(attribute.name);
        }
      }
      attributes = new ArrayList<String>(tmp);
    }

    UIBuilder ui = new UIBuilder()
    ui.table() {
      row(decoration: bold, foreground: black, background: white) {
        label("OBJECT NAME");
        attributes.each { attribute ->
          label(attribute)
        }
      }
      names.each { name ->
        ObjectName on = ObjectName.getInstance(name);
        row() {
          label(value: on.getCanonicalName(), foreground: red)
          attributes.each { attribute ->
            label(String.valueOf(server.getAttribute(on, attribute)))
          }
        }
      }
    }
    out << ui;
  }

}
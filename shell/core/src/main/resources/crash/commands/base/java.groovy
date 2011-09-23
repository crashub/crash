/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 *
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





import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Required
import org.crsh.shell.ui.UIBuilder

class java extends CRaSHCommand {
  @Usage("print information about a java class")
  @Command
  void clazz(@Usage("The full qualified class name") @Required @Argument String name) throws ScriptException {
    try {
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(name)
      out << "Class $name:\n"

      // Interface hierarchy
      def hierarchy = new UIBuilder()
      this.hierarchy(hierarchy, clazz)
      out.println(hierarchy)

      //
      if (clazz.declaredFields.length > 0)
        out << "declared fields:\n"
      clazz.declaredFields.each() { field ->
        out << "$field.type: $field.name\n";
      }

    } catch (ClassNotFoundException e) {
      out << "Class $name was not found";
    };
  }

  private void hierarchy(UIBuilder builder, Class clazz) {
    builder.node(clazz.name) {
      if (clazz.superclass != null) {
        hierarchy(builder, clazz.superclass);
      }
      def interfaces = clazz.interfaces
      if (interfaces.length > 0) {
        node("interfaces") {
          interfaces.each() { itf ->
            hierarchy(builder, itf);
          }
        }
      }
    }
  }

}
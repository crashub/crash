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
import org.crsh.text.ui.UIBuilder
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Usage("shell related command")
class shell extends CRaSHCommand {

  static def STATUS_MAP = [
      (CRaSHPlugin.CONSTRUCTED):"constructed",
      (CRaSHPlugin.FAILED):"failed",
      (CRaSHPlugin.INITIALIZED):"initialized",
      (CRaSHPlugin.INITIALIZING):"initializing"
  ];

  @Usage("list the loaded plugins and their configuration")
  @Command
  public Object plugins() {
    def builder = new UIBuilder();
    crash.context.plugins.each() { plugin ->
      builder.node("plugin") {
        label("type: $plugin.type")
        label("implementation: ${plugin.type}")
        node("status: ${STATUS_MAP[plugin.status]}")
        node("properties") {
          plugin.configurationCapabilities.each() { desc ->
            node(desc.name) {
              label("description: ${desc.description}")
              label("type: ${desc.description}")
              label("default: ${desc.defaultValue}")
            }
          }
        }
      }
    };
    return builder;
  }

  @Usage("list the configuration properties and their description")
  @Command
  public Object properties() {
    def builder = new UIBuilder();
    PropertyDescriptor.ALL.values().each() { desc ->
      builder.node(desc.name) {
        def prop = crash.context.getProperty(desc);
        label("value: ${prop ?: desc.defaultValue}")
        label("description: $desc.description")
        node("type $desc.type")
        node("default: $desc.defaultValue")
      }
    };
    return builder;
  }
}

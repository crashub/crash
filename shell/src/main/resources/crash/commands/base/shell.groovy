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

import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.text.ui.UIBuilder
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor
import org.crsh.text.Color;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@Usage("shell related command")
class shell {

  static def STATUS_MAP = [
      (CRaSHPlugin.CONSTRUCTED):"constructed",
      (CRaSHPlugin.FAILED):"failed",
      (CRaSHPlugin.INITIALIZED):"initialized",
      (CRaSHPlugin.INITIALIZING):"initializing"
  ];

  static def STATUS_COLOR = [
      (CRaSHPlugin.CONSTRUCTED): Color.blue,
      (CRaSHPlugin.FAILED):Color.red,
      (CRaSHPlugin.INITIALIZED):Color.green,
      (CRaSHPlugin.INITIALIZING):Color.yellow
  ];

  @Usage("list the loaded plugins and their configuration")
  @Command
  public Object plugins() {
    def table = new UIBuilder().table(rightCellPadding: 1) {
      crash.context.plugins.each() { plugin ->
        header(bold: true, fg: black, bg: white) {
          table(rightCellPadding: 1) {
            row {
              label("$plugin.type.simpleName")
              label(fg: STATUS_COLOR[plugin.status], "(${STATUS_MAP[plugin.status]})")
            }
          }
        }
        def capabilities = plugin.configurationCapabilities
        if (capabilities.iterator().hasNext()) {
          row {
            table(columns: [2,2,1,1], rightCellPadding: 1) {
              header {
                label("name"); label("description"); label("type"); label("default")
              }
              capabilities.each { desc ->
                row {
                  label(desc.name); label(desc.description); label(desc.type.simpleName); label(desc.defaultDisplayValue)
                }
              }
            }
          }
        }
      }
    }
    return table;
  }

  @Usage("list the configuration properties and their description")
  @Command
  public Object properties() {
    def capabilities = PropertyDescriptor.ALL.values()
    def table = new UIBuilder().table(rightCellPadding: 1) {
      header(bold: true, fg: black, bg: white) {
        label("name"); label("description"); label("type"); label("value"); label("default")
      }
      capabilities.each { desc ->
        def property = crash.context.propertyManager.getProperty(desc);
        String value = property != null ? property.displayValue : "";
        row {
          label(desc.name); label(desc.description); label(desc.type.simpleName); label(value); label(desc.defaultValue)
        }
      }
    }
    return table
  }
}

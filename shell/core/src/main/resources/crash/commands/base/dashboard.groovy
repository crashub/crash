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

import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Option
import org.crsh.text.ui.UIBuilder

class dashboard extends CRaSHCommand {
  @Usage("display and update sorted information about processes")
  @Command
  void main(
      @Usage("Set the delay between updates to <delay> seconds. The default delay between updates is 1 second.")
      @Option(names = 's') Integer delay) {
    if (delay == null)
      delay = 1
    if (delay < 0)
      throw new ScriptException("Cannot provide negative time value $delay");
    while (!Thread.interrupted()) {
      out.cls()

      UIBuilder ui = new UIBuilder()
      ui.table(weights: [3,1,1], border: dashed) {
        header(bold: true, fg: black, bg: white) {
          label("top");
          label("vm");
          label("env");
        }
        row {
          eval {
            thread.ls();
          }
          eval {
            system.propls f:'java.vm.*';
          }
          eval {
            env();
          }
        }
      }
      out << ui;

      out.flush();
      Thread.sleep(delay * 1000);
    }
  }
}

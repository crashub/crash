/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.standalone;

import org.crsh.Processor;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.term.ConsoleTerm;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Main {

  public static void main(String[] args) {

    ConsoleTerm term = new ConsoleTerm();
    Processor processor = new Processor(term, new Shell() {
      public String getWelcome() {
        return "Welcome";
      }

      public String getPrompt() {
        return "% ";
      }

      public void process(String request, ShellProcessContext processContext) {
        processContext.begin(new ShellProcess() {
          public void cancel() {
          }
        });
        if ("bye".equals(request)) {
          processContext.end(new ShellResponse.Close());
        } else {
          processContext.end(new ShellResponse.Display(request));
        }
      }

      public List<String> complete(String prefix) {
        return Collections.emptyList();
      }
    });

    //
    processor.run();
  }
}

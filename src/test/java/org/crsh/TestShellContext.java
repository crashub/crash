/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.crsh;

import org.crsh.shell.ShellContext;
import org.crsh.util.IO;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestShellContext implements ShellContext {

  /** . */
  private final Map<String, String> commands = new HashMap<String, String>();

  public void addCommand(String name, String script) {
    commands.put("/commands/" + name + ".groovy", script);
  }

  public String loadScript(String resourceId) {
    String s = commands.get(resourceId);
    if (s == null) {
      // Remove leading '/'
      resourceId = resourceId.substring(1);
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceId);
      s = in != null ? IO.readAsUTF8(in) : null;
    }
    return s;
  }

  public ClassLoader getLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}

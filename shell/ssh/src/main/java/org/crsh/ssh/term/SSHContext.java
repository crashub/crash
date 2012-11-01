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
package org.crsh.ssh.term;

import org.apache.sshd.common.PtyMode;
import org.apache.sshd.server.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHContext {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(SSHContext.class);

  /** . */
  public final int verase;

  /** . */
  private final Environment env;

  public SSHContext(Environment env) {
    if (env == null) {
      throw new NullPointerException("No null env");
    }

    //
    Integer verase = env.getPtyModes().get(PtyMode.VERASE);

    //
    this.env = env;
    this.verase = verase != null ? verase : -1;
  }

  public int getWidth() {
    String s = env.getEnv().get(Environment.ENV_COLUMNS);
    int width = 0;
    if (s != null) {
      try {
        width = Integer.parseInt(s);
      }
      catch (NumberFormatException e) {
        log.warn("Could not parse ssh term width " + s);
      }
    }
    return width;
  }

  public int getHeight() {
    String s = env.getEnv().get(Environment.ENV_LINES);
    int height = 0;
    if (s != null) {
      try {
        height = Integer.parseInt(s);
      }
      catch (NumberFormatException e) {
        log.warn("Could not parse ssh term height " + s);
      }
    }
    return height;
  }

  public String getProperty(String key)
  {
    return env.getEnv().get(key);
  }
}

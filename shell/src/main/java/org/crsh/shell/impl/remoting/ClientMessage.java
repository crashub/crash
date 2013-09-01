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

package org.crsh.shell.impl.remoting;

import java.io.Serializable;

public abstract class ClientMessage implements Serializable {

  public static class GetWelcome extends ClientMessage {
  }

  public static class GetPrompt extends ClientMessage {
  }

  public static class GetCompletion extends ClientMessage {

    /** . */
    public final String prefix;

    public GetCompletion(String prefix) {
      this.prefix = prefix;
    }
  }

  public static class SetSize extends ClientMessage {

    /** . */
    public final int width;

    /** . */
    public final int height;

    public SetSize(int width, int height) {
      this.width = width;
      this.height = height;
    }
  }

  public static class Execute extends ClientMessage {

    /** . */
    public final int width;

    /** . */
    public final int height;

    /** . */
    public final String line;

    public Execute(int width, int height, String line) {
      this.width = width;
      this.height = height;
      this.line = line;
    }
  }

  public static class Cancel extends ClientMessage {
  }

  public static class Close extends ClientMessage {
  }
}

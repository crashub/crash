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
package org.crsh.term;

import junit.framework.Assert;

import java.io.IOException;

public abstract class IOEvent {

  public final void assertEquals(IOEvent event) {
    Assert.assertEquals(this, event);
  }

  public static class IO extends IOEvent {

    /** . */
    private final int code;

    /** . */
    private final CodeType type;

    public IO(char c) {
      this(c, CodeType.CHAR);
    }

    public IO(CodeType type) {
      this(-1, type);

      //
      if (type == CodeType.CHAR) {
        throw new IllegalArgumentException();
      }
    }

    public IO(int code, CodeType type) {
      this.code = code;
      this.type = type;
    }

    public int getCode() {
      return code;
    }

    public CodeType getType() {
      return type;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      } else if (obj instanceof IO) {
        IO that = (IO)obj;
        if (type == that.type) {
          return type != CodeType.CHAR || code == that.code;
        }
      }
      return false;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[code=" + code + ",type=" + type + "]";
    }
  }

  public static class Error extends IOEvent {

    /** . */
    private final IOException cause;

    public Error(IOException cause) {
      this.cause = cause;
    }

    public IOException getCause() {
      return cause;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[cause=" + cause.getMessage() + "]";
    }
  }
}

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

public abstract class IOAction {

  public static IOAction write(String s) {
    return new Write(s);
  }

  public static IOAction crlf() {
    return new CRLF();
  }

  public static IOAction flush() {
    return new Flush();
  }

  public static IOAction read() {
    return new Read();
  }

  public static IOAction end() {
    return new End();
  }

  public static IOAction close() {
    return new Close();
  }

  public static IOAction del() {
    return new Del();
  }

  public static IOAction left() {
    return new Left();
  }

  public static class Write extends IOAction {

    /** . */
    public final String s;

    public Write(String s) {
      this.s = s;
    }
  }

  public static class CRLF extends IOAction {
    private CRLF() {
    }
  }

  public static class Del extends IOAction {
    private Del() {
    }
  }

  public static class Left extends IOAction {
    private Left() {
    }
  }

  public static class Flush extends IOAction {
    private Flush() {
    }
  }

  public static class Read extends IOAction {
    private Read() {
    }
  }

  public static class End extends IOAction {
    private End() {
    }
  }

  public static class Close extends IOAction {
    private Close() {
    }
  }
}

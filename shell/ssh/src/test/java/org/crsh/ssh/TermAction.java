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
package org.crsh.ssh;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class TermAction {

  public static TermAction write(String s) {
    return new Write(s);
  }

  public static TermAction crlf() {
    return new CRLF();
  }

  public static TermAction flush() {
    return new Flush();
  }

  public static TermAction read() {
    return new Read();
  }

  public static TermAction end() {
    return new End();
  }

  public static TermAction close() {
    return new Close();
  }

  public static TermAction del() {
    return new Del();
  }

  public static class Write extends TermAction {

    /** . */
    public final String s;

    public Write(String s) {
      this.s = s;
    }
  }

  public static class CRLF extends TermAction {
    private CRLF() {
    }
  }

  public static class Del extends TermAction {
    private Del() {
    }
  }

  public static class Flush extends TermAction {
    private Flush() {
    }
  }

  public static class Read extends TermAction {
    private Read() {
    }
  }

  public static class End extends TermAction {
    private End() {
    }
  }

  public static class Close extends TermAction {
    private Close() {
    }
  }
}

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

package org.crsh.shell;

import org.crsh.command.ScriptException;

import java.util.Collections;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ShellResponse {

  public abstract String getText();

  public static class SyntaxError extends ShellResponse {

    public SyntaxError() {
    }

    @Override
    public String getText() {
      return "Syntax error";
    }
  }

  public static class UnknownCommand extends ShellResponse {

    /** . */
    private final String name;

    public UnknownCommand(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String getText() {
      return "Unknown command " + name;
    }
  }

  public static class NoCommand extends ShellResponse {
    @Override
    public String getText() {
      return "Please type something";
    }
  }

  public static class Close extends ShellResponse {

    @Override
    public String getText() {
      return "Have a good day!\r\n";
    }
  }

  /**
   * Command execution is terminated.
   */
  public static class Ok extends ShellResponse {

    /** . */
    private final Iterable<?> produced;

    public Ok() {
      this(Collections.<Object>emptyList());
    }

    public Ok(Iterable<?> produced) {
      this.produced = produced;
    }

    public Iterable<?> getProduced() {
      return produced;
    }

    @Override
    public String getText() {
      return "";
    }
  }

  public static class Display extends Ok {

    /** . */
    private final String text;

    public Display(String text) {
      this.text = text;
    }

    public Display(Iterable<?> produced, String text) {
      super(produced);

      //
      this.text = text;
    }

    @Override
    public String getText() {
      return text;
    }
  }

  public static class Cancelled extends ShellResponse {
    @Override
    public String getText() {
      return "cancelled" ;
    }
  }

  public static class Error extends ShellResponse {

    /** . */
    private final ErrorType type;

    /** . */
    private final Throwable throwable;

    private final String msg;

    public Error(ErrorType type, Throwable throwable) {
      this.type = type;
      this.msg = build(throwable);
      this.throwable = throwable;
    }

    public Error(ErrorType type, String msg) {
      this.type = type;
      this.msg = msg;
      this.throwable = null;
    }

    public Error(ErrorType type, String msg, Throwable throwable) {
      this.type = type;
      this.msg = msg;
      this.throwable = throwable;
    }

    public ErrorType getType() {
      return type;
    }

    public Throwable getThrowable() {
      return throwable;
    }

    @Override
    public String getText() {
      return msg;
    }

    private static String build(Throwable throwable) {
      String result;
      String msg = throwable.getMessage();
      if (msg == null) {
        msg = throwable.getClass().getSimpleName();
      }
      if (throwable instanceof ScriptException) {
        result = "Error: " + msg;
      } else if (throwable instanceof RuntimeException) {
        result = "Unexpected exception: " + msg;
      } else if (throwable instanceof Exception) {
        result = "Unexpected exception: " + msg;
      } else if (throwable instanceof java.lang.Error) {
        result = "Unexpected error: " + msg;
      } else {
        result = "Unexpected throwable: " + msg;
      }
      return result;
    }

    public String toString() {
      return "ShellResponse.Error[type=" + type + ",msg=" + msg + "]";
    }
  }
}

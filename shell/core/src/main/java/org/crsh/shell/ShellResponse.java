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
 * The response of a shell invocation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ShellResponse {

  public static UnknownCommand unknownCommand(String name) {
    return new UnknownCommand(name);
  }

  public static NoCommand noCommand() {
    return new NoCommand();
  }

  public static Ok ok(Iterable<?> produced) {
    return new Ok(produced);
  }

  public static Ok ok() {
    return new Ok();
  }

  public static Display display(String text) {
    return new Display(text);
  }

  public static Display display(Iterable<?> produced, String text) {
    return new Display(produced, text);
  }

  public static Error evalError(Throwable throwable) {
    return new Error(ErrorType.EVALUATION, throwable);
  }

  public static Error evalError(String msg, Throwable throwable) {
    return new Error(ErrorType.EVALUATION, msg, throwable);
  }

  public static Error evalError(String msg) {
    return new Error(ErrorType.EVALUATION, msg);
  }

  public static Error internalError(Throwable throwable) {
    return new Error(ErrorType.INTERNAL, throwable);
  }

  public static Error internalError(String msg, Throwable throwable) {
    return new Error(ErrorType.INTERNAL, msg, throwable);
  }

  public static Error internalError(String msg) {
    return new Error(ErrorType.INTERNAL, msg);
  }

  public static Error error(ErrorType type, Throwable throwable) {
    return new Error(type, throwable);
  }

  public static Error error(ErrorType type, String msg, Throwable throwable) {
    return new Error(type, msg, throwable);
  }

  public static Error error(ErrorType type, String msg) {
    return new Error(type, msg);
  }

  public abstract String getText();

  public static class UnknownCommand extends ShellResponse {

    /** . */
    private final String name;

    private UnknownCommand(String name) {
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

    private NoCommand() {
    }

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

    private Ok() {
      this(Collections.<Object>emptyList());
    }

    private Ok(Iterable<?> produced) {
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

    private Display(String text) {
      this.text = text;
    }

    private Display(Iterable<?> produced, String text) {
      super(produced);

      //
      this.text = text;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Display) {
        Display that = (Display)obj;
        return text.equals(that.text);
      }
      return false;
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

    private Error(ErrorType type, Throwable throwable) {
      this.type = type;
      this.msg = build(throwable);
      this.throwable = throwable;
    }

    private Error(ErrorType type, String msg) {
      this.type = type;
      this.msg = msg;
      this.throwable = null;
    }

    private Error(ErrorType type, String msg, Throwable throwable) {
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

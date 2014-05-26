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

package org.crsh.shell;

import java.io.Serializable;
import java.util.Collections;

public abstract class ShellResponse implements Serializable {

  public static UnknownCommand unknownCommand(String name) {
    return new UnknownCommand(name);
  }

  public static NoCommand noCommand() {
    return NoCommand.INSTANCE;
  }

  public static Ok ok(Iterable<?> produced) {
    return new Ok(produced);
  }

  public static Ok ok() {
    return new Ok();
  }

  public static Error evalError(String msg, Throwable throwable) {
    return new Error(ErrorKind.EVALUATION, msg, throwable);
  }

  public static Error evalError(String msg) {
    return new Error(ErrorKind.EVALUATION, msg);
  }

  public static Error internalError(String msg, Throwable throwable) {
    return new Error(ErrorKind.INTERNAL, msg, throwable);
  }

  public static Error internalError(String msg) {
    return new Error(ErrorKind.INTERNAL, msg);
  }

  public static Error error(ErrorKind type, String msg, Throwable throwable) {
    return new Error(type, msg, throwable);
  }

  public static Error error(ErrorKind type, String msg) {
    return new Error(type, msg);
  }

  public static Cancelled cancelled() {
    return Cancelled.INSTANCE;
  }

  public static Close close() {
    return Close.INSTANCE;
  }

  public abstract String getMessage();

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
    public String getMessage() {
      return name + ": command not found";
    }

    @Override
    public String toString() {
      return "UnknownCommand[" + name + "]";
    }
  }

  public static class NoCommand extends ShellResponse {

    /** . */
    private static final NoCommand INSTANCE = new NoCommand();

    private NoCommand() {
    }

    @Override
    public String getMessage() {
      return "";
    }
  }

  public static class Close extends ShellResponse {

    /** . */
    private static final Close INSTANCE = new Close();

    private Close() {
    }

    @Override
    public String getMessage() {
      return "Have a good day!\r\n";
    }
  }

  /**
   * Command execution is terminated.
   */
  public static class Ok extends ShellResponse {

    /** . */
    private final transient Iterable<?> produced;

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
    public String getMessage() {
      return "";
    }
  }

  public static class Cancelled extends ShellResponse {

    /** . */
    private static final Cancelled INSTANCE = new Cancelled();

    private Cancelled() {
    }

    @Override
    public String getMessage() {
      return "";
    }
  }

  public static class Error extends ShellResponse {

    /** . */
    private final ErrorKind kind;

    /** The throwable. */
    private final Throwable throwable;

    /** . */
    private final String msg;

    private Error(ErrorKind kind, String msg) {
      this.kind = kind;
      this.msg = msg;
      this.throwable = null;
    }

    private Error(ErrorKind kind, String msg, Throwable throwable) {
      this.kind = kind;
      this.msg = msg;
      this.throwable = throwable;
    }

    public ErrorKind getKind() {
      return kind;
    }

    public Throwable getThrowable() {
      return throwable;
    }

    @Override
    public String getMessage() {
      return msg;
    }

    public String toString() {
      return "ShellResponse.Error[kind=" + kind + ",msg=" + msg + "]";
    }
  }
}

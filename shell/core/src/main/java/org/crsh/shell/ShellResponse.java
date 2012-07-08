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

import org.crsh.text.Data;

import java.io.Serializable;
import java.util.Collections;

/**
 * The response of a shell invocation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
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

  public static Display display(String text) {
    return new Display(new Data(text));
  }

  public static Display display(Data data) {
    return new Display(data);
  }

  public static Display display(Iterable<?> produced, Data data) {
    return new Display(produced, data);
  }

  public static Error evalError(String msg, Throwable throwable) {
    return new Error(ErrorType.EVALUATION, msg, throwable);
  }

  public static Error evalError(String msg) {
    return new Error(ErrorType.EVALUATION, msg);
  }

  public static Error internalError(String msg, Throwable throwable) {
    return new Error(ErrorType.INTERNAL, msg, throwable);
  }

  public static Error internalError(String msg) {
    return new Error(ErrorType.INTERNAL, msg);
  }

  public static Error error(ErrorType type, String msg, Throwable throwable) {
    return new Error(type, msg, throwable);
  }

  public static Error error(ErrorType type, String msg) {
    return new Error(type, msg);
  }

  public static Cancelled cancelled() {
    return Cancelled.INSTANCE;
  }

  public static Close close() {
    return Close.INSTANCE;
  }

  public abstract Data getData();

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
    public Data getData() {
      return new Data(name + ": command not found");
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
    public Data getData() {
      return new Data("Please type something");
    }
  }

  public static class Close extends ShellResponse {

    /** . */
    private static final Close INSTANCE = new Close();

    private Close() {
    }

    @Override
    public Data getData() {
      return new Data("Have a good day!\r\n");
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
    public Data getData() {
      return new Data("");
    }
  }

  public static class Display extends Ok {

    /** . */
    private final Data data;

    private Display(Data data) {
      this.data = data;
    }

    private Display(Iterable<?> produced, Data data) {
      super(produced);

      //
      this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Display) {
        Display that = (Display)obj;
        return data.equals(that.data);
      }
      return false;
    }

    @Override
    public Data getData() {
      return data;
    }
  }

  public static class Cancelled extends ShellResponse {

    /** . */
    private static final Cancelled INSTANCE = new Cancelled();

    private Cancelled() {
    }

    @Override
    public Data getData() {
      return new Data("cancelled");
    }
  }

  public static class Error extends ShellResponse {

    /** . */
    private final ErrorType type;

    /** . */
    private final Throwable throwable;

    /** . */
    private final String msg;

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
    public Data getData() {
      return new Data(msg);
    }

    public String toString() {
      return "ShellResponse.Error[type=" + type + ",msg=" + msg + "]";
    }
  }
}

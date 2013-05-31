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

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.shell.ErrorType;
import org.crsh.shell.ShellResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ServerMessage implements Serializable {

  public static class Welcome extends ServerMessage {

    /** . */
    public final String value;

    public Welcome(String value) {
      this.value = value;
    }
  }

  public static class Prompt extends ServerMessage {

    /** . */
    public final String value;

    public Prompt(String value) {
      this.value = value;
    }
  }

  public static class Completion extends ServerMessage {

    /** . */
    public final CompletionMatch value;

    public Completion(CompletionMatch value) {
      this.value = value;
    }
  }

  public static class UseMainBuffer extends ServerMessage {

  }

  public static class UseAlternateBuffer extends ServerMessage {

  }

  public static class GetSize extends ServerMessage {

  }

  public static class ReadLine extends ServerMessage {

  }

  public static class Chunk extends ServerMessage {

    /** . */
    public final org.crsh.text.Chunk payload;

    public Chunk(org.crsh.text.Chunk payload) {
      this.payload = payload;
    }
  }

  public static class Flush extends ServerMessage {

  }

  public static class End extends ServerMessage {

    /** . */
    public ShellResponse response;

    public End(ShellResponse response) {
      if (response == null) {
        throw new NullPointerException("No null response accepted");
      }

      //
      this.response = response;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
      if (response instanceof ShellResponse.Error) {
        oos.writeBoolean(false);
        ShellResponse.Error error = (ShellResponse.Error)response;
        oos.writeObject(error.getType());
        oos.writeObject(error.getMessage());
        oos.writeObject(error.getThrowable().getMessage());
        oos.writeObject(error.getThrowable().getStackTrace());
      } else {
        oos.writeBoolean(true);
        oos.writeObject(response);
      }
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      if (ois.readBoolean()) {
        response = (ShellResponse)ois.readObject();
      } else {
        ErrorType type = (ErrorType)ois.readObject();
        String message = (String)ois.readObject();
        String errorMessage = (String)ois.readObject();
        StackTraceElement[] errorTrace = (StackTraceElement[])ois.readObject();
        Exception ex = new Exception(errorMessage);
        ex.setStackTrace(errorTrace);
        response = ShellResponse.error(type, message, ex);
      }
    }
  }
}

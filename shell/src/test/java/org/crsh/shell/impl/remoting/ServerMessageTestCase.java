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

import org.crsh.AbstractTestCase;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.ShellResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/** @author Julien Viet */
public class ServerMessageTestCase extends AbstractTestCase {

  public static class MyException extends Throwable {
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      throw new ClassNotFoundException("Simulates a class not found");
    }
  }

  public void testErrorResponseSerialiation() throws Exception {
    ShellResponse.Error expectedResponse = ShellResponse.error(ErrorKind.EVALUATION, "hell", new MyException());
    ServerMessage expectedMessage = new ServerMessage.End(expectedResponse);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(expectedMessage);
    oos.close();
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);
    Object o = ois.readObject();
    ServerMessage.End message = assertInstance(ServerMessage.End.class, o);
    ShellResponse.Error response = assertInstance(ShellResponse.Error.class, message.response);
    assertEquals(expectedResponse.getMessage(), response.getMessage());
    assertEquals(Arrays.asList(expectedResponse.getThrowable().getStackTrace()), Arrays.asList(response.getThrowable().getStackTrace()));
  }
}

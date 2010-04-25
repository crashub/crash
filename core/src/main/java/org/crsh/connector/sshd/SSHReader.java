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
package org.crsh.connector.sshd;

import org.crsh.util.Input;
import org.crsh.util.ReaderStateMachine;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHReader extends ReaderStateMachine {

  /** . */
  private final Reader in;

  public SSHReader(Reader in, int verase, Writer echo) {
    super(verase, echo);

    //
    this.in = in;
  }

  public String nextLine() throws IOException {
    while (true) {
      if (hasNext()) {
        Input next = next();
        if (next instanceof Input.Chars) {
          return ((Input.Chars)next).getValue();
        }
      }
      int r = in.read();
      if (r == -1) {
        return null;
      }
      append((char)r);
    }
  }
}

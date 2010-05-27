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

import org.crsh.util.InputDecoder;
import org.crsh.util.OutputCode;

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ReaderStateMachine extends InputDecoder {

  /** . */
  private static final String DEL_SEQ = OutputCode.DELETE_PREV_CHAR + " " + OutputCode.DELETE_PREV_CHAR;

  /** . */
  private final int verase;

  /** . */
  private Writer echo;

  public ReaderStateMachine(int verase) {
    this(verase, null);
  }

  public ReaderStateMachine(int verase, Writer echo) {
    this.verase = verase;
    this.echo = echo;
  }

  protected void doEcho(String s) throws IOException {
    if (echo != null) {
      echo.write(s);
      echo.flush();
    }
  }

  @Override
  protected void doEchoCRLF() throws IOException {
    doEcho("\r\n");
  }

  @Override
  protected void doEchoDel() throws IOException {
    doEcho(DEL_SEQ);
  }

  public void append(String s) throws IOException {
    for (int i = 0;i < s.length();i++) {
      append(s.charAt(i));
    }
  }

  public void append(char c) throws IOException {
    if (c == verase) {
      appendDel();
    } else {
      appendData(c);
    }
  }
}

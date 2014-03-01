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

package org.crsh.telnet.term.console;

import org.crsh.telnet.term.spi.TermIO;

import java.io.IOException;

/**
 * Wraps {@link TermIO} and care about CRLF.
 */
public class TermIOWriter {

  /** . */
  private boolean previousCR;

  /** . */
  private final TermIO io;

  public TermIOWriter(TermIO io) {
    this.io = io;
  }

  /**
   * Write a char sequence to the output.
   *
   * @param s the char sequence
   * @throws IOException any io exception
   */
  public void write(CharSequence s) throws IOException {
    int len = s.length();
    if (len > 0) {
      for (int i = 0;i < len;i++) {
        char c = s.charAt(i);
        writeNoFlush(c);
      }
    }
  }

  /**
   * Write a single char to the output.
   *
   * @param c the char to write
   * @throws IOException any io exception
   */
  public void write(char c) throws IOException {
    writeNoFlush(c);
  }

  private void writeNoFlush(char c) throws IOException {
    if (previousCR && c == '\n') {
      previousCR = false;
    } else if (c == '\r' || c == '\n') {
      previousCR = c == '\r';
      io.writeCRLF();
    } else {
      io.write(c);
    }
  }
}

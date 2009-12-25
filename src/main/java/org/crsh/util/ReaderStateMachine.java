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
package org.crsh.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ReaderStateMachine implements Iterator<String> {

  /** . */
  private static final String DEL_SEQ = OutputCode.DELETE_PREV_CHAR + " " + OutputCode.DELETE_PREV_CHAR;

  /** . */
  private final int verase;

  /** . */
  private char[] buffer;

  /** . */
  private int size;

  /** . */
  private LinkedList<String> lines;

  /** -1 means the starts of a new line. */
  private int previous;

  /** . */
  private Writer echo;

  public ReaderStateMachine(int verase) {
    this(verase, null);
  }

  public ReaderStateMachine(int verase, Writer echo) {
    this.verase = verase;
    this.buffer = new char[128];
    this.size = 0;
    this.lines = new LinkedList<String>();
    this.previous = -1;
    this.echo = echo;
  }

  public void append(String s) throws IOException {
    for (int i = 0;i < s.length();i++) {
      append(s.charAt(i));
    }
  }

  private void echo(char c) throws IOException {
    if (echo != null) {
      echo.write(new char[]{c});
      echo.flush();
    }
  }

  private void echo(String s) throws IOException {
    if (echo != null) {
      echo.write(s);
      echo.flush();
    }
  }

  public void append(char c) throws IOException {
    if (c == verase) {
      pop();

      //
      if (size > 0) {
        previous = buffer[size];
      } else {
        previous = -1;
      }

      //
      echo(DEL_SEQ);
    } else if ((c == '\n' && previous == '\r') || (c == '\r' && previous == '\n')) {
      previous = -1;
    } else if (c == '\n' || c == '\r') {
      String line = new String(buffer, 0, size);
      lines.add(line);
      previous = c;
      size = 0;
      echo("\r\n");
    } else {
      push(c);
      previous = c;
      echo(c);
    }
  }

  public int getSize() {
    return size;
  }

  private void pop() {
    if (size > 0) {
      size--;
    }
  }

  public boolean hasNext() {
    return lines.size() > 0;
  }

  public String next() {
    if (lines.size() > 0) {
      return lines.removeFirst();
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void push(char c) {
    if (size >= buffer.length) {
      char[] tmp = new char[buffer.length * 2 + 1];
      System.arraycopy(tmp, 0, buffer, 0, tmp.length);
      this.buffer = tmp;
    }
    buffer[size++] = c;
  }

}

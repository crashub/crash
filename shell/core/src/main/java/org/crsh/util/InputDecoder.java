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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class InputDecoder implements Iterator<Input> {

  /** . */
  private char[] buffer;

  /** . */
  private int size;

  /**
   * Cursor Position, always equal to {@link #size} unless the underlying *.IO class supports editing.
   */
  private int curAt;

  /** . */
  private LinkedList<Input> lines;

  /** -1 means the starts of a new line. */
  private int previous;

  /** Whether or not we do echoing. */
  private boolean echoing;

  public InputDecoder() {
    this.buffer = new char[128];
    this.size = 0;
    this.curAt = 0;
    this.lines = new LinkedList<Input>();
    this.previous = -1;
    this.echoing = true;
  }

  /**
   * Clears the buffer without doing any echoing.
   */
  public final void clearBuffer() {
    curAt = 0;
    size = 0;
  }

  public final boolean isEchoing() {
    return echoing;
  }

  public final void setEchoing(boolean echoing) {
    this.echoing = echoing;
  }

  private void echo(char c) throws IOException {
    echo(new String(new char[]{c}));
  }

  private void echo(String s) throws IOException {
    if (echoing) {
      doEcho(s);
    }
  }

  private void echoDel() throws IOException {
    if (echoing) {
      doEchoDel();
    }
  }

  private void echoCRLF() throws IOException {
    if (echoing) {
      doEchoCRLF();
    }
  }

  protected abstract void doEchoCRLF() throws IOException;

  protected abstract void doEcho(String s) throws IOException;

  protected abstract void doEchoDel() throws IOException;

  protected abstract boolean doMoveLeft() throws IOException;

  protected abstract boolean doMoveRight() throws IOException;

  public String set(String s) throws IOException {
    StringBuilder builder = new StringBuilder();
    for (int i = appendDel();i != -1;i = appendDel()) {
      builder.append((char)i);
    }
    appendData(s);
    return builder.reverse().toString();
  }

  public int size() {
    return lines.size();
  }

  public void appendData(String s) throws IOException {
    for (int i = 0;i < s.length();i++) {
      appendData(s.charAt(i));
    }
  }

  public void appendData(char c) throws IOException {
    if ((c == '\n' && previous == '\r') || (c == '\r' && previous == '\n')) {
      previous = -1;
    } else if (c == '\n' || c == '\r') {
      String line = new String(buffer, 0, size);
      lines.add(new Input.Chars(line));
      previous = c;
      size = 0;
      curAt = size;
      echoCRLF();
    } else {
      push(c);
    }
  }

  public int appendDel() throws IOException {
    if (curAt == size){
      int popped = pop();

      //
      if (popped != -1) {
        previous = buffer[size];
        echoDel();
      } else {
        previous = -1;
      }

      //
      return popped;
    } else {
      // Cursor in body
      if (curAt == 0) {
        previous = -1;
        return -1;
      }
      // remove previous char from buffer
      for (int idx = curAt-1; idx < size; idx++) {
        buffer[idx] = buffer[idx+1];
      }
      buffer[size-1] = ' ';
      // Adjust cursor at and size
      --size;
      moveLeft();
      // Redisplay from cursor to end
      String disp = new String(buffer, curAt, size - curAt + 1);
      doEcho(disp);
      // position cursor one to left from where started
      int saveCurAt = curAt;
      curAt = size + 1;   // Size before delete
      while (curAt > saveCurAt) {
        moveLeft();
      }
      if (curAt == 0) {
        return -1;
      }
      return buffer[curAt - 1];
    }
  }

  public int getSize() {
    return size;
  }

  private int pop() {
    if (size > 0) {
      --size;
      curAt = size;
      return buffer[size];
    } else {
      curAt = size;
      return -1;
    }
  }

  public boolean hasNext() {
    return lines.size() > 0;
  }

  public Input next() {
    if (lines.size() > 0) {
      return lines.removeFirst();
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void push(char c) throws IOException {
    if (size >= buffer.length) {
    char[] tmp = new char[buffer.length * 2 + 1];
     System.arraycopy(buffer, 0, tmp, 0, buffer.length);
      this.buffer = tmp;
    }
    if (curAt == size) {
      buffer[size++] = c;
      curAt = size;
      previous = c;
      echo(c);
    } else {
      // In body
      // Shift buffer one from position before cursor
      for (int idx = size-1; idx > curAt - 1; idx--) {
        buffer[idx+1] = buffer[idx];
      }
      buffer[curAt] = c;
      // Adjust size and display from inserted character to end
      ++size;
      String disp = new String(buffer, curAt, size - curAt);
      doEcho(disp);
      // Move cursor to original character
      int saveCurAt = ++curAt;
      curAt = size;
      while (curAt > saveCurAt) {
        moveLeft();
      }
      previous = buffer[curAt-1];
    }
  }

  public void moveRight() throws IOException {
    if (curAt < size) {
      if (doMoveRight())
      {
        curAt++;
      }
    }
  }

  public void moveLeft() throws IOException {
    if (curAt > 0) {
      if (doMoveLeft())
      {
        curAt--;
      }
    }
  }
}
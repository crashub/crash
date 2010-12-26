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

package org.crsh.term.console;

import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * <p>This class provides an abstraction for a console. This implementation wraps the input and output of a terminal
 * based on a bidirectional io.</p>
 *
 * <p>Interactions between terminal and console are done though the {@link ViewReader} and {@link ViewWriter}
 * classes.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class Console {

  /** . */
  private char[] buffer;

  /** . */
  private int size;

  /** Cursor Position, always equal to {@link #size} unless the underlying *.IO class supports editing. */
  private int curAt;

  /** . */
  private LinkedList<CharSequence> lines;

  /** Do we have a issued a CR previously? */
  private boolean previousCR;

  /** Whether or not we do echoing. */
  private boolean echoing;

  /** . */
  private final ViewWriter viewWriter;

  /** . */
  private final ViewReader viewReader = new ViewReader() {

    @Override
    public CharSequence replace(CharSequence s) throws IOException {
      StringBuilder builder = new StringBuilder();
      for (int i = appendDel();i != -1;i = appendDel()) {
        builder.append((char)i);
      }
      appendData(s);
      return builder.reverse().toString();
    }

    @Override
    public void write(char c) throws IOException {
      appendData(c);
    }

    @Override
    public void write(CharSequence s) throws IOException {
      appendData(s.toString());
    }

    @Override
    public void del() throws IOException {
      appendDel();
    }

    @Override
    public void moveRight() throws IOException {
      Console.this.moveRight();
    }

    @Override
    public void moveLeft() throws IOException {
      Console.this.moveLeft();
    }
  };

  /** . */
  private final ConsoleReader reader = new ConsoleReader() {
    @Override
    public int getSize() {
      return size;
    }

    @Override
    public boolean hasNext() {
      return lines.size() > 0;
    }

    @Override
    public CharSequence next() {
      if (lines.size() > 0) {
        return lines.removeFirst();
      } else {
        throw new NoSuchElementException();
      }
    }
  };

  /** . */
  private final ConsoleWriter writer = new ConsoleWriter() {

    //
    private boolean previousCR;

    @Override
    public void write(CharSequence s) throws IOException {
      for (int i = 0;i < s.length();i++) {
        char c = s.charAt(i);
        writeNoFlush(c);
      }
      viewWriter.flush();
    }

    public void write(char c) throws IOException {
      writeNoFlush(c);
      viewWriter.flush();
    }

    private void writeNoFlush(char c) throws IOException {
      if (previousCR && c == '\n') {
        previousCR = false;
      } else if (c == '\r' || c == '\n') {
        previousCR = c == '\r';
        viewWriter.writeCRLF();
      } else {
        viewWriter.write(c);
      }
    }
  };

  public Console(ViewWriter viewWriter) {
    this.buffer = new char[128];
    this.size = 0;
    this.curAt = 0;
    this.lines = new LinkedList<CharSequence>();
    this.previousCR = false;
    this.echoing = true;
    this.viewWriter = viewWriter;
  }

  /**
   * Clears the buffer without doing any echoing.
   */
  public void clearBuffer() {
    this.previousCR = false;
    this.curAt = 0;
    this.size = 0;
  }

  public CharSequence getBuffer() {
    return new String(buffer, 0, size);
  }

  public CharSequence getBufferToCursor() {
    return new String(buffer, 0, curAt);
  }

  public boolean isEchoing() {
    return echoing;
  }

  public void setEchoing(boolean echoing) {
    Console.this.echoing = echoing;
  }

  /**
   * Returns the console reader.
   *
   * @return the console reader
   */
  public ConsoleReader getReader() {
    return reader;
  }

  public ViewReader getViewReader() {
    return viewReader;
  }

  public ConsoleWriter getWriter() {
    return writer;
  }

  private void appendData(CharSequence s) throws IOException {
    for (int i = 0;i < s.length();i++) {
      appendData(s.charAt(i));
    }
  }

  private void appendData(char c) throws IOException {
    if (previousCR && c == '\n') {
      previousCR = false;
    } else if (c == '\r' || c == '\n') {
      previousCR = c == '\r';
      String line = new String(buffer, 0, size);
      lines.add(line);
      size = 0;
      curAt = size;
      echoCRLF();
    } else {
      push(c);
    }
  }

  private int appendDel() throws IOException {
    if (curAt == size){
      int popped = pop();

      //
      if (popped != -1) {
//        previous = buffer[size];
        echoDel();
      } else {
//        previous = -1;
      }

      //
      return popped;
    } else {
      // Cursor in body
      if (curAt == 0) {
//        previous = -1;
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
      viewWriter.write(disp);
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

  private void moveRight() throws IOException {
    if (curAt < size) {
      if (viewWriter.writeMoveRight())
      {
        viewWriter.flush();
        curAt++;
      }
    }
  }

  private void moveLeft() throws IOException {
    if (curAt > 0) {
      if (viewWriter.writeMoveLeft())
      {
        viewWriter.flush();
        curAt--;
      }
    }
  }

  private void echo(char c) throws IOException {
    if (echoing) {
      viewWriter.write(c);
      viewWriter.flush();
    }
  }

  private void echo(String s) throws IOException {
    if (echoing) {
      viewWriter.write(s);
      viewWriter.flush();
    }
  }

  private void echoDel() throws IOException {
    if (echoing) {
      viewWriter.writeDel();
      viewWriter.flush();
    }
  }

  private void echoCRLF() throws IOException {
    if (echoing) {
      viewWriter.writeCRLF();
      viewWriter.flush();
    }
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

  private void push(char c) throws IOException {
    if (size >= buffer.length) {
      char[] tmp = new char[buffer.length * 2 + 1];
      System.arraycopy(buffer, 0, tmp, 0, buffer.length);
      Console.this.buffer = tmp;
    }
    if (curAt == size) {
      buffer[size++] = c;
      curAt = size;
//      previous = c;
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
      viewWriter.write(disp);
      // Move cursor to original character
      int saveCurAt = ++curAt;
      curAt = size;
      while (curAt > saveCurAt) {
        moveLeft();
      }
//      previous = buffer[curAt-1];
    }
  }
}
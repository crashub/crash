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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public final class TermIOBuffer implements Appendable, Iterator<CharSequence> {

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
  private final TermIO io;

  public TermIOBuffer(TermIO io) {
    this.buffer = new char[128];
    this.size = 0;
    this.curAt = 0;
    this.lines = new LinkedList<CharSequence>();
    this.previousCR = false;
    this.echoing = true;
    this.io = io;
  }

  /**
   * Clears the buffer without doing any echoing.
   */
  public void clear() {
    this.previousCR = false;
    this.curAt = 0;
    this.size = 0;
  }

  /**
   * Returns the total number of chars in the buffer, independently of the cursor position.
   *
   * @return the number of chars
   */
  public int getSize() {
    return size;
  }

  /**
   * Returns the current cursor position.
   *
   * @return the cursor position
   */
  int getCursor() {
    return curAt;
  }

  /**
   * Returns a character at a specified index in the buffer.
   *
   * @param index the index
   * @return the char
   * @throws IndexOutOfBoundsException if the index is negative or larget than the size
   */
  char charAt(int index) throws IndexOutOfBoundsException {
    if (index < 0) {
      throw new IndexOutOfBoundsException("No negative position accepted");
    }
    if (index >= size) {
      throw new IndexOutOfBoundsException("Cannot accept position greater than size:" + index + " >= " + size);
    }
    return buffer[index];
  }

  CharSequence getBufferToCursor() {
    return new String(buffer, 0, curAt);
  }

  boolean isEchoing() {
    return echoing;
  }

  void setEchoing(boolean echoing) {
    this.echoing = echoing;
  }

  // Iterator<CharSequence> implementation *****************************************************************************

  public boolean hasNext() {
    return lines.size() > 0;
  }

  public CharSequence next() {
    if (lines.size() > 0) {
      return lines.removeFirst();
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  // Appendable implementation *****************************************************************************************

  public TermIOBuffer append(char c) throws IOException {
    if (appendData(c)) {
      io.flush();
    }
    return this;
  }

  public TermIOBuffer append(CharSequence s) throws IOException {
    return append(s, 0, s.length());
  }

  public TermIOBuffer append(CharSequence csq, int start, int end) throws IOException {
    if (appendData(csq, start, end)) {
      io.flush();
    }
    return this;
  }

  // Protected methods *************************************************************************************************

  /**
   * Replace all the characters before the cursor by the provided char sequence.
   *
   * @param s the new char sequence
   * @return the l
   * @throws IOException any IOException
   */
  CharSequence replace(CharSequence s) throws IOException {
    StringBuilder builder = new StringBuilder();
    boolean flush = false;
    for (int i = appendDel();i != -1;i = appendDel()) {
      builder.append((char)i);
      flush = true;
    }
    flush |= appendData(s, 0, s.length());
    if (flush) {
      io.flush();
    }
    return builder.reverse().toString();
  }

  public boolean moveRight() throws IOException {
    return moveRight(1) == 1;
  }

  public boolean moveLeft() throws IOException {
    return moveLeft(1) == 1;
  }

  public int moveRight(int count) throws IOException, IllegalArgumentException {
    if (count < 0) {
      throw new IllegalArgumentException("Cannot move with negative count " + count);
    }
    int delta = 0;
    while (delta < count) {
      if (curAt + delta < size && io.moveRight(buffer[curAt + delta])) {
        delta++;
      } else {
        break;
      }
    }
    if (delta > 0) {
      io.flush();
      curAt += delta;
    }
    return delta;
  }

  int moveLeft(int count) throws IOException, IllegalArgumentException {
    if (count < 0) {
      throw new IllegalArgumentException("Cannot move with negative count " + count);
    }
    int delta = 0;
    while (delta < count) {
      if (delta < curAt && io.moveLeft()) {
        delta++;
      } else {
        break;
      }
    }
    if (delta > 0) {
      io.flush();
      curAt -= delta;
    }
    return delta;
  }

  /**
   * Delete the char under the cursor or return -1 if no char was deleted.
   *
   * @return the deleted char
   * @throws IOException any IOException
   */
  public int del() throws IOException {
    int ret = appendDel();
    if (ret != -1) {
      io.flush();
    }
    return ret;
  }

  private boolean appendData(CharSequence s, int start, int end) throws IOException {
    if (start < 0) {
      throw new IndexOutOfBoundsException("No negative start");
    }
    if (end < 0) {
      throw new IndexOutOfBoundsException("No negative end");
    }
    if (end > s.length()) {
      throw new IndexOutOfBoundsException("End cannot be greater than sequence length");
    }
    if (end < start) {
      throw new IndexOutOfBoundsException("Start cannot be greater than end");
    }
    boolean flush = false;
    for (int i = start;i < end;i++) {
      flush |= appendData(s.charAt(i));
    }
    return flush;
  }

  /**
   * Append a char at the current cursor position and increment the cursor position.
   *
   * @param c the char to append
   * @return true if flush is required
   * @throws IOException any IOException
   */
  private boolean appendData(char c) throws IOException {
    if (previousCR && c == '\n') {
      previousCR = false;
      return false;
    } else if (c == '\r' || c == '\n') {
      previousCR = c == '\r';
      String line = new String(buffer, 0, size);
      lines.add(line);
      size = 0;
      curAt = size;
      return echoCRLF();
    } else {
      if (push(c)) {
        return echo(c);
      } else {
        String disp = new String(buffer, curAt, size - curAt);
        io.write(disp);
        int amount = size - curAt - 1;
        curAt++;
        while (amount > 0) {
          io.moveLeft();
          amount--;
        }
        return true;
      }
    }
  }

  /**
   * Delete the char before the cursor.
   *
   * @return the removed char value or -1 if no char was removed
   * @throws IOException any IOException
   */
  private int appendDel() throws IOException {

    // If the cursor is at the most right position (i.e no more chars after)
    if (curAt == size){
      int popped = pop();

      //
      if (popped != -1) {
        echoDel();
        // We do not care about the return value of echoDel, but we will return a value that indcates
        // that a flush is required although it may not
        // to properly carry out the status we should have two things to return
        // 1/ the popped char
        // 2/ the boolean indicating if flush is required
      }

      //
      return popped;
    } else {
      // We are editing the line

      // Shift all the chars after the cursor
      int popped = pop();

      //
      if (popped != -1) {

        // We move the cursor to left
        if (io.moveLeft()) {
          StringBuilder disp = new StringBuilder();
          disp.append(buffer, curAt, size - curAt);
          disp.append(' ');
          io.write(disp);
          int amount = size - curAt + 1;
          while (amount > 0) {
            io.moveLeft();
            amount--;
          }
        } else {
          throw new UnsupportedOperationException("not implemented");
        }
      }

      //
      return popped;
    }
  }

  private boolean echo(char c) throws IOException {
    if (echoing) {
      io.write(c);
      return true;
    } else {
      return false;
    }
  }

  private void echo(String s) throws IOException {
    if (echoing) {
      io.write(s);
      io.flush();
    }
  }

  private boolean echoDel() throws IOException {
    if (echoing) {
      io.writeDel();
      return true;
    } else {
      return false;
    }
  }

  private boolean echoCRLF() throws IOException {
    if (echoing) {
      io.writeCRLF();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Popup one char from buffer at the current cursor position.
   *
   * @return the popped char or -1 if none was removed
   */
  private int pop() {
    if (curAt > 0) {
      char popped = buffer[curAt - 1];
      if (curAt == size) {
        buffer[curAt] = 0;
        size = --curAt;
        return popped;
      } else {
        for (int i = curAt;i < size;i++) {
          buffer[i - 1] = buffer[i];
        }
        buffer[--size] = 0;
        curAt--;
      }
      return popped;
    } else {
      return -1;
    }
  }

  /**
   * Push  one char in the buffer at the current cursor position. This operation ensures that the buffer
   * is large enough and it may increase the buffer capacity when required. The cursor position is incremented
   * when a char is appended at the last position, otherwise the cursor position remains unchanged.
   *
   * @param c the char to push
   * @return true if the cursor position was incremented
   */
  private boolean push(char c) {
    if (size >= buffer.length) {
      char[] tmp = new char[buffer.length * 2 + 1];
      System.arraycopy(buffer, 0, tmp, 0, buffer.length);
      TermIOBuffer.this.buffer = tmp;
    }
    if (curAt == size) {
      buffer[size++] = c;
      curAt++;
      return true;
    } else {
      for (int i = size - 1;i > curAt - 1;i--) {
        buffer[i + 1] = buffer[i];
      }
      buffer[curAt] = c;
      ++size;
      return false;
    }
  }
}
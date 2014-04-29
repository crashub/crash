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

package org.crsh.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

final class EditorBuffer implements Appendable, Iterator<String> {

  /** . */
  private StringBuilder current;

  /** Cursor position. */
  private int cursor;

  /** Previous lines. */
  private LinkedList<String> lines;

  /** The output. */
  private final ConsoleDriver driver;

  /** True if flush is needed. */
  private boolean needFlush;

  EditorBuffer(ConsoleDriver driver) {
    this.current = new StringBuilder();
    this.cursor = 0;
    this.lines = new LinkedList<String>();
    this.driver = driver;
    this.needFlush = false;
  }

  void flush() throws IOException {
    flush(false);
  }

  void flush(boolean force) throws IOException {
    if (needFlush || force) {
      driver.flush();
      needFlush = false;
    }
  }

  /**
   * Reset the buffer state.
   */
  void reset() {
    this.lines.clear();
    this.cursor = 0;
    this.current.setLength(0);
  }

  /**
   * Returns the total number of chars in the buffer, independently of the cursor position.
   *
   * @return the number of chars
   */
  int getSize() {
    return current.length();
  }

  /**
   * Returns the current cursor position.
   *
   * @return the cursor position
   */
  int getCursor() {
    return cursor;
  }

  /**
   * Returns a character at a specified index in the buffer.
   *
   * @param index the index
   * @return the char
   * @throws StringIndexOutOfBoundsException if the index is negative or larget than the size
   */
  char charAt(int index) throws StringIndexOutOfBoundsException {
    return current.charAt(index);
  }

  /**
   * @return the current line
   */
  public String getLine() {
    return current.toString();
  }

  /**
   * @return the lines
   */
  public List<String> getLines() {
    ArrayList<String> tmp = new ArrayList<String>(lines.size() + 1);
    tmp.addAll(lines);
    tmp.add(getLine());
    return tmp;
  }

  // Iterator<String> implementation ***********************************************************************************

  @Override
  public boolean hasNext() {
    return lines.size() > 0;
  }

  @Override
  public String next() {
    if (lines.size() == 0) {
      throw new NoSuchElementException();
    }
    return lines.removeFirst();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  // Appendable implementation *****************************************************************************************

  public EditorBuffer append(char c) throws IOException {
    appendData(Character.toString(c), 0, 1);
    return this;
  }

  public EditorBuffer append(CharSequence s) throws IOException {
    return append(s, 0, s.length());
  }

  public EditorBuffer append(CharSequence csq, int start, int end) throws IOException {
    appendData(csq, start, end);
    return this;
  }

  // Protected methods *************************************************************************************************

  /**
   * Replace all the characters before the cursor by the provided char sequence.
   *
   * @param s the new char sequence
   * @return the l
   * @throws java.io.IOException any IOException
   */
  String replace(CharSequence s) throws IOException {
    StringBuilder builder = new StringBuilder();
    for (int i = appendDel();i != -1;i = appendDel()) {
      builder.append((char)i);
      needFlush = true;
    }
    appendData(s, 0, s.length());
    return builder.reverse().toString();
  }

  /**
   * Move the cursor right by one char with the provided char.
   *
   * @param c the char to overwrite
   * @return true if it happended
   * @throws IOException
   */
  boolean moveRight(char c) throws IOException {
    if (cursor < current.length()) {
      if (driver.moveRight(c)) {
        current.setCharAt(cursor++, c);
        return true;
      }
    }
    return false;
  }

  boolean moveRight() throws IOException {
    return moveRightBy(1) == 1;
  }

  boolean moveLeft() throws IOException {
    return moveLeftBy(1) == 1;
  }

  int moveRightBy(int count) throws IOException, IllegalArgumentException {
    if (count < 0) {
      throw new IllegalArgumentException("Cannot move with negative count " + count);
    }
    int delta = 0;
    while (delta < count) {
      if (cursor + delta < current.length() && driver.moveRight(current.charAt(cursor + delta))) {
        delta++;
      } else {
        break;
      }
    }
    if (delta > 0) {
      needFlush = true;
      cursor += delta;
    }
    return delta;
  }

  int moveLeftBy(int count) throws IOException, IllegalArgumentException {
    if (count < 0) {
      throw new IllegalArgumentException("Cannot move with negative count " + count);
    }
    int delta = 0;
    while (delta < count) {
      if (delta < cursor && driver.moveLeft()) {
        delta++;
      } else {
        break;
      }
    }
    if (delta > 0) {
      needFlush = true;
      cursor -= delta;
    }
    return delta;
  }

  /**
   * Delete the char under the cursor or return -1 if no char was deleted.
   *
   * @return the deleted char
   * @throws java.io.IOException any IOException
   */
  int del() throws IOException {
    int ret = appendDel();
    if (ret != -1) {
      needFlush = true;
    }
    return ret;
  }

  private void appendData(CharSequence s, int start, int end) throws IOException {
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

    // Break into lines
    int pos = start;
    while (pos < end) {
      char c = s.charAt(pos);
      if (c == '\n') {
        newAppendNoLF(s, start, pos);
        String line = current.toString();
        lines.add(line);
        cursor = 0;
        current.setLength(0);
        echoCRLF();
        start = ++pos;
      } else {
        pos++;
      }
    }

    // Append the rest if any
    newAppendNoLF(s, start, pos);
  }

  private void newAppendNoLF(CharSequence s, int start, int end) throws IOException {

    // Count the number of chars
    // at the moment we ignore \r
    // since this behavior is erratic and not well defined
    // not sure we need to handle this here... since we kind of handle it too in the ConsoleDriver.write(int)
    int len = 0;
    for (int i = start;i < end;i++) {
      if (s.charAt(i) != '\r') {
        len++;
      }
    }

    //
    if (len > 0) {

      // Now insert our data
      int count = cursor;
      int size = current.length();
      for (int i = start;i < end;i++) {
        char c = s.charAt(i);
        if (c != '\r') {
          current.insert(count++, c);
          driver.write(c);
        }
      }

      // Now redraw what is missing and put the cursor back at the correct place
      for (int i = cursor;i < size;i++) {
        driver.write(current.charAt(len + i));
      }
      for (int i = cursor;i < size;i++) {
        driver.moveLeft();
      }

      // Update state
      size += len;
      cursor += len;
      needFlush = true;
    }
  }


  /**
   * Delete the char before the cursor.
   *
   * @return the removed char value or -1 if no char was removed
   * @throws java.io.IOException any IOException
   */
  private int appendDel() throws IOException {

    // If the cursor is at the most right position (i.e no more chars after)
    if (cursor == current.length()){
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
        if (driver.moveLeft()) {
          StringBuilder disp = new StringBuilder();
          disp.append(current, cursor, current.length());
          disp.append(' ');
          driver.write(disp);
          int amount = current.length() - cursor + 1;
          while (amount > 0) {
            driver.moveLeft();
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

  private void echoDel() throws IOException {
    driver.writeDel();
    needFlush = true;
  }

  private void echoCRLF() throws IOException {
    driver.writeCRLF();
    needFlush = true;
  }

  /**
   * Popup one char from buffer at the current cursor position.
   *
   * @return the popped char or -1 if none was removed
   */
  private int pop() {
    if (cursor > 0) {
      char popped = current.charAt(cursor - 1);
      current.deleteCharAt(cursor - 1);
      cursor--;
     return popped;
    } else {
      return -1;
    }
  }
}
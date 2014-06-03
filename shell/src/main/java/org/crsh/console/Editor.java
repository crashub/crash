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

import org.crsh.cli.impl.line.LineParser;
import org.crsh.cli.impl.line.MultiLineVisitor;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * An editor state machine.
 *
 * todo:
 * - undo
 * - optimize operation with an improvement of {@link org.crsh.console.EditorBuffer}
 *
 * @author Julien Viet
 */
class Editor extends Plugin {

  /** . */
  final Console console;

  /** . */
  final EditorBuffer buffer;

  /** . */
  final MultiLineVisitor visitor;

  /** The line parser : updated on enter key. */
  final LineParser lineParser;

  /** . */
  final LinkedList<String> history;

  /** . */
  private Mode mode;

  /** . */
  int historyCursor;

  /** . */
  String historyBuffer;

  /** The buffer that holds what we kill. */
  final StringBuilder killBuffer;

  /** . */
  private final ArrayList<Runnable> modeListeners;

  Editor(Console console) {
    this(console, true);
  }

  Editor(Console console, boolean echo) {


    //
    EditorBuffer buffer = new EditorBuffer(echo ? console.driver : NULL);

    //
    this.console = console;
    this.buffer = buffer;
    this.visitor = new MultiLineVisitor();
    this.lineParser = new LineParser(visitor);
    this.history = new LinkedList<String>();
    this.historyCursor = -1;
    this.historyBuffer = null;
    this.killBuffer = new StringBuilder();
    this.mode = Mode.EMACS;
    this.modeListeners = new ArrayList<Runnable>();
  }

  Mode getMode() {
    return mode;
  }

  void setMode(Mode mode) {
    this.mode = mode;
    for (Runnable listener : modeListeners) {
      listener.run();
    }
  }

  void addModeListener(Runnable runnable) {
    modeListeners.add(runnable);
  }

  void addToHistory(String line) {
    history.addFirst(line);
  }

  /**
   * Returns the right cursor bound depending on the current mode.
   *
   * @return the current bound
   */
  int getCursorBound() {
    if (console.getMode() == Mode.EMACS) {
      return buffer.getSize();
    } else {
      return Math.max(0, buffer.getSize() - 1);
    }
  }

  String getKillBuffer() {
    return killBuffer.toString();
  }

  void setKillBuffer(CharSequence s) {
    if (s == null) {
      throw new NullPointerException("No null buffer content");
    }
    killBuffer.setLength(0);
    killBuffer.append(s);
  }

  boolean isEmpty() {
    return buffer.getSize() == 0 && buffer.getLines().size() == 1;
  }

  String getCurrentLine() {
    return buffer.getLine();
  }

  int getCurrentPosition() {
    return buffer.getCursor();
  }

  String append(EditorAction action, int[] sequence) {
    try {
      return action.execute(this, buffer, sequence, true);
    }
    catch (IOException e) {
      AssertionError ae = new AssertionError("Not yet supported");
      ae.initCause(e);
      throw ae;
    }
  }

  void reset() {
    lineParser.reset();
    buffer.reset();
    historyCursor = -1;
  }

  // Null impl for echo
  private static final ConsoleDriver NULL = new ConsoleDriver() {
    @Override public int getWidth() { return 80; }
    @Override public int getHeight() { return 40; }
    @Override public String getProperty(String name) { return null; }
    @Override public boolean takeAlternateBuffer() throws IOException { return false; }
    @Override public boolean releaseAlternateBuffer() throws IOException { return false; }
    @Override public void flush() throws IOException { }
    @Override public void write(CharSequence s) throws IOException { }
    @Override public void write(CharSequence s, int start, int end) throws IOException { }
    @Override public void write(char c) throws IOException { }
    @Override public void write(Style d) throws IOException { }
    @Override public void writeDel() throws IOException { }
    @Override public void writeCRLF() throws IOException { }
    @Override public void cls() throws IOException { }
    @Override public boolean moveRight(char c) throws IOException { return true; }
    @Override public boolean moveLeft() throws IOException { return true; }
    @Override public void close() throws IOException { }
  };
}

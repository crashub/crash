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
package org.crsh.web.servlet;

import org.crsh.keyboard.KeyHandler;
import org.crsh.keyboard.KeyType;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Color;
import org.crsh.text.Screenable;
import org.crsh.text.Style;
import org.crsh.util.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.logging.Level;

/** @author Julien Viet */
public class WSProcessContext implements ShellProcessContext, KeyHandler {

  /** . */
  final ShellProcess process;

  /** . */
  final CRaSHSession session;

  /** . */
  private StringBuilder buffer = new StringBuilder();

  /** . */
  final int width;

  /** . */
  final int height;

  /** . */
  final String command;

  public WSProcessContext(CRaSHSession session, ShellProcess process, String command, int width, int height) {
    this.session = session;
    this.process = process;
    this.width = width;
    this.height = height;
    this.command = command;
  }

  @Override
  public void handle(KeyType type, int[] sequence) {
    KeyHandler keyHandler = process.getKeyHandler();
    if (keyHandler != null) {
      CRaSHConnector.log.fine("Processing key event " + type + " " + Arrays.toString(sequence));
      try {
        keyHandler.handle(type, sequence);
      }
      catch (Exception e) {
        CRaSHConnector.log.log(Level.SEVERE, "Processing key handler " + keyHandler + " threw an exception", e);
      }
    }
  }

  public void end(ShellResponse response) {
    CRaSHConnector.log.fine("Ended \"" + command + "\"");
    session.current.compareAndSet(this, null);
    flush();
    String msg = response.getMessage();
    if (msg.length() > 0) {
      session.send("print", msg);
    }
    String prompt = session.shell.getPrompt();
    session.send("prompt", prompt);
    session.send("end");
  }

  public boolean takeAlternateBuffer() throws IOException {
    return false;
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return false;
  }

  public String getProperty(String propertyName) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    return null;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  private static final EnumMap<Color, String> COLOR_MAP = new EnumMap<Color, String>(Color.class);

  static {
    COLOR_MAP.put(Color.black, "#000");
    COLOR_MAP.put(Color.blue, "#0000AA");
    COLOR_MAP.put(Color.cyan, "#00AAAA");
    COLOR_MAP.put(Color.green, "#00AA00");
    COLOR_MAP.put(Color.magenta, "#AA00AA");
    COLOR_MAP.put(Color.white, "#AAAAAA");
    COLOR_MAP.put(Color.yellow, "#AAAA00");
    COLOR_MAP.put(Color.red, "#AA0000");
  }

  /** . */
  private Style style = Style.reset;

  @Override
  public Appendable append(char c) throws IOException {
    return append(Character.toString(c));
  }

  @Override
  public Appendable append(CharSequence s) throws IOException {
    return append(s, 0, s.length());
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    if (start < end) {
      if (style.equals(Style.reset)) {
        buffer.append(csq, start, end);
      } else {
        Style.Composite composite = (Style.Composite)style;
        buffer.append("[[");
        if (composite.getUnderline() == Boolean.TRUE) {
          buffer.append('u');
        }
        if (composite.getBold() == Boolean.TRUE) {
          buffer.append('b');
        }
        buffer.append(';');
        if (composite.getForeground() != null) {
          buffer.append(COLOR_MAP.get(composite.getForeground()));
        }
        buffer.append(';');
        if (composite.getBackground() != null) {
          buffer.append(COLOR_MAP.get(composite.getBackground()));
        }
        buffer.append(']');
        while (start < end) {
          char c = csq.charAt(start++);
          if (c == ']') {
            buffer.append("\\]");
          } else {
            buffer.append(c);
          }
        }
        buffer.append(']');
      }
    }
    return this;
  }

  @Override
  public Screenable append(Style style) throws IOException {
    this.style = style.merge(style);
    return this;
  }

  @Override
  public Screenable cls() throws IOException {
    buffer.append("\033[");
    buffer.append("2J");
    buffer.append("\033[");
    buffer.append("1;1H");
    return this;
  }

  public void flush() {
    if (buffer.length() > 0) {
      session.send("print", buffer.toString());
      buffer.setLength(0);
    }
  }
}

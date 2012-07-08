/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.term;

import org.crsh.term.console.Console;
import org.crsh.term.console.ConsoleWriter;
import org.crsh.term.console.ViewWriter;
import org.crsh.term.spi.TermIO;
import org.crsh.text.Data;
import org.crsh.text.DataFragment;
import org.crsh.text.FormattingData;
import org.crsh.text.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BaseTerm implements Term {

  /** . */
  private final Logger log = LoggerFactory.getLogger(BaseTerm.class);

  /** . */
  private final LinkedList<CharSequence> history;

  /** . */
  private CharSequence historyBuffer;

  /** . */
  private int historyCursor;

  /** . */
  private final TermIO io;

  /** . */
  private final Console console;

  public BaseTerm(final TermIO io) {
    this.history = new LinkedList<CharSequence>();
    this.historyBuffer = null;
    this.historyCursor = -1;
    this.io = io;
    this.console = new Console(new ViewWriter() {

      @Override
      protected void flush() throws IOException {
        io.flush();
      }

      @Override
      protected void writeCRLF() throws IOException {
        io.writeCRLF();
      }

      @Override
      protected void write(CharSequence s) throws IOException {
        io.write(s.toString());
      }

      @Override
      protected void write(char c) throws IOException {
        io.write(c);
      }

      @Override
      protected void write(Style style) throws IOException {
        io.write(style);
      }

      @Override
      protected void writeDel() throws IOException {
        io.writeDel();
      }

      @Override
      protected boolean writeMoveLeft() throws IOException {
        return io.moveLeft();
      }

      @Override
      protected boolean writeMoveRight(char c) throws IOException {
        return io.moveRight(c);
      }
    });
  }

  public int getWidth() {
    return io.getWidth();
  }

  public String getProperty(String name) {
    return io.getProperty(name);
  }

  public void setEcho(boolean echo) {
    console.setEchoing(echo);
  }

  public TermEvent read() throws IOException {

    //
    while (true) {
      int code = io.read();
      CodeType type = io.decode(code);
      switch (type) {
        case CLOSE:
          return TermEvent.close();
        case BACKSPACE:
          console.getViewReader().del();
          break;
        case UP:
        case DOWN:
          int nextHistoryCursor = historyCursor +  (type == CodeType.UP ? + 1 : -1);
          if (nextHistoryCursor >= -1 && nextHistoryCursor < history.size()) {
            CharSequence s = nextHistoryCursor == -1 ? historyBuffer : history.get(nextHistoryCursor);
            while (console.getViewReader().moveRight()) {
              // Do nothing
            }
            CharSequence t = console.getViewReader().replace(s);
            if (historyCursor == -1) {
              historyBuffer = t;
            }
            if (nextHistoryCursor == -1) {
              historyBuffer = null;
            }
            historyCursor = nextHistoryCursor;
          }
          break;
        case RIGHT:
          console.getViewReader().moveRight();
          break;
        case LEFT:
          console.getViewReader().moveLeft();
          break;
        case BREAK:
          log.debug("Want to cancel evaluation");
          console.clearBuffer();
          return TermEvent.brk();
        case CHAR:
          if (code >= 0 && code < 128) {
            console.getViewReader().append((char)code);
          } else {
            log.debug("Unhandled char " + code);
          }
          break;
        case TAB:
          log.debug("Tab");
          return TermEvent.complete(console.getBufferToCursor());
      }

      //
      if (console.getReader().hasNext()) {
        historyCursor = -1;
        historyBuffer = null;
        CharSequence input = console.getReader().next();
        return TermEvent.readLine(input);
      }
    }
  }

  public Appendable getInsertBuffer() {
    return console.getViewReader();
  }

  public void addToHistory(CharSequence line) {
    history.addFirst(line);
  }

  public CharSequence getBuffer() {
    return console.getBufferToCursor();
  }

  public void close() {
    try {
      log.debug("Closing connection");
      io.flush();
      io.close();
    } catch (IOException e) {
      log.debug("Exception thrown during term close()", e);
    }
  }

  public void write(Data data) throws IOException {
    ConsoleWriter writer = console.getWriter();
    for (DataFragment f : data) {
      if (f instanceof FormattingData) {
        writer.write(((FormattingData)f).getStyle());
      } else {
        writer.write(f.toString());
      }
    }
  }
}
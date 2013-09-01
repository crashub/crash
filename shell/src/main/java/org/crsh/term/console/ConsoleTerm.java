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

package org.crsh.term.console;

import org.crsh.term.CodeType;
import org.crsh.term.Term;
import org.crsh.term.TermEvent;
import org.crsh.term.spi.TermIO;
import org.crsh.text.CLS;
import org.crsh.text.Chunk;
import org.crsh.text.Style;
import org.crsh.text.Text;

import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the {@link Term interface}.
 */
public class ConsoleTerm implements Term {

  /** . */
  private final Logger log = Logger.getLogger(ConsoleTerm.class.getName());

  /** . */
  private final LinkedList<CharSequence> history;

  /** . */
  private CharSequence historyBuffer;

  /** . */
  private int historyCursor;

  /** . */
  private final TermIO io;

  /** . */
  private final TermIOBuffer buffer;

  /** . */
  private final TermIOWriter writer;

  public ConsoleTerm(final TermIO io) {
    this.history = new LinkedList<CharSequence>();
    this.historyBuffer = null;
    this.historyCursor = -1;
    this.io = io;
    this.buffer = new TermIOBuffer(io);
    this.writer = new TermIOWriter(io);
  }

  public int getWidth() {
    return io.getWidth();
  }

  public int getHeight() {
    return io.getHeight();
  }

  public String getProperty(String name) {
    return io.getProperty(name);
  }

  public void setEcho(boolean echo) {
    buffer.setEchoing(echo);
  }

  public boolean takeAlternateBuffer() throws IOException {
    return io.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return io.releaseAlternateBuffer();
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
          buffer.del();
          break;
        case UP:
        case DOWN:
          int nextHistoryCursor = historyCursor +  (type == CodeType.UP ? + 1 : -1);
          if (nextHistoryCursor >= -1 && nextHistoryCursor < history.size()) {
            CharSequence s = nextHistoryCursor == -1 ? historyBuffer : history.get(nextHistoryCursor);
            while (buffer.moveRight()) {
              // Do nothing
            }
            CharSequence t = buffer.replace(s);
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
          buffer.moveRight();
          break;
        case LEFT:
          buffer.moveLeft();
          break;
        case BREAK:
          log.log(Level.FINE, "Want to cancel evaluation");
          buffer.clear();
          return TermEvent.brk();
        case CHAR:
          if (code >= 0 && code < 128) {
            buffer.append((char)code);
          } else {
            log.log(Level.FINE, "Unhandled char " + code);
          }
          break;
        case TAB:
          log.log(Level.FINE, "Tab");
          return TermEvent.complete(buffer.getBufferToCursor());
        case BACKWARD_WORD: {
          int cursor = buffer.getCursor();
          int pos = cursor;
          // Skip any white char first
          while (pos > 0 && buffer.charAt(pos - 1) == ' ') {
            pos--;
          }
          // Skip until next white char
          while (pos > 0 && buffer.charAt(pos - 1) != ' ') {
            pos--;
          }
          if (pos < cursor) {
            buffer.moveLeft(cursor - pos);
          }
          break;
        }
        case FORWARD_WORD: {
          int size = buffer.getSize();
          int cursor = buffer.getCursor();
          int pos = cursor;
          // Skip any white char first
          while (pos < size && buffer.charAt(pos) == ' ') {
            pos++;
          }
          // Skip until next white char
          while (pos < size && buffer.charAt(pos) != ' ') {
            pos++;
          }
          if (pos > cursor) {
            buffer.moveRight(pos - cursor);
          }
          break;
        }
        case BEGINNING_OF_LINE: {
          int cursor = buffer.getCursor();
          if (cursor > 0) {
            buffer.moveLeft(cursor);
          }
          break;
        }
        case END_OF_LINE: {
          int cursor = buffer.getSize() - buffer.getCursor();
          if (cursor > 0) {
            buffer.moveRight  (cursor);
          }
          break;
        }
      }

      //
      if (buffer.hasNext()) {
        historyCursor = -1;
        historyBuffer = null;
        CharSequence input = buffer.next();
        return TermEvent.readLine(input);
      }
    }
  }

  public Appendable getDirectBuffer() {
    return buffer;
  }

  public void addToHistory(CharSequence line) {
    history.addFirst(line);
  }

  public CharSequence getBuffer() {
    return buffer.getBufferToCursor();
  }

  public void flush() {
    try {
      io.flush();
    }
    catch (IOException e) {
      log.log(Level.FINE, "Exception thrown during term flush()", e);
    }
  }

  public void close() {
    try {
      log.log(Level.FINE, "Closing connection");
      io.flush();
      io.close();
    } catch (IOException e) {
      log.log(Level.FINE, "Exception thrown during term close()", e);
    }
  }

  public Class<Chunk> getConsumedType() {
    return Chunk.class;
  }

  public void write(Chunk chunk) throws IOException {
    provide(chunk);
  }

  public void provide(Chunk element) throws IOException {
    if (element == null) {
      throw new NullPointerException("No null chunk accepted");
    }
    if (element instanceof Text) {
      Text textChunk = (Text)element;
      writer.write(textChunk.getText());
    } else if (element instanceof Style) {
      io.write(((Style)element));
    } else if (element instanceof CLS) {
      io.cls();
    } else {
      throw new UnsupportedOperationException("todo");
    }
  }
}
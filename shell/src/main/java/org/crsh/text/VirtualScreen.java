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
package org.crsh.text;

import org.crsh.util.Pair;
import org.crsh.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A virtual screen that can be scrolled. This class is thread safe, as it can be used concurrently by two
 * threads, for example one thread can provide new elements while another thread is repainting the buffer
 * to the screen, both threads can either modify the underlying data structure. Paint could also be called concurrently
 * by two threads, one that just provided a new element and wants to repaint the structure and another that changes
 * the current cursor and asks for a repaint too.
 *
 * @author Julien Viet
 */
public class VirtualScreen implements ScreenContext {

  /** The cached width and height for the current refresh. */
  private int width, height;

  /** . */
  private final ArrayList<Foo> buffer;

  /** The current style for last chunk in the buffer. */
  private Style style;

  /** The absolute offset, index and row. */
  private int offset, index, row;

  /** The cursor coordinate. */
  private int cursorX, cursorY;

  // Invariant:
  // currentIndex always points at the end of a valid offset
  // except when the buffer is empty, in this situation we have
  // (currentOffset = 0, currentIndex = 0)
  // othewise we always have
  // (currentOffset = 0, currentIndex = 1) for {"a"} and not (currentOffset = 1, currentIndex = 0)

  /** The cursor offset in the {@link #buffer}. */
  private int cursorOffset;

  /** The cursor index in the chunk at the current {@link #cursorOffset}. */
  private int cursorIndex;

  /** . */
  private Style cursorStyle;

  /** . */
  private final ScreenContext out;

  /** Do we need to clear screen. */
  private int status;

  private static final int
      REFRESH = 0,  // Need a full refresh
      PAINTING = 1, // Screen is partially painted
      PAINTED = 3;  // Screen is fully painted

  private static class Foo {
    final CharSequence text;
    final Style style;
    private Foo(CharSequence text, Style style) {
      this.text = text;
      this.style = style;
    }
  }

  public VirtualScreen(ScreenContext out) {
    this.out = out;
    this.width = Utils.notNegative(out.getWidth());
    this.height = Utils.notNegative(out.getHeight());
    this.cursorX = 0;
    this.cursorY = 0;
    this.cursorOffset = 0;
    this.cursorIndex = 0;
    this.offset = 0;
    this.index = 0;
    this.row = 0;
    this.buffer = new ArrayList<Foo>();
    this.style = Style.style();
    this.status = REFRESH;
    this.cursorStyle = null; // on purpose
  }

  public int getWidth() {
    return out.getWidth();
  }

  public int getHeight() {
    return out.getHeight();
  }

  @Override
  public Screenable append(CharSequence s) throws IOException {
    buffer.add(new Foo(s, style));
    return this;
  }

  @Override
  public Screenable append(char c) throws IOException {
    return append(Character.toString(c));
  }

  @Override
  public Screenable append(CharSequence csq, int start, int end) throws IOException {
    return append(csq.subSequence(start, end));
  }

  @Override
  public Screenable append(Style style) throws IOException {
    this.style = style.merge(style);
    return this;
  }

  @Override
  public Screenable cls() throws IOException {
    buffer.clear();
    cursorX = 0;
    cursorY = 0;
    cursorOffset = 0;
    cursorIndex = 0;
    offset = 0;
    index = 0;
    row = 0;
    status = REFRESH;
    return this;
  }

  /**
   * Pain the underlying screen context.
   *
   * @return this screen buffer
   * @throws IOException any io exception
   */
  public synchronized VirtualScreen paint() throws IOException {
    if (status == REFRESH) {
      out.cls();
      out.append(Style.reset);
      cursorStyle = Style.reset;
      status = PAINTING;
    }
    if (buffer.size() > 0) {
      // We ensure there is a least one chunk in the buffer, otherwise it will throw a NullPointerException
      int prev = cursorIndex;
      while (cursorX < width && cursorY < height) {
        if (cursorIndex >= buffer.get(cursorOffset).text.length()) {
          if (prev < cursorIndex) {
            if (!buffer.get(cursorOffset).style.equals(cursorStyle)) {
              out.append(buffer.get(cursorOffset).style);
              cursorStyle = cursorStyle.merge(buffer.get(cursorOffset).style);
            }
            out.append(buffer.get(cursorOffset).text, prev, cursorIndex);
          }
          if (cursorOffset + 1 >= buffer.size()) {
            return this;
          } else {
            prev = 0;
            cursorIndex = 0;
            cursorOffset++;
          }
        } else {
          char c = buffer.get(cursorOffset).text.charAt(cursorIndex);
          if (c == '\n') {
            cursorX = 0;
            cursorY++;
            if (cursorY < height) {
              cursorIndex++;
            }
          } else if (c >= 32) {
            cursorX++;
            cursorIndex++; // Not sure that should be done all the time -> maybe bug with edge case
            if (cursorX == width) {
              cursorX = 0;
              cursorY++;
            }
          } else {
            cursorIndex++;
          }
        }
      }
      if (prev < cursorIndex) {
        if (!buffer.get(cursorOffset).style.equals(cursorStyle)) {
          out.append(buffer.get(cursorOffset).style);
          cursorStyle = cursorStyle.merge(buffer.get(cursorOffset).style);
        }
        out.append(buffer.get(cursorOffset).text.subSequence(prev, cursorIndex));
      }
      status = PAINTED;
    }
    return this;
  }

  public synchronized boolean previousRow() throws IOException {
    // Current strategy is to increment updates, a bit dumb, but fast (in memory) and works
    // correctly
    if (row > 0) {
      int previousOffset = 0;
      int previousIndex = 0;
      int previousRow = 0;
      while (previousRow < row - 1) {
        Pair<Integer, Integer> next = nextRow(previousOffset, previousIndex, width);
        if (next != null) {
          previousOffset = next.getFirst();
          previousIndex = next.getSecond();
          previousRow++;
        } else {
          break;
        }
      }
      status = REFRESH;
      cursorX = cursorY = 0;
      cursorOffset = offset = previousOffset;
      cursorIndex = index = previousIndex;
      row = previousRow;
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return true if the buffer is painted
   */
  public synchronized boolean isPainted() {
    return status == PAINTED;
  }

  /**
   * @return true if the buffer is stale and needs a full repaint
   */
  public synchronized boolean isRefresh() {
    return status == REFRESH;
  }

  /**
   * @return true if the buffer is waiting for input to become painted
   */
  public synchronized boolean isPainting() {
    return status == PAINTING;
  }

  public synchronized boolean nextRow() throws IOException {
    return scroll(1) == 1;
  }

  public synchronized int nextPage() throws IOException {
    return scroll(height);
  }

  private int scroll(int amount) throws IOException {
    if (amount < 0) {
      throw new UnsupportedOperationException("Not implemented for negative operations");
    } else if (amount == 0) {
      // Nothing to do
      return 0;
    } else {
      // This mean we already painted the screen and therefore maybe we can scroll
      if (isPainted()) {
        int count = 0;
        int _offset = cursorOffset;
        int _index = cursorIndex;
        while (count < amount) {
          Pair<Integer, Integer> next = nextRow(_offset, _index, width);
          if (next != null) {
            _offset = next.getFirst();
            _index = next.getSecond();
            count++;
          } else {
            // Perhaps we can scroll one more line
            if (nextRow(_offset, _index, 1) != null) {
              count++;
            }
            break;
          }
        }
        if (count > 0) {
          _offset = offset;
          _index = index;
          for (int i = 0;i < count;i++) {
            Pair<Integer, Integer> next = nextRow(_offset, _index, width);
            _offset = next.getFirst();
            _index = next.getSecond();
          }
          status = REFRESH;
          cursorX = cursorY = 0;
          cursorOffset = offset = _offset;
          cursorIndex = index = _index;
          row += count;
        }
        return count;
      } else {
        return 0;
      }
    }
  }

  private Pair<Integer, Integer> nextRow(int offset, int index, int width) {
    int count = 0;
    while (true) {
      if (index >= buffer.get(offset).text.length()) {
        if (offset + 1 >= buffer.size()) {
          return null;
        } else {
          index = 0;
          offset++;
        }
      } else {
        char c = buffer.get(offset).text.charAt(index++);
        if (c == '\n') {
          return new Pair<Integer, Integer>(offset, index);
        } else if (c >= 32) {
          if (++count == width) {
            return new Pair<Integer, Integer>(offset, index);
          }
        }
      }
    }
  }

  public synchronized boolean update() throws IOException {
    int nextWidth = out.getWidth();
    int nextHeight = out.getHeight();
    if (width != nextWidth || height != nextHeight) {
      width = nextWidth;
      height = nextHeight;
      if (buffer.size() > 0) {
        cursorIndex = index;
        cursorOffset = offset;
        cursorX = 0;
        cursorY = 0;
        status = REFRESH;
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public synchronized void flush() throws IOException {
    // I think flush should not always be propagated, specially when we consider that the screen context
    // is already filled
    out.flush();
  }
}

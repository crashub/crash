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

import java.util.Iterator;

/**
 * A line oriented renderer.
 */
public abstract class LineRenderer {

  public static final LineRenderer NULL = new LineRenderer() {
    @Override
    public int getActualWidth() {
      return 0;
    }
    @Override
    public int getMinWidth() {
      return 0;
    }
    @Override
    public int getMinHeight(int width) {
      return 0;
    }
    @Override
    public int getActualHeight(int width) {
      return 0;
    }
    @Override
    public LineReader reader(int width) {
      return new LineReader() {
        public boolean hasLine() {
          return false;
        }
        public void renderLine(RenderAppendable to) throws IllegalStateException {
          throw new IllegalStateException();
        }
      };
    }
  };

  public static LineRenderer vertical(Iterable<? extends LineRenderer> renderers) {
    Iterator<? extends LineRenderer> i = renderers.iterator();
    if (i.hasNext()) {
      LineRenderer renderer = i.next();
      if (i.hasNext()) {
        return new Composite(renderers);
      } else {
        return renderer;
      }
    } else {
      return NULL;
    }
  }

  /**
   * Returns the element actual width.
   *
   * @return the actual width
   */
  public abstract int getActualWidth();

  /**
   * Returns the element minimum width.
   *
   * @return the minimum width
   */
  public abstract int getMinWidth();

  /**
   * Return the minimum height for the specified with.
   *
   * @param width the width
   * @return the actual height
   */
  public abstract int getMinHeight(int width);

  /**
   * Return the actual height for the specified with.
   *
   * @param width the width
   * @return the minimum height
   */
  public abstract int getActualHeight(int width);

  /**
   * Create a renderer for the specified width and height or return null if the element does not provide any output
   * for the specified dimensions. The default implementation delegates to the {@link #reader(int)} method when the
   * <code>height</code> argument is not positive otherwise it returns null. Subclasses should override this method
   * when they want to provide content that can adapts to the specified height.
   *
   * @param width the width
   * @param height the height
   * @return the renderer
   */
  public LineReader reader(int width, int height) {
    if (height > 0) {
      return null;
    } else {
      return reader(width);
    }
  }

  /**
   * Create a renderer for the specified width or return null if the element does not provide any output.
   *
   * @param width the width
   * @return the renderer
   */
  public abstract LineReader reader(int width);

  /**
   * Renders this object to the provided output.
   *
   * @param out the output
   */
  public final void render(RenderAppendable out) {
    LineReader renderer = reader(out.getWidth());
    if (renderer != null) {
      while (renderer.hasLine()) {
        renderer.renderLine(out);
        out.append('\n');
      }
    }
  }

  private static class Composite extends LineRenderer {

    /** . */
    private final Iterable<? extends LineRenderer> renderers;

    /** . */
    private final int actualWidth;

    /** . */
    private final int minWidth;

    private Composite(Iterable<? extends LineRenderer> renderers) {

      int actualWidth = 0;
      int minWidth = 0;
      for (LineRenderer renderer : renderers) {
        actualWidth = Math.max(actualWidth, renderer.getActualWidth());
        minWidth = Math.max(minWidth, renderer.getMinWidth());
      }

      this.actualWidth = actualWidth;
      this.minWidth = minWidth;
      this.renderers = renderers;
    }

    @Override
    public int getActualWidth() {
      return actualWidth;
    }

    @Override
    public int getMinWidth() {
      return minWidth;
    }

    @Override
    public int getActualHeight(int width) {
      int actualHeight = 0;
      for (LineRenderer renderer : renderers) {
        actualHeight += renderer.getActualHeight(width);
      }
      return actualHeight;
    }

    @Override
    public int getMinHeight(int width) {
      return 1;
    }

    @Override
    public LineReader reader(final int width, final int height) {

      final Iterator<? extends LineRenderer> i = renderers.iterator();

      //
      return new LineReader() {

        /** . */
        private LineReader current;

        /** . */
        private int index = 0;

        public boolean hasLine() {
          if (height > 0 && index >= height) {
            return false;
          } else {
            if (current == null || !current.hasLine()) {
              while (i.hasNext()) {
                LineRenderer next = i.next();
                LineReader reader = next.reader(width);
                if (reader != null && reader.hasLine()) {
                  current = reader;
                  return true;
                }
              }
              return false;
            } else {
              return true;
            }
          }
        }

        public void renderLine(RenderAppendable to) throws IllegalStateException {
          if (hasLine()) {
            current.renderLine(to);
            index++;
          } else {
            throw new IllegalStateException();
          }
        }
      };
    }

    @Override
    public LineReader reader(final int width) {
      return reader(width, -1);
    }
  }
}

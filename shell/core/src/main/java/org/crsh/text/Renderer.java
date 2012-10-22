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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Something that can be rendered within a context.
 */
public abstract class Renderer {

  public static final Renderer NULL = new Renderer() {
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

  public static Renderer compose(Iterable<Renderer> renderers) {
    Iterator<Renderer> i = renderers.iterator();
    if (i.hasNext()) {
      Renderer renderer = i.next();
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

  private static class Composite extends Renderer {

    /** . */
    private final Iterable<? extends Renderer> renderers;

    /** . */
    private final int actualWidth;

    /** . */
    private final int minWidth;

    private Composite(Iterable<? extends Renderer> renderers) {

      int actualWidth = 0;
      int minWidth = 0;
      for (Renderer renderer : renderers) {
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
      for (Renderer renderer : renderers) {
        actualHeight += renderer.getActualHeight(width);
      }
      return actualHeight;
    }

    @Override
    public int getMinHeight(int width) {
      int minHeight = 0;
      for (Renderer renderer : renderers) {
        minHeight += renderer.getMinHeight(width);
      }
      return minHeight;
    }

    @Override
    public LineReader reader(final int width) {

      //
      final ArrayList<LineReader> readers = new ArrayList<LineReader>();
      for (Renderer renderer : renderers) {
        LineReader reader = renderer.reader(width);
        readers.add(reader);
      }

      //
      return new LineReader() {

        /** . */
        Iterator<? extends LineReader> i = readers.iterator();

        /** . */
        LineReader current = null;

        public boolean hasLine() {
          while (true) {
            if (current != null) {
              if (current.hasLine()) {
                break;
              } else {
                current = null;
              }
            } else {
              if (i.hasNext()) {
                current = i.next();
              } else {
                break;
              }
            }
          }
          return current != null;
        }

        public void renderLine(RenderAppendable to) throws IllegalStateException {
          if (!hasLine()) {
            throw new IllegalStateException();
          }
          current.renderLine(to);
        }
      };
    }
  }
}

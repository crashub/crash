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

import org.crsh.text.ui.LabelElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Provide a renderable.
 */
public abstract class Renderable<E> {

  /** . */
  private static final Renderable<?>[] renderables;

  static {
    ArrayList<Renderable<?>> tmp = new ArrayList<Renderable<?>>();
    Iterator<Renderable> i = ServiceLoader.load(Renderable.class).iterator();
    while (i.hasNext()) {
      try {
        Renderable renderable = i.next();
        tmp.add(renderable);
      }
      catch (ServiceConfigurationError e) {
        // Config error
      }
    }
    renderables = tmp.toArray(new Renderable<?>[tmp.size()]);
  }

  public static Renderable<Object> ANY = new Renderable<Object>() {
    @Override
    public Class<Object> getType() {
      return Object.class;
    }

    @Override
    public Renderer renderer(Iterator<Object> stream) {
      StringBuilder sb = new StringBuilder();
      while (stream.hasNext()) {
        Object next = stream.next();
        if (next instanceof Chunk) {
          if (next instanceof Text) {
            sb.append(((Text)next).buffer);
          }
        } else {
          sb.append(next);
        }
      }
      return new LabelElement(sb.toString()).renderer();
    }
  };

  public static <I> Renderable<? super I> getRenderable(Class<I> itemType) {
    for (Renderable<?> formatter : renderables) {
      try {
        if (formatter.getType().isAssignableFrom(itemType)) {
          return (Renderable<I>)formatter;
        }
      }
      catch (Exception e) {
      }
      catch (NoClassDefFoundError e) {
      }
    }
    return null;
  }

  public abstract Class<E> getType();

  public abstract Renderer renderer(Iterator<E> stream);

}

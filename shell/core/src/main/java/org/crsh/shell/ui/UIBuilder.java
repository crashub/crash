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

package org.crsh.shell.ui;

import groovy.util.BuilderSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UIBuilder extends BuilderSupport {

  /** . */
  private final List<Element> elements;

  public UIBuilder() {
    this.elements = new ArrayList<Element>();
  }

  public List<Element> getElements() {
    return elements;
  }

  @Override
  protected Object createNode(Object name) {
    return createNode(name, (Object)null);
  }

  @Override
  protected Object createNode(Object name, Object value) {
    if ("node".equals(name)) {
      if (value == null) {
        return new TreeElement();
      } else {
        return new TreeElement(new LabelElement(value));
      }
    } else if ("label".equals(name)) {
      return new LabelElement(value);
    } else {
      throw new UnsupportedOperationException("Cannot build object with name " + name + " and value " + value);
    }
  }

  @Override
  protected Object createNode(Object name, Map attributes, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Object createNode(Object name, Map attributes) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void setParent(Object parent, Object child) {
    if (parent instanceof TreeElement) {
      TreeElement parentElement = (TreeElement)parent;
      Element childElement = (Element)child;
      parentElement.addNode(childElement);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  protected void nodeCompleted(Object parent, Object child) {
    if (parent == null) {
      elements.add((Element)child);
    }
    super.nodeCompleted(parent, child);
  }
}

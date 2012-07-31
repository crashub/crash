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
import org.crsh.text.Color;
import org.crsh.text.Decoration;

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

    return initElement(name, value);

  }

  @Override
  protected Object createNode(Object name, Map attributes, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Object createNode(Object name, Map attributes) {

    Element e = initElement(name, attributes.get("value"));
    setStyles(e, attributes);
    Boolean border = (Boolean) attributes.get("border");
    if (e instanceof TableElement) {
      ((TableElement) e).setBorder(border == null ? false : border);
    }
    return e;
    
  }

  private Element initElement(Object name, Object value) {

    if ("node".equals(name)) {
      if (value == null) {
        return new TreeElement();
      } else {
        return new TreeElement(new LabelElement(value));
      }
    } else if ("label".equals(name)) {
      return new LabelElement(value);
    } else if ("table".equals(name)) {
      return new TableElement();
    } else if ("row".equals(name)) {
      return new RowElement();
    } else if ("header".equals(name)) {
      return new RowElement(true);
    } else {
      throw new UnsupportedOperationException("Cannot build object with name " + name + " and value " + value);
    }

  }
  
  private void setStyles(Element e, Map attributes) {
    e.setDecoration((Decoration) attributes.get("decoration"));
    e.setForeground((Color) attributes.get("foreground"));
    e.setBackground((Color) attributes.get("background"));
  }

  @Override
  protected void setParent(Object parent, Object child) {
    if (parent instanceof TreeElement) {
      TreeElement parentElement = (TreeElement)parent;
      Element childElement = (Element)child;
      parentElement.addNode(childElement);
      childElement.setParent(parentElement);
    } else if (parent instanceof TableElement) {
      TableElement parentElement = (TableElement)parent;
      RowElement childElement = (RowElement)child;
      parentElement.addRow(childElement);
      childElement.setParent(parentElement);
    } else if (parent instanceof RowElement) {
      RowElement parentElement = (RowElement)parent;
      Element childElement = (Element)child;
      if (child instanceof TreeElement) {
        throw new IllegalArgumentException("A table cannot contain node element");
      }
      parentElement.addValue(childElement);
      childElement.setParent(parentElement);
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

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

package org.crsh.text.ui;

import groovy.util.BuilderSupport;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
  protected Object createNode(Object name, Map attributes, Object value) {
    Element element;
    if ("node".equals(name)) {
      if (value == null) {
        element = new TreeElement();
      } else {
        element = new TreeElement(new LabelElement(value));
      }
    } else if ("label".equals(name)) {
      element = new LabelElement(value);
    } else if ("table".equals(name)) {
      element = new TableElement();
    } else if ("row".equals(name)) {
      element = new RowElement();
    } else if ("header".equals(name)) {
      element = new RowElement(true);
    } else {
      throw new UnsupportedOperationException("Cannot build object with name " + name + " and value " + value);
    }

    //
    Style.Composite style = element.getStyle();
    if (style == null) {
      style = Style.style();
    }
    style = style.
        foreground((Color)attributes.get("fg")).
        foreground((Color)attributes.get("foreground")).
        background((Color)attributes.get("bg")).
        background((Color)attributes.get("background")).
        bold((Boolean)attributes.get("bold")).
        underline((Boolean)attributes.get("underline")).
        blink((Boolean)attributes.get("blink"));
    element.setStyle(style);

    //
    if (element instanceof TableElement) {
      TableElement table = (TableElement)element;

      // Weights
      Object weightsAttr = attributes.get("weights");
      if (weightsAttr instanceof Iterable) {
        List<Integer> list = Utils.list((Iterable<Integer>)weightsAttr);
        int[] weights = new int[list.size()];
        for (int i = 0;i < weights.length;i++) {
          weights[i] = list.get(i);
        }
        table.layout(ColumnLayout.weighted(weights));
      }

      // Border
      Object border = attributes.get("border");
      Border borderChar;
      if (border instanceof Boolean && (Boolean)border) {
        borderChar = Border.dashed;
      } else if (border instanceof Border) {
        borderChar = (Border)border;
      } else {
        borderChar = null;
      }
      table.border(borderChar);
    }

    //
    return element;
  }

  @Override
  protected Object createNode(Object name, Object value) {
    return createNode(name, Collections.emptyMap(), value);
  }

  @Override
  protected Object createNode(Object name, Map attributes) {
    return createNode(name, attributes, null);
  }

  @Override
  protected void setParent(Object parent, Object child) {
    if (parent instanceof TreeElement) {
      TreeElement parentElement = (TreeElement)parent;
      Element childElement = (Element)child;
      parentElement.addChild(childElement);
    } else if (parent instanceof TableElement) {
      TableElement parentElement = (TableElement)parent;
      RowElement childElement = (RowElement)child;
      parentElement.add(childElement);
    } else if (parent instanceof RowElement) {
      RowElement parentElement = (RowElement)parent;
      Element childElement = (Element)child;
      if (child instanceof TreeElement) {
        throw new IllegalArgumentException("A table cannot contain a tree element");
      }
      parentElement.add(childElement);
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

  @Override
  public String toString() {
    throw new UnsupportedOperationException();
  }
}

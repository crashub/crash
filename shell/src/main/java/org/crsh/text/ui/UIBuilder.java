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

import groovy.lang.Closure;
import groovy.util.BuilderSupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.crsh.text.Color;
import org.crsh.text.LineRenderer;
import org.crsh.text.Style;
import org.crsh.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UIBuilder extends BuilderSupport implements Iterable<LineRenderer> {

  /** . */
  private final List<Element> elements;

  public UIBuilder() {
    this.elements = new ArrayList<Element>();
  }

  public List<Element> getElements() {
    return elements;
  }

  @Override
  protected Object doInvokeMethod(String methodName, Object name, Object args) {
    if ("eval".equals(name)) {
      List list = InvokerHelper.asList(args);
      if (list.size() == 1 && list.get(0) instanceof Closure) {
        EvalElement element = (EvalElement)super.doInvokeMethod(methodName, name, null);
        element.closure = (Closure)list.get(0);
        return element;
      } else {
        return super.doInvokeMethod(methodName, name, args);
      }
    } else {
      return super.doInvokeMethod(methodName, name, args);
    }
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
    } else if ("eval".equals(name)) {
      element = new EvalElement();
    } else {
      throw new UnsupportedOperationException("Cannot build object with name " + name + " and value " + value);
    }

    //
    Style.Composite style = element.getStyle();
    if (style == null) {
      style = Style.style();
    }
    style = style.
      bold((Boolean)attributes.get("bold")).
      underline((Boolean)attributes.get("underline")).
      blink((Boolean)attributes.get("blink"));
    if (attributes.containsKey("fg")) {
      style = style.foreground((Color)attributes.get("fg"));
    }
    if (attributes.containsKey("foreground")) {
      style = style.foreground((Color)attributes.get("foreground"));
    }
    if (attributes.containsKey("bg")) {
      style = style.background((Color)attributes.get("bg"));
    }
    if (attributes.containsKey("background")) {
      style = style.background((Color)attributes.get("background"));
    }
    element.setStyle(style);

    //
    if (element instanceof TableElement) {
      TableElement table = (TableElement)element;

      // Columns
      Object columns = attributes.get("columns");
      if (columns instanceof Iterable) {
        List<Integer> list = Utils.list((Iterable<Integer>)columns);
        int[] weights = new int[list.size()];
        for (int i = 0;i < weights.length;i++) {
          weights[i] = list.get(i);
        }
        table.withColumnLayout(Layout.weighted(weights));
      }

      // Columns
      Object rows = attributes.get("rows");
      if (rows instanceof Iterable) {
        List<Integer> list = Utils.list((Iterable<Integer>)rows);
        int[] weights = new int[list.size()];
        for (int i = 0;i < weights.length;i++) {
          weights[i] = list.get(i);
        }
        table.withRowLayout(Layout.weighted(weights));
      }

      // Border
      Object borderAttr = attributes.get("border");
      BorderStyle border;
      if (borderAttr instanceof Boolean && (Boolean)borderAttr) {
        border = BorderStyle.DASHED;
      } else if (borderAttr instanceof BorderStyle) {
        border = (BorderStyle)borderAttr;
      } else {
        border = null;
      }
      table.border(border);

      // Separator
      Object separatorAttr = attributes.get("separator");
      BorderStyle separator;
      if (separatorAttr instanceof Boolean && (Boolean)separatorAttr) {
        separator = BorderStyle.DASHED;
      } else if (separatorAttr instanceof BorderStyle) {
        separator = (BorderStyle)separatorAttr;
      } else {
        separator = null;
      }
      table.separator(separator);

      // Overflow
      Object overflowAttr = attributes.get("overflow");
      Overflow overflow;
      if ("hidden".equals(overflowAttr)) {
        overflow = Overflow.HIDDEN;
      } else if ("wrap".equals(overflowAttr)) {
        overflow = Overflow.WRAP;
      } else if (overflowAttr instanceof Overflow) {
        overflow = (Overflow)separatorAttr;
      } else {
        overflow = Overflow.WRAP;
      }
      table.overflow(overflow);

      // Cell left padding
      Object leftCellPaddingAttr = attributes.get("leftCellPadding");
      int leftCellPadding = 0;
      if (leftCellPaddingAttr instanceof Number) {
        leftCellPadding = ((Number)leftCellPaddingAttr).intValue();
      }
      table.setLeftCellPadding(leftCellPadding);

      // Cell right padding
      Object rightCellPaddingAttr = attributes.get("rightCellPadding");
      int rightCellPadding = 0;
      if (rightCellPaddingAttr instanceof Number) {
        rightCellPadding = ((Number)rightCellPaddingAttr).intValue();
      }
      table.setRightCellPadding(rightCellPadding);
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
      throw new UnsupportedOperationException("Unrecognized parent " + parent);
    }
  }

  @Override
  protected void nodeCompleted(Object parent, Object child) {
    if (parent == null) {
      elements.add((Element)child);
    }
    super.nodeCompleted(parent, child);
  }

  public Iterator<LineRenderer> iterator() {
    return new Iterator<LineRenderer>() {
      Iterator<Element> i = elements.iterator();
      public boolean hasNext() {
        return i.hasNext();
      }
      public LineRenderer next() {
        return i.next().renderer();
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}

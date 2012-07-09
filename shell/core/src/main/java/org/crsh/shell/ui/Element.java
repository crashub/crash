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

import org.crsh.command.InvocationContext;
import org.crsh.shell.io.ShellWriter;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Element {

  /** . */
  private Decoration decoration;

  /** . */
  private Color foreground;

  /** . */
  private Color background;
  
  /** . */
  private Element parent;

  public void print(ShellWriter writer, InvocationContext context) throws IOException {
    print(new UIWriterContext(context), writer);
  }

  public void print(UIWriterContext ctx, ShellWriter writer) throws IOException {

    if (ctx == null) {
      throw new NullPointerException();
    }

    if (haveStyle()) {
      new FormattingElement(Style.style(getDecoration(), getForeground(), getBackground())).print(ctx, writer);
    }

    doPrint(ctx, writer);

    if (haveStyle()) {
      new FormattingElement(Style.reset).print(ctx, writer);
    }
    
  }

  abstract void doPrint(UIWriterContext ctx, ShellWriter writer) throws IOException;
  
  abstract int width();

  private boolean haveStyle() {
    return (getDecoration() != null) || (getForeground() != null) || (getBackground() != null);
  }
  
  public Decoration getDecoration() {

    if (decoration != null) {
      return decoration;
    } else if (parent != null) {
      return parent.getDecoration();
    } else {
      return null;
    }
    
  }

  public Color getForeground() {

    if (foreground != null) {
      return foreground;
    } else if (parent != null) {
      return parent.getForeground();
    } else {
      return null;
    }

  }

  public Color getBackground() {

    if (background != null) {
      return background;
    } else if (parent != null) {
      return parent.getBackground();
    } else {
      return null;
    }
        
  }

  public void setDecoration(Decoration decoration) {
    this.decoration = decoration;
  }

  public void setForeground(Color foreground) {
    this.foreground = foreground;
  }

  public void setBackground(Color background) {
    this.background = background;
  }

  public Element getParent() {
    return parent;
  }

  public void setParent(Element parent) {
    this.parent = parent;
  }
}

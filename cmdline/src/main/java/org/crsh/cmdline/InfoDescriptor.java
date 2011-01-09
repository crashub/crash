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

package org.crsh.cmdline;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class InfoDescriptor {

  /** . */
  private final String display;

  /** . */
  private final String usage;

  /** . */
  private final String man;

  public InfoDescriptor() {
    this.display = this.usage = this.man = "";
  }

  InfoDescriptor(InfoDescriptor child, InfoDescriptor parent) {
    if (child == null) {
      throw new NullPointerException();
    }
    if (parent == null) {
      throw new NullPointerException();
    }

    //
    this.display = child.display.length() > 0 ? child.display : parent.display;
    this.usage = child.usage.length() > 0 ? child.usage : parent.usage;
    this.man = child.man.length() > 0 ? child.man : parent.man;
  }

  InfoDescriptor(String display, String usage, String man) {
    if (display == null) {
      throw new NullPointerException();
    }
    if (usage == null) {
      throw new NullPointerException();
    }
    if (man == null) {
      throw new NullPointerException();
    }

    //
    this.display = display;
    this.usage = usage;
    this.man = man;
  }

  public InfoDescriptor(AnnotatedElement annotated) {
    this(annotated.getAnnotations());
  }

  InfoDescriptor(Annotation... annotations) {
    if (annotations == null) {
      throw new NullPointerException();
    }

    //
    String display = "";
    String usage = "";
    String man = "";
    for (Annotation annotation : annotations) {
      if (annotation instanceof Description) {
        display = ((Description)annotation).value();
      } else if (annotation instanceof Usage) {
        usage = ((Usage)annotation).value();
      } else if (annotation instanceof Man) {
        man = ((Man)annotation).value();
      }
    }

    //
    this.display  = display;
    this.usage  = usage;
    this.man  = man;
  }

  public String getDisplay() {
    return display;
  }

  public String getUsage() {
    return usage;
  }

  public String getMan() {
    return man;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof InfoDescriptor) {
      InfoDescriptor that = (InfoDescriptor)obj;
      return display.equals(that.display) && usage.equals(that.usage) && man.equals(that.man);
    } else {
      return false;
    }
  }
}

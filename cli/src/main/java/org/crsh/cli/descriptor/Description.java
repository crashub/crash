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

package org.crsh.cli.descriptor;

import org.crsh.cli.Man;
import org.crsh.cli.Usage;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public final class Description {

  /** . */
  private final String usage;

  /** . */
  private final String man;

  public Description() {
    this.usage = this.man = "";
  }

  public Description(Description child, Description parent) {
    if (child == null) {
      throw new NullPointerException();
    }
    if (parent == null) {
      throw new NullPointerException();
    }

    //
    this.usage = child.usage.length() > 0 ? child.usage : parent.usage;
    this.man = child.man.length() > 0 ? child.man : parent.man;
  }

  public Description(String usage, String man) {
    if (usage == null) {
      throw new NullPointerException();
    }
    if (man == null) {
      throw new NullPointerException();
    }

    //
    this.usage = usage;
    this.man = man;
  }

  public Description(AnnotatedElement annotated) {
    this(annotated.getAnnotations());
  }

  public Description(Annotation... annotations) {
    if (annotations == null) {
      throw new NullPointerException();
    }

    //
    String usage = "";
    String man = "";
    for (Annotation annotation : annotations) {
      if (annotation instanceof Usage) {
        usage = ((Usage)annotation).value();
      } else if (annotation instanceof Man) {
        man = ((Man)annotation).value();
      }
    }

    //
    this.usage  = usage;
    this.man  = man;
  }

  public String getUsage() {
    return usage;
  }

  public String getMan() {
    return man;
  }

  String getBestEffortMan() {
    if (man.length() > 0) {
      return man;
    } else {
      return usage;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Description) {
      Description that = (Description)obj;
      return usage.equals(that.usage) && man.equals(that.man);
    } else {
      return false;
    }
  }
}

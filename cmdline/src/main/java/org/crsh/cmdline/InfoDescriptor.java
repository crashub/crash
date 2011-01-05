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

  InfoDescriptor(Description description) {
    if (description == null) {
      throw new NullPointerException();
    }

    //
    this.display  = description.display();
    this.usage  = description.usage();
    this.man  = description.man();
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
}

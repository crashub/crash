/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.crsh.shell;

import org.crsh.display.structure.Element;
import org.crsh.display.structure.LabelElement;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ShellResponse {

  public static class UnkownCommand extends ShellResponse {

    /** . */
    private final String name;

    public UnkownCommand(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static class NoCommand extends ShellResponse {
  }

  public static class Close extends ShellResponse {
  }

  /**
   * Command execution is terminated.
   */
  public static class Ok extends ShellResponse {
  }

  public static class Display extends Ok implements Iterable<Element> {

    /** . */
    private final List<Element> elements;

    public Display(List<Element> elements) {
      this.elements = elements;
    }

    public Display(String label) {
      this(Collections.<Element>singletonList(new LabelElement(label)));
    }

    public Iterator<Element> iterator() {
      return elements.iterator();
    }
  }

  public static class Error extends ShellResponse {

    /** . */
    private final ErrorType type;

    /** . */
    private final Throwable throwable;

    public Error(ErrorType type, Throwable throwable) {
      this.type = type;
      this.throwable = throwable;
    }

    public ErrorType getType() {
      return type;
    }

    public Throwable getThrowable() {
      return throwable;
    }

    public String toString() {
      return "ShellResponse.Error[type=" + type + ",throwable=" + throwable.getMessage() + "]";
    }
  }
}

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

import org.crsh.command.ScriptException;
import org.crsh.display.SimpleDisplayContext;
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

  public abstract String getText();

  public static class UnkownCommand extends ShellResponse {

    /** . */
    private final String name;

    public UnkownCommand(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String getText() {
      return "Unknown command " + name;
    }
  }

  public static class NoCommand extends ShellResponse {
    @Override
    public String getText() {
      return "Please type something";
    }
  }

  public static class Close extends ShellResponse {

    @Override
    public String getText() {
      return "Have a good day!\r\n";
    }
  }

  /**
   * Command execution is terminated.
   */
  public static class Ok extends ShellResponse {
    @Override
    public String getText() {
      return "";
    }
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

    @Override
    public String getText() {
      SimpleDisplayContext context = new SimpleDisplayContext("\r\n");
      for (Element element : elements) {
        element.print(context);
      }
      return context.getText();
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

    @Override
    public String getText() {
      String result;
      if (throwable instanceof java.lang.Error) {
        throw ((java.lang.Error)throwable);
      } else if (throwable instanceof ScriptException) {
        result = "Error: " + throwable.getMessage();
      } else if (throwable instanceof RuntimeException) {
        result = "Unexpected exception: " + throwable.getMessage();
        throwable.printStackTrace(System.err);
      } else if (throwable instanceof Exception) {
        result = "Unexpected exception: " + throwable.getMessage();
        throwable.printStackTrace(System.err);
      } else {
        result = "Unexpected throwable: " + throwable.getMessage();
        throwable.printStackTrace(System.err);
      }
      return result;
    }

    public String toString() {
      return "ShellResponse.Error[type=" + type + ",throwable=" + throwable.getMessage() + "]";
    }
  }
}

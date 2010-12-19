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

package org.crsh.command.info;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Match<P> {

  /** . */
  private final P parameter;

  public Match(P parameter) {
    if (parameter == null) {
      throw new NullPointerException();
    }

    //
    this.parameter = parameter;
  }

  public final P getParameter() {
    return parameter;
  }

  public final static class Option extends Match<OptionInfo> {

    /** . */
    private final String name;

    /** . */
    private final List<String> values;

    Option(OptionInfo option, String name, List<String> values) {
      super(option);

      //
      this.name = name;
      this.values = values;
    }

    public String getName() {
      return name;
    }

    public List<String> getValues() {
      return values;
    }

    public boolean isPartial() {
      return values.contains(null);
    }

    public boolean isFull() {
      return !isPartial();
    }
  }

  public static class Argument extends Match<ArgumentInfo> {

    /** . */
    private final List<String> values;

    public Argument(ArgumentInfo argument, List<String> values) {
      super(argument);

      //
      this.values = values;
    }

    public List<String> getValues() {
      return values;
    }
  }

  public static class Command {

  }

}

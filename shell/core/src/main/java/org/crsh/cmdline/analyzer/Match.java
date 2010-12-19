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

package org.crsh.cmdline.analyzer;

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.ParameterBinding;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Match<B extends ParameterBinding> {

  public final static class Option<B extends ParameterBinding> extends Match<B> {

    /** . */
    private final OptionDescriptor<B> option;

    /** . */
    private final String name;

    /** . */
    private final List<String> values;

    Option(OptionDescriptor<B> option, String name, List<String> values) {
      this.option = option;
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

  public static class Argument<B extends ParameterBinding> extends Match<B> {

    /** . */
    private final ArgumentDescriptor<B> argument;

    /** . */
    private final List<String> values;

    /** . */
    private int start;

    /** . */
    private int end;

    public Argument(ArgumentDescriptor<B> argument, int start, int end, List<String> values) {
      this.argument = argument;
      this.start = start;
      this.end = end;
      this.values = values;
    }

    public int getStart() {
      return start;
    }

    public int getEnd() {
      return end;
    }

    public List<String> getValues() {
      return values;
    }
  }

/*
  public static class Command {

  }
*/

}

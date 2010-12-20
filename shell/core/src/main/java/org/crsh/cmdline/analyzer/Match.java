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
import org.crsh.cmdline.ParameterDescriptor;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Match<B extends ParameterBinding> {

  public static class Parameter<P extends ParameterDescriptor<B>,  B extends ParameterBinding> extends Match<B> {

    /** . */
    private final P parameter;

    /** . */
    private final List<String> values;

    public Parameter(P parameter, List<String> values) {
      this.parameter = parameter;
      this.values = values;
    }

    public P getParameter() {
      return parameter;
    }

    public List<String> getValues() {
      return values;
    }
  }

  public final static class Option<B extends ParameterBinding> extends Parameter<OptionDescriptor<B>, B> {

    /** . */
    private final String name;

    Option(OptionDescriptor<B> option, String name, List<String> values) {
      super(option, values);

      //
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public boolean isPartial() {
      return getValues().contains(null);
    }

    public boolean isFull() {
      return !isPartial();
    }
  }

  public static class Argument<B extends ParameterBinding> extends Parameter<ArgumentDescriptor<B>, B> {

    /** . */
    private int start;

    /** . */
    private int end;

    public Argument(ArgumentDescriptor<B> argument, int start, int end, List<String> values) {
      super(argument, values);

      //
      this.start = start;
      this.end = end;
    }

    public int getStart() {
      return start;
    }

    public int getEnd() {
      return end;
    }
  }

/*
  public static class Command {

  }
*/

}

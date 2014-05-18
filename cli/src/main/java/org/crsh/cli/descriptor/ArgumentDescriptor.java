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

import org.crsh.cli.impl.descriptor.IllegalParameterException;
import org.crsh.cli.impl.descriptor.IllegalValueTypeException;
import org.crsh.cli.impl.Multiplicity;
import org.crsh.cli.impl.ParameterType;
import org.crsh.cli.impl.SyntaxException;
import org.crsh.cli.spi.Completer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ArgumentDescriptor extends ParameterDescriptor {

  /** . */
  private final String name;

  public ArgumentDescriptor(
      String name,
      ParameterType<?> type,
      Description info,
      boolean required,
      boolean password,
      boolean unquote,
      Class<? extends Completer> completerType,
      Annotation annotation) throws IllegalValueTypeException, IllegalParameterException {
    super(
        type,
      info,
      required,
      password,
      unquote,
      completerType,
      annotation);

    //
    this.name = name;
  }

  /**
   * Returns the argument name, that can be null. This value is used for display capabilities and does not play a role
   * when a command line is parsed.
   *
   * @return the argument name
   */
  public String getName() {
    return name;
  }

  @Override
  public Object parse(List<String> values) throws SyntaxException {
    if (getMultiplicity() == Multiplicity.SINGLE) {
      if (values.size() > 1) {
        throw new SyntaxException("Too many option values " + values);
      }
      String value = values.get(0);
      try {
        return parse(value);
      } catch (Exception e) {
        throw new SyntaxException("Could not parse " + value);
      }
    } else {
      List<Object> v = new ArrayList<Object>(values.size());
      for (String value : values) {
        try {
          v.add(parse(value));
        } catch (Exception e) {
          throw new SyntaxException("Could not parse " + value);
        }
      }
      return v;
    }
  }

  /**
   * Prints the argument:
   *
   * <ul>
   * <li>Single valued arguments use the "$arg" pattern.</li>
   * <li>Multi valued arguments use the "... $arg" pattern.</li>
   * </ul>
   *
   * Where $arg is the value "arg" or the argument name when it is not null.
   *
   * @param writer the writer to print to
   * @throws IOException any io exception
   */
  public void printUsage(Appendable writer) throws IOException {
    writer.append('<');
    writer.append((name == null || name.length() == 0) ? "arg" : name);
    writer.append('>');
    if (getMultiplicity() == Multiplicity.MULTI) {
      writer.append("... ");
    }
  }

  @Override
  public String toString() {
    return "ArgumentDescriptor[" + name + "]";
  }
}

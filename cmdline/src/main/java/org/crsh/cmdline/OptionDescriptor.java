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

package org.crsh.cmdline;

import org.crsh.cmdline.binding.TypeBinding;
import org.crsh.cmdline.matcher.CmdSyntaxException;
import org.crsh.cmdline.spi.Completer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptionDescriptor<B extends TypeBinding> extends ParameterDescriptor<B> {

  /** . */
  private final int arity;

  /** . */
  private final List<String> names;

  public OptionDescriptor(
    B binding,
    Type javaType,
    List<String> names,
    Description info,
    boolean required,
    boolean password,
    boolean unquote,
    Class<? extends Completer> completerType,
    Annotation annotation) throws IllegalValueTypeException, IllegalParameterException {
    super(
      binding,
      javaType,
      info,
      required,
      password,
      unquote,
      completerType,
      annotation);

    //
    if (getMultiplicity() == Multiplicity.MULTI && getType() == SimpleValueType.BOOLEAN) {
      throw new IllegalParameterException();
    }

    //
    names = new ArrayList<String>(names);
    for (String name : names) {
      if (name == null) {
        throw new IllegalParameterException("Option name must not be null");
      }
      if (name.length() == 0) {
        throw new IllegalParameterException("Option name cannot be empty");
      }
      if (name.contains("-")) {
        throw new IllegalParameterException("Option name must not contain the hyphen character");
      }
    }

    //
    if (getType() == SimpleValueType.BOOLEAN) {
      arity = 0;
    } else {
      arity = 1;
    }

    //
    this.names = Collections.unmodifiableList(names);
  }

  public int getArity() {
    return arity;
  }

  public List<String> getNames() {
    return names;
  }

  @Override
  public Object parse(List<String> values) throws CmdSyntaxException {
    if (arity == 0) {
      if (values.size() > 0) {
        throw new CmdSyntaxException("Too many option values: " + values);
      }
      // It's a boolean and it is true
      return Boolean.TRUE;
    } else {
      if (getMultiplicity() == Multiplicity.SINGLE) {
        if (values.size() > 1) {
          throw new CmdSyntaxException("Too many option values: " + values);
        }
        String value = values.get(0);
        try {
          return parse(value);
        } catch (Exception e) {
          throw new CmdSyntaxException("Could not parse value: <" + value + ">");
        }
      } else {
        List<Object> v = new ArrayList<Object>(values.size());
        for (String value : values) {
          try {
            v.add(parse(value));
          } catch (Exception e) {
            throw new CmdSyntaxException("Could not parse value: <" + value + ">");
          }
        }
        return v;
      }
    }
  }

  /**
   * Prints the option names as an alternative of switches surrounded by a square brace,
   * for instance:  "[-f --foo]"
   *
   * @param writer the writer to print to
   * @throws IOException any io exception
   */
  public void printUsage(Appendable writer) throws IOException {
    writer.append("[");
    boolean a = false;
    for (String optionName : names) {
      if (a) {
        writer.append(" | ");
      }
      writer.append(optionName.length() == 1 ? "-" : "--").append(optionName);
      a = true;
    }
    writer.append("]");
  }

  @Override
  public String toString() {
    return "OptionDescriptor[" + names + "]";
  }
}

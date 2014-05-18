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
import org.crsh.cli.completers.EmptyCompleter;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.type.ValueType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

public abstract class ParameterDescriptor {

  /** . */
  private final Description description;

  /** . */
  private final ParameterType<?> type;

  /** . */
  private final boolean required;

  /** . */
  private final boolean password;

  /** . */
  private final Class<? extends Completer> completerType;

  /** The annotation when it exists.  */
  private final Annotation annotation;

  /** . */
  private final boolean unquote;

  public ParameterDescriptor(
      ParameterType<?> type,
      Description description,
      boolean required,
      boolean password,
      boolean unquote,
      Class<? extends Completer> completerType,
      Annotation annotation) throws IllegalValueTypeException, IllegalParameterException {

    //
    if (completerType == EmptyCompleter.class) {
      completerType = type.getValueType().getCompleter();
    }

    //
    this.description = description;
    this.required = required;
    this.password = password;
    this.completerType = completerType;
    this.annotation = annotation;
    this.unquote = unquote;
    this.type = type;
  }

  public Object parse(String s) throws Exception {
    return type.parse(s);
  }

  public abstract Object parse(List<String> values) throws SyntaxException;

  public Class<?> getDeclaredType() {
    return type.getDeclaredType();
  }

  public final String getUsage() {
    return description != null ? description.getUsage() : "";
  }

  public Description getDescription() {
    return description;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public final boolean isRequired() {
    return required;
  }

  public boolean isUnquote() {
    return unquote;
  }

  public final boolean isPassword() {
    return password;
  }

  public final ValueType getType() {
    return type.getValueType();
  }

  public final Multiplicity getMultiplicity() {
    return type.getMultiplicity();
  }

  public final boolean isSingleValued() {
    return getMultiplicity() == Multiplicity.SINGLE;
  }

  public final boolean isMultiValued() {
    return getMultiplicity() == Multiplicity.MULTI;
  }

  public final Class<? extends Completer> getCompleterType() {
    return completerType;
  }

  public abstract void printUsage(Appendable writer) throws IOException;
}
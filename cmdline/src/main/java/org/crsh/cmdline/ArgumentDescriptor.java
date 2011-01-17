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

import org.crsh.cmdline.binding.TypeBinding;
import org.crsh.cmdline.spi.Completer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ArgumentDescriptor<B extends TypeBinding> extends ParameterDescriptor<B> {

  /** . */
  private final String name;

  public ArgumentDescriptor(
    B binding,
    String name,
    Type javaType,
    Description info,
    boolean required,
    boolean password,
    Class<? extends Completer> completerType,
    Annotation annotation) throws IllegalValueTypeException, IllegalParameterException {
    super(
      binding,
      javaType,
      info,
      required,
      password,
      completerType,
      annotation);

    //
    this.name = name;
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
    if (getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
      writer.append("... ");
    }
    writer.append((name == null || name.length() == 0) ? "arg" : name);
  }

  @Override
  public String toString() {
    return "ArgumentDescriptor[" + name + "]";
  }
}

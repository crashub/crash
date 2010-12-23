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

import java.io.PrintWriter;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ArgumentDescriptor<B extends TypeBinding> extends ParameterDescriptor<B> {

  public ArgumentDescriptor(
    B binding,
    Type javaType,
    String description,
    boolean required,
    boolean password) throws IllegalValueTypeException, IllegalParameterException {
    super(
      binding,
      javaType,
      description,
      required,
      password);
  }

  public void printUsage(PrintWriter writer) {
    if (getMultiplicity() == Multiplicity.SINGLE) {
      writer.append("...");
    } else {
      writer.append("arg");
    }
  }
}

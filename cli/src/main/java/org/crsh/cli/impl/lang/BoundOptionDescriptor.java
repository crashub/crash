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
package org.crsh.cli.impl.lang;

import org.crsh.cli.descriptor.Description;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.impl.ParameterType;
import org.crsh.cli.impl.descriptor.IllegalParameterException;
import org.crsh.cli.impl.descriptor.IllegalValueTypeException;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.spi.Completer;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author Julien Viet
 */
class BoundOptionDescriptor extends OptionDescriptor implements Binding {

  /** . */
  final Binding binding;

  BoundOptionDescriptor(Binding binding, ParameterType<?> type, List<String> names, Description info, boolean required, boolean password, boolean unquote, Class<? extends Completer> completerType, Annotation annotation) throws IllegalValueTypeException, IllegalParameterException {
    super(type, names, info, required, password, unquote, completerType, annotation);

    //
    this.binding = binding;
  }

  @Override
  public void set(Object o, Object[] args, Object value) throws InvocationException {
    binding.set(o, args, value);
  }
}

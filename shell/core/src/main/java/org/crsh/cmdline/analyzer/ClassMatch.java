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

import org.crsh.cmdline.ClassCommandDescriptor;
import org.crsh.cmdline.ParameterBinding;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ClassMatch<T> extends CommandMatch<T, ClassCommandDescriptor<T>, ParameterBinding.ClassField> {

  /** . */
  private final ClassCommandDescriptor<T> descriptor;

  public ClassMatch(
    ClassCommandDescriptor<T> descriptor,
    List<OptionMatch<ParameterBinding.ClassField>> optionMatches,
    List<ArgumentMatch<ParameterBinding.ClassField>> argumentMatches,
    String rest) {
    super(optionMatches, argumentMatches, rest);

    //
    this.descriptor = descriptor;
  }

  @Override
  public ClassCommandDescriptor<T> getCommand() {
    return descriptor;
  }
}

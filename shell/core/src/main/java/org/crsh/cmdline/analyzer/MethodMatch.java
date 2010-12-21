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

import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.ParameterBinding;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MethodMatch<T> extends CommandMatch<T, MethodDescriptor<T>, ParameterBinding.MethodArgument> {

  /** . */
  private final List<OptionMatch<ParameterBinding.ClassField>> sharedOptionMatches;

  /** . */
  private final MethodDescriptor<T> descriptor;

  public MethodMatch(
    MethodDescriptor<T> descriptor,
    List<OptionMatch<ParameterBinding.ClassField>> sharedOptionMatches,
    List<OptionMatch<ParameterBinding.MethodArgument>> optionMatches,
    List<ArgumentMatch<ParameterBinding.MethodArgument>> argumentMatches,
    String rest) {
    super(optionMatches, argumentMatches, rest);

    //
    this.sharedOptionMatches = sharedOptionMatches;
    this.descriptor = descriptor;
  }

  public List<OptionMatch<ParameterBinding.ClassField>> getSharedOptionMatches() {
    return sharedOptionMatches;
  }

  @Override
  public MethodDescriptor<T> getCommand() {
    return descriptor;
  }
}

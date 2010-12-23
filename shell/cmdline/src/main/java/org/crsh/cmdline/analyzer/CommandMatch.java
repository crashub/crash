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

import org.crsh.cmdline.CmdLineException;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.binding.ParameterBinding;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandMatch<C, D extends CommandDescriptor<C, B>, B extends ParameterBinding> {

  /** . */
  private final List<OptionMatch<B>> optionMatches;

  /** . */
  private final List<ArgumentMatch<B>> argumentMatches;

  /** . */
  private final String rest;

  public CommandMatch(List<OptionMatch<B>> optionMatches, List<ArgumentMatch<B>> argumentMatches, String rest) {
    this.optionMatches = optionMatches;
    this.argumentMatches = argumentMatches;
    this.rest = rest;
  }

  public abstract D getDescriptor();

  public abstract Object invoke(InvocationContext context, C command) throws CmdLineException;

  public List<OptionMatch<B>> getOptionMatches() {
    return optionMatches;
  }

  public List<ArgumentMatch<B>> getArgumentMatches() {
    return argumentMatches;
  }

  public String getRest() {
    return rest;
  }
}

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

package org.crsh.cli.impl.invocation;

import org.crsh.cli.impl.LiteralValue;
import org.crsh.cli.descriptor.OptionDescriptor;

import java.util.Collections;
import java.util.List;

public class OptionMatch extends ParameterMatch<OptionDescriptor> {

  /** . */
  private final List<String> names;

  public OptionMatch(OptionDescriptor parameter, String name, List<LiteralValue> values) {
    this(parameter, Collections.singletonList(name), values);
  }

  public OptionMatch(OptionDescriptor parameter, List<String> names, List<LiteralValue> values) {
    super(parameter, values);

    //
    if (names.isEmpty()) {
      throw new IllegalArgumentException("names cannot be empty");
    }

    //
    this.names = names;
  }

  public String getName() {
    return names.get(0);
  }

  public List<String> getNames() {
    return names;
  }
}

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

package org.crsh.cmdline.matcher;

import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.binding.TypeBinding;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class OptionMatch<B extends TypeBinding> extends ParameterMatch<OptionDescriptor<B>, B> {

  /** . */
  private final List<String> names;

  public OptionMatch(OptionDescriptor<B> parameter, String name, List<Value> values) {
    this(parameter, Collections.singletonList(name), values);
  }

  public OptionMatch(OptionDescriptor<B> parameter, List<String> names, List<Value> values) {
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

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

import org.crsh.cli.impl.SyntaxException;
import org.crsh.cli.impl.LiteralValue;
import org.crsh.cli.descriptor.ParameterDescriptor;

import java.util.ArrayList;
import java.util.List;

public class ParameterMatch<P extends ParameterDescriptor> {

  /** . */
  private final P parameter;

  /** . */
  private final List<LiteralValue> values;

  /** . */
  private List<String> strings;

  public ParameterMatch(P parameter, List<LiteralValue> values) {
    this.parameter = parameter;
    this.values = values;
    this.strings = null;
  }

  public P getParameter() {
    return parameter;
  }

  public List<LiteralValue> getValues() {
    return values;
  }

  public List<String> getStrings() {
    if (strings == null) {
      List<String> strings = new ArrayList<String>(values.size());
      for (LiteralValue value : values) {
        strings.add(parameter.isUnquote() ? value.getValue() : value.getRawValue());
      }
      this.strings = strings;
    }
    return strings;
  }

  /**
   * Compute the value from the parameter metadata and the values list.
   *
   * @return the invocation value
   * @throws org.crsh.cli.impl.SyntaxException anything that would prevent the value from being computed
   */
  public Object computeValue() throws SyntaxException {
    List<String> strings = getStrings();
    return parameter.parse(strings);
  }
}

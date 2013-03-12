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

package org.crsh.cli.completers;

import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.spi.Completion;
import org.crsh.cli.type.ValueType;
import org.crsh.cli.spi.Completer;

import java.lang.reflect.Method;

public class EnumCompleter implements Completer {

  /** . */
  private static final EnumCompleter instance = new EnumCompleter();

  /**
   * Returns the empty completer instance.
   *
   * @return the instance
   */
  public static EnumCompleter getInstance() {
    return instance;
  }

  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    if (parameter.getType() == ValueType.ENUM) {
      Completion.Builder builder = null;
      Class<?> vt = parameter.getDeclaredType();
      Method valuesM = vt.getDeclaredMethod("values");
      Method nameM = vt.getMethod("name");
      Enum<?>[] values = (Enum<?>[])valuesM.invoke(null);
      for (Enum<?> value : values) {
        String name = (String)nameM.invoke(value);
        if (name.startsWith(prefix)) {
          if (builder == null) {
            builder = Completion.builder(prefix);
          }
          builder.add(name.substring(prefix.length()), true);
        }
      }
      return builder != null ? builder.build() : Completion.create();
    } else {
      return Completion.create();
    }
  }
}

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

import org.crsh.cmdline.spi.Completer;
import org.crsh.cmdline.spi.CompletionResult;

import java.lang.reflect.Method;

/**
 * A completer for enums.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
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

  public CompletionResult<Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {
    CompletionResult<Boolean> completions = CompletionResult.create();
    if (parameter.getType() == SimpleValueType.ENUM) {
      Class<?> vt = parameter.getJavaValueType();
      Method valuesM = vt.getDeclaredMethod("values");
      Method nameM = vt.getMethod("name");
      Enum<?>[] values = (Enum<?>[])valuesM.invoke(null);
      for (Enum<?> value : values) {
        String name = (String)nameM.invoke(value);
        if (name.startsWith(prefix)) {
          if (completions.isEmpty()) {
            completions = new CompletionResult<Boolean>();
          }
          completions.put(name.substring(prefix.length()), true);
        }
      }
    }
    return completions;
  }
}

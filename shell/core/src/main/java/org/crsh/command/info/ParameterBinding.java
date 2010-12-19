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

package org.crsh.command.info;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParameterBinding {

  public static class ClassField extends ParameterBinding {

    /** . */
    private final Field field;

    public ClassField(Field field) {
      this.field = field;
    }

    public Field getField() {
      return field;
    }
  }

  public static class MethodArgument extends ParameterBinding {

    /** . */
    private final Method method;

    /** . */
    private final int index;

    public MethodArgument(Method method, int index) {
      this.method = method;
      this.index = index;
    }

    public Method getMethod() {
      return method;
    }

    public int getIndex() {
      return index;
    }
  }
}

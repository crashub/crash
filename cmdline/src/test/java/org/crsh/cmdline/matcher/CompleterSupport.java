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

import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;
import org.crsh.cmdline.spi.ValueCompletion;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CompleterSupport {

  public static abstract class Abstract implements Completer {
  }

  public static class RuntimeException implements Completer {
    public ValueCompletion complete(ParameterDescriptor<?> parameter, String prefix) throws java.lang.Exception {
      throw new java.lang.RuntimeException();
    }
  }

  public static class Exception implements Completer {
    public ValueCompletion complete(ParameterDescriptor<?> parameter, String prefix) throws java.lang.Exception {
      throw new java.lang.Exception();
    }
  }

  public static class Mirror implements Completer {
    public ValueCompletion complete(ParameterDescriptor<?> parameter, String prefix) {
      return ValueCompletion.create(new StringBuilder(prefix).reverse().toString(), false);
    }
  }

  public static class Echo implements Completer {
    public ValueCompletion complete(ParameterDescriptor<?> parameter, String prefix) {
      return ValueCompletion.create(prefix, false);
    }
  }

  public static class Foo implements Completer {
    public ValueCompletion complete(ParameterDescriptor<?> parameter, String prefix) {
      if (prefix.equals("foo")) {
        return ValueCompletion.create("", true);
      } else if (prefix.equals("fo")) {
        return ValueCompletion.create("o", true);
      } else if (prefix.equals("f")) {
        return ValueCompletion.create("oo", true);
      } else if (prefix.equals("")) {
        return ValueCompletion.create("foo", true);
      } else {
        return ValueCompletion.create();
      }
    }
  }
}

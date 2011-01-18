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

package org.crsh.cmdline.matcher.impl2;

import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.OptionDescriptor;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Event {

  public static class Option extends Event {

    /** . */
    private final OptionDescriptor<?> descriptor;

    /** . */
    private final List<String> values;

    public Option(OptionDescriptor<?> descriptor, List<String> values) {
      this.descriptor = descriptor;
      this.values = values;
    }

    public OptionDescriptor<?> getDescriptor() {
      return descriptor;
    }

    public List<String> getValues() {
      return values;
    }

    @Override
    public String toString() {
      return "Event.Option[descriptor=" + descriptor + ",values=" + values +  "]";
    }
  }

  public static class Separator extends Event {

  }

  public static class Method extends Event {

    /** . */
    private final MethodDescriptor<?> descriptor;

    public Method(MethodDescriptor<?> descriptor) {
      this.descriptor = descriptor;
    }

    public MethodDescriptor<?> getDescriptor() {
      return descriptor;
    }
  }

  public static class End extends Event {

    public static enum Code {

      /** . */
      DONE,

      /** . */
      NO_SUCH_CLASS_OPTION,

      /** . */
      NO_SUCH_METHOD_OPTION,

      /** . */
      NO_METHOD,

      /** No command argument is available to consume the remaining arguments. */
      NO_ARGUMENT

    }

    /** . */
    private final Code code;

    public End(Code code) {
      this.code = code;
    }

    public Code getCode() {
      return code;
    }
  }
}

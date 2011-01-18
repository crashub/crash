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

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
abstract class Status {

  static class ReadingOption extends Status {

  }

  static class WantReadArg extends Status {

  }

  static class ReadingArg extends Status {

    /** . */
    final int index;

    ReadingArg() {
      this(0);
    }

    private ReadingArg(int index) {
      this.index = index;
    }

    ReadingArg next() {
      return new ReadingArg(index + 1);
    }
  }

  static class Arg extends Status {

    /** . */
    final Arg next;

    /** . */
    final List<String> values;

    Arg(Arg next, List<String> values) {
      this.next = next;
      this.values = values;
    }

    Arg(List<String> values) {
      this(null, values);
    }
  }

  static class End extends Status {

    /** . */
    final Code code;

    End(Code code) {
      if (code == null) {
        throw new AssertionError();
      }
      this.code = code;
    }
  }
}

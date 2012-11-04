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

package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.matcher.CmdCompletionException;

class MethodCompletion<T> extends org.crsh.cmdline.matcher.impl.Completion {

  /** . */
  private final ClassDescriptor<T> descriptor;

  /** . */
  private final String mainName;

  /** . */
  private final  String prefix;

  /** . */
  private final Delimiter delimiter;

  MethodCompletion(ClassDescriptor<T> descriptor, String mainName, String prefix, Delimiter delimiter) {
    this.descriptor = descriptor;
    this.mainName = mainName;
    this.prefix = prefix;
    this.delimiter = delimiter;
  }

  @Override
  protected CommandCompletion complete() throws CmdCompletionException {
    org.crsh.cmdline.spi.Completion.Builder builder = org.crsh.cmdline.spi.Completion.builder(prefix);
    for (MethodDescriptor<?> m : descriptor.getMethods()) {
      String name = m.getName();
      if (name.startsWith(prefix)) {
        if (!name.equals(mainName)) {
          builder.add(name.substring(prefix.length()), true);
        }
      }
    }
    return new CommandCompletion(delimiter, builder.build());
  }
}

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

package org.crsh.cmdline;

import org.crsh.cmdline.impl.CommandFactoryImpl;
import org.crsh.cmdline.type.ValueTypeFactory;

public abstract class CommandFactory {

  /** . */
  public static final CommandFactory DEFAULT = new CommandFactoryImpl();

  /** . */
  protected final ValueTypeFactory valueTypeFactory;

  public CommandFactory() {
    this.valueTypeFactory = ValueTypeFactory.DEFAULT;
  }

  public CommandFactory(ClassLoader loader) throws NullPointerException {
    this(new ValueTypeFactory(loader));
  }

  public CommandFactory(ValueTypeFactory valueTypeFactory) throws NullPointerException {
    if (valueTypeFactory == null) {
      throw new NullPointerException("No null value type factory accepted");
    }

    //
    this.valueTypeFactory = valueTypeFactory;
  }

  public abstract <T> CommandDescriptor<T> create(Class<T> type) throws IntrospectionException;
}

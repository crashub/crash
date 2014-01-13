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

package org.crsh.cli.impl.descriptor;

import org.crsh.cli.SyntaxException;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.Description;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.impl.ParameterType;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.ParameterMatch;
import org.crsh.cli.impl.invocation.Resolver;
import org.crsh.cli.type.ValueTypeFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class HelpDescriptor<T> extends CommandDescriptorImpl<T> {

  public static <T> HelpDescriptor<T> create(CommandDescriptor<T> descriptor) {
    return new HelpDescriptor<T>(descriptor);
  }

  /** . */
  static final OptionDescriptor HELP_OPTION = new OptionDescriptor(
      null,
      ParameterType.create(ValueTypeFactory.DEFAULT, Boolean.class),
      Arrays.asList("h", "help"),
      new Description("this help", "Display this help message"),
      false,
      false,
      false,
      null,
      null
  );

  /** . */
  private final HelpDescriptor<T> owner;

  /** . */
  private final CommandDescriptorImpl<T> delegate;

  /** . */
  private final LinkedHashMap<String, HelpDescriptor<T>> subordinates;

  public HelpDescriptor(CommandDescriptor<T> delegate) throws IntrospectionException {
    this(null, (CommandDescriptorImpl<T>)delegate);
  }

  private HelpDescriptor(HelpDescriptor<T> owner, CommandDescriptorImpl<T> delegate) throws IntrospectionException {
    super(delegate.getName(), delegate.getDescription());

    //
    for (ParameterDescriptor parameter : delegate.getParameters()) {
      addParameter(parameter);
    }

    // Override the help parameter only for the root level
    // otherwise it may be repeated several times
    if (owner == null) {
      addParameter(HELP_OPTION);
    }

    // Wrap subordinates
    LinkedHashMap<String, HelpDescriptor<T>> subordinates = new LinkedHashMap<String, HelpDescriptor<T>>();
    for (CommandDescriptorImpl<T> subordinate : delegate.getSubordinates().values()) {
      subordinates.put(subordinate.getName(), new HelpDescriptor<T>(this, subordinate));
    }

    //
    this.owner = owner;
    this.delegate = delegate;
    this.subordinates = subordinates;
  }

  public CommandDescriptor<T> getDelegate() {
    return delegate;
  }

  @Override
  public CommandInvoker<T, ?> getInvoker(final InvocationMatch<T> match) {

    //
    final CommandInvoker<T, ?> invoker = delegate.getInvoker(match);

    // Get the option from the top match
    ParameterMatch<OptionDescriptor> helpDesc = null;
    for (InvocationMatch<T> current = match;current != null && helpDesc == null;current = current.owner()) {
      helpDesc = current.getParameter(HELP_OPTION);
    }

    //
    final boolean help = helpDesc != null || invoker == null;

    //
    if (help) {
      return new CommandInvoker<T, Help>() {
        @Override
        public InvocationMatch<T> getMatch() {
          return match;
        }
        @Override
        public Class<Help> getReturnType() {
          return Help.class;
        }
        @Override
        public Type getGenericReturnType() {
          return Help.class;
        }
        @Override
        public Class<?>[] getParameterTypes() {
          return new Class[0];
        }
        @Override
        public Type[] getGenericParameterTypes() {
          return new Type[0];
        }
        @Override
        public Help invoke(Resolver resolver, T command) throws InvocationException, SyntaxException {
          return new Help<T>(delegate);
        }
      };
    } else {
      return invoker;
    }
  }

  @Override
  public Class<T> getType() {
    return delegate.getType();
  }

  @Override
  public CommandDescriptor<T> getOwner() {
    return owner;
  }

  @Override
  public Map<String, ? extends HelpDescriptor<T>> getSubordinates() {
    return subordinates;
  }

  @Override
  public HelpDescriptor<T> getSubordinate(String name) {
    return subordinates.get(name);
  }
}

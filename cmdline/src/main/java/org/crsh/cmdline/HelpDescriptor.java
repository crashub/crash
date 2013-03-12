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

package org.crsh.cmdline;

import org.crsh.cmdline.binding.TypeBinding;
import org.crsh.cmdline.invocation.CommandInvoker;
import org.crsh.cmdline.invocation.InvocationException;
import org.crsh.cmdline.invocation.InvocationMatch;
import org.crsh.cmdline.invocation.ParameterMatch;
import org.crsh.cmdline.invocation.Resolver;
import org.crsh.cmdline.type.ValueTypeFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class HelpDescriptor<T> extends CommandDescriptor<T> {

  public static <T> HelpDescriptor<T> create(CommandDescriptor<T> descriptor) {
    return new HelpDescriptor<T>(descriptor);
  }

  /** . */
  static final OptionDescriptor HELP_OPTION = new OptionDescriptor(
      new TypeBinding(),
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
  private final CommandDescriptor<T> delegate;

  /** . */
  private final LinkedHashMap<String, HelpDescriptor<T>> subordinates;

  public HelpDescriptor(CommandDescriptor<T> delegate) throws IntrospectionException {
    this(null, delegate);
  }

  private HelpDescriptor(HelpDescriptor<T> owner, CommandDescriptor<T> delegate) throws IntrospectionException {
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
    for (CommandDescriptor<T> subordinate : delegate.getSubordinates().values()) {
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
  public CommandInvoker<T> getInvoker(final InvocationMatch<T> match) {
    final CommandInvoker<T> invoker = delegate.getInvoker(match);
    return new CommandInvoker<T>() {
      @Override
      public Class<?> getReturnType() {
        return invoker != null ? invoker.getReturnType() : Void.class;
      }

      @Override
      public Type getGenericReturnType() {
        return invoker != null ? invoker.getGenericReturnType() : Void.class;
      }

      @Override
      public Class<?>[] getParameterTypes() {
        return invoker != null ? invoker.getParameterTypes() : new Class[0];
      }

      @Override
      public Type[] getGenericParameterTypes() {
        return invoker != null ? invoker.getGenericParameterTypes() : new Type[0];
      }

      @Override
      public Object invoke(Resolver resolver, T command) throws InvocationException, SyntaxException {

        // Get the option from the top match
        ParameterMatch<OptionDescriptor> help = null;
        for (InvocationMatch<T> current = match;current.owner() != null && help == null;current = current.owner()) {
          help = current.getParameter(HELP_OPTION);
        }

        //
        if (help == null && invoker != null) {
          return invoker.invoke(resolver, command);
        } else {
          StringBuilder sb = new StringBuilder();
          try {
            printUsage(sb);
          }
          catch (IOException e) {
            throw new AssertionError(e);
          }
          return sb.toString();
        }
      }
    };
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

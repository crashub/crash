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

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.Description;
import org.crsh.cli.impl.completion.CompletionMatcher;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;

import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class CommandDescriptorImpl<T> extends CommandDescriptor<T> {

  public CommandDescriptorImpl(String name, Description description) throws IntrospectionException {
    super(name, description);
  }

  @Override
  public abstract Map<String, ? extends CommandDescriptorImpl<T>> getSubordinates();

  @Override
  public abstract CommandDescriptorImpl<T> getSubordinate(String name);

  public abstract CommandInvoker<T, ?> getInvoker(InvocationMatch<T> match);

  @Override
  public InvocationMatcher<T> matcher(String mainName) {
    return new InvocationMatcher<T>(this, mainName);
  }

  public final CompletionMatcher<T> completer(String mainName) {
    return new CompletionMatcher<T>(this, mainName);
  }
}

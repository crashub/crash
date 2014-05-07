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

package org.crsh.cli.impl.bootstrap;

import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.impl.lang.ObjectCommandDescriptor;
import org.crsh.cli.impl.lang.Util;

import java.util.Iterator;
import java.util.ServiceLoader;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Main {

  public static void main(String[] args) throws Exception {
    ServiceLoader<CommandProvider> loader = ServiceLoader.load(CommandProvider.class);
    Iterator<CommandProvider> iterator = loader.iterator();
    if (iterator.hasNext()) {

      //
      StringBuilder line = new StringBuilder();
      for (int i = 0;i < args.length;i++) {
        if (i  > 0) {
          line.append(' ');
        }
        Delimiter.EMPTY.escape(args[i], line);
      }

      //
      CommandProvider commandProvider = iterator.next();
      Class<?> commandClass = commandProvider.getCommandClass();
      handle(commandClass, line.toString());
    }
  }

  private static <T> void handle(Class<T> commandClass, String line) throws Exception {
    ObjectCommandDescriptor<T> descriptor = CommandFactory.DEFAULT.create(commandClass);
    HelpDescriptor<Instance<T>> helpDescriptor = HelpDescriptor.create(descriptor);
    InvocationMatcher<Instance<T>> matcher = helpDescriptor.matcher();
    InvocationMatch<Instance<T>> match = matcher.parse(line);
    final T instance = commandClass.newInstance();
    Object o = match.invoke(Util.wrap(instance));
    if (o != null) {
      System.out.println(o);
    }
  }
}

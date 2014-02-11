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
package org.crsh.cli.impl;

import junit.framework.TestCase;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.impl.descriptor.CommandDescriptorImpl;
import org.crsh.cli.impl.descriptor.Help;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.CommandFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author Julien Viet */
public class HelpTestCase extends TestCase {

  public class A {
    @Command
    public Integer main() {
      throw new UnsupportedOperationException();
    }
  }

  public class B {
    @Command
    public String main(@Option(names = {"h"}) Boolean h) {
      return "override";
    }
  }

  private <T> CommandInvoker<T, ?> createInvoker(Class<T> cls, Map<String, List<?>> options, List<?> arguments) {
    CommandDescriptorImpl<T> desc = HelpDescriptor.create(CommandFactory.DEFAULT.create(cls));
    InvocationMatcher<T> matcher = desc.matcher("main");
    InvocationMatch<T> match = matcher.options(options).arguments(arguments);
    return match.getInvoker();
  }

  public void testFoo() {
    CommandInvoker<A, ?> invoker = createInvoker(A.class, Collections.<String, List<?>>singletonMap("h", Collections.singletonList(Boolean.TRUE)), Collections.emptyList());
    Object ret = invoker.invoke(new A());
    assertTrue(ret instanceof Help);
    assertEquals(Help.class, invoker.getReturnType());
  }

  public void testOverrideHelp() {
    CommandInvoker<B, ?> invoker = createInvoker(B.class, Collections.<String, List<?>>singletonMap("h", Collections.singletonList(Boolean.TRUE)), Collections.emptyList());
    Object ret = invoker.invoke(new B());
    assertEquals("override", ret);
  }
}

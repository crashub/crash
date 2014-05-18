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
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.descriptor.Help;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.impl.lang.Util;

import java.util.Collections;
import java.util.List;

/** @author Julien Viet */
public class HelpTestCase extends TestCase {

  public class A {
    @Command
    public Integer main() {
      throw new UnsupportedOperationException();
    }
  }

  public void testFoo() throws Exception {
    CommandDescriptor<Instance<A>> desc = HelpDescriptor.create(CommandFactory.DEFAULT.create(A.class));
    InvocationMatcher<Instance<A>> matcher = desc.matcher();
    InvocationMatch<Instance<A>> match = matcher.options(Collections.<String, List<?>>singletonMap("h", Collections.singletonList(Boolean.TRUE))).arguments(Collections.emptyList());
    CommandInvoker<Instance<A>, ?> invoker = match.getInvoker();
    Object ret = invoker.invoke(Util.wrap(new A()));
    assertTrue(ret instanceof Help);
    assertEquals(Help.class, invoker.getReturnType());
  }

  public class B1 {
    @Command
    public String main(@Option(names = "h") boolean help) {
      return "my help " + help;
    }
  }

  public class B2 {
    @Command
    public String main(@Option(names = "help") boolean help) {
      return "my help " + help;
    }
  }

  public class B3 {
    @Option(names = "h") boolean help;
    @Command
    public String main() {
      return "my help " + help;
    }
  }

  public class B4 {
    @Option(names = "help") boolean help;
    @Command
    public String main() {
      return "my help " + help;
    }
  }

  public void testPreserveHelp() throws Exception {
    assertPreserveHelp(B1.class, new B1(), "h");
    assertPreserveHelp(B2.class, new B2(), "help");
    assertPreserveHelp(B3.class, new B3(), "h");
    assertPreserveHelp(B4.class, new B4(), "help");
  }

  public <C> void assertPreserveHelp(Class<C> clazz, C instance, String option) throws Exception {
    CommandDescriptor<Instance<C>> desc = HelpDescriptor.create(CommandFactory.DEFAULT.create(clazz));
    InvocationMatcher<Instance<C>> matcher = desc.matcher();
    InvocationMatch<Instance<C>> match = matcher.options(Collections.<String, List<?>>singletonMap(option, Collections.singletonList(Boolean.TRUE))).arguments(Collections.emptyList());
    CommandInvoker<Instance<C>, ?> invoker = match.getInvoker();
    Object ret = invoker.invoke(Util.wrap(instance));
    assertEquals("my help " + true, ret);
  }

  public class C1 {
    @Command
    public String sub(@Option(names = "h") boolean help) {
      return "my help " + help;
    }
  }

  public class C2 {
    @Command
    public String sub(@Option(names = "help") boolean help) {
      return "my help " + help;
    }
  }

  public void testPreserveHelpInSubCommand() throws Exception {
    assertPreserveHelpInSubCommand1(C1.class, new C1(), "h");
    assertPreserveHelpInSubCommand1(C2.class, new C2(), "help");
  }

  public <C> void assertPreserveHelpInSubCommand1(Class<C> clazz, C instance, String option) throws Exception {
    CommandDescriptor<Instance<C>> desc = HelpDescriptor.create(CommandFactory.DEFAULT.create(clazz));
    InvocationMatcher<Instance<C>> matcher = desc.matcher();
    InvocationMatch<Instance<C>> match = matcher.
        subordinate("sub").
        options(Collections.<String, List<?>>singletonMap(option, Collections.singletonList(Boolean.TRUE))).arguments(Collections.emptyList());
    CommandInvoker<Instance<C>, ?> invoker = match.getInvoker();
    Object ret = invoker.invoke(Util.wrap(instance));
    assertEquals("my help " + true, ret);
  }

  public class D1 {
    @Option(names = "h") boolean help;
    @Command
    public String sub() {
      return "my help " + help;
    }
  }

  public class D2 {
    @Option(names = "help") boolean help;
    @Command
    public String sub() {
      return "my help " + help;
    }
  }

  public void testPreserveHelpInSubCommand2() throws Exception {
    assertPreserveHelpInSubCommand2(D1.class, new D1(), "h");
    assertPreserveHelpInSubCommand2(D2.class, new D2(), "help");
  }
  public <C> void assertPreserveHelpInSubCommand2(Class<C> clazz, C instance, String option) throws Exception {
    CommandDescriptor<Instance<C>> desc = HelpDescriptor.create(CommandFactory.DEFAULT.create(clazz));
    InvocationMatcher<Instance<C>> matcher = desc.matcher();
    InvocationMatch<Instance<C>> match = matcher.
        options(Collections.<String, List<?>>singletonMap(option, Collections.singletonList(Boolean.TRUE))).arguments(Collections.emptyList()).
        subordinate("sub");
    CommandInvoker<Instance<C>, ?> invoker = match.getInvoker();
    Object ret = invoker.invoke(Util.wrap(instance));
    assertEquals("my help " + true, ret);
  }
}

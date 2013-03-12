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

package org.crsh.cmdline.bootstrap;

import junit.framework.TestCase;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.HelpDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.invocation.CommandInvoker;
import org.crsh.cmdline.invocation.InvocationMatch;
import org.crsh.cmdline.invocation.InvocationMatcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class BootstrapTestCase extends TestCase {

  public class A {
    @Option(names = {"f", "foo"}) String foo;
    @Command
    public String bar() {
      return "invoked:" + foo;
    }
  }

  public void testOption() throws Exception {


    //
    HelpDescriptor<A> desc = new HelpDescriptor<A>(CommandFactory.DEFAULT.create(A.class));
    OptionDescriptor help = desc.getOption("-h");
    assertNotNull(help);
    OptionDescriptor foo = desc.getOption("-f");
    assertNotNull(foo);
    CommandDescriptor<A> bar = desc.getSubordinate("bar");
    OptionDescriptor barHelp = bar.getOption("-h");
    assertNull(barHelp);

    //
    InvocationMatcher<A> matcher = desc.invoker("main");
    InvocationMatch<A> match = matcher.match("");
    CommandInvoker<A> invoker = match.getInvoker();
    invoker.invoke(new A());

    match = matcher.match("-f foo_value bar");
    invoker = match.getInvoker();
    assertEquals("invoked:foo_value", invoker.invoke(new A()));

  }
}

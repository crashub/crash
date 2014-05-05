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

import junit.framework.TestCase;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.descriptor.Help;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.impl.invocation.ParameterMatch;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.InvocationContext;

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
    CommandDescriptor<InvocationContext<A>> desc = CommandFactory.DEFAULT.create(A.class);
    HelpDescriptor<InvocationContext<A>> helpDesc = new HelpDescriptor<InvocationContext<A>>(desc);
    OptionDescriptor optionDesc = helpDesc.getOption("-h");
    assertNotNull(optionDesc);
    OptionDescriptor foo = helpDesc.getOption("-f");
    assertNotNull(foo);
    InvocationMatcher<InvocationContext<A>> matcher = helpDesc.matcher();

    //
    InvocationMatch<InvocationContext<A>> match = matcher.parse("--help");
    ParameterMatch<OptionDescriptor> helpMatch = match.getParameter(optionDesc);
    assertNotNull(helpMatch);
    CommandInvoker<InvocationContext<A>, ?> invoker = match.getInvoker();
    Help help = (Help)invoker.invoke(InvocationContext.wrap(new A()));
    assertNotNull(help);
    assertSame(desc, help.getDescriptor().getDelegate());

    //
    match = matcher.parse("");
    invoker = match.getInvoker();
    invoker.invoke(InvocationContext.wrap(new A()));
    match = matcher.parse("-f foo_value bar");
    invoker = match.getInvoker();
    assertEquals("invoked:foo_value", invoker.invoke(InvocationContext.wrap(new A())));
  }

  public static class B {

    @Command
    public void main() throws Exception {
      throw new UnsupportedOperationException("Should not invoked");
    }
  }

  public void testMain1() {

    CommandDescriptor<InvocationContext<B>> desc = CommandFactory.DEFAULT.create(B.class);
    HelpDescriptor<InvocationContext<B>> helpDesc = new HelpDescriptor<InvocationContext<B>>(desc);
    OptionDescriptor optionDesc = helpDesc.getOption("-h");
    assertNotNull(optionDesc);
    InvocationMatcher<InvocationContext<B>> matcher = helpDesc.matcher();

    //
    InvocationMatch<InvocationContext<B>> match = matcher.parse("--help");
    ParameterMatch<OptionDescriptor> helpMatch = match.getParameter(optionDesc);
    assertNotNull(helpMatch);
    CommandInvoker<InvocationContext<B>, ?> invoker = match.getInvoker();
    Help help = (Help)invoker.invoke(InvocationContext.wrap(new B()));
    assertNotNull(help);
    CommandDescriptor mainDescriptor = help.getDescriptor();
    assertEquals("b", mainDescriptor.getName());
    assertSame(null, mainDescriptor.getOwner());
  }
}

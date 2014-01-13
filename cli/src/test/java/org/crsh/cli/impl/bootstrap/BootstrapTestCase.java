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
    CommandDescriptor<A> desc = CommandFactory.DEFAULT.create(A.class);
    HelpDescriptor<A> helpDesc = new HelpDescriptor<A>(desc);
    OptionDescriptor optionDesc = helpDesc.getOption("-h");
    assertNotNull(optionDesc);
    OptionDescriptor foo = helpDesc.getOption("-f");
    assertNotNull(foo);
    CommandDescriptor<A> bar = helpDesc.getSubordinate("bar");
    OptionDescriptor barHelp = bar.getOption("-h");
    assertNull(barHelp);
    InvocationMatcher<A> matcher = helpDesc.matcher("main");

    //
    InvocationMatch<A> match = matcher.parse("--help");
    ParameterMatch<OptionDescriptor> helpMatch = match.getParameter(optionDesc);
    assertNotNull(helpMatch);
    CommandInvoker<A, ?> invoker = match.getInvoker();
    Help help = (Help)invoker.invoke(new A());
    assertNotNull(help);
    assertSame(desc, help.getDescriptor());

    //
    match = matcher.parse("");
    invoker = match.getInvoker();
    invoker.invoke(new A());
    match = matcher.parse("-f foo_value bar");
    invoker = match.getInvoker();
    assertEquals("invoked:foo_value", invoker.invoke(new A()));
  }

  public static class B {

    @Command
    public void main() throws Exception {
      throw new UnsupportedOperationException("Should not invoked");
    }
  }

  public void testMain() {

    CommandDescriptor<B> desc = CommandFactory.DEFAULT.create(B.class);
    HelpDescriptor<B> helpDesc = new HelpDescriptor<B>(desc);
    OptionDescriptor optionDesc = helpDesc.getOption("-h");
    assertNotNull(optionDesc);
    InvocationMatcher<B> matcher = helpDesc.matcher("main");

    //
    InvocationMatch<B> match = matcher.parse("--help");
    ParameterMatch<OptionDescriptor> helpMatch = match.getParameter(optionDesc);
    assertNull(helpMatch);
    InvocationMatch<B> ownerMatch = match.owner();
    helpMatch = ownerMatch.getParameter(optionDesc);
    assertNotNull(helpMatch);
    CommandInvoker<B, ?> invoker = match.getInvoker();
    Help help = (Help)invoker.invoke(new B());
    assertNotNull(help);
    CommandDescriptor mainDescriptor = help.getDescriptor();
    assertEquals("main", mainDescriptor.getName());
    assertSame(desc, mainDescriptor.getOwner());
  }

}

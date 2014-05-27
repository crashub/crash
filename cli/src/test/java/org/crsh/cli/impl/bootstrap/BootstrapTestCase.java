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
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.impl.lang.Util;

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
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    HelpDescriptor<Instance<A>> helpDesc = new HelpDescriptor<Instance<A>>(desc);
    OptionDescriptor optionDesc = helpDesc.getOption("-h");
    assertNotNull(optionDesc);
    OptionDescriptor foo = helpDesc.getOption("-f");
    assertNotNull(foo);
    InvocationMatcher<Instance<A>> matcher = helpDesc.matcher();

    //
    InvocationMatch<Instance<A>> match = matcher.parse("--help");
    ParameterMatch<OptionDescriptor> helpMatch = match.getParameter(optionDesc);
    assertNotNull(helpMatch);
    CommandInvoker<Instance<A>, ?> invoker = match.getInvoker();
    Help help = (Help)invoker.invoke(Util.wrap(new A()));
    assertNotNull(help);
    assertSame(desc, help.getDescriptor().getDelegate());

    //
    match = matcher.parse("");
    invoker = match.getInvoker();
    invoker.invoke(Util.wrap(new A()));
    match = matcher.parse("-f foo_value bar");
    invoker = match.getInvoker();
    assertEquals("invoked:foo_value", invoker.invoke(Util.wrap(new A())));
  }

  public static class b {

    @Command
    public void main() throws Exception {
      throw new UnsupportedOperationException("Should not invoked");
    }
  }

  public void testMain1() throws Exception {

    CommandDescriptor<Instance<b>> desc = CommandFactory.DEFAULT.create(b.class);
    HelpDescriptor<Instance<b>> helpDesc = new HelpDescriptor<Instance<b>>(desc);
    OptionDescriptor optionDesc = helpDesc.getOption("-h");
    assertNotNull(optionDesc);
    InvocationMatcher<Instance<b>> matcher = helpDesc.matcher();

    //
    InvocationMatch<Instance<b>> match = matcher.parse("--help");
    ParameterMatch<OptionDescriptor> helpMatch = match.getParameter(optionDesc);
    assertNotNull(helpMatch);
    CommandInvoker<Instance<b>, ?> invoker = match.getInvoker();
    Help help = (Help)invoker.invoke(Util.wrap(new b()));
    assertNotNull(help);
    CommandDescriptor mainDescriptor = help.getDescriptor();
    assertEquals("b", mainDescriptor.getName());
    assertSame(null, mainDescriptor.getOwner());
  }
}

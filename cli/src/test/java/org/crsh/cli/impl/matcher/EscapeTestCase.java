/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.cli.impl.matcher;

import junit.framework.TestCase;
import org.crsh.cli.Argument;
import org.crsh.cli.Option;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.impl.lang.Util;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class EscapeTestCase extends TestCase {


  public void testOption() throws Exception {
    class A implements Runnable {
      @Option(names = "o")
      String s;
      public void run() {}
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<Instance<A>> matcher = desc.matcher();

    //
    A a = new A();
    matcher.parse("-o \" \"").invoke(Util.wrap(a));
    assertEquals(" ", a.s);

    //
    a = new A();
    matcher.parse("-o \"'\"").invoke(Util.wrap(a));
    assertEquals("'", a.s);

    //
    a = new A();
    matcher.parse("-o \" a b").invoke(Util.wrap(a));
    assertEquals(" a b", a.s);
  }


  public void testArgumentList() throws Exception {
    class A implements Runnable {
      @Argument
      List<String> s;
      public void run() {}
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<Instance<A>> analyzer = desc.matcher();

    //
    A a = new A();
    analyzer.parse("\" \" b").invoke(Util.wrap(a));
    assertEquals(Arrays.asList(" ", "b"), a.s);

    //
    a = new A();
    analyzer.parse("\"'\" b").invoke(Util.wrap(a));
    assertEquals(Arrays.asList("'", "b"), a.s);

    //
    a = new A();
    analyzer.parse("\"a b\" c").invoke(Util.wrap(a));
    assertEquals(Arrays.asList("a b", "c"), a.s);
  }
}

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

package org.crsh.cmdline.matcher;

import junit.framework.TestCase;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.annotations.Option;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class EscapeTestCase extends TestCase {


  public void testOption() throws Exception {
    class A {
      @Option(names = "o")
      String s;
    }
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> analyzer = desc.matcher();

    //
    A a = new A();
    analyzer.match("-o \" \"").invoke(a);
    assertEquals(" ", a.s);

    //
    a = new A();
    analyzer.match("-o \"'\"").invoke(a);
    assertEquals("'", a.s);

    //
    a = new A();
    analyzer.match("-o \" a b").invoke(a);
    assertEquals(" a b", a.s);
  }

  public void testArgumentList() throws Exception {
    class A {
      @Argument
      List<String> s;
    }
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> analyzer = desc.matcher();

    //
    A a = new A();
    analyzer.match("\" \" b").invoke(a);
    assertEquals(Arrays.asList(" ", "b"), a.s);

    //
    a = new A();
    analyzer.match("\"'\" b").invoke(a);
    assertEquals(Arrays.asList("'", "b"), a.s);

    //
    a = new A();
    analyzer.match("\"a b\" c").invoke(a);
    assertEquals(Arrays.asList("a b", "c"), a.s);
  }
}

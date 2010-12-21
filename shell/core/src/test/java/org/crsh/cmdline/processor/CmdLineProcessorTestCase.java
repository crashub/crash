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

package org.crsh.cmdline.processor;

import junit.framework.TestCase;
import org.crsh.cmdline.Argument;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Option;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CmdLineProcessorTestCase extends TestCase {


  public void testRequiredOption() throws Exception {
    class A {
      @Option(names = "o", required = true)
      String s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    CmdLineProcessor.Clazz<A> conf = new CmdLineProcessor.Clazz<A>(desc);

    A a = new A();
    conf.process(a, "-o foo");
    assertEquals("foo", a.s);

    try {
      a = new A();
      conf.process(a, "");
      fail();
    }
    catch (SyntaxException e) {
    }
  }

  public void testOptionalOption() throws Exception {
    class A {
      @Option(names = "o")
      String s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    CmdLineProcessor.Clazz<A> conf = new CmdLineProcessor.Clazz<A>(desc);

    A a = new A();
    conf.process(a, "-o foo");
    assertEquals("foo", a.s);

    a = new A();
    conf.process(a, "");
    assertEquals(null, a.s);
  }

  public void testArgument() throws Exception {
    class A {
      @Argument(required = true)
      String s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    CmdLineProcessor.Clazz<A> conf = new CmdLineProcessor.Clazz<A>(desc);

    A a = new A();
    conf.process(a, "foo");
    assertEquals("foo", a.s);

    a = new A();
    conf.process(a, "foo bar");
    assertEquals("foo", a.s);

    try {
      a = new A();
      conf.process(a, "");
      fail();
    }
    catch (SyntaxException e) {
    }
  }

  public void testOptionalArgument() throws Exception {
    class A {
      @Argument
      String s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    CmdLineProcessor.Clazz<A> conf = new CmdLineProcessor.Clazz<A>(desc);

    A a = new A();
    conf.process(a, "foo");
    assertEquals("foo", a.s);

    a = new A();
    conf.process(a, "foo bar");
    assertEquals("foo", a.s);

    a = new A();
    conf.process(a, "");
    assertEquals(null, a.s);
  }

  public void testOptionalArgumentList() throws Exception {
    class A {
      @Argument
      List<String> s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    CmdLineProcessor.Clazz<A> conf = new CmdLineProcessor.Clazz<A>(desc);

    A a = new A();
    conf.process(a, "");
    assertEquals(null , a.s);

    a = new A();
    conf.process(a, "foo");
    assertEquals(Arrays.asList("foo"), a.s);

    a = new A();
    conf.process(a, "foo bar");
    assertEquals(Arrays.asList("foo", "bar"), a.s);
  }

  public void testRequiredArgumentList() throws Exception {
    class A {
      @Argument(required = true)
      List<String> s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    CmdLineProcessor.Clazz<A> conf = new CmdLineProcessor.Clazz<A>(desc);

    A a = new A();
    try {
      conf.process(a, "");
      fail();
    }
    catch (SyntaxException expected) {
    }

    a = new A();
    conf.process(a, "foo");
    assertEquals(Arrays.asList("foo"), a.s);

    a = new A();
    conf.process(a, "foo bar");
    assertEquals(Arrays.asList("foo", "bar"), a.s);
  }
}

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
import org.crsh.cmdline.CmdSyntaxException;
import org.crsh.cmdline.Command;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Option;
import org.crsh.cmdline.matcher.MatchFactory;
import org.crsh.cmdline.matcher.InvocationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    MatchFactory<A> analyzer = new MatchFactory<A>(desc);

    A a = new A();
    analyzer.create("-o foo").invoke(new InvocationContext(), a);
    assertEquals("foo", a.s);

    try {
      a = new A();
      analyzer.create("").invoke(new InvocationContext(), a);
      fail();
    }
    catch (CmdSyntaxException e) {
    }
  }

  public void testOptionalOption() throws Exception {
    class A {
      @Option(names = "o")
      String s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    MatchFactory<A> analyzer = new MatchFactory<A>(desc);

    A a = new A();
    analyzer.create("-o foo").invoke(new InvocationContext(), a);
    assertEquals("foo", a.s);

    a = new A();
    analyzer.create("").invoke(new InvocationContext(), a);
    assertEquals(null, a.s);
  }

  public void testArgument() throws Exception {
    class A {
      @Argument(required = true)
      String s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    MatchFactory<A> analyzer = new MatchFactory<A>(desc);

    A a = new A();
    analyzer.create("foo").invoke(new InvocationContext(), a);
    assertEquals("foo", a.s);

    a = new A();
    analyzer.create("foo bar").invoke(new InvocationContext(), a);
    assertEquals("foo", a.s);

    try {
      a = new A();
      analyzer.create("").invoke(new InvocationContext(), a);
      fail();
    }
    catch (CmdSyntaxException e) {
    }
  }

  public void testOptionalArgument() throws Exception {
    class A {
      @Argument
      String s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    MatchFactory<A> analyzer = new MatchFactory<A>(desc);

    A a = new A();
    analyzer.create("foo").invoke(new InvocationContext(), a);
    assertEquals("foo", a.s);

    a = new A();
    analyzer.create("foo bar").invoke(new InvocationContext(), a);
    assertEquals("foo", a.s);

    a = new A();
    analyzer.create("").invoke(new InvocationContext(), a);
    assertEquals(null, a.s);
  }

  public void testOptionalArgumentList() throws Exception {
    class A {
      @Argument
      List<String> s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    MatchFactory<A> analyzer = new MatchFactory<A>(desc);

    A a = new A();
    analyzer.create("").invoke(new InvocationContext(), a);
    assertEquals(null , a.s);

    a = new A();
    analyzer.create("foo").invoke(new InvocationContext(), a);
    assertEquals(Arrays.asList("foo"), a.s);

    a = new A();
    analyzer.create("foo bar").invoke(new InvocationContext(), a);
    assertEquals(Arrays.asList("foo", "bar"), a.s);
  }

  public void testRequiredArgumentList() throws Exception {
    class A {
      @Argument(required = true)
      List<String> s;
    }
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    MatchFactory<A> analyzer = new MatchFactory<A>(desc);

    A a = new A();
    try {
      analyzer.create("").invoke(new InvocationContext(), a);
      fail();
    }
    catch (CmdSyntaxException expected) {
    }

    a = new A();
    analyzer.create("foo").invoke(new InvocationContext(), a);
    assertEquals(Arrays.asList("foo"), a.s);

    a = new A();
    analyzer.create("foo bar").invoke(new InvocationContext(), a);
    assertEquals(Arrays.asList("foo", "bar"), a.s);
  }

  public static class A {
    @Option(names = "s")
    String s;
    @Command
    public void m(@Option(names = "o") String o, @Argument String a) {
      this.o = o;
      this.a = a;
    }
    String o;
    String a;
  }

  public void testMethodInvocation() throws Exception {

    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    MatchFactory<A> analyzer = new MatchFactory<A>(desc);

    //
    A a = new A();
    analyzer.create("-s foo m -o bar juu").invoke(new InvocationContext(), a);
    assertEquals("foo", a.s);
    assertEquals("bar", a.o);
    assertEquals("juu", a.a);

    //
    a = new A();
    analyzer.create("m -o bar juu").invoke(new InvocationContext(), a);
    assertEquals(null, a.s);
    assertEquals("bar", a.o);
    assertEquals("juu", a.a);

    //
    a = new A();
    analyzer.create("m juu").invoke(new InvocationContext(), a);
    assertEquals(null, a.s);
    assertEquals(null, a.o);
    assertEquals("juu", a.a);

    //
    a = new A();
    analyzer.create("m -o bar").invoke(new InvocationContext(), a);
    assertEquals(null, a.s);
    assertEquals("bar", a.o);
    assertEquals(null, a.a);

    a = new A();
    analyzer.create("m").invoke(new InvocationContext(), a);
    assertEquals(null, a.s);
    assertEquals(null, a.o);
    assertEquals(null, a.a);
  }

  public static class B {

    int count;

    @Command
    public void main() {
      count++;
    }
  }

  public void testMainMethodInvocation() throws Exception {
    ClassDescriptor<B> desc = CommandDescriptor.create(B.class);
    MatchFactory<B> analyzer = new MatchFactory<B>("main", desc);

    //
    B b = new B();
    analyzer.create("").invoke(new InvocationContext(), b);
    assertEquals(1, b.count);
  }

  public static class C {

    Locale locale;

    @Command
    public void main(Locale locale) {
      this.locale = locale;
    }
  }

  public void testInvocationAttributeInjection() throws Exception {

    ClassDescriptor<C> desc = CommandDescriptor.create(C.class);
    MatchFactory<C> analyzer = new MatchFactory<C>("main", desc);

    //
    C c = new C();
    InvocationContext context = new InvocationContext();
    context.setAttribute(Locale.class, Locale.FRENCH);
    analyzer.create("").invoke(context, c);
    assertEquals(Locale.FRENCH, c.locale);
  }

  public static class D {

    private Integer i;

    @Command
    public void a(@Option(names = "o") Integer i) {
      this.i = i;
    }

    @Command
    public void b(@Option(names = "o") int i) {
      this.i = i;
    }
  }

  public void testInvocationTypeConversionInjection() throws Exception {

    ClassDescriptor<D> desc = CommandDescriptor.create(D.class);

    //
    D d = new D();
    InvocationContext context = new InvocationContext();
    new MatchFactory<D>("a", desc).create("-o 5").invoke(context, d);
    assertEquals((Integer)5, d.i);

    //
    d = new D();
    new MatchFactory<D>("b", desc).create("-o 5").invoke(context, d);
    assertEquals((Integer)5, d.i);
  }
}

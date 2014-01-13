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
import org.crsh.cli.CLIException;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.SyntaxException;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.Required;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.invocation.Resolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MatcherTestCase extends TestCase {


  public void testRequiredClassOption() throws Exception {
    class A implements Runnable{
      @Option(names = "o")
      @Required
      String s;
      public void run() {}
    }
    CommandDescriptor<A> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<A> analyzer = desc.matcher();

    A a = new A();
    analyzer.parse("-o foo").invoke(a);
    assertEquals("foo", a.s);

    try {
      a = new A();
      analyzer.parse("").invoke(a);
      fail();
    }
    catch (SyntaxException e) {
    }
  }

  public void testOptionalClassOption() throws Exception {
    class A implements Runnable {
      @Option(names = "o")
      String s;
      public void run() {}
    }
    CommandDescriptor<A> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<A> analyzer = desc.matcher();

    A a = new A();
    analyzer.parse("-o foo").invoke(a);
    assertEquals("foo", a.s);

    a = new A();
    analyzer.parse("").invoke(a);
    assertEquals(null, a.s);
  }

  public void testPrimitiveClassArgument() throws Exception {
    class A implements Runnable {
      @Argument
      int i;
      public void run() {}
    }
    CommandDescriptor<A> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<A> analyzer = desc.matcher();

    A a = new A();
    analyzer.parse("5").invoke(a);
    assertEquals(5, a.i);

    a = new A();
    analyzer.parse("5 6").invoke(a);
    assertEquals(5, a.i);

    a = new A();
    a.i = -3;
    analyzer.parse("").invoke(a);
    assertEquals(-3, a.i);
  }

  public static class PMA {
    int i;
    @Command
    public void m(@Argument int i) {
      this.i = i;
    }
  }

  public void testPrimitiveMethodArgument() throws Exception {
    CommandDescriptor<PMA> desc = CommandFactory.DEFAULT.create(PMA.class);
    InvocationMatcher<PMA> analyzer = desc.matcher();

    PMA a = new PMA();
    analyzer.parse("m 5").invoke(a);
    assertEquals(5, a.i);

    a = new PMA();
    analyzer.parse("m 5 6").invoke(a);
    assertEquals(5, a.i);

    a = new PMA();
    try {
      analyzer.parse("m").invoke(a);
      fail();
    }
    catch (SyntaxException e) {
    }
  }

  public void testOptionalClassArgument() throws Exception {
    class A implements Runnable {
      @Argument
      String s;
      public void run() {}
    }
    CommandDescriptor<A> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<A> analyzer = desc.matcher();

    A a = new A();
    analyzer.parse("foo").invoke(a);
    assertEquals("foo", a.s);

    a = new A();
    analyzer.parse("foo bar").invoke(a);
    assertEquals("foo", a.s);

    a = new A();
    analyzer.parse("").invoke(a);
    assertEquals(null, a.s);
  }

  public static class BC implements Runnable {
    @Argument
    List<String> s;
    @Command
    public void bar(@Argument List<String> s) { this.s = s; }
    public void run() {}
  }

  public void testOptionalArgumentList() throws Exception {
    CommandDescriptor<BC> desc = CommandFactory.DEFAULT.create(BC.class);
    InvocationMatcher<BC> analyzer = desc.matcher();

    for (String s : Arrays.asList("", "bar ")) {
      BC a = new BC();
      analyzer.parse(s + "").invoke(a);
      assertEquals(null, a.s);

      a = new BC();
      analyzer.parse(s + "foo").invoke(a);
      assertEquals(Arrays.asList("foo"), a.s);

      a = new BC();
      analyzer.parse(s + "foo bar").invoke(a);
      assertEquals(Arrays.asList("foo", "bar"), a.s);

      a = new BC();
      analyzer.parse(s + "foo ").invoke(a);
      assertEquals(Arrays.asList("foo"), a.s);
    }
  }

  public class OptionSyntaxException {
    @Option(names = "o")
    int option;
    @Command
    public void main() {
    }
  }

  public void testOptionSyntaxException() throws Exception {
    CommandDescriptor<OptionSyntaxException> desc = CommandFactory.DEFAULT.create(OptionSyntaxException.class);
    InvocationMatcher<OptionSyntaxException> analyzer = desc.matcher("main");
    OptionSyntaxException cmd = new OptionSyntaxException();

    //
    try {
      analyzer.parse("-o").invoke(cmd);
      fail();
    }
    catch (SyntaxException ignore) {
    }

    //
    try {
      analyzer.parse("-o 0 -o 1").invoke(cmd);
      fail();
    }
    catch (SyntaxException ignore) {
    }

    //
    try {
      analyzer.parse("-o a").invoke(cmd);
      fail();
    }
    catch (SyntaxException ignore) {
    }

    //
    analyzer.parse("-o 45").invoke(cmd);
    assertEquals(45, cmd.option);
  }

  public void testRequiredArgumentList() throws Exception {
    class A implements Runnable {
      @Argument
      @Required
      List<String> s;
      public void run() {}
    }
    CommandDescriptor<A> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<A> analyzer = desc.matcher();

    A a = new A();
    try {
      analyzer.parse("").invoke(a);
      fail();
    }
    catch (SyntaxException expected) {
    }

    a = new A();
    analyzer.parse("foo").invoke(a);
    assertEquals(Arrays.asList("foo"), a.s);

    a = new A();
    analyzer.parse("foo bar").invoke(a);
    assertEquals(Arrays.asList("foo", "bar"), a.s);
  }

  public static class A implements Runnable {
    @Option(names = "s")
    String s;
    @Command
    public void m(@Option(names = "o") String o, @Argument String a) {
      this.o = o;
      this.a = a;
    }
    String o;
    String a;
    public void run() {}
  }

  public void testMethodInvocation() throws Exception {

    CommandDescriptor<A> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<A> analyzer = desc.matcher();

    //
    A a = new A();
    analyzer.parse("-s foo m -o bar juu").invoke(a);
    assertEquals("foo", a.s);
    assertEquals("bar", a.o);
    assertEquals("juu", a.a);

    //
    a = new A();
    analyzer.parse("m -o bar juu").invoke(a);
    assertEquals(null, a.s);
    assertEquals("bar", a.o);
    assertEquals("juu", a.a);

    //
    a = new A();
    analyzer.parse("m juu").invoke(a);
    assertEquals(null, a.s);
    assertEquals(null, a.o);
    assertEquals("juu", a.a);

    //
    a = new A();
    analyzer.parse("m -o bar").invoke(a);
    assertEquals(null, a.s);
    assertEquals("bar", a.o);
    assertEquals(null, a.a);

    a = new A();
    analyzer.parse("m").invoke(a);
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
    CommandDescriptor<B> desc = CommandFactory.DEFAULT.create(B.class);

    //
    B b = new B();
    desc.matcher("main").parse("").invoke(b);
    assertEquals(1, b.count);
    b = new B();
    desc.matcher("main").arguments(Collections.emptyList()).invoke(b);
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

    CommandDescriptor<C> desc = CommandFactory.DEFAULT.create(C.class);
    InvocationMatcher<C> analyzer = desc.matcher("main");

    //
    C c = new C();
    Resolver context = new Resolver() {
      public <T> T resolve(Class<T> type) {
        if (type.equals(Locale.class)) {
          return type.cast(Locale.FRENCH);
        } else {
          return null;
        }
      }
    };
    analyzer.parse("").invoke(context, c);
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

    CommandDescriptor<D> desc = CommandFactory.DEFAULT.create(D.class);

    //
    D d = new D();
    desc.matcher("a").parse("-o 5").invoke(d);
    assertEquals((Integer)5, d.i);

    //
    d = new D();
    desc.matcher("b").parse("-o 5").invoke(d);
    assertEquals((Integer)5, d.i);
  }

  public static class E {

    private String i;

    @Command
    public void a(@Option(names = "o", unquote = false) String i) {
      this.i = i;
    }
  }

  public void testQuoted() throws Exception {

    CommandDescriptor<E> desc = CommandFactory.DEFAULT.create(E.class);

    //
    E e = new E();
    desc.matcher("a").parse("-o a").invoke(e);
    assertEquals("a", e.i);

    //
    e = new E();
    desc.matcher("a").parse("-o \"a\"").invoke(e);
    assertEquals("\"a\"", e.i);
  }

  public static class F {
    List<String> s;
    @Command
    public void foo(@Option(names = "o") List<String> s) { this.s = s; }
  }

  public void testOptionList() throws Exception {

    CommandDescriptor<F> desc = CommandFactory.DEFAULT.create(F.class);

    //
    F f = new F();
    desc.matcher("foo").parse("-o a").invoke(f);
    assertEquals(Arrays.asList("a"), f.s);
    f = new F();
    desc.matcher("foo").option("o", Collections.singletonList("a")).arguments(Collections.emptyList()).invoke(f);
    assertEquals(Arrays.asList("a"), f.s);

    //
    f = new F();
    desc.matcher("foo").parse("-o a -o b").invoke(f);
    assertEquals(Arrays.asList("a", "b"), f.s);
    f = new F();
    desc.matcher("foo").option("o", Arrays.asList("a", "b")).arguments(Collections.emptyList()).invoke(f);
    assertEquals(Arrays.asList("a", "b"), f.s);
  }

  public static class G {
    Custom o;
    @Command
    public void foo(@Option(names = "o") Custom o) { this.o = o; }
  }

  public void testValue() throws Exception {

    //
    CommandDescriptor<G> desc = new CommandFactory(MatcherTestCase.class.getClassLoader()).create(G.class);

    //
    G g = new G();
    desc.matcher("foo").parse("-o a").invoke(g);
    assertEquals(new Custom("a"), g.o);
    g = new G();
    desc.matcher("foo").option("o", Collections.singletonList("a")).arguments(Collections.emptyList()).invoke(g);
    assertEquals(new Custom("a"), g.o);
  }

  public static class H {
    @Command
    public void foo()  throws Exception { throw new Exception("fooexception"); }
  }

  public void testException() throws Exception {

    CommandDescriptor<H> desc = CommandFactory.DEFAULT.create(H.class);

    //
    H h = new H();
    InvocationMatch<H> matcher = desc.matcher("foo").parse("");
    try {
      matcher.invoke(h);
      fail();
    } catch (CLIException e) {
      assertEquals(Exception.class, e.getCause().getClass());
      assertEquals("fooexception", e.getCause().getMessage());
    }
  }

  public static class I {
    @Command
    public void foo() { throw new RuntimeException("fooruntimeexception"); }
  }

  public void testRuntimeException() throws Exception {

    CommandDescriptor<I> desc = CommandFactory.DEFAULT.create(I.class);

    //
    I i = new I();
    InvocationMatch<I> matcher = desc.matcher("foo").parse("");
    try {
      matcher.invoke(i);
      fail();
    } catch (CLIException e) {
      assertEquals(RuntimeException.class, e.getCause().getClass());
      assertEquals("fooruntimeexception", e.getCause().getMessage());
    }
  }

  public static class J {
    @Command
    public void foo() { throw new Error("fooerror"); }
  }

  public void testError() throws Exception {

    CommandDescriptor<J> desc = CommandFactory.DEFAULT.create(J.class);

    //
    J j = new J();
    InvocationMatch<J> matcher = desc.matcher("foo").parse("");
    try {
      matcher.invoke(j);
      fail();
    } catch (Error e) {
      assertEquals("fooerror", e.getMessage());
    }
  }

  public static class K {
    @Option(names = "o")
    String opt;
    @Command
    public void cmd() {}
  }

  public void testSpecifyClassOptionBeforeSubordinate() throws Exception {
    CommandDescriptor<K> desc = CommandFactory.DEFAULT.create(K.class);
    K k = new K();
    desc.matcher("main").parse("-o foo cmd").invoke(k);
    assertEquals("foo", k.opt);
    k = new K();
    desc.matcher("main").option("o", Collections.singletonList("foo")).subordinate("cmd").arguments(Collections.emptyList()).invoke(k);
    assertEquals("foo", k.opt);
  }

  public void testSpecifyClassOptionAfterSubordinate() throws Exception {
    CommandDescriptor<K> desc = CommandFactory.DEFAULT.create(K.class);
    K k = new K();
    desc.matcher("main").parse("cmd -o foo").invoke(k);
    assertEquals(null, k.opt);
    k = new K();
    desc.matcher("main").subordinate("cmd").option("o", Collections.singletonList("foo")).arguments(Collections.emptyList()).invoke(k);
    assertEquals(null, k.opt);
  }

  public static class L {
    String opt;
    @Command
    public void cmd(@Option(names = "o") String opt) { this.opt = opt; }
  }

  public void testSpecifySubordinateOptionBeforeSubordinate() throws Exception {
    CommandDescriptor<L> desc = CommandFactory.DEFAULT.create(L.class);
    L l = new L();
    desc.matcher("main").parse("-o foo cmd").invoke(l);
    assertEquals(null, l.opt);
    l = new L();
    desc.matcher("main").option("o", Collections.singletonList("foo")).subordinate("cmd").arguments(Collections.emptyList()).invoke(l);
    assertEquals(null, l.opt);
  }

  public void testSpecifySubordinateOptionAfterSubordinate() throws Exception {
    CommandDescriptor<L> desc = CommandFactory.DEFAULT.create(L.class);
    L l = new L();
    desc.matcher("main").parse("cmd -o foo").invoke(l);
    assertEquals("foo", l.opt);
    l = new L();
    desc.matcher("main").subordinate("cmd").option("o", Collections.singletonList("foo")).arguments(Collections.emptyList()).invoke(l);
    assertEquals("foo", l.opt);
  }

  public static class M {
    String opt;
    @Command
    public void main(@Option(names = "o") String opt) { this.opt = opt; }
  }

  public void testImplicitSubordinateOption() throws Exception {
    CommandDescriptor<M> desc = CommandFactory.DEFAULT.create(M.class);
    M m = new M();
    desc.matcher("main").parse("-o foo").invoke(m);
    assertEquals("foo", m.opt);
    m = new M();
    desc.matcher("main").option("o", Collections.singletonList("foo")).arguments(Collections.emptyList()).invoke(m);
    assertEquals("foo", m.opt);
  }

  public void testBooleanParameter() throws Exception {
    class A implements Runnable {
      @Option(names = "o")
      boolean o;
      public void run() {}
    }
    CommandDescriptor<A> desc = CommandFactory.DEFAULT.create(A.class);
    InvocationMatcher<A> analyzer = desc.matcher();

    //
    A a = new A();
    analyzer.parse("-o").invoke(a);
    assertEquals(true, a.o);
    a = new A();
    analyzer.option("o", Collections.singletonList(true)).arguments(Collections.emptyList()).invoke(a);
    assertEquals(true, a.o);
  }

  public void testSCP() throws Exception {
    class SCP implements Runnable {
      @Option(names = "t")
      boolean t;
      @Argument
      @Required
      String target;
      public void run() {}
    }
    CommandDescriptor<SCP> desc = CommandFactory.DEFAULT.create(SCP.class);
    InvocationMatcher<SCP> matcher = desc.matcher();

    //
    SCP scp = new SCP();
    matcher.parse("-t -- portal:collaboration:/Documents").invoke(scp);
    assertEquals(true, scp.t);
    assertEquals("portal:collaboration:/Documents", scp.target);
    scp = new SCP();
    matcher.option("t", Collections.singletonList(true)).arguments(Collections.singletonList("portal:collaboration:/Documents")).invoke(scp);
    assertEquals(true, scp.t);
    assertEquals("portal:collaboration:/Documents", scp.target);

    //
    scp = new SCP();
    try {
      matcher.parse("-t").invoke(scp);
      fail();
    } catch (CLIException e) {
    }
    scp = new SCP();
    try {
      matcher.option("t", Collections.singletonList(true)).arguments(Collections.emptyList()).invoke(scp);
      fail();
    } catch (CLIException e) {
    }
  }
}

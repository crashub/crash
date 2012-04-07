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
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.spi.CompletionResult;

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CompleteTestCase extends TestCase {

  public void testCompleterResolution() throws Exception {

    class A {
      @Command
      void m(@Argument() String arg) {}
      @Command
      void n(@Argument(completer =  CompleterSupport.Foo.class) String arg) {}
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);

    //
    assertEquals(CompletionResult.<String>create(), matcher.complete("m fo"));
    assertEquals(CompletionResult.create("o", " "), matcher.complete("n fo"));
    assertEquals(CompletionResult.create("ab", ""), matcher.complete(new CompleterSupport.Echo(), "m ab"));
    assertEquals(CompletionResult.create("o", " "), matcher.complete(new CompleterSupport.Echo(), "n fo"));
  }

  public void testExplicitCommandSingleArgument() throws Exception
  {

    class A {
      @Command
      void m(@Argument(completer =  CompleterSupport.Foo.class) String arg) {}
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);

    //
    assertEquals(CompletionResult.create("foo", " "), matcher.complete("m "));
    assertEquals(CompletionResult.create("oo", " "), matcher.complete("m f"));
    assertEquals(CompletionResult.create("o", " "), matcher.complete("m fo"));
    assertEquals(CompletionResult.<String>create(), matcher.complete("m a "));
    assertEquals(CompletionResult.<String>create(), matcher.complete("m a f"));
  }

  public void testImplicitCommandSingleArgument() throws Exception
  {

    class A {
      @Command
      void main(@Argument(completer =  CompleterSupport.Foo.class) String arg) {}
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher("main", desc);

    //
    assertEquals(CompletionResult.create("foo", " "), matcher.complete(""));
    assertEquals(CompletionResult.create("oo", " "), matcher.complete("f"));
    assertEquals(CompletionResult.create("o", " "), matcher.complete("fo"));
    assertEquals(CompletionResult.<String>create(), matcher.complete("a "));
    assertEquals(CompletionResult.<String>create(), matcher.complete("a f"));
  }

  public void testMultiArgument() throws Exception
  {

    class A {
      @Command
      void m(@Argument(completer =  CompleterSupport.Foo.class) List<String> arg) {}
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);

    //
    assertEquals(CompletionResult.create("foo", " "), matcher.complete("m "));
    assertEquals(CompletionResult.create("oo", " "), matcher.complete("m f"));
    assertEquals(CompletionResult.create("o", " "), matcher.complete("m fo"));
    assertEquals(CompletionResult.create("foo", " "), matcher.complete("m a "));
    assertEquals(CompletionResult.create("oo", " "), matcher.complete("m a f"));
    assertEquals(CompletionResult.create("o", " "), matcher.complete("m a fo"));
  }

  public void testOption() throws Exception
  {

    class A {
      @Option(names = {"a", "add", "addition"}) String add;
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);

    //
    assertEquals(CompletionResult.create("-", "a", " "), matcher.complete("-"));
    assertEquals(CompletionResult.create("", " "), matcher.complete("-a"));

    CompletionResult<String> a = CompletionResult.create("--", "add", " ").put("addition", " ");
    CompletionResult<String> b = CompletionResult.create("--ad", "d", " ").put("dition", " ");
    CompletionResult<String> c = CompletionResult.create("--addi", "tion", " ");

    //
    assertEquals(a, matcher.complete("--"));
    assertEquals(b, matcher.complete("--ad"));
    assertEquals(c, matcher.complete("--addi"));
  }

  public void testDoubleDash() throws Exception {

    class A {
      @Command
      void main(@Option(names = "o") String o, @Argument(completer = CompleterSupport.Foo.class) String arg) { }
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher("main", desc);

    //
    assertEquals(CompletionResult.create("oo", " "), matcher.complete("-- f"));
  }

  public void testOptionValue() throws Exception
  {

    class A {
      @Option(names = "a", completer = CompleterSupport.Foo.class) String a;
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);
    assertEquals(CompletionResult.create("foo", " "), matcher.complete("-a "));
    assertEquals(CompletionResult.create("oo", " "), matcher.complete("-a f"));
    assertEquals(CompletionResult.create("o", " "), matcher.complete("-a fo"));
    assertEquals(CompletionResult.<String>create("-b"), matcher.complete("-a -b"));
    assertEquals(CompletionResult.<String>create(), matcher.complete("-a b "));
    assertEquals(CompletionResult.<String>create("c"), matcher.complete("-a b c"));
  }

  public void testImplicitCommandOptionName() throws Exception
  {
    class A {
      @Command
      void main(@Option(names = {"o", "option"}) String o) { }
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher("main", desc);

    //
    assertEquals(CompletionResult.create("-", "o", " "), matcher.complete("-"));
    assertEquals(CompletionResult.create("--", "option", " "), matcher.complete("--"));
    assertEquals(CompletionResult.create("--o", "ption", " "), matcher.complete("--o"));
    assertEquals(CompletionResult.create("--op", "tion", " "), matcher.complete("--op"));
  }

  public void testOptionArgument() throws Exception
  {

    class A {
      @Command
      void main(@Option(names = "o") String o, @Argument(completer = CompleterSupport.Foo.class) String arg) { }
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher("main", desc);

    //
    assertEquals(CompletionResult.create("foo", " "), matcher.complete("-o bar "));
    assertEquals(CompletionResult.create("oo", " "), matcher.complete("-o bar f"));
  }

  public void testCommand() throws Exception
  {

    class A {
      @Option(names = "a") String a;
      @Command
      void foo(@Option(names = "b") String b) { }
      @Command
      void faa() { }
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher("main", desc);

    //
    CompletionResult<String> a = CompletionResult.create("foo", " ").put("faa", " ");
    CompletionResult<String> b = CompletionResult.create("f", "oo", " ").put("aa", " ");
    CompletionResult<String> c = CompletionResult.create("", " ");
    CompletionResult<String> d = CompletionResult.create();

    //
    assertEquals(a, matcher.complete(""));
    assertEquals(b, matcher.complete("f"));
    assertEquals(c, matcher.complete("foo"));
    assertEquals(d, matcher.complete("foo "));

    //
    assertEquals(a, matcher.complete("-a a "));
    assertEquals(b, matcher.complete("-a a f"));
    assertEquals(c, matcher.complete("-a a foo"));
    assertEquals(d, matcher.complete("-a a foo "));
  }

  public void testArgumentValuedMain() throws Exception
  {

    class A {
      @Command
      void main(@Argument(completer = CompleterSupport.Echo.class) String s) { }
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher("main", desc);

    //
    assertEquals(CompletionResult.create("", ""), matcher.complete(""));
    assertEquals(CompletionResult.create("m", ""), matcher.complete("m"));
    assertEquals(CompletionResult.create("ma", ""), matcher.complete("ma"));
    assertEquals(CompletionResult.create("mai", ""), matcher.complete("mai"));
    assertEquals(CompletionResult.create("main", ""), matcher.complete("main"));
    assertEquals(CompletionResult.<String>create(), matcher.complete("main "));
    assertEquals(CompletionResult.<String>create(), matcher.complete("main a"));
  }

  public void testEnum() throws Exception
  {
    class A {
      @Command
      void foo(@Option(names = "a") RetentionPolicy a) { }
      @Command
      void bar(@Argument RetentionPolicy a) { }
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);

    //
    CompletionResult<String> a = CompletionResult.create("SOURCE", " ").put("CLASS", " ").put("RUNTIME", " ");
    CompletionResult<String> b = CompletionResult.create("SOURCE", "\"").put("CLASS", "\"").put("RUNTIME", "\"");
    CompletionResult<String> c = CompletionResult.create("SOURCE", "'").put("CLASS", "'").put("RUNTIME", "'");
    CompletionResult<String> d = CompletionResult.create("RCE", " ");
    CompletionResult<String> e = CompletionResult.create("RCE", "\"");
    CompletionResult<String> f = CompletionResult.create("RCE", "'");
    CompletionResult<String> g = CompletionResult.create("", " ");
    CompletionResult<String> h = CompletionResult.create("", " ");

    //
    for (String m : Arrays.asList("foo -a", "bar")) {
      assertEquals("testing " + m, a, matcher.complete(m + " "));
      assertEquals("testing " + m, b, matcher.complete(m + " \""));
      assertEquals("testing " + m, c, matcher.complete(m + " '"));
      assertEquals("testing " + m, d, matcher.complete(m + " SOU"));
      assertEquals("testing " + m, e, matcher.complete(m + " \"SOU"));
      assertEquals("testing " + m, f, matcher.complete(m + " 'SOU"));
      assertEquals("testing " + m, g, matcher.complete(m + " SOURCE"));
      assertEquals("testing " + m, h, matcher.complete(m + " \"SOURCE\""));
    }
  }

  public void testCommandOption() throws Exception
  {
    class A {
      @Command
      void bar(@Option(names = "a", completer = CompleterSupport.Foo.class) String a) { }
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);

    //
    assertEquals(CompletionResult.create("bar", " "), matcher.complete(""));
    assertEquals(CompletionResult.create("b", "ar", " "), matcher.complete("b"));
    assertEquals(CompletionResult.create("", " "), matcher.complete("bar"));
    assertEquals(CompletionResult.<String>create(), matcher.complete("bar "));

    //
    assertEquals(CompletionResult.create("foo", " "), matcher.complete("bar -a "));
    assertEquals(CompletionResult.create("oo", " "), matcher.complete("bar -a f"));
    assertEquals(CompletionResult.create("o", " "), matcher.complete("bar -a fo"));
  }

  public void testFailure() throws Exception
  {

    //
    class A {
      @Command
      void foo(@Option(names = "a", completer = CompleterSupport.Exception.class) String a) { }
    }
    Matcher<A> matcherA = Matcher.createMatcher(CommandFactory.create(A.class));
    try {
      matcherA.complete("foo -a b");
      fail();
    }
    catch (CmdCompletionException e) {
    }

    //
    class B {
      @Command
      void foo(@Option(names = "a", completer = CompleterSupport.RuntimeException.class) String a) { }
    }
    Matcher<B> matcherB = Matcher.createMatcher(CommandFactory.create(B.class));
    try {
      matcherB.complete("foo -a b");
      fail();
    }
    catch (CmdCompletionException e) {
    }

    //
    class C {
      @Command
      void foo(@Option(names = "a", completer = CompleterSupport.Abstract.class) String a) { }
    }
    Matcher<C> matcherC = Matcher.createMatcher(CommandFactory.create(C.class));
    try {
      matcherC.complete("foo -a b");
      fail();
    }
    catch (CmdCompletionException e) {
    }
  }

  public void testArgumentProvidedValue() throws Exception {

    class A {
      ValueSupport.Provided o;
      @Command
      public void foo(@Argument ValueSupport.Provided o) { this.o = o; }
    }

    ClassDescriptor<A> desc = CommandFactory.create(A.class);

    //
    Matcher<A> matcher = Matcher.createMatcher(desc);
    assertEquals(CompletionResult.<String>create(), matcher.complete("foo "));
  }
}

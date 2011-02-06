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

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("m fo"));
    assertEquals(Collections.singletonMap("o", " "), matcher.complete("n fo"));
    assertEquals(Collections.singletonMap("ab", ""), matcher.complete(new CompleterSupport.Echo(), "m ab"));
    assertEquals(Collections.singletonMap("o", " "), matcher.complete(new CompleterSupport.Echo(), "n fo"));
  }

  public void testSingleArgument() throws Exception
  {

    class A {
      @Command
      void m(@Argument(completer =  CompleterSupport.Foo.class) String arg) {}
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);

    //
    assertEquals(Collections.singletonMap("foo", " "), matcher.complete("m "));
    assertEquals(Collections.singletonMap("oo", " "), matcher.complete("m f"));
    assertEquals(Collections.singletonMap("o", " "), matcher.complete("m fo"));
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("m a "));
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("m a f"));
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
    assertEquals(Collections.singletonMap("foo", " "), matcher.complete("m "));
    assertEquals(Collections.singletonMap("oo", " "), matcher.complete("m f"));
    assertEquals(Collections.singletonMap("o", " "), matcher.complete("m fo"));
    assertEquals(Collections.singletonMap("foo", " "), matcher.complete("m a "));
    assertEquals(Collections.singletonMap("oo", " "), matcher.complete("m a f"));
    assertEquals(Collections.singletonMap("o", " "), matcher.complete("m a fo"));
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
    assertEquals(Collections.singletonMap("a", " "), matcher.complete("-"));
    assertEquals(Collections.singletonMap("", " "), matcher.complete("-a"));

    Map<String, String> a = new HashMap<String, String>();
    a.put("add", " ");
    a.put("addition", " ");
    Map<String, String> b = new HashMap<String, String>();
    b.put("d", " ");
    b.put("dition", " ");
    Map<String, String> c = new HashMap<String, String>();
    c.put("tion", " ");

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
    assertEquals(Collections.singletonMap("oo", " "), matcher.complete("-- f"));
  }

  public void testOptionValue() throws Exception
  {

    class A {
      @Option(names = "a", completer = CompleterSupport.Foo.class) String a;
    }

    //
    ClassDescriptor<A> desc = CommandFactory.create(A.class);
    Matcher<A> matcher = Matcher.createMatcher(desc);
    assertEquals(Collections.singletonMap("foo", " "), matcher.complete("-a "));
    assertEquals(Collections.singletonMap("oo", " "), matcher.complete("-a f"));
    assertEquals(Collections.singletonMap("o", " "), matcher.complete("-a fo"));
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("-a -b"));
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("-a b "));
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("-a b c"));
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
    assertEquals(Collections.singletonMap("foo", " "), matcher.complete("-o bar "));
    assertEquals(Collections.singletonMap("oo", " "), matcher.complete("-o bar f"));
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
    Map<String, String> a = new HashMap<String, String>();
    a.put("foo", " ");
    a.put("faa", " ");
    Map<String, String> b = new HashMap<String, String>();
    b.put("oo", " ");
    b.put("aa", " ");
    Map<String, String> c = Collections.singletonMap("", " ");
    Map<String, String> d = Collections.<String, String>emptyMap();

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
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete(""));
    assertEquals(Collections.singletonMap("m", ""), matcher.complete("m"));
    assertEquals(Collections.singletonMap("ma", ""), matcher.complete("ma"));
    assertEquals(Collections.singletonMap("mai", ""), matcher.complete("mai"));
    assertEquals(Collections.singletonMap("main", ""), matcher.complete("main"));
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("main "));
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("main a"));
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
    Map<String, String> a = new HashMap<String, String>();
    a.put("SOURCE", " ");
    a.put("CLASS", " ");
    a.put("RUNTIME", " ");
    Map<String, String> b = new HashMap<String, String>();
    b.put("SOURCE", "\"");
    b.put("CLASS", "\"");
    b.put("RUNTIME", "\"");
    Map<String, String> c = new HashMap<String, String>();
    c.put("SOURCE", "'");
    c.put("CLASS", "'");
    c.put("RUNTIME", "'");
    Map<String, String> d = Collections.singletonMap("RCE", " ");
    Map<String, String> e = Collections.singletonMap("RCE", "\"");
    Map<String, String> f = Collections.singletonMap("RCE", "'");
    Map<String, String> g = Collections.singletonMap("", " ");
    Map<String, String> h = Collections.singletonMap("", " ");

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
    assertEquals(Collections.singletonMap("bar", " "), matcher.complete(""));
    assertEquals(Collections.singletonMap("ar", " "), matcher.complete("b"));
    assertEquals(Collections.singletonMap("", " "), matcher.complete("bar"));
    assertEquals(Collections.<String, String>emptyMap(), matcher.complete("bar "));

    //
    assertEquals(Collections.singletonMap("foo", " "), matcher.complete("bar -a "));
    assertEquals(Collections.singletonMap("oo", " "), matcher.complete("bar -a f"));
    assertEquals(Collections.singletonMap("o", " "), matcher.complete("bar -a fo"));
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
}

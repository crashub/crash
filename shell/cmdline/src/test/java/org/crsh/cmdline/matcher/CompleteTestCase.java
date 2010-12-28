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
import org.crsh.cmdline.Argument;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.Command;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Option;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CompleteTestCase extends TestCase {

  public static class FooCompleter implements Completer {
    public List<String> complete(ParameterDescriptor<?> parameter, String prefix) {
      return Arrays.asList(new StringBuilder(prefix).reverse().toString());
    }
  }

  public void testSingleArgument() throws Exception {

    class A {
      @Command
      void m(@Argument(completer =  FooCompleter.class) String arg) {}
    }

    //
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    Matcher<A> matcher = new Matcher<A>(desc);

    assertEquals(Arrays.asList(""), matcher.complete("m "));
    assertEquals(Arrays.asList("a"), matcher.complete("m a"));
    assertEquals(Arrays.asList("ba"), matcher.complete("m ab"));
    assertEquals(Arrays.<String>asList(), matcher.complete("m a "));
    assertEquals(Arrays.<String>asList(), matcher.complete("m a c"));
  }

  public void testMultiArgument() throws Exception {

    class A {
      @Command
      void m(@Argument(completer =  FooCompleter.class) List<String> arg) {}
    }

    //
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    Matcher<A> matcher = new Matcher<A>(desc);

    //
    assertEquals(Arrays.asList(""), matcher.complete("m "));
    assertEquals(Arrays.asList("a"), matcher.complete("m a"));
    assertEquals(Arrays.asList("ba"), matcher.complete("m ab"));
    assertEquals(Arrays.asList(""), matcher.complete("m a "));
    assertEquals(Arrays.asList("c"), matcher.complete("m a c"));
    assertEquals(Arrays.asList("dc"), matcher.complete("m a cd"));
  }

  public void testOption() throws Exception {

    class A {
      @Option(names = "a", completer = FooCompleter.class) String a;
    }

    //
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    Matcher<A> matcher = new Matcher<A>(desc);
    assertEquals(Arrays.asList(""), matcher.complete("-a "));
    assertEquals(Arrays.asList("a"), matcher.complete("-a a"));
    assertEquals(Arrays.asList("ba"), matcher.complete("-a ab"));
    assertEquals(Arrays.<String>asList(), matcher.complete("-a -b"));
    assertEquals(Arrays.<String>asList(), matcher.complete("-a b "));
    assertEquals(Arrays.<String>asList(), matcher.complete("-a b c"));
  }

  public void testCommand() throws Exception {

    class A {
      @Option(names = "a", completer = FooCompleter.class) String a;
      @Command
      void foo(@Option(names = "b", completer = FooCompleter.class) String b) { }
      @Command
      void faa() { }
    }

    //
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    Matcher<A> matcher = new Matcher<A>(desc);

    //
    assertEquals(Arrays.asList("foo ","faa "), matcher.complete(""));
    assertEquals(Arrays.asList("oo ","aa "), matcher.complete("f"));
    assertEquals(Arrays.asList(" "), matcher.complete("foo"));
    assertEquals(Arrays.<String>asList(), matcher.complete("foo "));

    //
    assertEquals(Arrays.asList("foo ","faa "), matcher.complete("-a a "));
    assertEquals(Arrays.asList("oo ","aa "), matcher.complete("-a a f"));
    assertEquals(Arrays.asList(" "), matcher.complete("-a a foo"));
    assertEquals(Arrays.<String>asList(), matcher.complete("-a a foo "));
  }

  public void testEnum() throws Exception {
    class A {
      @Command
      void foo(@Option(names = "a") RetentionPolicy a) { }
    }

    //
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    Matcher<A> matcher = new Matcher<A>(desc);

    //
    assertEquals(Arrays.asList("RCE "), matcher.complete("foo -a SOU"));
  }

  public void testCommandOption() throws Exception {
    class A {
      @Command
      void foo(@Option(names = "a", completer = FooCompleter.class) String a) { }
    }

    //
    ClassDescriptor<A> desc = CommandDescriptor.create(A.class);
    Matcher<A> matcher = new Matcher<A>(desc);

    //
    assertEquals(Arrays.asList("foo "), matcher.complete(""));
    assertEquals(Arrays.asList("oo "), matcher.complete("f"));
    assertEquals(Arrays.asList(" "), matcher.complete("foo"));
    assertEquals(Arrays.<String>asList(), matcher.complete("foo "));

    //
    assertEquals(Arrays.asList(""), matcher.complete("foo -a "));
    assertEquals(Arrays.asList("a"), matcher.complete("foo -a a"));
    assertEquals(Arrays.asList("ba"), matcher.complete("foo -a ab"));
  }

  static abstract class AbstractCompleter implements Completer {
  }

  static class RuntimeExceptionCompleter implements Completer {
    public List<String> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {
      throw new RuntimeException();
    }
  }

  static class ExceptionCompleter implements Completer {
    public List<String> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {
      throw new Exception();
    }
  }

  public void testFailure() throws Exception {

    //
    class A {
      @Command
      void foo(@Option(names = "a", completer = ExceptionCompleter.class) String a) { }
    }
    Matcher<A> matcherA = new Matcher<A>(CommandDescriptor.create(A.class));
    try {
      matcherA.complete("foo -a b");
      fail();
    }
    catch (CmdCompletionException e) {
    }

    //
    class B {
      @Command
      void foo(@Option(names = "a", completer = RuntimeExceptionCompleter.class) String a) { }
    }
    Matcher<B> matcherB = new Matcher<B>(CommandDescriptor.create(B.class));
    try {
      matcherB.complete("foo -a b");
      fail();
    }
    catch (CmdCompletionException e) {
    }

    //
    class C {
      @Command
      void foo(@Option(names = "a", completer = AbstractCompleter.class) String a) { }
    }
    Matcher<C> matcherC = new Matcher<C>(CommandDescriptor.create(C.class));
    try {
      matcherC.complete("foo -a b");
      fail();
    }
    catch (CmdCompletionException e) {
    }
  }
}

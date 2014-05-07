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
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.completion.CompletionException;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.impl.completion.CompletionMatcher;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.spi.Completion;

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CompleteTestCase extends TestCase {

  public void testUseInstance() throws Exception {

    class Some extends CompleterSupport.Constant {
      private Some() {
        super("bilto");
      }
    }
    Some some = new Some();

    class A {
      @Command
      void n(@Argument(completer =  Some.class) String arg) {}
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    try {
      matcher.match("n b");
      fail();
    }
    catch (CompletionException ignore) {
    }
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("ilto", true)), matcher.match(some, "n b"));
  }

  public void testSubordinateCommandSingleArgument() throws Exception
  {

    class A {
      @Command
      void m(@Argument(completer =  CompleterSupport.Foo.class) String arg) {}
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("foo", true)), matcher.match("m "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("oo", true)), matcher.match("m f"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("o", true)), matcher.match("m fo"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create()), matcher.match("m a "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("f")), matcher.match("m a f"));
  }

  public void testMainCommandSingleArgument() throws Exception
  {

    class A {
      @Command
      void main(@Argument(completer =  CompleterSupport.Foo.class) String arg) {}
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("foo", true)), matcher.match(""));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("oo", true)), matcher.match("f"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("o", true)), matcher.match("fo"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create()), matcher.match("a "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("f")), matcher.match("a f"));
  }

  public void testSecondArgument() throws Exception {

    class A {
      @Command
      void main(
        @Argument String arg1,
        @Argument(completer =  CompleterSupport.Foo.class) String arg2) {}
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("foo", true)), matcher.match("foo "));
    assertEquals(new CompletionMatch(Delimiter.DOUBLE_QUOTE, Completion.create("foo", true)), matcher.match("foo \""));
  }

  public void testMultiArgument() throws Exception
  {

    class A {
      @Command
      void m(@Argument(completer =  CompleterSupport.Foo.class) List<String> arg) {}
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("foo", true)), matcher.match("m "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("oo", true)), matcher.match("m f"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("o", true)), matcher.match("m fo"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("foo", true)), matcher.match("m a "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("oo", true)), matcher.match("m a f"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("o", true)), matcher.match("m a fo"));
  }

  public void testOption() throws Exception
  {

    class A {
      @Option(names = {"a", "add", "addition"}) String add;
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("-", "a", true)), matcher.match("-"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("", true)), matcher.match("-a"));

    CompletionMatch a = new CompletionMatch(Delimiter.EMPTY, Completion.builder("--").add("add", true).add("addition", true).build());
    CompletionMatch b = new CompletionMatch(Delimiter.EMPTY, Completion.builder("--ad").add("d", true).add("dition", true).build());
    CompletionMatch c = new CompletionMatch(Delimiter.EMPTY, Completion.create("--addi", "tion", true));

    //
    assertEquals(a, matcher.match("--"));
    assertEquals(b, matcher.match("--ad"));
    assertEquals(c, matcher.match("--addi"));
  }

  public void testDoubleDash() throws Exception {

    class A {
      @Command
      void main(@Option(names = "o") String o, @Argument(completer = CompleterSupport.Foo.class) String arg) { }
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("oo", true)), matcher.match("-- f"));
  }

  public void testOptionValue() throws Exception
  {

    class A {
      @Option(names = "a", completer = CompleterSupport.Foo.class) String a;
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("foo", true)), matcher.match("-a "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("oo", true)), matcher.match("-a f"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("o", true)), matcher.match("-a fo"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("-b")), matcher.match("-a -b"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create()), matcher.match("-a b "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("c")), matcher.match("-a b c"));
  }

  public void testImplicitCommandOptionName() throws Exception
  {
    class A {
      @Command
      void main(@Option(names = {"o", "option"}) String o) { }
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("-", "o", true)), matcher.match("-"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("--", "option", true)), matcher.match("--"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("--o", "ption", true)), matcher.match("--o"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("--op", "tion", true)), matcher.match("--op"));
  }

  public void testOptionArgument() throws Exception
  {

    class A {
      @Command
      void main(@Option(names = "o") String o, @Argument(completer = CompleterSupport.Foo.class) String arg) { }
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("foo", true)), matcher.match("-o bar "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("oo", true)), matcher.match("-o bar f"));
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
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    CompletionMatch a = new CompletionMatch(Delimiter.EMPTY, Completion.builder("").add("foo", true).add("faa", true).build());
    CompletionMatch b = new CompletionMatch(Delimiter.EMPTY, Completion.builder("f").add("oo", true).add("aa", true).build());
    CompletionMatch c = new CompletionMatch(Delimiter.EMPTY, Completion.create("", true));
    CompletionMatch d = new CompletionMatch(Delimiter.EMPTY, Completion.create());

    //
    assertEquals(a, matcher.match(""));
    assertEquals(b, matcher.match("f"));
    assertEquals(c, matcher.match("foo"));
    assertEquals(d, matcher.match("foo "));
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
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    CompletionMatch a = new CompletionMatch(Delimiter.EMPTY, Completion.builder("").add("SOURCE", true).add("CLASS", true).add("RUNTIME", true).build());
    CompletionMatch b = new CompletionMatch(Delimiter.DOUBLE_QUOTE, Completion.builder("").add("SOURCE", true).add("CLASS", true).add("RUNTIME", true).build());
    CompletionMatch c = new CompletionMatch(Delimiter.SINGLE_QUOTE, Completion.builder("").add("SOURCE", true).add("CLASS", true).add("RUNTIME", true).build());
    CompletionMatch d = new CompletionMatch(Delimiter.EMPTY, Completion.create("SOU", "RCE", true));
    CompletionMatch e = new CompletionMatch(Delimiter.DOUBLE_QUOTE, Completion.create("SOU", "RCE", true));
    CompletionMatch f = new CompletionMatch(Delimiter.SINGLE_QUOTE, Completion.create("SOU", "RCE", true));
    CompletionMatch g = new CompletionMatch(Delimiter.EMPTY, Completion.create("SOURCE", "", true));
    CompletionMatch h = new CompletionMatch(Delimiter.EMPTY, Completion.create("SOURCE", "", true));

    //
    for (String m : Arrays.asList("foo -a", "bar")) {
      assertEquals("testing " + m, a, matcher.match(m + " "));
      assertEquals("testing " + m, b, matcher.match(m + " \""));
      assertEquals("testing " + m, c, matcher.match(m + " '"));
      assertEquals("testing " + m, d, matcher.match(m + " SOU"));
      assertEquals("testing " + m, e, matcher.match(m + " \"SOU"));
      assertEquals("testing " + m, f, matcher.match(m + " 'SOU"));
      assertEquals("testing " + m, g, matcher.match(m + " SOURCE"));
      assertEquals("testing " + m, h, matcher.match(m + " \"SOURCE\""));
    }
  }

  public void testCommandOption() throws Exception
  {
    class A {
      @Command
      void bar(@Option(names = "a", completer = CompleterSupport.Foo.class) String a) { }
    }

    //
    CommandDescriptor<Instance<A>> desc = CommandFactory.DEFAULT.create(A.class);
    CompletionMatcher<Instance<A>> matcher = desc.completer();

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("bar", true)), matcher.match(""));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("b", "ar", true)), matcher.match("b"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("", true)), matcher.match("bar"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create()), matcher.match("bar "));

    //
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("foo", true)), matcher.match("bar -a "));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("oo", true)), matcher.match("bar -a f"));
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create("o", true)), matcher.match("bar -a fo"));
  }

  public void testFailure() throws Exception
  {

    //
    class A {
      @Command
      void foo(@Option(names = "a", completer = CompleterSupport.Exception.class) String a) { }
    }
    CompletionMatcher<Instance<A>> matcherA = CommandFactory.DEFAULT.create(A.class).completer();
    try {
      matcherA.match("foo -a b");
      fail();
    }
    catch (CompletionException e) {
    }

    //
    class B {
      @Command
      void foo(@Option(names = "a", completer = CompleterSupport.RuntimeException.class) String a) { }
    }
    CompletionMatcher<Instance<B>> matcherB = CommandFactory.DEFAULT.create(B.class).completer();
    try {
      matcherB.match("foo -a b");
      fail();
    }
    catch (CompletionException e) {
    }

    //
    class C {
      @Command
      void foo(@Option(names = "a", completer = CompleterSupport.Abstract.class) String a) { }
    }
    CompletionMatcher<Instance<C>> matcherC = CommandFactory.DEFAULT.create(C.class).completer();
    try {
      matcherC.match("foo -a b");
      fail();
    }
    catch (CompletionException e) {
    }
  }

  public void testArgumentProvidedValue() throws Exception {

    class A {
      Custom o;
      @Command
      public void foo(@Argument Custom o) { this.o = o; }
    }

    CommandDescriptor<Instance<A>> desc = new CommandFactory(CompleteTestCase.class.getClassLoader()).create(A.class);

    //
    CompletionMatcher<Instance<A>> matcher = desc.completer();
    assertEquals(new CompletionMatch(Delimiter.EMPTY, Completion.create()), matcher.match("foo "));
  }
}

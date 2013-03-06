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

package org.crsh.cmdline.impl;

import junit.framework.TestCase;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Required;
import org.crsh.cmdline.parser.Event;
import org.crsh.cmdline.parser.Mode;
import org.crsh.cmdline.parser.Parser;
import org.crsh.cmdline.tokenizer.TokenizerImpl;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParserTestCase extends TestCase {

  private static class Tester<T> {

    /** . */
    private final CommandDescriptor<T> command;

    /** . */
    private Parser parser;

    private Tester(CommandDescriptor<T> command, String s) {
      this(command, s, Mode.COMPLETE);
    }

    private Tester(CommandDescriptor<T> command, String s, Mode mode) {
      this.command = command;
      this.parser = new Parser<T>(new TokenizerImpl(s), (ClassDescriptor<T>)command, "main", mode);
    }

    public void assertSeparator() {
      Event event = parser.next();
      assertTrue("was expecting a separator instead of " + event, event instanceof Event.Separator);
    }

    public void assertMethod(String name) {
      Event.Subordinate event = (Event.Subordinate)parser.next();
      assertEquals(name, event.getDescriptor().getName());
    }

    public void assertOption(String name, String... values) {
      Event.Option event = (Event.Option)parser.next();
      assertTrue(event.getParameter().getNames().contains(name));
      assertEquals(Arrays.asList(values), event.getStrings());
    }

    public void assertArgument(String name, String... values) {
      Event.Argument event = (Event.Argument)parser.next();
      assertEquals(name, event.getParameter().getName());
      assertEquals(Arrays.asList(values), event.getStrings());
    }

    public void assertEnd(Class expectedClass, int expectedIndex) {
      Event.Stop event = (Event.Stop)parser.next();
      assertEquals(expectedClass, event.getClass());
      assertEquals(expectedIndex, event.getIndex());
    }
  }

  public void testUnkownClassOption() throws Exception {

    class A {
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertEnd(Event.Stop.Unresolved.NoSuchOption.class, 0);
  }

  public void testUnkownMethodOption1() throws Exception {

    class A {
      @Command
      void main() {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertMethod("main");
    tester.assertEnd(Event.Stop.Unresolved.NoSuchOption.class, 0);
  }

  public void testUnkownMethodOption2() throws Exception {

    class A {
      @Command
      void m() {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "m -o");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.NoSuchOption.class, 2);
  }

  public void testClassOption() throws Exception {

    class A {
      @Option(names = "o") String o;
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertEnd(Event.Stop.Done.class,  2);
    tester = new Tester<A>(cmd, "-o ");
    tester.assertOption("o");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 3);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 5);
  }

  public void testMethodOption() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o") String o) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertMethod("main");
    tester.assertOption("o");
    tester.assertEnd(Event.Stop.Done.class, 2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 5);
  }

  public void testClassOptionList() throws Exception {

    class A {
      @Option(names = "o")
      List<String> o;
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertEnd(Event.Stop.Done.class, 2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-o a -o b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("o", "b");
    tester.assertEnd(Event.Stop.Done.class, 9);
  }

  public void testMethodOptionList() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o") List<String> o) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertMethod("main");
    tester.assertOption("o");
    tester.assertEnd(Event.Stop.Done.class, 2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-o a -o b");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("o", "b");
    tester.assertEnd(Event.Stop.Done.class, 9);
  }

  public void testOptions1() throws Exception {

    class A {
      @Option(names = "o") String o;
      @Command
      public void main(@Option(names = "p") String p) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertEnd(Event.Stop.Done.class, 2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 5);

    //
    tester = new Tester<A>(cmd, "-p");
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 2);
    tester = new Tester<A>(cmd, "-p a");
    tester.assertMethod("main");
    tester.assertOption("p", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-p a b");
    tester.assertMethod("main");
    tester.assertOption("p", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 5);

    //
    tester = new Tester<A>(cmd, "-o -p");
    tester.assertOption("o");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 5);
    tester = new Tester<A>(cmd, "-o a -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 7);
    tester = new Tester<A>(cmd, "-o a -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p", "b");
    tester.assertEnd(Event.Stop.Done.class, 9);
  }

  public void testOptions2() throws Exception {

    class A {
      @Option(names = "o") String o;
      @Command
      public void m(@Option(names = "p") String p) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertEnd(Event.Stop.Done.class, 2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 5);

    //
    tester = new Tester<A>(cmd, "m -p");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "m -p a");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "a");
    tester.assertEnd(Event.Stop.Done.class, 6);
    tester = new Tester<A>(cmd, "m -p a b");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 7);

    //
    tester = new Tester<A>(cmd, "-o a m -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 9);
    tester = new Tester<A>(cmd, "-o a m -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertEnd(Event.Stop.Done.class, 11);

    //
    tester = new Tester<A>(cmd, "m -o a -p");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 9);
    tester = new Tester<A>(cmd, "m -o a -p b");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertEnd(Event.Stop.Done.class, 11);
  }

  public void testClassArgument() throws Exception {

    class A {
      @Argument(name = "arg") String arg;
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertArgument("arg", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertArgument("arg", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 2);
  }

  public void testMethodArgument() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("arg", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 2);
  }

  public void testSatisfyAllMethodArgument() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("arg", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("arg", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 2);
  }

  public void testMethodArgumentList() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "args") List<String> args) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("args", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a ");
    tester.assertMethod("main");
    tester.assertArgument("args", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 2);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("args", "a", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);

    //
    tester = new Tester<A>(cmd, "a b ");
    tester.assertMethod("main");
    tester.assertArgument("args", "a", "b");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 4);
  }

  public void testSatisfyAllMethodArgumentList() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "args") List<String> args) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("args", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a ", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("args", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 2);

    //
    tester = new Tester<A>(cmd, "a b", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("args", "a", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);

    //
    tester = new Tester<A>(cmd, "a b ", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("args", "a", "b");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 4);
  }

  public void testMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") String arg1, @Argument(name = "arg2") String arg2) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);
  }

  public void testSatisfyAllMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") String arg1, @Argument(name = "arg2") String arg2) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);
  }

  public void testRequiredMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Required @Argument(name = "arg1") String arg1, @Required @Argument(name = "arg2") String arg2) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);
  }

  public void testSatisfyAllRequiredMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Required @Argument(name = "arg1") String arg1, @Required @Argument(name = "arg2") String arg2) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);
  }

  public void testMixedMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") List<String> arg1, @Argument(name = "arg2") String arg2) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);
  }

  public void testSatisfyAllMixedMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") List<String> arg1, @Argument(name = "arg2") String arg2) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("arg2", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);
  }

  public void testExplicitMainMethod() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "main");
    tester.assertMethod("main");
    tester.assertArgument("arg", "main");
    tester.assertEnd(Event.Stop.Done.class, 4);
  }

  public void testSatisfyAllExplicitMainMethod() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "main", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertArgument("arg", "main");
    tester.assertEnd(Event.Stop.Done.class, 4);
  }

  public void testDoubleDash() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o") String o, @Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "--", Mode.COMPLETE);
    tester.assertMethod("main");
    tester.assertEnd(Event.Stop.Unresolved.NoSuchOption.class, 0);

    //
    tester = new Tester<A>(cmd, "-- ", Mode.COMPLETE);
    tester.assertMethod("main");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 3);

    //
    tester = new Tester<A>(cmd, "-- foo", Mode.COMPLETE);
    tester.assertMethod("main");
    tester.assertSeparator();
    tester.assertArgument("arg", "foo");
    tester.assertEnd(Event.Stop.Done.class, 6);
  }

  public void testSatisfyAllDoubleDash() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o") String o, @Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<A> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "--", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertEnd(Event.Stop.Done.class, 2);

    //
    tester = new Tester<A>(cmd, "-- ", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 3);

    //
    tester = new Tester<A>(cmd, "-- foo", Mode.INVOKE);
    tester.assertMethod("main");
    tester.assertSeparator();
    tester.assertArgument("arg", "foo");
    tester.assertEnd(Event.Stop.Done.class, 6);
  }
}

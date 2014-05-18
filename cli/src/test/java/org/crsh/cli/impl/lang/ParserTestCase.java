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

package org.crsh.cli.impl.lang;

import junit.framework.TestCase;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.Required;
import org.crsh.cli.impl.parser.Event;
import org.crsh.cli.impl.parser.Mode;
import org.crsh.cli.impl.parser.Parser;
import org.crsh.cli.impl.tokenizer.TokenizerImpl;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParserTestCase extends TestCase {

  private static class Tester<T> {

    /** . */
    private Parser parser;

    private Tester(CommandDescriptor<Instance<T>> command, String s) {
      this(command, s, Mode.COMPLETE);
    }

    private Tester(CommandDescriptor<Instance<T>> command, String s, Mode mode) {
      this.parser = new Parser<Instance<T>>(new TokenizerImpl(s), command, mode);
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
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertEnd(Event.Stop.Unresolved.NoSuchOption.class, 0);
  }

  public void testUnkownMethodOption1() throws Exception {

    class A {
      @Command
      void main() {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertEnd(Event.Stop.Unresolved.NoSuchOption.class, 0);
  }

  public void testUnkownMethodOption2() throws Exception {

    class A {
      @Command
      void m() {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertEnd(Event.Stop.Unresolved.NoSuchOption.class, 0);
  }

  public void testClassOption() throws Exception {

    class A {
      @Option(names = "o") String o;
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

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
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

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
  }

  public void testClassOptionList() throws Exception {

    class A {
      @Option(names = "o")
      List<String> o;
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

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
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

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

  public void testOptions1() throws Exception {

    class A {
      @Option(names = "o") String o;
      @Command
      public void main(@Option(names = "p") String p) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

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
    tester = new Tester<A>(cmd, "-p");
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 2);
    tester = new Tester<A>(cmd, "-p a");
    tester.assertOption("p", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-p a b");
    tester.assertOption("p", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 5);

    //
    tester = new Tester<A>(cmd, "-o -p");
    tester.assertOption("o");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 5);
    tester = new Tester<A>(cmd, "-o a -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 7);
    tester = new Tester<A>(cmd, "-o a -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertEnd(Event.Stop.Done.class, 9);
  }

  public void testOptions2() throws Exception {

    class A {
      @Option(names = "o") String o;
      @Command
      public void main(@Option(names = "p") String p) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

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
    tester = new Tester<A>(cmd, "-p");
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 2);
    tester = new Tester<A>(cmd, "-p a");
    tester.assertOption("p", "a");
    tester.assertEnd(Event.Stop.Done.class, 4);
    tester = new Tester<A>(cmd, "-p a b");
    tester.assertOption("p", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Unresolved.TooManyArguments.class, 5);

    //
    tester = new Tester<A>(cmd, "-o a -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 7);
    tester = new Tester<A>(cmd, "-o a -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertEnd(Event.Stop.Done.class, 9);

    //
    tester = new Tester<A>(cmd, "-o a -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertEnd(Event.Stop.Done.class, 7);
    tester = new Tester<A>(cmd, "-o a -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertEnd(Event.Stop.Done.class, 9);
  }

  public void testImplicitSubordinateOption() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o") String o) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o foo");
    tester.assertOption("o", "foo");
    tester.assertEnd(Event.Stop.Done.class, 6);
  }

  public void testClassArgument() throws Exception {

    class A {
      @Argument(name = "arg") String arg;
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

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
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

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

  public void testSatisfyAllMethodArgument() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertArgument("arg", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b", Mode.INVOKE);
    tester.assertArgument("arg", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 2);
  }

  public void testMethodArgumentList() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "args") List<String> args) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertArgument("args", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a ");
    tester.assertArgument("args", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 2);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertArgument("args", "a", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);

    //
    tester = new Tester<A>(cmd, "a b ");
    tester.assertArgument("args", "a", "b");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 4);
  }

  public void testSatisfyAllMethodArgumentList() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "args") List<String> args) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertArgument("args", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a ", Mode.INVOKE);
    tester.assertArgument("args", "a");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 2);

    //
    tester = new Tester<A>(cmd, "a b", Mode.INVOKE);
    tester.assertArgument("args", "a", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);

    //
    tester = new Tester<A>(cmd, "a b ", Mode.INVOKE);
    tester.assertArgument("args", "a", "b");
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 4);
  }

  public void testMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") String arg1, @Argument(name = "arg2") String arg2) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
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
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b", Mode.INVOKE);
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
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
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
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
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
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertArgument("arg1", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertArgument("arg1", "a", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);
  }

  public void testSatisfyAllMixedMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") List<String> arg1, @Argument(name = "arg2") String arg2) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", Mode.INVOKE);
    tester.assertArgument("arg2", "a");
    tester.assertEnd(Event.Stop.Done.class, 1);

    //
    tester = new Tester<A>(cmd, "a b", Mode.INVOKE);
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertEnd(Event.Stop.Done.class, 3);
  }

  public void testDoubleDash() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o") String o, @Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "--", Mode.COMPLETE);
    tester.assertEnd(Event.Stop.Unresolved.NoSuchOption.class, 0);

    //
    tester = new Tester<A>(cmd, "-- ", Mode.COMPLETE);
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 3);

    //
    tester = new Tester<A>(cmd, "-- foo", Mode.COMPLETE);
    tester.assertSeparator();
    tester.assertArgument("arg", "foo");
    tester.assertEnd(Event.Stop.Done.class, 6);
  }

  public void testSatisfyAllDoubleDash() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o") String o, @Argument(name = "arg") String arg) {}
    }
    CommandDescriptor<Instance<A>> cmd = CommandFactory.DEFAULT.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "--", Mode.INVOKE);
    tester.assertEnd(Event.Stop.Done.class, 2);

    //
    tester = new Tester<A>(cmd, "-- ", Mode.INVOKE);
    tester.assertSeparator();
    tester.assertEnd(Event.Stop.Done.class, 3);

    //
    tester = new Tester<A>(cmd, "-- foo", Mode.INVOKE);
    tester.assertSeparator();
    tester.assertArgument("arg", "foo");
    tester.assertEnd(Event.Stop.Done.class, 6);
  }
}

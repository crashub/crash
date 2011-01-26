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

package org.crsh.cmdline.matcher.impl;

import junit.framework.TestCase;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Required;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParserTestCase extends TestCase {

  private static class Tester<T> {

    /** . */
    private final ClassDescriptor<T> command;

    /** . */
    private Parser parser;

    private Tester(ClassDescriptor<T> command, String s) {
      this(command, s, false);
    }

    private Tester(ClassDescriptor<T> command, String s, boolean satisfyArguments) {
      this.command = command;
      this.parser = new Parser<T>(new Tokenizer(s), command, "main", satisfyArguments);
    }

    public void assertSeparator() {
      Event event = parser.bilto();
      assertTrue("was expecting a separator instead of " + event, event instanceof Event.Separator);
    }

    public void assertMethod(String name) {
      Event.Method event = (Event.Method)parser.bilto();
      assertEquals(name, event.getDescriptor().getName());
    }

    public void assertOption(String name, String... values) {
      Event.Option event = (Event.Option)parser.bilto();
      assertTrue(event.getDescriptor().getNames().contains(name));
      assertEquals(Arrays.asList(values), event.getStrings());
    }

    public void assertArgument(String name, String... values) {
      Event.Argument event = (Event.Argument)parser.bilto();
      assertEquals(name, event.getDescriptor().getName());
      assertEquals(Arrays.asList(values), event.getStrings());
    }

    public void assertEnd(Code code, int expectedIndex) {
      Event.Stop event = (Event.Stop)parser.bilto();
      assertEquals(code, event.getCode());
      assertEquals(expectedIndex, event.getIndex());
    }

    public void assertDone(int expectedIndex) {
      assertEnd(Code.DONE, expectedIndex);
    }
  }

  public void testUnkownClassOption() throws Exception {

    class A {
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertEnd(Code.NO_SUCH_CLASS_OPTION, 0);
  }

  public void testUnkownMethodOption1() throws Exception {

    class A {
      @Command
      void main() {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertEnd(Code.NO_SUCH_METHOD_OPTION, 0);
  }

  public void testUnkownMethodOption2() throws Exception {

    class A {
      @Command
      void m() {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "m -o");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertEnd(Code.NO_SUCH_METHOD_OPTION, 2);
  }

  public void testClassOption() throws Exception {

    class A {
      @Option(names = "o") String o;
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertDone(2);
    tester = new Tester<A>(cmd, "-o ");
    tester.assertOption("o");
    tester.assertSeparator();
    tester.assertDone(3);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertDone(4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertEnd(Code.NO_ARGUMENT, 5);
  }

  public void testMethodOption() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o") String o) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertMethod("main");
    tester.assertOption("o");
    tester.assertDone(2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertDone(4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertEnd(Code.NO_ARGUMENT, 5);
  }

  public void testClassOptionList() throws Exception {

    class A {
      @Option(names = "o", arity = 2)
      List<String> o;
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertDone(2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertDone(4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a", "b");
    tester.assertDone(6);
  }

  public void testMethodOptionList() throws Exception {

    class A {
      @Command
      public void main(@Option(names = "o", arity = 2) List<String> o) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertMethod("main");
    tester.assertOption("o");
    tester.assertDone(2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertDone(4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertMethod("main");
    tester.assertOption("o", "a", "b");
    tester.assertDone(6);
  }

  public void testOptions1() throws Exception {

    class A {
      @Option(names = "o") String o;
      @Command
      public void main(@Option(names = "p") String p) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertDone(2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertDone(4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertEnd(Code.NO_ARGUMENT, 5);

    //
    tester = new Tester<A>(cmd, "-p");
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertDone(2);
    tester = new Tester<A>(cmd, "-p a");
    tester.assertMethod("main");
    tester.assertOption("p", "a");
    tester.assertDone(4);
    tester = new Tester<A>(cmd, "-p a b");
    tester.assertMethod("main");
    tester.assertOption("p", "a");
    tester.assertSeparator();
    tester.assertEnd(Code.NO_ARGUMENT, 5);

    //
    tester = new Tester<A>(cmd, "-o -p");
    tester.assertOption("o");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertDone(5);
    tester = new Tester<A>(cmd, "-o a -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertDone(7);
    tester = new Tester<A>(cmd, "-o a -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p", "b");
    tester.assertDone(9);
  }

  public void testOptions2() throws Exception {

    class A {
      @Option(names = "o") String o;
      @Command
      public void m(@Option(names = "p") String p) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertDone(2);
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertDone(4);
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertEnd(Code.NO_ARGUMENT, 5);

    //
    tester = new Tester<A>(cmd, "m -p");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertDone(4);
    tester = new Tester<A>(cmd, "m -p a");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "a");
    tester.assertDone(6);
    tester = new Tester<A>(cmd, "m -p a b");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "a");
    tester.assertSeparator();
    tester.assertEnd(Code.NO_ARGUMENT, 7);

    //
    tester = new Tester<A>(cmd, "-o a m -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertDone(9);
    tester = new Tester<A>(cmd, "-o a m -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertDone(11);

    //
    tester = new Tester<A>(cmd, "m -o a -p");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertDone(9);
    tester = new Tester<A>(cmd, "m -o a -p b");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertDone(11);
  }

  public void testClassArgument() throws Exception {

    class A {
      @Argument(name = "arg") String arg;
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertArgument("arg", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertArgument("arg", "a");
    tester.assertSeparator();
    tester.assertEnd(Code.NO_ARGUMENT, 2);
  }

  public void testMethodArgument() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("arg", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg", "a");
    tester.assertSeparator();
    tester.assertEnd(Code.NO_ARGUMENT, 2);
  }

  public void testSatisfyAllMethodArgument() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", true);
    tester.assertMethod("main");
    tester.assertArgument("arg", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b", true);
    tester.assertMethod("main");
    tester.assertArgument("arg", "a");
    tester.assertSeparator();
    tester.assertDone(2);
  }

  public void testMethodArgumentList() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "args") List<String> args) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("args", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a ");
    tester.assertMethod("main");
    tester.assertArgument("args", "a");
    tester.assertSeparator();
    tester.assertDone(2);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("args", "a", "b");
    tester.assertDone(3);

    //
    tester = new Tester<A>(cmd, "a b ");
    tester.assertMethod("main");
    tester.assertArgument("args", "a", "b");
    tester.assertSeparator();
    tester.assertDone(4);
  }

  public void testSatisfyAllMethodArgumentList() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "args") List<String> args) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", true);
    tester.assertMethod("main");
    tester.assertArgument("args", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a ", true);
    tester.assertMethod("main");
    tester.assertArgument("args", "a");
    tester.assertSeparator();
    tester.assertDone(2);

    //
    tester = new Tester<A>(cmd, "a b", true);
    tester.assertMethod("main");
    tester.assertArgument("args", "a", "b");
    tester.assertDone(3);

    //
    tester = new Tester<A>(cmd, "a b ", true);
    tester.assertMethod("main");
    tester.assertArgument("args", "a", "b");
    tester.assertSeparator();
    tester.assertDone(4);
  }

  public void testMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") String arg1, @Argument(name = "arg2") String arg2) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertDone(3);
  }

  public void testSatisfyAllMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") String arg1, @Argument(name = "arg2") String arg2) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", true);
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b", true);
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertDone(3);
  }

  public void testRequiredMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Required @Argument(name = "arg1") String arg1, @Required @Argument(name = "arg2") String arg2) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertDone(3);
  }

  public void testSatisfyAllRequiredMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Required @Argument(name = "arg1") String arg1, @Required @Argument(name = "arg2") String arg2) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", true);
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertDone(3);
  }

  public void testMixedMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") List<String> arg1, @Argument(name = "arg2") String arg2) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b");
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a", "b");
    tester.assertDone(3);
  }

  public void testSatisfyAllMixedMethodArguments() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg1") List<String> arg1, @Argument(name = "arg2") String arg2) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a", true);
    tester.assertMethod("main");
    tester.assertArgument("arg2", "a");
    tester.assertDone(1);

    //
    tester = new Tester<A>(cmd, "a b", true);
    tester.assertMethod("main");
    tester.assertArgument("arg1", "a");
    tester.assertSeparator();
    tester.assertArgument("arg2", "b");
    tester.assertDone(3);
  }

  public void testExplicitMainMethod() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "main");
    tester.assertMethod("main");
    tester.assertArgument("arg", "main");
    tester.assertDone(4);
  }

  public void testSatisfyAllExplicitMainMethod() throws Exception {

    class A {
      @Command
      public void main(@Argument(name = "arg") String arg) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "main", true);
    tester.assertMethod("main");
    tester.assertArgument("arg", "main");
    tester.assertDone(4);
  }
}

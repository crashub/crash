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

package org.crsh.cmdline.matcher.impl2;

import junit.framework.TestCase;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;

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
      this.command = command;
      this.parser = new Parser<T>(new Tokenizer(s), command, "main", false);
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
      assertEquals(Arrays.asList(values), event.getValues());
    }

    public void assertError(Code code) {
      Event.End event = (Event.End)parser.bilto();
      assertEquals(code, event.getCode());
    }

    public void assertDone() {
      assertError(Code.DONE);
    }
  }

  public void testUnkownClassOption() throws Exception {

    class A {
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertError(Code.NO_SUCH_CLASS_OPTION);
  }

  public void testUnkownMethodOption1() throws Exception {

    class A {
      @Command
      void main() {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertError(Code.NO_SUCH_METHOD_OPTION);
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
    tester.assertError(Code.NO_SUCH_METHOD_OPTION);
  }

  public void testClassOption() throws Exception {

    class A {
      @Option(names = "o") String o;
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertError(Code.NO_METHOD);
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
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertError(Code.NO_ARGUMENT);
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
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a", "b");
    tester.assertDone();
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
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a");
    tester.assertMethod("main");
    tester.assertOption("o", "a");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertMethod("main");
    tester.assertOption("o", "a", "b");
    tester.assertDone();
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
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertError(Code.NO_ARGUMENT);

    //
    tester = new Tester<A>(cmd, "-p");
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-p a");
    tester.assertMethod("main");
    tester.assertOption("p", "a");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-p a b");
    tester.assertMethod("main");
    tester.assertOption("p", "a");
    tester.assertSeparator();
    tester.assertError(Code.NO_ARGUMENT);

    //
    tester = new Tester<A>(cmd, "-o -p");
    tester.assertOption("o");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("main");
    tester.assertOption("p", "b");
    tester.assertDone();
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
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertError(Code.NO_METHOD);

    //
    tester = new Tester<A>(cmd, "m -p");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertDone();
    tester = new Tester<A>(cmd, "m -p a");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "a");
    tester.assertDone();
    tester = new Tester<A>(cmd, "m -p a b");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "a");
    tester.assertSeparator();
    tester.assertError(Code.NO_ARGUMENT);

    //
    tester = new Tester<A>(cmd, "-o a m -p");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertDone();
    tester = new Tester<A>(cmd, "-o a m -p b");
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertDone();

    //
    tester = new Tester<A>(cmd, "m -o a -p");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p");
    tester.assertDone();
    tester = new Tester<A>(cmd, "m -o a -p b");
    tester.assertMethod("m");
    tester.assertSeparator();
    tester.assertOption("o", "a");
    tester.assertSeparator();
    tester.assertOption("p", "b");
    tester.assertDone();
  }

  public void testMethodArgument() throws Exception {

    class A {
      @Command
      public void main(@Argument String a) {}
    }
    ClassDescriptor<A> cmd = CommandFactory.create(A.class);

    //
    Tester<A> tester = new Tester<A>(cmd, "a");
    tester.assertMethod("main");
    try {
      tester.assertDone();
    }
    catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    //
//    tester = new Tester<A>(cmd, "a b");
//    tester.assertMethod("main");
//    tester.assertDone();
  }
}

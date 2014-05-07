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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.invocation.ArgumentMatch;
import org.crsh.cli.impl.invocation.OptionMatch;
import org.crsh.cli.impl.LiteralValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Parser2TestCase extends TestCase {

  private static class Test {

    /** . */
    private LinkedList<OptionMatch> sharedOptionMatches;

    /** . */
    private LinkedList<OptionMatch> optionMatches;

    /** . */
    private LinkedList<ArgumentMatch> argumentMatches;

    /** . */
    private String rest;

    /** . */
    private String methodName;

    private <T> Test(Class<T> type, String s) {
      try {
        CommandDescriptor<Instance<T>> command = CommandFactory.DEFAULT.create(type);
        InvocationMatcher<Instance<T>> parser = command.matcher();
        InvocationMatch<Instance<T>> match = parser.parse(s);

        //
        if (match.owner() == null) {
          this.sharedOptionMatches = new LinkedList<OptionMatch>();
          this.optionMatches = new LinkedList<OptionMatch>(match.options());
          this.argumentMatches = new LinkedList<ArgumentMatch>(match.arguments());
          this.rest = match.getRest();
          this.methodName = null;
        } else {
          this.sharedOptionMatches = new LinkedList<OptionMatch>(match.owner().options());
          this.optionMatches = new LinkedList<OptionMatch>(match.options());
          this.argumentMatches = new LinkedList<ArgumentMatch>(match.arguments());
          this.rest = match.getRest();
          this.methodName = match.getDescriptor().getName();
        }

      }
      catch (Exception e) {
        AssertionFailedError afe = new AssertionFailedError();
        afe.initCause(e);
        throw afe;
      }
    }

    public Test assertSharedOption(String expectedName, String... expectedValues) {
      assertTrue(sharedOptionMatches.size() > 0);
      OptionMatch match = sharedOptionMatches.removeFirst();
      assertEquals(expectedName, match.getName());
      ArrayList<String> values = new ArrayList<String>();
      for (LiteralValue value : match.getValues()) {
        values.add(value.getValue());
      }
      assertEquals(Arrays.asList(expectedValues), values);
      return this;
    }

    public Test assertOption(String expectedName, String... expectedValues) {
      assertTrue(optionMatches.size() > 0);
      OptionMatch match = optionMatches.removeFirst();
      assertEquals(expectedName, match.getName());
      ArrayList<String> values = new ArrayList<String>();
      for (LiteralValue value : match.getValues()) {
        values.add(value.getValue());
      }
      assertEquals(Arrays.asList(expectedValues), values);
      return this;
    }

    public Test assertArgument(int start, int end, String... expectedValues) {
      assertTrue(argumentMatches.size() > 0);
      ArgumentMatch match = argumentMatches.removeFirst();
      assertEquals(start, match.getStart());
      assertEquals(end, match.getEnd());
      ArrayList<String> values = new ArrayList<String>();
      for (LiteralValue value : match.getValues()) {
        values.add(value.getValue());
      }
      assertEquals(Arrays.asList(expectedValues), values);
      return this;
    }

    public Test assertMethod(String name) {
      assertEquals(methodName, name);
      methodName = null;
      return this;
    }

    public void assertDone(String expectedRest) {
      assertEquals(expectedRest, rest);
      assertNull(methodName);
      assertEquals(Collections.<OptionMatch>emptyList(), sharedOptionMatches);
      assertEquals(Collections.<OptionMatch>emptyList(), optionMatches);
      assertEquals(Collections.<ArgumentMatch>emptyList(), argumentMatches);
    }

    public void assertDone() {
      assertDone("");
    }
  }

  public void testMixed() throws Exception {
    class A {
      @Option(names = "o")
      String o;
      @Option(names = "p")
      boolean p;
      @Argument
      String a;
      @Argument
      List<String> b;
    }
    new Test(A.class, "-o foo bar").assertOption("o", "foo").assertArgument(7, 10, "bar").assertDone();
    new Test(A.class, "-o foo -p bar").assertOption("o", "foo").assertOption("p").assertArgument(10, 13, "bar").assertDone();
  }

  public void testArgument() throws Exception {
    class A {
      @Argument
      String a;
    }
    new Test(A.class, "foo").assertArgument(0, 3, "foo").assertDone();
    new Test(A.class, "foo bar").assertArgument(0, 3, "foo").assertDone("bar");
    class B {
      @Argument
      List<String> a;
    }
    new Test(B.class, "foo").assertArgument(0, 3, "foo").assertDone();
    new Test(B.class, "foo bar").assertArgument(0, 7, "foo", "bar").assertDone();
    class C {
      @Argument
      String a;
      @Argument
      List<String> b;
    }
    new Test(C.class, "foo").assertArgument(0, 3, "foo").assertDone();
    new Test(C.class, "foo bar").assertArgument(0, 3, "foo").assertArgument(4, 7, "bar").assertDone();
    new Test(C.class, "foo bar juu").assertArgument(0, 3, "foo").assertArgument(4, 11, "bar", "juu").assertDone();
    class D {
      @Argument
      List<String> a;
      @Argument
      String b;
    }
    new Test(D.class, "").assertDone();
    new Test(D.class, "foo").assertArgument(0, 3, "foo").assertDone();
    new Test(D.class, "foo bar").assertArgument(0, 3, "foo").assertArgument(4, 7, "bar").assertDone();
    new Test(D.class, "foo bar juu").assertArgument(0, 7, "foo", "bar").assertArgument(8, 11, "juu").assertDone();
    class E {
      @Argument
      String a;
      @Argument
      List<String> b;
      @Argument
      String c;
    }
    new Test(E.class, "").assertDone();
    new Test(E.class, "foo").assertArgument(0, 3, "foo").assertDone();
    new Test(E.class, "foo bar").assertArgument(0, 3, "foo").assertArgument(4, 7, "bar").assertDone();
    new Test(E.class, "foo bar juu").assertArgument(0, 3, "foo").assertArgument(4, 7, "bar").assertArgument(8, 11, "juu").assertDone();
    new Test(E.class, "foo bar juu daa").assertArgument(0, 3, "foo").assertArgument(4, 11, "bar", "juu").assertArgument(12, 15, "daa").assertDone();
  }

  public void testEmpty() throws Exception {
    class A {
    }
    new Test(A.class, "").assertDone();
    new Test(A.class, "-foo").assertDone("-foo");
    new Test(A.class, "foo").assertDone("foo");
  }

  public void testOptions() throws Exception {

    class A {
      @Option(names = "o")
      String o;
      @Option(names = "p")
      List<String> p;
      @Option(names = "b")
      boolean b;
    }

    //
    new Test(A.class, "-o foo").assertOption("o", "foo").assertDone();
    new Test(A.class, "-p foo -p bar").assertOption("p", "foo", "bar").assertDone();
    new Test(A.class, "-b foo").assertOption("b").assertDone("foo");
    new Test(A.class, "-b").assertOption("b");
    new Test(A.class, "-o foo -p bar -p juu").assertOption("o", "foo").assertOption("p", "bar", "juu").assertDone();
    new Test(A.class, "-o foo -b -p bar -p juu").assertOption("o", "foo").assertOption("b").assertOption("p", "bar", "juu").assertDone();

    // Partial matching
    new Test(A.class, "-p foo").assertOption("p", "foo").assertDone();
  }

  public void testMethod() throws Exception {

    class A {
      @Command
      void m() {
      }
      @Command
      void dummy() {
      }
    }

    //
    new Test(A.class, "m").assertMethod("m").assertDone();
  }

  public void testMixedMethod() throws Exception {

    class A {
      @Option(names = "s")
      String s;
      @Command
      void m(@Option(names = "o") String o, @Argument String a) {
      }
      @Command
      void dummy() {
      }
    }

    //
    new Test(A.class, "-s foo m -o bar juu").assertSharedOption("s", "foo").assertMethod("m").assertOption("o", "bar").assertArgument(16, 19, "juu").assertDone();
  }
}

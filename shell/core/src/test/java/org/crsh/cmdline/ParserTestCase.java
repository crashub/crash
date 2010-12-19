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

package org.crsh.cmdline;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.command.Argument;
import org.crsh.command.Option;
import org.crsh.cmdline.analyzer.ArgumentParser;
import org.crsh.cmdline.analyzer.Match;
import org.crsh.cmdline.analyzer.MatchIterator;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParserTestCase extends TestCase {

  private static class Test {

    /** . */
    private final MatchIterator matcher;

    private <T> Test(Class<T> type, String s) {
      try {
        CommandInfo<T, ?> command = CommandInfo.create(type);
        ArgumentParser<T> parser = new ArgumentParser<T>(command);
        this.matcher = parser.parse(s);
      }
      catch (IntrospectionException e) {
        AssertionFailedError afe = new AssertionFailedError();
        afe.initCause(e);
        throw afe;
      }
    }

    public Test assertOption(String expectedName, String... expectedValues) {
      assertTrue(matcher.hasNext());
      Match.Option match = (Match.Option)matcher.next();
      assertEquals(expectedName, match.getName());
      assertEquals(Arrays.asList(expectedValues), match.getValues());
      return this;
    }

    public Test assertArgument(int start, int end, String... expectedValues) {
      assertTrue(matcher.hasNext());
      Match.Argument match = (Match.Argument)matcher.next();
      assertEquals(start, match.getStart());
      assertEquals(end, match.getEnd());
      assertEquals(Arrays.asList(expectedValues), match.getValues());
      return this;
    }

    public void assertDone(String expectedRest) {
      assertEquals(expectedRest, matcher.getRest());
      assertFalse(matcher.hasNext());
    }

    public void assertDone() {
      assertDone("");
    }
  }

  public void testMixed() throws Exception {
    class A {
      @Option(opt = 'o')
      String o;
      @Option(opt = 'p')
      boolean p;
      @Argument
      String a;
      @Argument
      List<String> b;
    }
    new Test(A.class, "-o foo bar").assertOption("o", "foo").assertArgument(7, 10, "bar").assertArgument(10, 10).assertDone();
    new Test(A.class, "-o foo -p bar").assertOption("o", "foo").assertOption("p").assertArgument(10, 13, "bar").assertArgument(13, 13).assertDone();
  }

  public void testArgument() throws Exception {
    class A {
      @Argument
      String a;
    }
    new Test(A.class, "foo").assertArgument(0, 3, "foo").assertDone();
    new Test(A.class, "foo bar").assertArgument(0, 3, "foo").assertDone(" bar");
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
    new Test(C.class, "foo").assertArgument(0, 3, "foo").assertArgument(3, 3).assertDone();
    new Test(C.class, "foo bar").assertArgument(0, 3, "foo").assertArgument(4, 7, "bar").assertDone();
    new Test(C.class, "foo bar juu").assertArgument(0, 3, "foo").assertArgument(4, 11, "bar", "juu").assertDone();
    class D {
      @Argument
      List<String> a;
      @Argument
      String b;
    }
    new Test(D.class, "").assertArgument(0, 0).assertDone();
    new Test(D.class, "foo").assertArgument(0, 0).assertArgument(0, 3, "foo").assertDone();
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
    new Test(E.class, "foo").assertArgument(0, 3, "foo").assertArgument(3, 3).assertDone();
    new Test(E.class, "foo bar").assertArgument(0, 3, "foo").assertArgument(3, 3).assertArgument(4, 7, "bar").assertDone();
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
      @Option(opt = 'o')
      String o;
      @Option(opt = 'p', arity = 2)
      List<String> p;
      @Option(opt = 'b')
      boolean b;
    }

    //
    new Test(A.class, "-o foo").assertOption("o", "foo").assertDone();
    new Test(A.class, "-p foo bar").assertOption("p", "foo", "bar").assertDone();
    new Test(A.class, "-b foo").assertOption("b").assertDone(" foo");
    new Test(A.class, "-b").assertOption("b");
    new Test(A.class, "-o foo -p bar juu").assertOption("o", "foo").assertOption("p", "bar", "juu").assertDone();
    new Test(A.class, "-o foo -b -p bar juu").assertOption("o", "foo").assertOption("b").assertOption("p", "bar", "juu").assertDone();

    // Partial matching
    new Test(A.class, "-p foo").assertOption("p", "foo", null).assertDone();
  }
}

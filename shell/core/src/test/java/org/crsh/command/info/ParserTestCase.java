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

package org.crsh.command.info;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.command.Option;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParserTestCase extends TestCase {

  public void testFoo() throws Exception {

    class A {
      @Option(opt = 'o')
      String o;
      @Option(opt = 'p', arity = 2)
      List<String> p;
      @Option(opt = 'b')
      boolean b;
    }

    //
    assertParse(A.class, "-o foo", "", new Match("o", Arrays.asList("foo")));
    assertParse(A.class, "-p foo bar", "", new Match("p", Arrays.asList("foo", "bar")));
    assertParse(A.class, "-b foo", " foo", new Match("b", Collections.<String>emptyList()));
    assertParse(A.class, "-b", "", new Match("b", Collections.<String>emptyList()));
    assertParse(A.class, "-o foo -p bar juu", "", new Match("o", Arrays.asList("foo")), new Match("p", Arrays.asList("bar", "juu")));
    assertParse(A.class, "-o foo -b -p bar juu", "", new Match("o", Arrays.asList("foo")), new Match("b", Collections.<String>emptyList()), new Match("p", Arrays.asList("bar", "juu")));
  }

  private <T> void assertParse(Class<T> type, String s, String rest, Match... expectedMatches) {
    CommandInfo<T> info;
    try {
      info = CommandInfo.create(type);
    }
    catch (IntrospectionException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
    ArgumentParser<T> parser = new ArgumentParser<T>(info);
    MatchIterator matcher = parser.parse(s);
    for (int i = 0;i < expectedMatches.length;i++) {
      assertTrue(matcher.hasNext());
      Match match = matcher.next();
      Match expectedMatch = expectedMatches[i];
      assertEquals(expectedMatch.getName(), match.getName());
      assertEquals(expectedMatch.getValues(), match.getValues());
    }
    assertFalse(matcher.hasNext());
    assertEquals(rest, matcher.getRest());
  }
}

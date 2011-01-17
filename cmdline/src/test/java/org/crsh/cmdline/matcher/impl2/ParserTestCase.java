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
import org.crsh.cmdline.annotations.Option;

import java.util.Arrays;

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
      this.parser = new Parser<T>(new Tokenizer(s), command, "main");
    }

    public void assertOption(String name, String... values) {
      Event.Option event = (Event.Option)parser.bilto();
      assertTrue(event.getDescriptor().getNames().contains(name));
      assertEquals(Arrays.asList(values), event.getValues());
    }
  }

  public void testFoo() throws Exception {

    class A {
      @Option(names = "o") String o;
    }

    ClassDescriptor<A> cmd = CommandFactory.create(A.class);
    Tester<A> tester = new Tester<A>(cmd, "-o");
    tester.assertOption("o");
    tester = new Tester<A>(cmd, "-o a");
    tester.assertOption("o", "a");
    tester = new Tester<A>(cmd, "-o a b");
    tester.assertOption("o", "a");
  }

}

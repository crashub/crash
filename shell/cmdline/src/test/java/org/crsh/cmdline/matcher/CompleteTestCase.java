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
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Option;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;

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

}

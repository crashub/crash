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

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RegexpTestCase extends TestCase {

  public void testOption() throws Exception {

    class A {
      @Option(names = "a") String a;
    }

    ClassDescriptor<?> desc = CommandDescriptor.create(A.class);
    String re = Matcher.buildOptions(new StringBuilder(), desc.getOptions()).toString();
    Pattern p = Pattern.compile(re);

    //
    java.util.regex.Matcher m1 = p.matcher("-a f");
    assertTrue(m1.find());
    assertEquals("-a", m1.group(2));
    assertEquals("f", m1.group(3));
    assertFalse(m1.find());

    //
    java.util.regex.Matcher m2 = p.matcher("-a");
    assertTrue(m2.find());
    assertEquals("-a", m2.group(2));
    assertEquals(null, m2.group(3));
    assertFalse(m2.find());

    //
    java.util.regex.Matcher m3 = p.matcher("-a ");
    assertTrue(m3.find());
    assertEquals("-a", m3.group(2));
    assertEquals("", m3.group(3));
    assertFalse(m3.find());

    //
    java.util.regex.Matcher m4 = p.matcher("-a -b");
    assertTrue(m4.find());
    assertEquals("-a", m4.group(2));
    assertEquals(null, m4.group(3));
    assertFalse(m3.find());
  }
}

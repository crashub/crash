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

package org.crsh.util;

import junit.framework.TestCase;

import java.io.StringWriter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LineFeedWriterTestCase extends TestCase {

  final StringWriter buffer;
  final LineFeedWriter writer = new LineFeedWriter(buffer = new StringWriter(), "_");

  private void assertWriter(String expected, String... texts) throws Exception {
    writer.write('\n');
    writer.flush();
    buffer.getBuffer().setLength(0);
    for (String text : texts) {
      writer.write(text.toCharArray(), 0, text.length());
    }
    assertEquals(expected, buffer.toString());

    //
    writer.write('\n');
    writer.flush();
    buffer.getBuffer().setLength(0);
    for (String text : texts) {
      writer.write(text, 0, text.length());
    }
    assertEquals(expected, buffer.toString());
  }

  public void testFoo1() throws Exception {
    assertWriter("", "");
  }

  public void testFoo2() throws Exception {
    assertWriter("a", "a");
  }

  public void testFoo3() throws Exception {
    assertWriter("_", "\n");
    assertWriter("_", "\r\n");
    assertWriter("_", "\r", "\n");
    assertWriter("_", "\r", "\r", "\n");
  }

  public void testFoo4() throws Exception {
    assertWriter("a_", "a\n");
  }

  public void testFoo5() throws Exception {
    assertWriter("_a", "\na");
  }

  public void testFoo6() throws Exception {
    assertWriter("a_b", "a\nb");
  }

  public void testFoo7() throws Exception {
    assertWriter("ab", "a\rb");
  }

  public void testFoo8() throws Exception {
    assertWriter("", "\r");
  }

  public void testPadding1() throws Exception {
    writer.setPadding("-");
    assertWriter("-a", "a");
  }

  public void testPadding2() throws Exception {
    writer.setPadding("-");
    assertWriter("-a_", "a\n");
  }

  public void testPadding3() throws Exception {
    writer.setPadding("-");
    assertWriter("-a_-b", "a\nb");
  }
}

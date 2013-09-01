/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.crsh.term.console;

import junit.framework.TestCase;

import java.io.IOException;

public class TermIOWriterTestCase extends TestCase {

  public void testCRLF() throws IOException {

    for (String test : new String[]{"a\n","a\r","a\r\n"}) {
      SimpleTermIO output = new SimpleTermIO(false);
      TermIOWriter writer = new TermIOWriter(output);
      writer.write(test);
      output.assertChars("a\r\n");
      output.assertEmpty();
    }

    for (String test : new String[]{"a\n\n","a\n\r","a\r\r"}) {
      SimpleTermIO output = new SimpleTermIO(false);
      TermIOWriter writer = new TermIOWriter(output);
      writer.write(test);
      output.assertChars("a\r\n\r\n");
      output.assertEmpty();
    }
  }
}

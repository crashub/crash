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

package org.crsh.text.ui;

public class RowRendererTestCase extends AbstractRendererTestCase {


  public void testRender() throws Exception {
    RowElement row = new RowElement().add(new LabelElement("foo"), new LabelElement("bar"));

    //
    assertRender(row, 8, "foobar");
    assertRender(row, 7, "foobar");
    assertRender(row, 6, "foobar");
    assertRender(row, 5, "fooba", "   r ");
    assertRender(row, 4, "foob", "   a", "   r");
    assertRender(row, 3, "foo");
    assertRender(row, 2, "fo", "o ");
    assertRender(row, 1, "f", "o", "o");
    assertNoRender(row, 0);
  }

  public void testCosmetic() throws Exception {
    RowElement row = new RowElement().add(new LabelElement("foo", 5), new LabelElement("This text is larger to be displayed in a cell of 32", 5));
    assertRender(row, 32,
        "fooThis text is larger to be dis",
        "   played in a cell of 32       ");
  }
}

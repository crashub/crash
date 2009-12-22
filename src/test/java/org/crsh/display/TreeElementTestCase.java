/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.crsh.display;

import junit.framework.TestCase;
import org.crsh.display.structure.LabelElement;
import org.crsh.display.structure.TreeElement;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TreeElementTestCase extends TestCase {

  public void testSimple() {

    TreeElement elt = new TreeElement();
    elt.addNode(new LabelElement("1\n1"));
    elt.addNode(new LabelElement("2\n"));

    SimpleDisplayContext ctx = new SimpleDisplayContext();

    elt.print(ctx);

    System.out.println(ctx.getText());

/*
    assertEquals(
      "+-1\n" +
      "+-2\n"
      , ctx.getText());
*/
  }

  public void testNested() {

    TreeElement elt = new TreeElement(new LabelElement("foo"));
    elt.addNode(new TreeElement().addNode(new LabelElement("1\n1")).addNode(new LabelElement("2\n2")));
    elt.addNode(new TreeElement().addNode(new LabelElement("3")).addNode(new LabelElement("4")));

    SimpleDisplayContext ctx = new SimpleDisplayContext();

    elt.print(ctx);

    System.out.println(ctx.getText());

/*
    assertEquals(
      "+-+-1\n" +
      "| +-2\n" +
      "+-+-3\n" +
      "| +-4\n"
      , ctx.getText());
*/
  }
}

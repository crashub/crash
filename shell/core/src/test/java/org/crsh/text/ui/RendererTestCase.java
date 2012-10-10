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

import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;

import static org.crsh.text.ui.Element.label;
import static org.crsh.text.ui.Element.row;

public class RendererTestCase extends AbstractRendererTestCase {

  public void testInNode() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d")));

    TreeElement node = new TreeElement();
    node.addChild(label("foo"));
    node.addChild(tableElement);
    node.addChild(label("bar"));

    assertRender(node, 32,
        "+-foo                           ",
        "+-ab                            ",
        "| cd                            ",
        "+-bar                           "
        );
  }

  public void testCascading() throws Exception {

    Element custom = new Element() {

      LabelElement foo = new LabelElement("foo");
      LabelElement bar = new LabelElement("bar").style(Style.style(Decoration.bold_off));
      LabelElement juu = new LabelElement("juu");

      @Override
      int getWidth() {
        return 9;
      }

      @Override
      int getMinWidth() {
        return 1;
      }

      @Override
      public Renderer renderer(int width) {
        return new Renderer() {

          boolean done = false;

          public boolean hasLine() {
            return !done;
          }

          public void renderLine(RendererAppendable to) throws IllegalStateException {
            if (done) {
              throw new IllegalStateException();
            }
            foo.renderer(3).renderLine(to);
            bar.renderer(3).renderLine(to);
            juu.renderer(3).renderLine(to);
            done = true;
          }
        };
      }
    };

    TableElement table = new TableElement().style(Style.style(Decoration.bold)).add(
        row().style(Color.red.fg()).add(custom));
    assertRender(table, 32, "\033[1;31mfoo\033[22mbar\033[1mjuu\033[39m                       ");
  }

  public void testStyleOff() {
    TableElement table = new TableElement().
        border(Border.dashed).
        style(Style.style(Decoration.bold)).
        add(
            row().style(Style.style(Decoration.underline)).add(label("foo"), label("bar")));

    assertRender(table, 32,
        " -------                        ",
        "\033[0m|\033[1;4mfoo\033[0m|\033[1;4mbar\033[0m|                       ",
        " -------                        ");
  }

}

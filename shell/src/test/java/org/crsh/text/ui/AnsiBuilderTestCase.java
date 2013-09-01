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

import junit.framework.TestCase;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;

public class AnsiBuilderTestCase extends TestCase {

  public void testReset() throws Exception {
    assertEquals("\u001B[0m", Style.reset.toAnsiSequence());
  }

  public void testDecoration() throws Exception {
    assertEquals("\u001B[5m", Style.style(Decoration.blink, null, null).toAnsiSequence());
    assertEquals("\u001B[25m", Style.style(Decoration.blink_off, null, null).toAnsiSequence());
    assertEquals("\u001B[1m", Style.style(Decoration.bold, null, null).toAnsiSequence());
    assertEquals("\u001B[22m", Style.style(Decoration.bold_off, null, null).toAnsiSequence());
    assertEquals("\u001B[4m", Style.style(Decoration.underline, null, null).toAnsiSequence());
    assertEquals("\u001B[24m", Style.style(Decoration.underline_off, null, null).toAnsiSequence());
  }

  public void testForeground() throws Exception {
    assertEquals("\u001B[30m", Style.style(null, Color.black, null).toAnsiSequence());
    assertEquals("\u001B[34m", Style.style(null, Color.blue, null).toAnsiSequence());
    assertEquals("\u001B[36m", Style.style(null, Color.cyan, null).toAnsiSequence());
    assertEquals("\u001B[32m", Style.style(null, Color.green, null).toAnsiSequence());
    assertEquals("\u001B[35m", Style.style(null, Color.magenta, null).toAnsiSequence());
    assertEquals("\u001B[31m", Style.style(null, Color.red, null).toAnsiSequence());
    assertEquals("\u001B[33m", Style.style(null, Color.yellow, null).toAnsiSequence());
    assertEquals("\u001B[37m", Style.style(null, Color.white, null).toAnsiSequence());
  }

  public void testBackground() throws Exception {
    assertEquals("\u001B[40m", Style.style(null, null, Color.black).toAnsiSequence());
    assertEquals("\u001B[44m", Style.style(null, null, Color.blue).toAnsiSequence());
    assertEquals("\u001B[46m", Style.style(null, null, Color.cyan).toAnsiSequence());
    assertEquals("\u001B[42m", Style.style(null, null, Color.green).toAnsiSequence());
    assertEquals("\u001B[45m", Style.style(null, null, Color.magenta).toAnsiSequence());
    assertEquals("\u001B[41m", Style.style(null, null, Color.red).toAnsiSequence());
    assertEquals("\u001B[43m", Style.style(null, null, Color.yellow).toAnsiSequence());
    assertEquals("\u001B[47m", Style.style(null, null, Color.white).toAnsiSequence());
  }

  public void testMany() throws Exception {
    assertEquals("\u001B[34;40m", Style.style(null, Color.blue, Color.black).toAnsiSequence());
    assertEquals("\u001B[4;40m", Style.style(Decoration.underline, null, Color.black).toAnsiSequence());
    assertEquals("\u001B[4;34m", Style.style(Decoration.underline, Color.blue, null).toAnsiSequence());
    assertEquals("\u001B[4;34;40m", Style.style(Decoration.underline, Color.blue, Color.black).toAnsiSequence());
  }

  public void testFluent() throws Exception {
    Style.Composite style = Color.red.bg().bold().underline().fg(Color.blue);
    assertEquals("\u001B[1;4;34;41m", style.toAnsiSequence());
    style = style.bold(false);
    assertEquals("\u001B[22;4;34;41m", style.toAnsiSequence());
    style = style.bold(null);
    assertEquals("\u001B[4;34;41m", style.toAnsiSequence());
  }
}

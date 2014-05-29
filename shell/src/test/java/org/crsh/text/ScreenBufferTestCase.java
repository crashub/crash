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

package org.crsh.text;

import org.crsh.AbstractTestCase;
import org.crsh.util.Utils;

import java.io.IOException;
import java.util.List;

public class ScreenBufferTestCase extends AbstractTestCase {

  /** . */
  private static final Style RED = Style.style(null, null, Color.red);

  /** . */
  private static final Style BLUE = Style.style(null, null, Color.blue);

  /** . */
  private static final Style UNDERLINE = Style.style(Decoration.underline, null, null);

  /** . */
  private static final Style RED_UNDERLINE = Style.style(Decoration.underline, null, Color.red);

  public void testSimple() {
    assertReader(new ScreenBuffer().append("a"), "a");
    assertReader(new ScreenBuffer().append(RED, "a"), RED, "a");
  }

  public void testMergeColor() {
    assertReader(new ScreenBuffer().append(RED, "a", RED, "b"), RED, "a", "b");
  }

  public void testOverwriteColor() {
    assertReader(new ScreenBuffer().append(BLUE, RED, "a"), RED, "a");
  }

  public void testOverwriteMergeColor() {
    assertReader(new ScreenBuffer().append(RED, "a", BLUE, RED, "b"), RED, "a", "b");
  }

  public void testLastColor() {
    assertReader(new ScreenBuffer().append(RED, "a", BLUE), RED, "a");
  }

  public void testBlendStyle() {
    assertReader(new ScreenBuffer().append(RED, UNDERLINE, "a"), RED_UNDERLINE, "a");
  }

  public void testConcatenation() {
    assertReader(new ScreenBuffer().append(RED).append(new ScreenBuffer().append("a")), RED, "a");
    assertReader(new ScreenBuffer().append(new ScreenBuffer().append(RED)).append("a"), RED, "a");
  }

  public void testBlend() throws IOException {
    assertReader(new ScreenBuffer().append(Color.red.fg(),"foo", Color.red.fg(), "bar"), Color.red.fg(), "foo", "bar");
  }

  private void assertReader(ScreenBuffer reader, Object... expected) {
    List<Object> res = Utils.list(reader);
    assertEquals(expected.length, res.size());
    for (int i = 0;i < expected.length;i++) {
      assertEquals(expected[i], res.get(i));
    }
  }
}

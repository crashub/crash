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

public class ChunkBufferTestCase extends AbstractTestCase {

  /** . */
  private static final Style RED = Style.style(null, null, Color.red);

  /** . */
  private static final Style BLUE = Style.style(null, null, Color.blue);

  /** . */
  private static final Style UNDERLINE = Style.style(Decoration.underline, null, null);

  /** . */
  private static final Style RED_UNDERLINE = Style.style(Decoration.underline, null, Color.red);

  public void testSimple() {
    assertReader(new ChunkBuffer().append("a"), Text.create("a"));
    assertReader(new ChunkBuffer().append(RED, "a"), RED, Text.create("a"));
  }

  public void testMergeCharSequence() {
    assertReader(new ChunkBuffer().append("a").append("b"), Text.create("ab"));
  }

  public void testMergeColor() {
    assertReader(new ChunkBuffer().append(RED, "a", RED, "b"), RED, Text.create("ab"));
  }

  public void testOverwriteColor() {
    assertReader(new ChunkBuffer().append(BLUE, RED, "a"), RED, Text.create("a"));
  }

  public void testOverwriteMergeColor() {
    assertReader(new ChunkBuffer().append(RED, "a", BLUE, RED, "b"), RED, Text.create("ab"));
  }

  public void testLastColor() {
    assertReader(new ChunkBuffer().append(RED, "a", BLUE), RED, Text.create("a"));
  }

  public void testBlendStyle() {
    assertReader(new ChunkBuffer().append(RED, UNDERLINE, "a"), RED_UNDERLINE, Text.create("a"));
  }

  public void testConcatenation() {
    assertReader(new ChunkBuffer().append(RED).append(new ChunkBuffer().append("a")), RED, Text.create("a"));
    assertReader(new ChunkBuffer().append(new ChunkBuffer().append(RED)).append("a"), RED, Text.create("a"));
  }

  public void testBlend() throws IOException {
    assertReader(new ChunkBuffer().append(Color.red.fg(),"foo", Color.red.fg(), "bar"), Color.red.fg(), Text.create("foobar"));
  }

  private void assertReader(ChunkBuffer reader, Chunk... expected) {
    List<Chunk> res = Utils.list(reader);
    assertEquals(expected.length, res.size());
    for (int i = 0;i < expected.length;i++) {
      assertEquals(expected[i], res.get(i));
    }
  }
}

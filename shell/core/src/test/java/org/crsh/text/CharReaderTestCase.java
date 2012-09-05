package org.crsh.text;

import org.crsh.AbstractTestCase;
import org.crsh.util.Utils;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CharReaderTestCase extends AbstractTestCase {

  /** . */
  private static final Style RED = Style.style(null, null, Color.red);

  /** . */
  private static final Style BLUE = Style.style(null, null, Color.blue);

  /** . */
  private static final Style UNDERLINE = Style.style(Decoration.underline, null, null);

  /** . */
  private static final Style RED_UNDERLINE = Style.style(Decoration.underline, null, Color.red);

  public void testSimple() {
    assertReader(new ChunkSequence().append("a"), new TextChunk("a"));
    assertReader(new ChunkSequence().append(RED, "a"), RED, new TextChunk("a"));
  }

  public void testMergeCharSequence() {
    assertReader(new ChunkSequence().append("a").append("b"), new TextChunk("ab"));
  }

  public void testMergeColor() {
    assertReader(new ChunkSequence().append(RED, "a", RED, "b"), RED, new TextChunk("ab"));
  }

  public void testOverwriteColor() {
    assertReader(new ChunkSequence().append(BLUE, RED, "a"), RED, new TextChunk("a"));
  }

  public void testOverwriteMergeColor() {
    assertReader(new ChunkSequence().append(RED, "a", BLUE, RED, "b"), RED, new TextChunk("ab"));
  }

  public void testLastColor() {
    assertReader(new ChunkSequence().append(RED, "a", BLUE), RED, new TextChunk("a"));
  }

  public void testBlendStyle() {
    assertReader(new ChunkSequence().append(RED, UNDERLINE, "a"), RED_UNDERLINE, new TextChunk("a"));
  }

  public void testConcatenation() {
    assertReader(new ChunkSequence().append(RED).append(new ChunkSequence().append("a")), RED, new TextChunk("a"));
    assertReader(new ChunkSequence().append(new ChunkSequence().append(RED)).append("a"), RED, new TextChunk("a"));
  }

  private void assertReader(ChunkSequence reader, Chunk... expected) {
    List<Chunk> res = Utils.list(reader);
    assertEquals(expected.length, res.size());
    for (int i = 0;i < expected.length;i++) {
      assertEquals(expected[i], res.get(i));
    }
  }
}

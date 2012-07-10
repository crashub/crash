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
    assertReader(new CharReader().append("a"), "a");
    assertReader(new CharReader().append(RED, "a"), RED, "a");
  }

  public void testMergeCharSequence() {
    assertReader(new CharReader().append("a").append("b"), "ab");
  }

  public void testMergeColor() {
    assertReader(new CharReader().append(RED, "a", RED, "b"), RED, "ab");
  }

  public void testOverwriteColor() {
    assertReader(new CharReader().append(BLUE, RED, "a"), RED, "a");
  }

  public void testOverwriteMergeColor() {
    assertReader(new CharReader().append(RED, "a", BLUE, RED, "b"), RED, "ab");
  }

  public void testLastColor() {
    assertReader(new CharReader().append(RED, "a", BLUE), RED, "a", BLUE);
  }

  public void testBlendStyle() {
    assertReader(new CharReader().append(RED, UNDERLINE, "a"), RED_UNDERLINE, "a");
  }

  public void testConcatenation() {
    assertReader(new CharReader().append(RED).append(new CharReader().append("a")), RED, "a");
    assertReader(new CharReader().append(new CharReader().append(RED)).append("a"), RED, "a");
  }

  private void assertReader(CharReader reader, Object... expected) {
    List<?> res = Utils.list(reader);
    assertEquals(expected.length, res.size());
    for (int i = 0;i < expected.length;i++) {
      assertEquals(expected[i].toString(), res.get(i).toString());
    }
  }
}

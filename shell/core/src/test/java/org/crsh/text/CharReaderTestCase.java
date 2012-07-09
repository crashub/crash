package org.crsh.text;

import org.crsh.AbstractTestCase;
import org.crsh.util.Utils;

import java.io.IOException;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CharReaderTestCase extends AbstractTestCase {

  /** . */
  private static final Style RED = Style.create(null, Color.red, null);

  /** . */
  private static final Style BLUE = Style.create(null, Color.blue, null);

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

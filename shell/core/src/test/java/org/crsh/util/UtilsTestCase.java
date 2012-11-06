package org.crsh.util;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class UtilsTestCase extends TestCase {
  
  @Test
  public void testRegex() throws Exception {
    assertEquals("^foo$", Utils.applyRegex("foo"));
    assertEquals("foo$", Utils.applyRegex("*foo"));
    assertEquals("^foo", Utils.applyRegex("foo*"));
    assertEquals("^f.*o$", Utils.applyRegex("f*o"));
    assertEquals("f.*o", Utils.applyRegex("*f*o*"));
  }
}

package org.crsh.util;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SimpleMapTestCase extends TestCase {

  static class TestMap extends SimpleMap<String, String> {

    /** . */
    final HashMap<String, String> state = new HashMap<String, String>();

    @Override
    protected Iterator<String> keys() {
      return state.keySet().iterator();
    }

    @Override
    public String get(Object key) {
      return state.get(key);
    }
  }

  public void testEmpty() {
    TestMap map = new TestMap();
    assertEquals(0, map.size());
    assertEquals(null, map.get("a"));
    assertEquals(Collections.<String>emptyList(), Utils.list(map.keys()));
  }

  public void testSingle() {
    TestMap map = new TestMap();
    map.state.put("a", "b");
    assertEquals(1, map.size());
    assertEquals("b", map.get("a"));
    assertEquals(Utils.list("a"), Utils.list(map.keys()));
  }
}

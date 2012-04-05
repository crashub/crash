package org.crsh.cmdline.completers;

import junit.framework.TestCase;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FileCompleterTestCase extends TestCase {

  /** . */
  private File base;

  @Override
  protected void setUp() throws Exception {
    base = File.createTempFile("crash", "");
    assertTrue(base.delete());
    assertTrue(base.mkdir());
    base.deleteOnExit();
  }

  public void testFile() throws Exception {
    File foo = new File(base, "foo");
    assertTrue(foo.createNewFile());
    FileCompleter completer = new FileCompleter();

    //
    Map<String, Boolean> completions = completer.complete (null, base.getCanonicalPath() + "/");
    assertEquals(Collections.singletonMap("foo", true), completions);
    completions = completer.complete(null, foo.getCanonicalPath());
    assertEquals(Collections.singletonMap("", true), completions);
    completions = completer.complete (null, base.getCanonicalPath() + "/f");
    assertEquals(Collections.singletonMap("oo", true), completions);
  }

  public void testDir() throws Exception {
    FileCompleter completer = new FileCompleter();

    //
    Map<String, Boolean> completions = completer.complete(null, base.getCanonicalPath());
    assertEquals(Collections.singletonMap("/", true), completions);
    completions = completer.complete (null, base.getCanonicalPath() + "/");
    assertEquals(Collections.singletonMap("", true), completions);

    //
    File foo = new File(base, "foo");
    assertTrue(foo.mkdir());
    completions = completer.complete(null, base.getCanonicalPath());
    assertEquals(Collections.singletonMap("/", false), completions);
    completions = completer.complete (null, base.getCanonicalPath() + "/");
    assertEquals(Collections.singletonMap("foo/", true), completions);
    completions = completer.complete (null, base.getCanonicalPath() + "/f");
    assertEquals(Collections.singletonMap("oo/", true), completions);

    //
    File bar = new File(foo, "bar");
    assertTrue(bar.mkdir());
    completions = completer.complete(null, base.getCanonicalPath());
    assertEquals(Collections.singletonMap("/", false), completions);
    completions = completer.complete (null, base.getCanonicalPath() + "/");
    assertEquals(Collections.singletonMap("foo/", false), completions);
    completions = completer.complete (null, base.getCanonicalPath() + "/f");
    assertEquals(Collections.singletonMap("oo/", false), completions);
  }
}

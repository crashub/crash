package org.crsh.cli.completers;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.cli.spi.Completion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PathCompleterTestCase extends TestCase {

  final String sep = File.separator;
  static class NodeCompleter extends AbstractPathCompleter<File> {

    /** . */
    private File current;

    /** . */ 
    private final File root;

    NodeCompleter(File root) {
      this.root = root;
      this.current = root;
    }

    public File getCurrent() {
      return current;
    }

    public void setCurrent(File current) throws IOException {
      if (current == null) {
        throw new AssertionFailedError("Current file should be null");
      }
      if (!current.getCanonicalPath().startsWith(root.getCanonicalPath())) {
        throw new AssertionFailedError("Current file should be a descendant of the root file");
      }
      this.current = current;
    }

    @Override
    protected String getCurrentPath() throws Exception {
      if (current.equals(root)) {
        return File.separator;
      } else {
        return current.getCanonicalPath().substring(root.getCanonicalPath().length());
      }
    }

    @Override
    protected File getPath(String path) throws Exception {
      if (!path.startsWith(File.separator)) {
        throw new AssertionFailedError("Path " + path + " does not start with separator");
      }
      return new File(root, path.substring(1));
    }

    @Override
    protected boolean exists(File path) throws Exception {
      return path.exists();
    }

    @Override
    protected boolean isDirectory(File path) throws Exception {
      return path.isDirectory();
    }

    @Override
    protected boolean isFile(File path) throws Exception {
      return path.isFile();
    }

    @Override
    protected Collection<File> getChilren(File path) throws Exception {
      File[] files = path.listFiles();
      return files != null ? Arrays.asList(files) : null;
    }

    @Override
    protected String getName(File path) throws Exception {
      return path.getName();
    }
  }

  private File root;

  private NodeCompleter completer;

  @Override
  public void setUp() throws Exception {
    File tmp = File.createTempFile("crash", "");
    assertTrue(tmp.delete());
    assertTrue(tmp.mkdirs());

    //
    this.root = tmp;
    this.completer = new NodeCompleter(root);
  }

  public void testAbsoluteFile() throws Exception {
    File foo = new File(root, "foo");
    assertTrue(foo.createNewFile());

    //
    assertCompletion(sep, Completion.create("foo", true));
    assertCompletion(sep + "f", Completion.create("f", "oo", true));
    assertCompletion(sep + "foo", Completion.create("", true));

    //
    assertTrue(foo.delete());
    assertTrue(foo.mkdirs());
    File bar = new File(foo, "bar");
    assertTrue(bar.createNewFile());

    //
    assertCompletion(sep, Completion.create("foo" + sep, false));
    assertCompletion(sep+"f", Completion.create("f", "oo" + sep, false));
    assertCompletion(sep + "foo", Completion.create(sep, false));
    assertCompletion(sep + "foo" + sep, Completion.create("bar", true));
    assertCompletion(sep + "foo" + sep + "b", Completion.create("b", "ar", true));
    assertCompletion(sep + "foo" + sep + "bar", Completion.create("", true));
  }

  public void testAbsoluteDir() throws Exception {
    assertCompletion(sep, Completion.create());
    assertCompletion(sep + "f", Completion.create("f"));

    //
    File foo = new File(root, "foo");
    assertTrue(foo.mkdir());
    assertCompletion(sep, Completion.create("foo" + sep, false));
    assertCompletion(sep + "f", Completion.create("f", "oo" + sep, false));
    assertCompletion(sep + "foo", Completion.create(sep, false));
    assertCompletion(sep + "foo" + sep, Completion.create());

    //
    File bar = new File(foo, "bar");
    assertTrue(bar.mkdir());
    assertCompletion(sep, Completion.create("foo" + sep, false));
    assertCompletion(sep + "f", Completion.create("f", "oo" + sep, false));
    assertCompletion(sep + "foo", Completion.create(sep, false));
    assertCompletion(sep + "foo" + sep, Completion.create("bar" + sep, false));
    assertCompletion(sep + "foo" + sep +"b", Completion.create("b", "ar" + sep, false));
    assertCompletion(sep + "foo" + sep +"bar", Completion.create(sep, false));
    assertCompletion(sep + "foo" + sep +"bar" + sep, Completion.create());

    //
    File juu = new File(bar, "juu");
    assertTrue(juu.mkdir());
    assertCompletion(sep, Completion.create("foo" + sep, false));
    assertCompletion(sep + "f", Completion.create("f", "oo" + sep, false));
    assertCompletion(sep + "foo", Completion.create(sep, false));
    assertCompletion(sep + "foo" + sep, Completion.create("bar" + sep, false));
    assertCompletion(sep + "foo" + sep +"b", Completion.create("b", "ar" + sep, false));
    assertCompletion(sep + "foo" + sep +"bar", Completion.create(sep, false));
    assertCompletion(sep + "foo" + sep +"bar" + sep, Completion.create("juu" + sep, false));
    assertCompletion(sep + "foo" + sep +"bar" + sep +"j", Completion.create("j", "uu" + sep, false));
    assertCompletion(sep + "foo" + sep +"bar" + sep +"juu", Completion.create(sep, false));
    assertCompletion(sep + "foo" + sep +"bar" + sep +"juu" + sep, Completion.create());
  }

  public void testRootRelativeDir() throws Exception {
    assertCompletion("", Completion.create());
    assertCompletion("f", Completion.create("f"));

    //
    File foo = new File(root, "foo");
    assertTrue(foo.mkdir());
    assertCompletion("", Completion.create("foo" + sep, false));
    assertCompletion("f", Completion.create("f", "oo" + sep, false));
    assertCompletion("foo", Completion.create(sep, false));
    assertCompletion("foo" + sep, Completion.create());

    //
    File bar = new File(foo, "bar");
    assertTrue(bar.mkdir());
    assertCompletion("", Completion.create("foo" + sep, false));
    assertCompletion("f", Completion.create("f", "oo" + sep, false));
    assertCompletion("foo", Completion.create(sep, false));
    assertCompletion("foo" + sep, Completion.create("bar" + sep, false));
    assertCompletion("foo" + sep +"b", Completion.create("b", "ar" + sep, false));
    assertCompletion("foo" + sep +"bar", Completion.create(sep, false));
    assertCompletion("foo" + sep +"bar" + sep, Completion.create());
  }

  public void testSubRelativeDir() throws Exception {
    File sub = new File(root, "sub");
    assertTrue(sub.mkdir());
    completer.setCurrent(sub);

    //
    assertCompletion("", Completion.create());
    assertCompletion("f", Completion.create("f"));

    //
    File foo = new File(sub, "foo");
    assertTrue(foo.mkdir());
    assertCompletion("", Completion.create("foo" + sep, false));
    assertCompletion("f", Completion.create("f", "oo" + sep, false));
    assertCompletion("foo", Completion.create(sep, false));
    assertCompletion("foo" + sep, Completion.create());

    //
    File bar = new File(foo, "bar");
    assertTrue(bar.mkdir());
    assertCompletion("", Completion.create("foo" + sep, false));
    assertCompletion("f", Completion.create("f", "oo" + sep, false));
    assertCompletion("foo", Completion.create(sep, false));
    assertCompletion("foo" + sep, Completion.create("bar" + sep, false));
    assertCompletion("foo" + sep +"b", Completion.create("b", "ar" + sep, false));
    assertCompletion("foo" + sep +"bar", Completion.create(sep, false));
    assertCompletion("foo" + sep +"bar" + sep, Completion.create());
  }

  private void assertCompletion(String path, Completion expected) throws Exception {
    Completion completions = completer.complete (null, path);
    assertEquals(expected, completions);
  }
}

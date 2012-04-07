package org.crsh.cmdline.completers;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * A completer for the current file system.
 * <ul>
 *   <li>When the prefix is absolute (it starts with <code>/</code> char) completion will be done from the prefix</li>
 *   <li>When the prefix is relative (it does not start with a <code>/</code> char, the completion is done from the
 *   directory evaluated with the expression <code>new java.io.File(".").getCanonicalPath()</code></li>
 * </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class FileCompleter extends AbstractPathCompleter<File> {

  @Override
  protected String getCurrentPath() throws Exception {
    return new File(".").getCanonicalPath();
  }

  @Override
  protected File getPath(String path) {
    return new File(path);
  }

  @Override
  protected boolean exists(File path) {
    return path.exists();
  }

  @Override
  protected boolean isDirectory(File path) {
    return path.isDirectory();
  }

  @Override
  protected boolean isFile(File path) {
    return path.isFile();
  }

  @Override
  protected Collection<File> getChilren(File path) {
    File[] files = path.listFiles();
    return files != null ? Arrays.asList(files) : null;
  }

  @Override
  protected String getName(File path) {
    return path.getName();
  }
}

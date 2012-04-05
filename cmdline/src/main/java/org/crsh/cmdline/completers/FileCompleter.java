package org.crsh.cmdline.completers;

import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
public class FileCompleter implements Completer {

  public Map<String, Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {

    // Handle empty dir
    if (!prefix.startsWith("/")) {
      prefix = new File(".").getCanonicalPath() + "/" + prefix;
    }

    //
    File f = new File(prefix);

    //
    if (f.exists()) {
      if (f.isDirectory()) {
        if (prefix.endsWith("/")) {
          File[] children = f.listFiles();
          if (children != null) {
            if (children.length > 0) {
              return listDir(f, "");
            } else {
              return Collections.singletonMap("", true);
            }
          } else {
            return Collections.emptyMap();
          }
        } else {
          File[] children = f.listFiles();
          if (children == null) {
            return Collections.emptyMap();
          } else {
            return Collections.singletonMap("/", children.length == 0);
          }
        }
      } else if (f.isFile()) {
        return Collections.singletonMap("", true);
      }
      return Collections.emptyMap();
    } else {
      int pos = prefix.lastIndexOf('/');
      if (pos != -1) {
        String filter;
        if (pos == 0) {
          f = new File("/");
          filter = prefix.substring(1);
        } else {
          f = new File(prefix.substring(0, pos));
          filter = prefix.substring(pos + 1);
        }
        if (f.exists()) {
          if (f.isDirectory()) {
            return listDir(f, filter);
          } else {
            return Collections.emptyMap();
          }
        } else {
          return Collections.emptyMap();
        }
      } else {
        return Collections.emptyMap();
      }
    }
  }

  private Map<String, Boolean> listDir(File dir, final String filter) {
    File[] children = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith(filter);
      }
    });
    if (children != null) {
      LinkedHashMap<String, Boolean> map = new LinkedHashMap<String, java.lang.Boolean>();
      for (File child : children) {
        String name = child.getName().substring(filter.length());
        if (child.isDirectory()) {
          File[] grandChildren = child.listFiles();
          if (grandChildren != null) {
            map.put(name + "/", grandChildren.length == 0);
          } else {
            // Skip it
          }
        } else {
          map.put(name, true);
        }
      }
      return map;
    } else {
      return Collections.emptyMap();
    }
  }
}

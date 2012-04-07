package org.crsh.cmdline.completers;

import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;
import org.crsh.cmdline.spi.CompletionResult;

import java.util.Collection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractPathCompleter<P> implements Completer {

  protected abstract String getCurrentPath() throws Exception;

  protected abstract P getPath(String path) throws Exception;

  protected abstract boolean exists(P path) throws Exception;

  protected abstract boolean isDirectory(P path) throws Exception;

  protected abstract boolean isFile(P path) throws Exception;

  protected abstract Collection<P> getChilren(P path) throws Exception;

  protected abstract String getName(P path) throws Exception;

  public final CompletionResult<Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {

    // Handle empty dir
    if (!prefix.startsWith("/")) {
      String currentPath = getCurrentPath();
      if (!currentPath.endsWith("/")) {
        currentPath += "/";
      }
      if (prefix.length() > 0) {
        prefix = currentPath + prefix;
      } else {
        prefix = currentPath;
      }
    }

    //
    P f = getPath(prefix);

    //
    if (exists(f)) {
      if (isDirectory(f)) {
        if (prefix.endsWith("/")) {
          Collection<P> children = getChilren(f);
          if (children != null) {
            if (children.size() > 0) {
              return listDir(f, "");
            } else {
              return CompletionResult.create();
            }
          } else {
            return CompletionResult.create();
          }
        } else {
          Collection<P> children = getChilren(f);
          if (children == null) {
            return CompletionResult.create();
          } else {
            return CompletionResult.create("/", false);
          }
        }
      } else if (isFile(f)) {
        return CompletionResult.create("", true);
      }
      return CompletionResult.create();
    } else {
      int pos = prefix.lastIndexOf('/');
      if (pos != -1) {
        String filter;
        if (pos == 0) {
          f = getPath("/");
          filter = prefix.substring(1);
        } else {
          f = getPath(prefix.substring(0, pos));
          filter = prefix.substring(pos + 1);
        }
        if (exists(f)) {
          if (isDirectory(f)) {
            return listDir(f, filter);
          } else {
            return CompletionResult.create();
          }
        } else {
          return CompletionResult.create();
        }
      } else {
        return CompletionResult.create();
      }
    }
  }

  private CompletionResult<Boolean> listDir(P dir, final String filter) throws Exception {
    Collection<P> children = getChilren(dir);
    if (children != null) {
      CompletionResult<Boolean> map = new CompletionResult<Boolean>(filter);
      for (P child : children) {
        String name = getName(child);
        if (name.startsWith(filter)) {
          String suffix = name.substring(filter.length());
          if (isDirectory(child)) {
            Collection<P> grandChildren = getChilren(child);
            if (grandChildren != null) {
              map.put(suffix + "/", false);
            } else {
              // Skip it
            }
          } else {
            map.put(suffix, true);
          }
        }
      }
      return map;
    } else {
      return CompletionResult.create();
    }
  }
}

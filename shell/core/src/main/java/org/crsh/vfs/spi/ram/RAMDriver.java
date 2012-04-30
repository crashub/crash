package org.crsh.vfs.spi.ram;

import org.crsh.vfs.Path;
import org.crsh.vfs.spi.AbstractFSDriver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMDriver extends AbstractFSDriver<Path> {

  /** . */
  private final Path root;

  /** . */
  final HashMap<Path, String> entries;

  /** . */
  URL baseURL;

  public RAMDriver() {
    try {
      this.root = Path.get("/");
      this.entries = new HashMap<Path, String>();
      this.baseURL = new URL("ram", null, 0, "/", new RAMURLStreamHandler(this));
    }
    catch (MalformedURLException e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public void add(String path, String file) {
    add(Path.get(path), file);
  }

  public void add(Path path, String file) {
    entries.put(path, file);
  }

  public Path root() throws IOException {
    return root;
  }

  public String name(Path handle) throws IOException {
    return handle.getName();
  }

  public boolean isDir(Path handle) throws IOException {
    return handle.isDir();
  }

  public Iterable<Path> children(Path handle) throws IOException {
    List<Path> children = Collections.emptyList();
    for (Path entry : entries.keySet()) {
      if (entry.isChildOf(handle)) {
        if (children.isEmpty()) {
          children = new ArrayList<Path>();
        }
        children.add(entry);
      }
    }
    return children;
  }

  public long getLastModified(Path handle) throws IOException {
    return 0;
  }

  public InputStream open(Path handle) throws IOException {
    return new ByteArrayInputStream(entries.get(handle).getBytes("UTF-8"));
  }
}

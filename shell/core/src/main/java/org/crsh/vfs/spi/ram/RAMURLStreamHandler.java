package org.crsh.vfs.spi.ram;

import org.crsh.vfs.Path;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMURLStreamHandler extends URLStreamHandler {

  /** . */
  private final RAMDriver driver;

  public RAMURLStreamHandler(RAMDriver driver) {
    this.driver = driver;
  }

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    Path path = Path.get(u.getFile());
    if (path.isDir()) {
      throw new IOException("Cannot open dir");
    }
    String file = driver.entries.get(path);
    if (file == null) {
      throw new IOException("Cannot open non existing dir " + path);
    }
    return new RAMURLConnection(u, file);
  }
}

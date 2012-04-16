package org.crsh.vfs.spi.ram;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RAMURLConnection extends URLConnection {

  /** . */
  private final String file;

  public RAMURLConnection(URL url, String file) {
    super(url);

    //
    this.file = file;
  }

  @Override
  public void connect() throws IOException {
  }

  @Override
  public InputStream getInputStream() throws IOException{
    return new ByteArrayInputStream(file.getBytes());
  }
}

/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class ZipIterator implements Closeable {

  public static ZipIterator create(URL url) throws IOException, URISyntaxException {
    if (url.getProtocol().equals("file")) {
      return create(Utils.toFile(url));
    } else if (url.getProtocol().equals("jar")) {
      int pos = url.getPath().lastIndexOf("!/");
      URL jarURL = new URL(url.getPath().substring(0, pos));
      String path = url.getPath().substring(pos + 2);
      final ZipIterator container = create(jarURL);
      ZipIterator zip = null;
      try {
        while (container.hasNext()) {
          ZipEntry entry = container.next();
          if (entry.getName().equals(path)) {
            InputStreamFactory resolved = container.getStreamFactory();
            final InputStream nested = resolved.open();
            InputStream filter = new InputStream() {
              @Override
              public int read() throws IOException {
                return nested.read();
              }
              @Override
              public int read(byte[] b) throws IOException {
                return nested.read(b);
              }
              @Override
              public int read(byte[] b, int off, int len) throws IOException {
                return nested.read(b, off, len);
              }
              @Override
              public long skip(long n) throws IOException {
                return nested.skip(n);
              }
              @Override
              public int available() throws IOException {
                return nested.available();
              }
              @Override
              public void close() throws IOException {
                Utils.close(nested);
                Utils.close(container);
              }
              @Override
              public synchronized void mark(int readlimit) {
                nested.mark(readlimit);
              }
              @Override
              public synchronized void reset() throws IOException {
                nested.reset();
              }
              @Override
              public boolean markSupported() {
                return nested.markSupported();
              }
            };
            zip = create(filter);
            break;
          }
        }
        if (zip != null) {
          return zip;
        } else {
          throw new IOException("Cannot resolve " + url);
        }
      }
      finally {
        // We close the container if we return nothing
        // otherwise it will be the responsibility of the caller to close the zip
        // with the wrapper that will close both the container and the nested zip
        if (zip != null) {
          Utils.close(container);
        }
      }
    } else {
      return create(url.openStream());
    }
  }

  static ZipIterator create(File file) throws IOException {
    // The fast way (but that requires a File object)
    final ZipFile jarFile = new ZipFile(file);
    final Enumeration<? extends ZipEntry> en = jarFile.entries();en.hasMoreElements();
    return new ZipIterator() {
      ZipEntry next;
      @Override
      public boolean hasNext() throws IOException {
        return en.hasMoreElements();
      }
      @Override
      public ZipEntry next() throws IOException {
        return next = en.nextElement();
      }
      public void close() throws IOException {
      }
      @Override
      public InputStreamFactory getStreamFactory() throws IOException {
        final ZipEntry capture = next;
        return new InputStreamFactory() {
          public InputStream open() throws IOException {
            return jarFile.getInputStream(capture);
          }
        };
      }
    };
  }

  static ZipIterator create(InputStream in) throws IOException {
    final byte[] tmp = new byte[512];
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ZipInputStream zip = new ZipInputStream(in);
    return new ZipIterator() {
      ZipEntry next;
      public boolean hasNext() throws IOException {
        if (next == null) {
          next = zip.getNextEntry();
        }
        return next != null;
      }
      public ZipEntry next() throws IOException {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        ZipEntry tmp = next;
        next = null;
        return tmp;
      }
      @Override
      public InputStreamFactory getStreamFactory() throws IOException {
        while (true) {
          int len = zip.read(tmp, 0, tmp.length);
          if (len == -1) {
            break;
          } else {
            baos.write(tmp, 0, len);
          }
        }
        final byte[] buffer = baos.toByteArray();
        baos.reset();
        return new InputStreamFactory() {
          public InputStream open() throws IOException {
            return new ByteArrayInputStream(buffer);
          }
        };
      }
      public void close() throws IOException {
        zip.close();
      }
    };
  }

  public abstract boolean hasNext() throws IOException;

  public abstract ZipEntry next() throws IOException;

  /**
   * Return a stream factory for the current entry.
   *
   * @return the stream factory
   * @throws IOException anything that would prevent to obtain a stream factory
   */
  public abstract InputStreamFactory getStreamFactory() throws IOException;

}

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
package org.crsh.jcr;

import org.crsh.util.Utils;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;

public class SourceCommand extends SCPCommand implements Runnable {

  /** . */
  private boolean recursive;

  public SourceCommand(String target, boolean recursive) {
    super(target);

    //
    this.recursive = recursive;
  }

  @Override
  protected void execute(Session session, String path) throws Exception {
    FileSystem fs = new FileSystem() {
      public void beginDirectory(String directoryName) throws IOException {
        out.write("D0755 0 ".getBytes());
        out.write(directoryName.getBytes());
        out.write("\n".getBytes());
        out.flush();
        readAck();
      }
      public void file(String fileName, int length, InputStream data) throws IOException {
        out.write("C0644 ".getBytes());
        out.write(Integer.toString(length).getBytes());
        out.write(" ".getBytes());
        out.write(fileName.getBytes());
        out.write("\n".getBytes());
        out.flush();
        readAck();
        Utils.copy(data, out);
        ack();
        readAck();
      }
      public void endDirectory(String directoryName) throws IOException {
        out.write("E\n".getBytes());
        out.flush();
        readAck();
      }
    };

    Item item = session.getItem(path);
    if (item instanceof Node) {

      //
      BytesOutputStream baos = new BytesOutputStream();

      // Perform export
      session.exportSystemView(path, baos, false, false);

      //
      String name = item.getName();
      if (name.length() == 0) {
        name = "jcr_root.xml";
      } else {
        name = name.replace(":", "_") + ".xml";
      }

      //
      baos.flush();
      baos.close();

      //
      fs.file(name, baos.size(), baos.getInputStream());
    } else {
      throw new Exception("Cannot export a property");
    }
  }
}

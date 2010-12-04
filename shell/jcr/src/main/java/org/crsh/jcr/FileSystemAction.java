/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.crsh.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
abstract class FileSystemAction {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(FileSystemAction.class);

  public static void read(SCPCommand cmd, FileSystem fs) throws IOException {
    cmd.ack();
    log.debug("Want to read line");
    String line = cmd.readLine();
    log.debug("Read line " + line);
    FileSystemAction action = decode(line);
    log.debug("Action: " + action);
    read(cmd, action, fs);
  }

  private static void read(final SCPCommand cmd, FileSystemAction action, FileSystem fs) throws IOException {
    if (action instanceof StartDirectory) {
      String directoryName = ((StartDirectory)action).name;
      fs.startDirectory(directoryName);

      //
      cmd.ack();

      //
      while (true) {
        String nextLine = cmd.readLine();
        FileSystemAction nextAction = decode(nextLine);
        log.debug("Next action: " + nextAction);
        if (nextAction instanceof FileSystemAction.EndDirectory) {
          fs.endDirectory(directoryName);
          break;
        } else {
          read(cmd, nextAction, fs);
        }
      }

      //
      cmd.ack();
    } else if (action instanceof File) {
      File file = (File)action;

      //
      cmd.ack();

      //
      fs.file(file.name, file.length, cmd.read(file.length));

      //
      log.debug("About to send ack for file");
      cmd.ack();
      cmd.readAck();
    }
  }

  private static FileSystemAction decode(String line) {
    if (line == null) {
      throw new NullPointerException();
    }
    if (line.length() == 0) {
      throw new IllegalArgumentException("Line has length zero");
    }
    char t = line.charAt(0);
    if (t == 'C' || t == 'D') {

      //
      int length;
      int endLength = line.indexOf(' ', 6);
      if (endLength == -1) {
        throw new IllegalArgumentException();
      } else {
        String s = line.substring(6, endLength);
        if (s.length() == 1 && s.charAt(0) == '0') {
          // Optimize for directories
          length = 0;
        } else {
          try {
            length = Integer.parseInt(s);
          }
          catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse file length " + s);
          }
        }
      }

      //
      String name = line.substring(endLength + 1);

      //
      if (t == 'D') {
        return new StartDirectory(name);
      } else {
        return new File(name, length);
      }
    } else if (t == 'E') {
      return new EndDirectory();
    } else {
      throw new IllegalArgumentException("Could not recognize file system action " + line);
    }
  }

  private static class StartDirectory extends FileSystemAction {

    /** . */
    private final String name;

    private StartDirectory(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "StartDirectory[name=" + name + "]";
    }
  }

  private static class File extends FileSystemAction {

    /** . */
    private final String name;

    /** . */
    private final int length;

    private File(String name, int length) {
      this.name = name;
      this.length = length;
    }

    @Override
    public String toString() {
      return "File[name=" + name + ",length=" + length + "]";
    }
  }

  private static class EndDirectory extends FileSystemAction {
    private EndDirectory() {
    }

    @Override
    public String toString() {
      return "EndDirectory[]";
    }
  }
}
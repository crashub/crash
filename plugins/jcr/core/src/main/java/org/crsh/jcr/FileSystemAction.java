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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class FileSystemAction {

  /** . */
  private static final Logger log = Logger.getLogger(FileSystemAction.class.getName());

  public static void read(SCPCommand cmd, FileSystem fs) throws IOException {
    cmd.ack();
    log.log(Level.FINE, "Want to read line");
    String line = cmd.readLine();
    log.log(Level.FINE, "Read line " + line);
    FileSystemAction action = decode(line);
    log.log(Level.FINE, "Action: " + action);
    read(cmd, action, fs);
  }

  private static void read(final SCPCommand cmd, FileSystemAction action, FileSystem fs) throws IOException {
    if (action instanceof StartDirectory) {
      String directoryName = ((StartDirectory)action).name;
      fs.beginDirectory(directoryName);

      //
      cmd.ack();

      //
      while (true) {
        String nextLine = cmd.readLine();
        FileSystemAction nextAction = decode(nextLine);
        log.log(Level.FINE, "Next action: " + nextAction);
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
      log.log(Level.FINE, "About to send ack for file");
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
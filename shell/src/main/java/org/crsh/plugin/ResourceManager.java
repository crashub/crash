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

package org.crsh.plugin;

import org.crsh.vfs.FS;
import org.crsh.vfs.File;
import org.crsh.vfs.Path;
import org.crsh.vfs.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ResourceManager {

  /** . */
  private static final Pattern p = Pattern.compile("([^.]+)\\.[^.]+");

  /** . */
  private static final Logger log = Logger.getLogger(ResourceManager.class.getName());

  /** . */
  private final FS cmdFS;

  /** . */
  private final FS confFS;

  /** . */
  private volatile List<File> dirs;

  ResourceManager(FS cmdFS, FS confFS) {
    this.cmdFS = cmdFS;
    this.confFS = confFS;
  }

  /**
   * Load a resource from the context.
   *
   * @param resourceId the resource id
   * @param resourceKind the resource kind
   * @return the resource or null if it cannot be found
   */
  Iterable<Resource> loadResource(String resourceId, ResourceKind resourceKind) {
    try {
      switch (resourceKind) {
        case LIFECYCLE:
          if ("login".equals(resourceId) || "logout".equals(resourceId)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            long timestamp = Long.MIN_VALUE;
            for (File path : dirs) {
              File f = path.child(resourceId + ".groovy");
              if (f != null) {
                Resource sub = f.getResource();
                if (sub != null) {
                  buffer.write(sub.getContent());
                  buffer.write('\n');
                  timestamp = Math.max(timestamp, sub.getTimestamp());
                }
              }
            }
            return Collections.singleton(new Resource(resourceId + ".groovy", buffer.toByteArray(), timestamp));
          }
          break;
        case COMMAND:
          // Find the resource first, we find for the first found
          for (File path : dirs) {
            File f = path.child(resourceId);
            if (f != null) {
              return Collections.singleton(f.getResource());
            }
          }
          break;
        case CONFIG:
          String path = "/" + resourceId;
          File file = confFS.get(Path.get(path));
          if (file != null) {
            return Collections.singleton(loadConf(file));
          }
      }
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not obtain resource " + resourceId, e);
    }
    return Collections.emptyList();
  }

  /**
   * List the resources id for a specific resource kind.
   *
   * @param kind the resource kind
   * @return the resource ids
   */
  Iterable<String> listResourceId(ResourceKind kind) {
    switch (kind) {
      case COMMAND:
        SortedSet<String> all = new TreeSet<String>();
        try {
          for (File path : dirs) {
            for (File file : path.children()) {
              String fileName = file.getName();
              Matcher matcher = p.matcher(fileName);
              if (matcher.matches()) {
                String name = matcher.group(1);
                if (!"login".equals(name) && !"logout".equals(name)) {
                  all.add(fileName);
                }
              }
            }
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        return all;
      default:
        return Collections.emptyList();
    }
  }

  /**
   * Refresh the fs system view. This is normally triggered by the periodic job but it can be manually
   * invoked to trigger explicit refreshes.
   */
  void refresh() {
    try {
      File commands = cmdFS.get(Path.get("/"));
      List<File> newDirs = new ArrayList<File>();
      newDirs.add(commands);
      for (File path : commands.children()) {
        if (path.children().iterator().hasNext()) {
          newDirs.add(path);
        }
      }
      dirs = newDirs;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** . */
  private static final byte[] SEPARATOR = System.getProperty("line.separator").getBytes();

  public static Resource loadConf(File file) throws IOException {
    // Special handling for property files
    if (file.getName().endsWith(".properties")) {
      Iterator<Resource> i = file.getResources().iterator();
      if (i.hasNext()) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        long timestamp = 0;
        while (i.hasNext()) {
          Resource resource = i.next();
          byte[] bytes = resource.getContent();
          buffer.write(bytes);
          timestamp = Math.max(timestamp, resource.getTimestamp());
          if (i.hasNext()) {
            // Go to line
            buffer.write(SEPARATOR);
            // Cosmetic blank line
            buffer.write(SEPARATOR);
          }
        }
        return new Resource(file.getName(), buffer.toByteArray(), timestamp);
      } else {
        return null;
      }
    } else {
      return file.getResource();
    }
  }
}

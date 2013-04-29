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

package org.crsh.cli.completers;

import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;

import java.io.File;
import java.util.Collection;

public abstract class AbstractPathCompleter<P> implements Completer {

  protected abstract String getCurrentPath() throws Exception;

  protected abstract P getPath(String path) throws Exception;

  protected abstract boolean exists(P path) throws Exception;

  protected abstract boolean isDirectory(P path) throws Exception;

  protected abstract boolean isFile(P path) throws Exception;

  protected abstract Collection<P> getChilren(P path) throws Exception;

  protected abstract String getName(P path) throws Exception;

  public final Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {

    //
    String sep = File.separator;

    // Handle empty dir
    if (!prefix.startsWith(sep)) {
      String currentPath = getCurrentPath();

        if (!currentPath.endsWith(sep)) {
        currentPath += sep;
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
        if (prefix.endsWith(sep)) {
          Collection<P> children = getChilren(f);
          if (children != null) {
            if (children.size() > 0) {
              return listDir(f, "");
            } else {
              return Completion.create();
            }
          } else {
            return Completion.create();
          }
        } else {
          Collection<P> children = getChilren(f);
          if (children == null) {
            return Completion.create();
          } else {
            return Completion.create(sep, false);
          }
        }
      } else if (isFile(f)) {
        return Completion.create("", true);
      }
      return Completion.create();
    } else {
      int pos = prefix.lastIndexOf(sep);
      if (pos != -1) {
        String filter;
        if (pos == 0) {
          f = getPath(sep);
          filter = prefix.substring(1);
        } else {
          f = getPath(prefix.substring(0, pos));
          filter = prefix.substring(pos + 1);
        }
        if (exists(f)) {
          if (isDirectory(f)) {
            return listDir(f, filter);
          } else {
            return Completion.create();
          }
        } else {
          return Completion.create();
        }
      } else {
        return Completion.create();
      }
    }
  }

  private Completion listDir(P dir, final String filter) throws Exception {
    Collection<P> children = getChilren(dir);
    if (children != null) {
      Completion.Builder builder = Completion.builder(filter);
      for (P child : children) {
        String name = getName(child);
        if (name.startsWith(filter)) {
          String suffix = name.substring(filter.length());
          if (isDirectory(child)) {
            Collection<P> grandChildren = getChilren(child);
            if (grandChildren != null) {
              builder.add(suffix + File.separator, false);
            } else {
              // Skip it
            }
          } else {
            builder.add(suffix, true);
          }
        }
      }
      return builder.build();
    } else {
      return Completion.create();
    }
  }
}

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
package org.crsh.servlet;

import org.crsh.shell.Resource;
import org.crsh.shell.ResourceKind;
import org.crsh.shell.ShellContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ServletShellContext implements ShellContext {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(ServletShellContext.class);

  /** . */
  private final ServletContext servletContext;

  /** . */
  private final ClassLoader loader;

  /** . */
  private final String version;

  /** . */
  private ScheduledExecutorService executor;

  /** . */
  private volatile List<String> dirs;

  public ServletShellContext(ServletContext servletContext, ClassLoader loader) {
    if (servletContext == null) {
      throw new NullPointerException();
    }
    if (loader == null) {
      throw new NullPointerException();
    }

    //
    String version = null;
    try {
      Properties props = new Properties();
      InputStream in = getClass().getClassLoader().getResourceAsStream("META-INF/maven/org.crsh/crsh.shell.core/pom.properties");
      if (in != null) {
        props.load(in);
        version = props.getProperty("version");
      }
    } catch (Exception e) {
      log.error("Could not load maven properties", e);
    }

    //
    if (version == null) {
      log.warn("No version found will use unknown value instead");
      version = "unknown";
    }

    //
    this.servletContext = servletContext;
    this.loader = loader;
    this.version = version;
    this.dirs = Collections.emptyList();
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public String getVersion() {
    return version;
  }

  public Resource loadResource(String resourceId, ResourceKind resourceKind) {
    Resource res = null;
    try {

      //
      switch (resourceKind) {
        case LIFECYCLE:
          if ("login".equals(resourceId) || "logout".equals(resourceId)) {
            StringBuilder sb = new StringBuilder();
            long timestamp = Long.MIN_VALUE;
            for (String path : dirs) {
              URL url = servletContext.getResource(path + resourceId + ".groovy");
              if (url != null) {
                Resource sub = Resource.create(url);
                if (sub != null) {
                  sb.append(sub.getContent());
                  timestamp = Math.max(timestamp, sub.getTimestamp());
                }
              }
            }
            return new Resource(sb.toString(), timestamp);
          }
          break;
        case SCRIPT:
          // Find the resource first, we find for the first found
          for (String path : dirs) {
            URL url = servletContext.getResource(path + resourceId + ".groovy");
            if (url != null) {
              return Resource.create(url);
            }
          }
          break;
        case CONFIG:
          if ("telnet.properties".equals(resourceId)) {
            URL url = servletContext.getResource("/WEB-INF/telnet/telnet.properties");
            if (url != null) {
              return Resource.create(url);
            }
          }
          break;
      }
    } catch (IOException e) {
      log.warn("Could not obtain resource " + resourceId, e);
    }
    return res;
  }

  private final Pattern p = Pattern.compile(".*/(.+)\\.groovy");

  public List<String> listResourceId(ResourceKind kind) {
    switch (kind) {
      case SCRIPT:
        SortedSet<String> all = new TreeSet<String>();
        for (String path : dirs) {
          @SuppressWarnings("unchecked")
          Set<String> files = servletContext.getResourcePaths(path);
          for (String file : files) {
            Matcher matcher = p.matcher(file);
            if (matcher.matches()) {
              all.add(matcher.group(1));
            }
          }
        }
        all.remove("login");
        all.remove("logout");
        return new ArrayList<String>(all);
      default:
        return Collections.emptyList();
    }
  }

  public ClassLoader getLoader() {
    return loader;
  }

  public synchronized  void start() {
    if (executor == null) {
      executor =  new ScheduledThreadPoolExecutor(1);
      executor.scheduleWithFixedDelay(new Runnable() {
        int count = 0;
        public void run() {
          @SuppressWarnings("unchecked")
          Set<String> set = servletContext.getResourcePaths("/WEB-INF/groovy/commands/");
          if (set != null) {
            List<String> newDirs = new ArrayList<String>();
            newDirs.add("/WEB-INF/groovy/commands/");
            for (String path : set) {
              if (path.endsWith("/")) {
                newDirs.add(path);
              }
            }
            dirs = newDirs;
          }
        }
      }, 0, 1, TimeUnit.SECONDS);
    } else {
      log.warn("Attempt to double start");
    }
  }

  public synchronized void stop() {
    if (executor != null) {
      ScheduledExecutorService tmp = executor;
      executor = null;
      tmp.shutdown();
    } else {
      log.warn("Attempt to stop when stopped");
    }
  }
}

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
package org.crsh.cli.impl.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;
import java.util.logging.LogManager;

/**
 * An internal class that configures java util logging with <code>ALL</code> level with the
 * <code>FileHandler</code> in the <code>java.util.logging.FileHandler.pattern</code> system property.
 *
 * @author Julien Viet
 */
public class LogConfigurator {

  public LogConfigurator() throws Exception {
    LogManager manager = LogManager.getLogManager();
    String target = System.getProperty("java.util.logging.FileHandler.pattern");
    if (target != null) {
      File f = new File(target);
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      Properties props = new Properties();
      props.put("handlers", "java.util.logging.FileHandler");
      props.put(".level", "ALL");
      props.put("java.util.logging.FileHandler.level", "ALL");
      props.put("java.util.logging.FileHandler.pattern", f.getAbsolutePath());
      props.put("java.util.logging.FileHandler.formatter", "java.util.logging.SimpleFormatter");
      props.store(buffer, null);
      buffer.close();
      manager.readConfiguration(new ByteArrayInputStream(buffer.toByteArray()));
    } else {
      manager.readConfiguration();
    }
  }
}

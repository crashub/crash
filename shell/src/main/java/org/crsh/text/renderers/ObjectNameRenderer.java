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
package org.crsh.text.renderers;

import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.Overflow;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;
import org.crsh.util.Utils;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Julien Viet
 */
public class ObjectNameRenderer extends Renderer<ObjectName> {

  @Override
  public Class<ObjectName> getType() {
    return ObjectName.class;
  }

  @Override
  public LineRenderer renderer(Iterator<ObjectName> stream) {

    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    List<ObjectName> names = Utils.list(stream);
    Collections.sort(names);

    //
    TableElement table = new TableElement().overflow(Overflow.HIDDEN).rightCellPadding(1);

    // Header
    table.add(
      new RowElement().
        style(Decoration.bold.fg(Color.black).bg(Color.white)).
        add("NAME", "CLASSNAME", "MXBEAN", "DESCRIPTION")
    );

    //
    for (ObjectName name : names) {

      String className;
      String description;
      String mxbean;
      try {
        MBeanInfo info = server.getMBeanInfo(name);
        className = info.getClassName();
        description = info.getDescription();
        Object mxbeanValue = info.getDescriptor().getFieldValue("mxbean");
        mxbean = mxbeanValue != null ? mxbeanValue.toString() : "false";
      }
      catch (JMException ignore) {
        className = "";
        description = "";
        mxbean = "";
      }

      //
      table.row("" + name, className, mxbean, description);
    }

    //
    return table.renderer();
  }
}

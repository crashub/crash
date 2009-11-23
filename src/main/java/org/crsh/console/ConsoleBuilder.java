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
package org.crsh.console;

import groovy.util.BuilderSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ConsoleBuilder extends BuilderSupport
{

   /** . */
   private final List<ConsoleElement> elements;

   public ConsoleBuilder()
   {
      this.elements = new ArrayList<ConsoleElement>();
   }

   public List<ConsoleElement> getElements()
   {
      return elements;
   }

   @Override
   protected Object createNode(Object name)
   {
      return createNode(name, (Object)null);
   }

   @Override
   protected Object createNode(Object name, Object value)
   {
      if ("message".equals(name))
      {
         return new MessageElement(String.valueOf(value));
      }
      else if ("table".equals(name))
      {
         return new TableElement();
      }
      else if ("row".equals(name))
      {
         Row row = new Row();
         if (value instanceof List<?>)
         {
            for (Object o : (List<?>)value)
            {
               String s = String.valueOf(o);
               row.values.add(s);
            }
         }
         else if (value != null)
         {
            String s = value.toString();
            row.values.add(s);
         }
         return row;
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   @Override
   protected Object createNode(Object name, Map attributes, Object value)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   protected Object createNode(Object name, Map attributes)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   protected void setParent(Object parent, Object child)
   {
      if (parent instanceof TableElement)
      {
         TableElement table = (TableElement)parent;
         Row row = (Row)child;
         table.data.add(row);
      }
      else
      {

      }
   }

   @Override
   protected void nodeCompleted(Object parent, Object node)
   {
      if (parent == null)
      {
         elements.add((ConsoleElement)node);
      }

      //
      super.nodeCompleted(parent, node);
   }
}

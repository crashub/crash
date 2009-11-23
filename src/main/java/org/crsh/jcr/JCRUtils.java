/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.crsh.jcr;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Calendar;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class JCRUtils
{

   public static final int PATH = PropertyType.PATH;
   public static final int STRING = PropertyType.STRING;
   public static final int DATE = PropertyType.DATE;
   public static final int DOUBLE = PropertyType.DOUBLE;
   public static final int LONG = PropertyType.LONG;
   public static final int BOOLEAN = PropertyType.BOOLEAN;
   public static final int REFERENCE = PropertyType.REFERENCE;


   public static Property getProperty(Node node, String propertyName) throws RepositoryException {
     return node.getProperty(propertyName);
   }

   public static void setProperty(Node node, String propertyName, boolean value) throws RepositoryException {
     node.setProperty(propertyName, value);
   }

   public static void setProperty(Node node, String propertyName, Value value) throws RepositoryException
   {
     node.setProperty(propertyName, value);
   }

   public static boolean isJCRPropertyType(Object value) {
     return value instanceof String ||
       value instanceof Node ||
       value instanceof Long ||
       value instanceof Boolean ||
       value instanceof Integer ||
       value instanceof Short ||
       value instanceof Byte ||
       value instanceof Float ||
       value instanceof Double ||
       value instanceof Calendar ||
       value instanceof Value[];
   }
}

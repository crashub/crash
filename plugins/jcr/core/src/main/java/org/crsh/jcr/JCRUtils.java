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

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import java.io.InputStream;
import java.util.Calendar;

public class JCRUtils {

  public static final int PATH = PropertyType.PATH;
  public static final int STRING = PropertyType.STRING;
  public static final int DATE = PropertyType.DATE;
  public static final int DOUBLE = PropertyType.DOUBLE;
  public static final int LONG = PropertyType.LONG;
  public static final int BOOLEAN = PropertyType.BOOLEAN;
  public static final int REFERENCE = PropertyType.REFERENCE;
  public static final int BINARY = PropertyType.BINARY;

  private JCRUtils() {
  }

  public static Property getProperty(Node node, String propertyName) throws RepositoryException {
    return node.getProperty(propertyName);
  }

  public static void setProperty(Node node, String propertyName, boolean value) throws RepositoryException {
    node.setProperty(propertyName, value);
  }

  public static void setProperty(Node node, String propertyName, Value value) throws RepositoryException {
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
      value instanceof InputStream ||
      value instanceof Value[];
  }

  public static String encodeName(String name) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0;i < name.length();i++) {
      char c = name.charAt(i);
      if (Character.isLetterOrDigit(c)) {
        builder.append(c);
      } else {
        String val = Integer.toString(c);
        int padding = 4 - val.length();
        builder.append("_x");
        while (padding > 0) {
          builder.append("0");
        }
        builder.append(val);
      }
    }
    return builder.toString();
  }

  public static String decodeName(String name) {
    throw new UnsupportedOperationException();
  }

  public static PropertyDefinition getPropertyDefinition(NodeType nodeType, String propertyName) throws RepositoryException {
    for (PropertyDefinition def : nodeType.getPropertyDefinitions()) {
      if (def.getName().equals(propertyName)) {
        return def;
      }
    }
    return null;
  }

  public static PropertyDefinition findPropertyDefinition(Node node, String propertyName) throws RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getDefinition();
    } else {
      NodeType primaryNodeType = node.getPrimaryNodeType();
      PropertyDefinition def = getPropertyDefinition(primaryNodeType, propertyName);
      if (def == null) {
        for (NodeType mixinNodeType : node.getMixinNodeTypes()) {
          def = getPropertyDefinition(mixinNodeType, propertyName);
          if (def != null) {
            break;
          }
        }
      }
      if (def == null && !propertyName.equals("*")) {
        def = getPropertyDefinition(primaryNodeType, "*");
      }
      return def;
    }
  }
}

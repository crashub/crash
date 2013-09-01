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
package org.crsh.jcr.groovy;

import groovy.lang.Closure;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.crsh.jcr.JCRUtils;
import org.crsh.jcr.PropertyType;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.beans.IntrospectionException;

public class NodeMetaClass extends MetaClassImpl {

  public static void setup() {

  }

  public NodeMetaClass(MetaClassRegistry registry, Class<? extends Node> theClass) throws IntrospectionException {
    super(registry, theClass);
  }

  @Override
  public Object invokeMethod(Object object, String name, Object[] args) {
    try {
      return _invokeMethod(object, name, args);
    }
    catch (RepositoryException e) {
      // Do that better
      throw new Error(e);
    }
  }

  private Object _invokeMethod(Object object, String name, Object[] args) throws RepositoryException {
    Node node = (Node)object;

    //
    if (args != null) {
      if (args.length == 0) {
        if ("iterator".equals(name)) {
          return node.getNodes();
        }
      }
      else if (args.length == 1) {
        Object arg = args[0];

        // This is the trick we need to use because the javax.jcr.Node interface
        // has a getProperty(String name) method that is shadowed by the GroovyObject
        // method with the same signature.
        if (arg instanceof String && "getProperty".equals(name)) {
          String propertyName = (String)arg;
          return JCRUtils.getProperty(node, propertyName);
        }
        else if (arg instanceof Closure) {
          Closure closure = (Closure)arg;
          if ("eachProperty".equals(name)) {
            PropertyIterator properties = node.getProperties();
            while (properties.hasNext()) {
              Property n = properties.nextProperty();
              closure.call(new Object[]{n});
            }
            return null;
          }/* else if ("eachWithIndex".equals(name)) {
              NodeIterator nodes = node.getNodes();
              int index = 0;
              while (nodes.hasNext()) {
                Node n = nodes.nextNode();
                closure.call(new Object[]{n,index++});
              }
              return null;
            }*/
        }
        else if ("getAt".equals(name)) {
          if (arg instanceof Integer) {
            NodeIterator it = node.getNodes();
            long size = it.getSize();
            long index = (Integer)arg;

            // Bounds detection
            if (index < 0) {
              if (index < -size) throw new ArrayIndexOutOfBoundsException((int)index);
              index = size + index;
            }
            else if (index >= size) throw new ArrayIndexOutOfBoundsException((int)index);

            //
            it.skip(index);
            return it.next();
          }
        }
      }
    }

    // We let groovy handle the call
    MetaMethod validMethod = super.getMetaMethod(name, args);
    if (validMethod != null) {
      return validMethod.invoke(node, args);
    }

    //
    throw new MissingMethodException(name, Node.class, args);
  }

  @Override
  public Object getProperty(Object object, String property) {
    try {
      return _getProperty(object, property);
    }
    catch (RepositoryException e) {
      throw new Error(e);
    }
  }

  private Object _getProperty(Object object, String propertyName) throws RepositoryException {
    Node node = (Node)object;

    // Access defined properties
    MetaProperty metaProperty = super.getMetaProperty(propertyName);
    if (metaProperty != null) {
      return metaProperty.getProperty(node);
    }

    // First we try to access a property
    try {
      Property property = node.getProperty(propertyName);
      PropertyType type = PropertyType.fromValue(property.getType());
      return type.get(property);
    }
    catch (PathNotFoundException e) {
    }

    // If we don't find it as a property we try it as a child node
    try {
      return node.getNode(propertyName);
    }
    catch (PathNotFoundException e) {
    }

    //
    return null;
  }

  @Override
  public void setProperty(Object object, String property, Object newValue) {
    try {
      _setProperty(object, property, newValue);
    }
    catch (Exception e) {
      throw new Error(e);
    }
  }

  private void _setProperty(Object object, String propertyName, Object propertyValue) throws RepositoryException {
    Node node = (Node)object;
    if (propertyValue == null) {
      node.setProperty(propertyName, (Value)null);
    } else {

      // Get property type
      PropertyType type;
      try {
        Property property = node.getProperty(propertyName);
        type = PropertyType.fromValue(property.getType());
      } catch (PathNotFoundException e) {
        type = PropertyType.fromCanonicalType(propertyValue.getClass());
      }

      // Update the property and get the updated property
      Property property;
      if (type != null) {
        property = type.set(node, propertyName, propertyValue);
      } else {
        property = null;
      }

      //
      if (property == null && propertyValue instanceof String) {
        if (propertyValue instanceof String) {
          // This is likely a conversion from String that should be handled natively by JCR itself
          node.setProperty(propertyName, (String)propertyValue);
        } else {
          throw new MissingPropertyException("Property " + propertyName + " does not have a correct type " + propertyValue.getClass().getName());
        }
      }
    }
  }
}

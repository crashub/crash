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
package org.crsh.jcr;

import groovy.lang.Closure;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.beans.IntrospectionException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NodeMetaClass extends MetaClassImpl
{

   static {
      try
      {
         MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
         Class<? extends Node> eXoNode = (Class<Node>)Thread.currentThread().getContextClassLoader().loadClass("org.exoplatform.services.jcr.impl.core.NodeImpl");
         NodeMetaClass mc2 = new NodeMetaClass(registry, eXoNode);
         mc2.initialize();
         registry.setMetaClass(eXoNode, mc2);
      }
      catch (Exception e)
      {
         throw new Error("Coult not integrate node meta class");
      }
   }

   public static void setup()
   {
      
   }
   
   public NodeMetaClass(MetaClassRegistry registry, Class<? extends Node> theClass) throws IntrospectionException
   {
      super(registry, theClass);
   }

   @Override
   public Object invokeMethod(Object object, String name, Object[] args)
   {
      try
      {
         return _invokeMethod(object, name, args);
      }
      catch (RepositoryException e)
      {
         // Do that better
         throw new Error(e);
      }
   }

   private Object _invokeMethod(Object object, String name, Object[] args) throws RepositoryException
   {
      Node node = (Node)object;

      //
      if (args != null) {
        if (args.length == 0) {
          if ("iterator".equals(name)) {
            return node.getNodes();
          }
        } else if (args.length == 1) {
          Object arg = args[0];

          // This is the trick we need to use because the javax.jcr.Node interface
          // has a getProperty(String name) method that is shadowed by the GroovyObject
          // method with the same signature.
          if (arg instanceof String && "getProperty".equals(name)) {
            String propertyName = (String)arg;
            return JCRUtils.getProperty(node, propertyName);
          } else if (arg instanceof Closure) {
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
          } else if ("getAt".equals(name)) {
            if (arg instanceof Integer) {
              NodeIterator it = node.getNodes();
              long size = it.getSize();
              long index = (Integer)arg;

              // Bounds detection
              if (index < 0) {
                if (index < -size) throw new ArrayIndexOutOfBoundsException((int)index);
                index = size + index;
              } else if (index >= size) throw new ArrayIndexOutOfBoundsException((int)index);

              //
              it.skip(index);
              return it.next();
            }
          }
        } else if (args.length == 2) {
          Object arg0 = args[0];
          Object arg1 = args[1];

          //
          if (arg0 instanceof String && "setProperty".equals(name)) {
            String propertyName = (String)arg0;
            if (arg1 instanceof Boolean) {
              JCRUtils.setProperty(node, propertyName, (Boolean)arg1);
              return null;
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
   public Object getProperty(Object object, String property)
   {
      try
      {
         return _getProperty(object, property);
      }
      catch (RepositoryException e)
      {
         throw new Error(e);
      }
   }

   private Object _getProperty(Object object, String propertyName) throws RepositoryException
   {
      Node node = (Node)object;

      // Access defined properties
      MetaProperty metaProperty = super.getMetaProperty(propertyName);
      if (metaProperty != null) {
        return metaProperty.getProperty(node);
      }

      // First we try to access a property
      try {
        Property property = node.getProperty(propertyName);
        int type = property.getType();
        switch (type) {
          case JCRUtils.PATH:
          case JCRUtils.STRING:
            return property.getString();
          case JCRUtils.DATE:
            return property.getDate();
          case JCRUtils.DOUBLE:
            return property.getDouble();
          case JCRUtils.LONG:
            return property.getLong();
          case JCRUtils.BOOLEAN:
            return property.getBoolean();
          case JCRUtils.REFERENCE:
            return property.getNode();
          default:
            throw new UnsupportedOperationException("JCR Property type ${type} not handled yet");
        }
      } catch (PathNotFoundException e) {
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
   public void setProperty(Object object, String property, Object newValue)
   {
      try
      {
         _setProperty(object, property, newValue);
      }
      catch (Exception e)
      {
         throw new Error(e);
      }
   }

   private void _setProperty(Object object, String property, Object newValue) throws RepositoryException
   {
      Node node = (Node)object;

      // Perform some unwrapping if necessary first
      if (newValue instanceof BigDecimal) {
        // Unwrap big decimal
        BigDecimal bdValue = (BigDecimal)newValue;
        newValue = bdValue.doubleValue();
      }
      else if (newValue instanceof BigInteger) {
        // Unwrap big integer
        BigInteger biValue = (BigInteger)newValue;
        newValue = biValue.longValue();
      }
      else if (newValue instanceof Character) {
        // Transform character to String
        newValue = Character.toString((Character)newValue);
      }

      //
      if (newValue == null || JCRUtils.isJCRPropertyType(newValue)) {
        if (newValue == null) {
          node.setProperty(property, (Value)null);
        } else if (newValue instanceof Double) {
          node.setProperty(property, (Double)newValue);
        } else if (newValue instanceof String) {
          node.setProperty(property, (String)newValue);
        } else if (newValue instanceof Long) {
          node.setProperty(property, (Long)newValue);
        } else if (newValue instanceof Integer) {
          node.setProperty(property, (Integer)newValue);
        } else if (newValue instanceof Byte) {
          node.setProperty(property, (Byte)newValue);
        } else if (newValue instanceof Boolean) {
          node.setProperty(property, (Boolean)newValue);
        } else if (newValue instanceof Calendar) {
          node.setProperty(property, (Calendar)newValue);
        } else if (newValue instanceof Float) {
          node.setProperty(property, (Float)newValue);
        } else {
          throw new UnsupportedOperationException("todo with object " + newValue);
        }
      }
      else {
        throw new MissingPropertyException("Property $name does not have a correct type");
      }
   }
}

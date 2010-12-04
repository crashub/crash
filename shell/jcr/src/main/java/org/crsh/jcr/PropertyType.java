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

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum PropertyType {

  PATH(javax.jcr.PropertyType.PATH){
    @Override
    public Object getValue(Property property) throws RepositoryException {
      return property.getString();
    }
    @Override
    public Property set(Node node, String name, Object value) throws RepositoryException {
      if (value instanceof String) {
        return node.setProperty(name, (String)value);
      } else {
        return null;
      }
    }
    @Override
    protected Collection<Class<?>> getCanonicalTypes() {
      return Collections.emptySet();
    }
  },

  STRING(javax.jcr.PropertyType.STRING){
    @Override
    public Object getValue(Property property) throws RepositoryException {
      return property.getString();
    }
    @Override
    public Property set(Node node, String name, Object value) throws RepositoryException {
      if (value instanceof String) {
        return node.setProperty(name, (String)value);
      } else if (value instanceof Character) {
        return node.setProperty(name, Character.toString((Character)value));
      } else {
        return null;
      }
    }
    @Override
    protected Collection<Class<?>> getCanonicalTypes() {
      return Arrays.<Class<?>>asList(String.class,Character.class);
    }
  },

  LONG(javax.jcr.PropertyType.LONG) {
    @Override
    public Object getValue(Property property) throws RepositoryException {
      return property.getLong();
    }
    @Override
    public Property set(Node node, String name, Object value) throws RepositoryException {
      if (value instanceof Long) {
        return node.setProperty(name, (Long)value);
      } else if (value instanceof Integer) {
        return node.setProperty(name, (Integer)value);
      } else if (value instanceof Byte) {
        return node.setProperty(name, (Byte)value);
      } else if (value instanceof BigInteger) {
        BigInteger biValue = (BigInteger)value;
        return node.setProperty(name, biValue.longValue());
      } else {
        return null;
      }
    }
    @Override
    protected Collection<Class<?>> getCanonicalTypes() {
      return Arrays.<Class<?>>asList(Long.class,Integer.class,Byte.class,BigInteger.class);
    }
  },

  DOUBLE(javax.jcr.PropertyType.DOUBLE) {
    @Override
    public Object getValue(Property property) throws RepositoryException {
      return property.getDouble();
    }
    @Override
    public Property set(Node node, String name, Object value) throws RepositoryException {
      if (value instanceof Double) {
        return node.setProperty(name, (Double)value);
      }  else if (value instanceof Float) {
        return node.setProperty(name, (Float)value);
      } else if (value instanceof BigDecimal) {
        BigDecimal bdValue = (BigDecimal)value;
        return node.setProperty(name, bdValue.doubleValue());
      } else {
        return null;
      }
    }
    @Override
    protected Collection<Class<?>> getCanonicalTypes() {
      return Arrays.<Class<?>>asList(Double.class,Float.class,BigDecimal.class);
    }
  },

  BOOLEAN(javax.jcr.PropertyType.BOOLEAN) {
    @Override
    public Object getValue(Property property) throws RepositoryException {
      return property.getBoolean();
    }
    @Override
    public Property set(Node node, String name, Object value) throws RepositoryException {
      if (value instanceof Boolean) {
        return node.setProperty(name, (Boolean)value);
      } else {
        return null;
      }
    }
    @Override
    protected Collection<Class<?>> getCanonicalTypes() {
      return Arrays.<Class<?>>asList(Boolean.class);
    }
  },

  DATE(javax.jcr.PropertyType.DATE) {
    @Override
    public Object getValue(Property property) throws RepositoryException {
      return property.getDate();
    }
    @Override
    public Property set(Node node, String name, Object value) throws RepositoryException {
      if (value instanceof Calendar) {
        return node.setProperty(name, (Calendar)value);
      } else {
        return null;
      }
    }
    @Override
    protected Collection<Class<?>> getCanonicalTypes() {
      return Arrays.<Class<?>>asList(Calendar.class);
    }
  },

  BINARY(javax.jcr.PropertyType.BINARY) {
    @Override
    public Object getValue(Property property) throws RepositoryException {
      return property.getStream();
    }
    @Override
    public Property set(Node node, String name, Object value) throws RepositoryException {
      if (value instanceof InputStream) {
        return node.setProperty(name, (InputStream)value);
      } else {
        return null;
      }
    }
    @Override
    protected Collection<Class<?>> getCanonicalTypes() {
      return Arrays.<Class<?>>asList(InputStream.class);
    }
  },

  REFERENCE(javax.jcr.PropertyType.REFERENCE) {
    @Override
    public Object getValue(Property property) throws RepositoryException {
      return property.getNode();
    }
    @Override
    public Property set(Node node, String name, Object value) throws RepositoryException {
      if (value instanceof Node) {
        return node.setProperty(name, (Node)value);
      } else {
        return null;
      }
    }
    @Override
    protected Collection<Class<?>> getCanonicalTypes() {
      return Arrays.<Class<?>>asList(Node.class);
    }
  };

  /** . */
  private static final PropertyType[] all = new PropertyType[20]; // 20 should be enough

  /** . */
  private static final Map<Class<?>, PropertyType> canonicalMapping = new HashMap<Class<?>, PropertyType>();

  static  {
    for (PropertyType type : PropertyType.values())
    {
      all[type.value] = type;
      for (Class<?> canonicalType : type.getCanonicalTypes())
      {
        canonicalMapping.put(canonicalType, type);
      }
    }
  }

  public static PropertyType fromCanonicalType(Class<?> canonicalType)
  {
    for (Class<?> currentType = canonicalType;currentType != null;currentType = currentType.getSuperclass()) {
      PropertyType type = canonicalMapping.get(currentType);
      if (type != null) {
        return type;
      }
    }

    //
    return null;
  }

  public static PropertyType fromValue(int v)
  {
    PropertyType type = null;
    if (v >= 0 && v < all.length)
    {
      type = all[v];
    }

    //
    if (type == null)
    {
      throw new IllegalArgumentException("JCR Property type " + v + " not handled yet");
    }
    else
    {
      return type;
    }
  }

  /** . */
  private final int value;

  PropertyType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public abstract Object getValue(Property property) throws RepositoryException;

  public abstract Property set(Node node, String name, Object value) throws RepositoryException;

  protected abstract Collection<Class<?>> getCanonicalTypes();
}

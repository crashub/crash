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

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public enum PropertyType {

  PATH(javax.jcr.PropertyType.PATH){
    @Override
    public Object unwrap(Value value) throws RepositoryException {
      return value.getString();
    }
    @Override
    protected Value wrap(ValueFactory factory, Object value) {
      if (value instanceof String) {
        return factory.createValue((String)value);
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
    public Object unwrap(Value value) throws RepositoryException {
      return value.getString();
    }
    @Override
    protected Value wrap(ValueFactory factory, Object value) {
      if (value instanceof String) {
        return factory.createValue((String) value);
      } else if (value instanceof Character) {
        return factory.createValue(Character.toString((Character) value));
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
    public Object unwrap(Value value) throws RepositoryException {
      return value.getLong();
    }
    @Override
    protected Value wrap(ValueFactory factory, Object value) {
      if (value instanceof Long) {
        return factory.createValue((Long) value);
      } else if (value instanceof Integer) {
        return factory.createValue((Integer) value);
      } else if (value instanceof Byte) {
        return factory.createValue((Byte) value);
      } else if (value instanceof BigInteger) {
        BigInteger biValue = (BigInteger)value;
        return factory.createValue(biValue.longValue());
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
    public Object unwrap(Value value) throws RepositoryException {
      return value.getDouble();
    }
    @Override
    protected Value wrap(ValueFactory factory, Object value) {
      if (value instanceof Double) {
        return factory.createValue((Double) value);
      }  else if (value instanceof Float) {
        return factory.createValue((Float) value);
      } else if (value instanceof BigDecimal) {
        BigDecimal bdValue = (BigDecimal)value;
        return factory.createValue(bdValue.doubleValue());
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
    public Object unwrap(Value value) throws RepositoryException {
      return value.getBoolean();
    }
    @Override
    protected Value wrap(ValueFactory factory, Object value) {
      if (value instanceof Boolean) {
        return factory.createValue((Boolean) value);
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
    public Object unwrap(Value value) throws RepositoryException {
      return value.getDate();
    }
    @Override
    protected Value wrap(ValueFactory factory, Object value) {
      if (value instanceof Calendar) {
        return factory.createValue((Calendar) value);
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
    public Object unwrap(Value value) throws RepositoryException {
      return value.getStream();
    }
    @Override
    protected Value wrap(ValueFactory factory, Object value) {
      if (value instanceof InputStream) {
        return factory.createValue((InputStream) value);
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
    public Object unwrap(Value value) throws RepositoryException {
      throw new AssertionError("It should not be called");
    }
    @Override
    protected Value wrap(ValueFactory factory, Object value) throws RepositoryException {
      if (value instanceof Node) {
        return factory.createValue((Node)value);
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

  public Object get(Property property) throws RepositoryException {
    if (this == REFERENCE) {
      return property.getNode();
    } else {
      Value value;
      if (property.getDefinition().isMultiple()) {
        Value[] values = property.getValues();
        value = values.length > 0 ? values[0] : null;
      } else {
        value = property.getValue();
      }
      return value != null ? unwrap(value) : null;
    }
  }

  public final Property set(Node node, String name, Object value) throws RepositoryException {
    Value v = wrap(node.getSession().getValueFactory(), value);
    if (v != null) {
      try {
        return node.setProperty(name, v);
      } catch (ValueFormatException e) {
        return node.setProperty(name, new Value[]{v});
      }
    }
    return null;
  }

  protected abstract Object unwrap(Value value) throws RepositoryException;

  protected abstract Value wrap(ValueFactory factory, Object value) throws RepositoryException;

  protected abstract Collection<Class<?>> getCanonicalTypes();
}

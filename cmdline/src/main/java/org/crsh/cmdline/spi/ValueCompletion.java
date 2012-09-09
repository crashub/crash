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

package org.crsh.cmdline.spi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ValueCompletion implements Iterable<Map.Entry<String, Boolean>>, Serializable {

  public static ValueCompletion create() {
    return new ValueCompletion();
  }

  public static ValueCompletion create(String prefix) {
    return new ValueCompletion(prefix);
  }

  public static ValueCompletion create(String prefix, String suffix, boolean value) {
    ValueCompletion result = new ValueCompletion(prefix);
    result.put(suffix, value);
    return result;
  }

  public static ValueCompletion create(String suffix, boolean value) {
    ValueCompletion result = new ValueCompletion();
    result.put(suffix, value);
    return result;
  }

  /** . */
  private final String prefix;

  /** . */
  private final Map<String, Boolean> entries;

  public ValueCompletion() {
    this("");
  }

  public ValueCompletion(String prefix) {
    this(prefix, new LinkedHashMap<String, Boolean>());
  }

  public ValueCompletion(String prefix, Map<String, Boolean> entries) {
    if (prefix == null) {
      throw new NullPointerException("No null prefix allowed");
    }
    if (entries == null) {
      throw new NullPointerException("No null values allowed");
    }

    //
    this.prefix = prefix;
    this.entries = entries;
  }

  public Iterator<Map.Entry<String, Boolean>> iterator() {
    return entries.entrySet().iterator();
  }

  public Set<String> getSuffixes() {
    return entries.keySet();
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public Object get(String key) {
    return entries.get(key);
  }

  public int getSize() {
    return entries.size();
  }

  public ValueCompletion put(String key, boolean value) {
    entries.put(key, value);
    return this;
  }

  public String getPrefix() {
    return prefix;
  }

  @Override
  public int hashCode() {
    return prefix.hashCode() ^ entries.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ValueCompletion) {
      ValueCompletion that = (ValueCompletion)obj;
      return prefix.equals(that.prefix) && entries.equals(that.entries);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Completion[prefix=" + prefix + ",entries=" + entries + "]";
  }
}

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

package org.crsh.cli.spi;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>An immutable object representing the complation of a value. A completion is described by:</p>
 *
 * <ol>
 *   <li>A prefix: an optional value that is relevant when more than a result is provided. The prefix value must be a
 *   suffix of the completion prefix, it is used to shorten the completed prefix in order to make the completions
 *   values shorter. For instance a path completion returning several values could be displayed in columns, however only
 *   the last name of the path would be displayed and not the full path.</li>
 *   <li>A list of <code>Map.Entry&lt;String, Boolean&gt;</code>  map where the key is string value of the completion
 *   and the boolean value tells whether the value is a suffix (i.e it ends the value) or not (i.e it can be further
 *   more completed).</li>
 * </ol>
 *
 * <p>The following guidelines should be respected:</p>
 * <ul>
 *   <li>An empty completion means no completion can be determined.</li>
 *   <li>A singleton map means the match was entire and completion will happen with the unique entry.</li>
 *   <li>A map containing a list of string values sharing a common prefix indicates to use this common prefix.</li>
 *   <li>A list containing strings with no common prefix (other than the empty string) instruct to display the list of
 *   possible completions. In that case the completion result prefix is used to preped the returned suffixes when
 *   displayed in rows.</li>
 *   <li>When a match is considered as entire (the entry value is set to true), the completion should contain a
 *   trailing value that is usually a white space (but it could be a quote for quoted values).</li>
 * </ul>
 *
 * <p>Example: a completer that would complete colors could</p>
 * <ul>
 *   <li>return the result ["lack ":true,"lue ":true] with the prefix "b" for the prefix "b".</li>
 *   <li>return the result ["e ":true] with the prefix "blu" for the prefix "blu".</li>
 *   <li>return the result [] for the prefix "z".</li>
 * </ul>
 *
 * <p>Example: a completer that would complete java packages could</p>
 * <ul>
 *   <li>return the map ["ext":true,"ext.spi":false] for the prefix "java.t"</li>
 * </ul>
 */
public final class Completion implements Iterable<Map.Entry<String, Boolean>>, Serializable {

  /** . */
  private static final Completion EMPTY = create("");

  public static Builder builder(String prefix) {
    return new Builder(prefix);
  }

  public static Completion create() {
    return EMPTY;
  }

  public static Completion create(String prefix) {
    return create(prefix, Collections.<String, Boolean>emptyMap());
  }

  public static Completion create(String prefix, String suffix, boolean value) {
    return create(prefix, Collections.singletonMap(suffix, value));
  }

  public static Completion create(String suffix, boolean value) {
    return create("", suffix, value);
  }

  public static Completion create(String prefix, Map<String, Boolean> suffixes) {
    return new Completion(prefix, suffixes);
  }

  /** . */
  private final String prefix;

  /** . */
  private final Map<String, Boolean> values;

  private Completion(String prefix, Map<String, Boolean> values) {
    if (prefix == null) {
      throw new NullPointerException("No null prefix allowed");
    }
    if (values == null) {
      throw new NullPointerException("No null suffixes allowed");
    }

    //
    this.prefix = prefix;
    this.values = values;
  }

  public Iterator<Map.Entry<String, Boolean>> iterator() {
    return values.entrySet().iterator();
  }

  public Set<String> getValues() {
    return values.keySet();
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  public Boolean get(String key) {
    return values.get(key);
  }

  public int getSize() {
    return values.size();
  }

  public String getPrefix() {
    return prefix;
  }

  @Override
  public int hashCode() {
    return prefix.hashCode() ^ values.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Completion) {
      Completion that = (Completion)obj;
      return prefix.equals(that.prefix) && values.equals(that.values);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Completion[prefix=" + prefix + ",entries=" + values + "]";
  }

  public static class Builder {

    /** . */
    private String prefix;

    /** . */
    private Map<String, Boolean> entries;

    public Builder(String prefix) {
      this.prefix = prefix;
      this.entries = null;
    }

    public Builder add(String key, boolean value) {
      if (entries == null) {
        entries = new LinkedHashMap<String, Boolean>();
      }
      entries.put(key, value);
      return this;
    }

    public Completion build() {
      return create(prefix,  entries != null ? entries : Collections.<String, Boolean>emptyMap());
    }
  }
}

package org.crsh.cmdline.spi;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A completion result.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class ValueCompletion implements Iterable<Map.Entry<String, Boolean>> {

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

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
public final class CompletionResult<V> implements Iterable<Map.Entry<String, V>> {

  public static <V> CompletionResult<V> create() {
    return new CompletionResult<V>();
  }

  public static <V> CompletionResult<V> create(String prefix) {
    return new CompletionResult<V>(prefix);
  }

  public static <V> CompletionResult<V> create(String prefix, String suffix, V value) {
    CompletionResult<V> result = new CompletionResult<V>(prefix);
    result.put(suffix, value);
    return result;
  }

  public static <V> CompletionResult<V> create(String suffix, V value) {
    CompletionResult<V> result = new CompletionResult<V>();
    result.put(suffix, value);
    return result;
  }

  /** . */
  private final String prefix;

  /** . */
  private final Map<String, V> entries;

  public CompletionResult() {
    this("");
  }

  public CompletionResult(String prefix) {
    this(prefix, new LinkedHashMap<String, V>());
  }

  public CompletionResult(String prefix, Map<String, V> entries) {
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

  public Iterator<Map.Entry<String, V>> iterator() {
    return entries.entrySet().iterator();
  }

  public Set<String> getSuffixes() {
    return entries.keySet();
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public V get(String key) {
    return entries.get(key);
  }

  public int getSize() {
    return entries.size();
  }

  public CompletionResult<V> put(String key, V value) {
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
    if (obj instanceof CompletionResult) {
      CompletionResult that = (CompletionResult)obj;
      return prefix.equals(that.prefix) && entries.equals(that.entries);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Completion[prefix=" + prefix + ",entries=" + entries + "]";
  }
}

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

package org.crsh.util;

import javax.naming.Context;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

  /** . */
  private static final Iterator EMPTY_ITERATOR = Collections.emptyList().iterator();

  /** . */
  private static final Pattern p = Pattern.compile("\\S+");

  public static <E> Iterator<E> iterator() {
    @SuppressWarnings("unchecked")
    Iterator<E> iterator = (Iterator<E>)EMPTY_ITERATOR;
    return iterator;
  }

  public static <E> Iterator<E> iterator(final E element) {
    return new BaseIterator<E>() {
      boolean hasNext = true;
      @Override
      public boolean hasNext() {
        return hasNext;
      }
      @Override
      public E next() {
        if (hasNext) {
          hasNext = false;
          return element;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  public static <E> E first(Iterable<E> elements) {
    Iterator<E> i = elements.iterator();
    return i.hasNext() ? i.next() : null;
  }

  public static <K, V, M extends Map<K, V>> M map(M map, K key, V value) {
    map.put(key, value);
    return map;
  }

  public static <K, V> HashMap<K, V> map(K key, V value) {
    HashMap<K, V> map = new HashMap<K, V>();
    map.put(key, value);
    return map;
  }

  public static <E> HashSet<E> set(E... elements) {
    HashSet<E> set = new HashSet<E>(elements.length);
    Collections.addAll(set, elements);
    return set;
  }

  public static <E> List<E> list(E... elements) {
    return Arrays.asList(elements);
  }

  public static <E> List<E> list(Iterable<E> iterable) {
    return list(iterable.iterator());
  }

  public static <E> List<E> list(Iterator<E> iterator) {
    ArrayList<E> list = new ArrayList<E>();
    while (iterator.hasNext()) {
      list.add(iterator.next());
    }
    return list;
  }

  public static int indexOf(CharSequence s, int off, char c) {
    for (int len = s.length();off < len;off++) {
      if (s.charAt(off) == c) {
        return off;
      }
    }
    return -1;
  }

  public static String trimLeft(String s) {
    if (s == null) {
      throw new NullPointerException("No null string accepted");
    }
    int index = 0;
    int len = s.length();
    while (index < len) {
      if (s.charAt(index) == ' ') {
        index++;
      } else {
        break;
      }
    }
    if (index > 0) {
      return s.substring(index);
    } else {
      return s;
    }
  }

  private static final Pattern blankPattern = Pattern.compile("^\\s*$");

  public static boolean notBlank(String s) {
    return !blankPattern.matcher(s).find();
  }

  public static <E> E notNull(E e1, E e2) {
    if (e1 != null) {
      return e1;
    } else {
      return e2;
    }
  }

  /**
   * Return the value when it is positive otherwise return 0.
   *
   * @param value the value
   * @return the non negative value
   */
  public static int notNegative(int value) {
    return value >= 0 ? value : 0;
  }

  private static final Pattern FOO = Pattern.compile("" +
      "(\\*)" + // Wildcard *
      "|" +
      "(\\?)" + // Wildcard ?
      "|" +
      "(?:\\[([^)]+)\\])" + // Range
      "|" +
      "(\\\\.)" // Escape
  );

  /**
   * Create a pattern that transforms a glob expression into a regular expression, the following task are supported
   * <ul>
   *   <li>* : Match any number of unknown characters</li>
   *   <li>? : Match one unknown character</li>
   *   <li>[characters] : Match a character as part of a group of characters</li>
   *   <li>\ : Escape character</li>
   * </ul>
   *
   * @param globex the glob expression
   * @return the regular expression
   * @throws NullPointerException when the globex argument is null
   */
  public static String globexToRegex(String globex) throws NullPointerException {
    if (globex == null) {
      throw new NullPointerException("No null globex accepted");
    }
    StringBuilder regex = new StringBuilder();
    int prev = 0;
    Matcher matcher = FOO.matcher(globex);
    while (matcher.find()) {
      int next = matcher.start();
      if (next > prev) {
        regex.append(Pattern.quote(globex.substring(prev, next)));
      }
      if (matcher.group(1) != null) {
        regex.append(".*");
      } else if (matcher.group(2) != null) {
        regex.append(".");
      } else if (matcher.group(3) != null) {
        regex.append("[");
        regex.append(Pattern.quote(matcher.group(3)));
        regex.append("]");
      } else if (matcher.group(4) != null) {
        regex.append(Pattern.quote(Character.toString(matcher.group(4).charAt(1))));
      } else {
        throw new UnsupportedOperationException("Not handled yet");
      }
      prev = matcher.end();
    }
    if (prev < globex.length()) {
      regex.append(Pattern.quote(globex.substring(prev)));
    }
    return regex.toString();
  }

  /**
<<<<<<< HEAD
   * Close the socket and catch any exception thrown.
   *
   * @param socket the socket to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(Socket socket) {
    if (socket != null) {
      try {
        socket.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the closeable and catch any exception thrown.
   *
   * @param closeable the closeable to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the connection and catch any exception thrown.
   *
   * @param connection the socket to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the statement and catch any exception thrown.
   *
   * @param statement the statement to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(java.sql.Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the result set and catch any exception thrown.
   *
   * @param rs the result set to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the context and catch any exception thrown.
   *
   * @param context the context to close
   * @return any Exception thrown during the <code>close</code> operation
   */
   public static Exception close(Context context) {
      if (context != null) {
         try {
            context.close();
         }
         catch (Exception e) {
           return e;
         }
      }
     return null;
   }

  public static <T extends Throwable> void rethrow(Class<T> throwableClass, Throwable cause) throws T {
    T throwable;

    //
    try {
      throwable = throwableClass.newInstance();
    }
    catch (Exception e) {
      throw new AssertionError(e);
    }

    //
    throwable.initCause(cause);

    //
    throw throwable;
  }

  public static boolean equals(Object o1, Object o2) {
    return o1 == null ? o2 == null : (o2 != null && o1.equals(o2));
  }

  public static boolean notEquals(Object o1, Object o2) {
    return !equals(o1, o2);
  }

  /**
   * Flush the flushable and catch any exception thrown.
   *
   * @param flushable the flushable to flush
   * @return any Exception thrown during the <code>flush</code> operation
   */
  public static Exception flush(Flushable flushable) {
    if (flushable != null) {
      try {
        flushable.flush();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  public static List<String> chunks(CharSequence s) {
    List<String> chunks = new ArrayList<String>();
    Matcher m = p.matcher(s);
    while (m.find()) {
      chunks.add(m.group());
    }
    return chunks;
  }

  public static String join(Iterable<String> strings, String separator) {
    Iterator<String> i = strings.iterator();
    if (i.hasNext()) {
      String first = i.next();
      if (i.hasNext()) {
        StringBuilder buf = new StringBuilder();
        buf.append(first);
        while (i.hasNext()) {
          buf.append(separator);
          buf.append(i.next());
        }
        return buf.toString();
      } else {
        return first;
      }
    } else {
      return "";
    }
  }

  public static String[] split(CharSequence s, char separator) {
    return foo(s, separator, 0, 0, 0);
  }

  public static String[] split(CharSequence s, char separator, int rightPadding) {
    if (rightPadding < 0) {
      throw new IllegalArgumentException("Right padding cannot be negative");
    }
    return foo(s, separator, 0, 0, rightPadding);
  }

  private static String[] foo(CharSequence s, char separator, int count, int from, int rightPadding) {
    int len = s.length();
    if (from < len) {
      int to = from;
      while (to < len && s.charAt(to) != separator) {
        to++;
      }
      String[] ret;
      if (to == len - 1) {
        ret = new String[count + 2 + rightPadding];
        ret[count + 1] = "";
      }
      else {
        ret = to == len ? new String[count + 1 + rightPadding] : foo(s, separator, count + 1, to + 1, rightPadding);
      }
      ret[count] = from == to ? "" : s.subSequence(from, to).toString();
      return ret;
    }
    else if (from == len) {
      return new String[count + rightPadding];
    }
    else {
      throw new AssertionError();
    }
  }

  /**
   * @see #findLongestCommonPrefix(Iterable)
   */
  public static String findLongestCommonPrefix(CharSequence... seqs) {
    return findLongestCommonPrefix(Arrays.asList(seqs));
  }

  /**
   * Find the longest possible common prefix of the provided char sequence.
   *
   * @param seqs the sequences
   * @return the longest possible prefix
   */
  public static String findLongestCommonPrefix(Iterable<? extends CharSequence> seqs) {
    String common = "";
    out:
    while (true) {
      String candidate = null;
      for (CharSequence s : seqs) {
        if (common.length() + 1 > s.length()) {
          break out;
        } else {
          if (candidate == null) {
            candidate = s.subSequence(0, common.length() + 1).toString();
          } else if (s.subSequence(0, common.length() + 1).toString().equals(candidate)) {
            // Ok it is a prefix
          } else {
            break out;
          }
        }
      }
      if (candidate == null) {
        break;
      } else {
        common = candidate;
      }
    }
    return common;
  }

  public static byte[] readAsBytes(InputStream in) throws IOException {
    return read(in).toByteArray();
  }

  public static String readAsUTF8(InputStream in) {
    try {
      ByteArrayOutputStream baos = read(in);
      return baos.toString("UTF-8");
    }
    catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    if (in == null) {
      throw new NullPointerException();
    }
    try {
      byte[] buffer = new byte[256];
      for (int l = in.read(buffer); l != -1; l = in.read(buffer)) {
        out.write(buffer, 0, l);
      }
    }
    finally {
      close(in);
    }
  }

  private static ByteArrayOutputStream read(InputStream in) throws IOException {
    if (in == null) {
      throw new NullPointerException();
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    copy(in, baos);
    return baos;
  }

  /*
   * Convert an file URL to a file, avoids issues on windows with whitespaces.
   *
   * @param url the URL to convert
   * @return the related file
   * @throws java.lang.IllegalArgumentException if the url protocol is not file
   * @throws java.lang.NullPointerException if the url argument is null
   */
  public static File toFile(URL url) throws IllegalArgumentException, NullPointerException {
    if (url == null) {
      throw new NullPointerException("No null URL accepted");
    }
    if (!url.getProtocol().equals("file")) {
      throw new IllegalArgumentException("Not file protocol");
    }
    try {
      return new File(url.toURI());
    } catch(URISyntaxException e) {
      return new File(url.getPath());
    }
  }

  public static Class<?> resolveToClass(Type implementation, Class<?> type, int parameterIndex) {
    if (implementation == null) {
      throw new NullPointerException("No null type accepted");
    }

    // First resolve to type
    Type resolvedType = resolve(implementation, type, parameterIndex);

    //
    if (resolvedType != null) {
      return resolveToClass(resolvedType);
    } else {
      return null;
    }
  }

  public static Class resolveToClass(Type type) {
    if (type == null) {
      throw new NullPointerException("No null type accepted");
    }
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      return resolveToClass(parameterizedType.getRawType());
    } else if (type instanceof TypeVariable) {
      TypeVariable resolvedTypeVariable = (TypeVariable)type;
      return resolveToClass(resolvedTypeVariable.getBounds()[0]);
    } else {
      throw new UnsupportedOperationException("Type resolution of " + type + " not yet implemented");
    }
  }

  /**
   * A simplistic implementation, it may not handle all cases but it should handle enough.
   *
   * @param implementation the type for which the parameter requires a resolution
   * @param type the type that owns the parameter
   * @param parameterIndex the parameter index
   * @return the resolved type
   */
  public static Type resolve(Type implementation, Class<?> type, int parameterIndex) {
    if (implementation == null) {
      throw new NullPointerException();
    }

    //
    if (implementation == type) {
      TypeVariable<? extends Class<?>>[] tp = type.getTypeParameters();
      if (parameterIndex < tp.length) {
        return tp[parameterIndex];
      } else {
        throw new IllegalArgumentException();
      }
    } else if (implementation instanceof Class<?>) {
      Class<?> c = (Class<?>) implementation;
      Type gsc = c.getGenericSuperclass();
      Type resolved = null;
      if (gsc != null) {
        resolved = resolve(gsc, type, parameterIndex);
        if (resolved == null) {
          // Try with interface
        }
      }
      return resolved;
    } else if (implementation instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) implementation;
      Type[] typeArgs = pt.getActualTypeArguments();
      Type rawType = pt.getRawType();
      if (rawType == type) {
        return typeArgs[parameterIndex];
      } else if (rawType instanceof Class<?>) {
        Class<?> classRawType = (Class<?>)rawType;
        Type resolved = resolve(classRawType, type, parameterIndex);
        if (resolved == null) {
          return null;
        } else if (resolved instanceof TypeVariable) {
          TypeVariable resolvedTV = (TypeVariable)resolved;
          TypeVariable[] a = classRawType.getTypeParameters();
          for (int i = 0;i < a.length;i++) {
            if (a[i].equals(resolvedTV)) {
              return resolve(implementation, classRawType, i);
            }
          }
          throw new AssertionError();
        } else {
          throw new UnsupportedOperationException("Cannot support resolution of " + resolved);
        }
      } else {
        throw new UnsupportedOperationException();
      }
    } else {
      throw new UnsupportedOperationException("todo " + implementation + " " + implementation.getClass());
    }
  }

  public static boolean instanceOf(Class c, List<String> types) {

    for (String type: types) {
      if (instanceOf(c, type)) {
        return true;
      }
    }

    return false;

  }

  public static boolean instanceOf(Class c, String type) {

    if (c.getName().equals(type)) {
      return true;
    }

    for (Class i : c.getInterfaces()) {
      if (instanceOf(i, type)) {
        return true;
      }
    }

    if (c.getSuperclass() != null) {
      return instanceOf(c.getSuperclass(), type);
    }

    return false;
  }

  /** . */
  private static final int ESCAPE = -1, SCANNING = 0, DOLLAR = 1, EVALUATING = 2;

  /**
   * Interpolate a string and replace the occurence from a context map, the syntax for a variable
   * is <code>${}</code> and it can accept a default value used when the variable cannot be resolved
   * with the <code>:-</code> separator:
   *
   * <ul>
   *   <li><code>{}</code> + "foo" => "foo"</li>
   *   <li><code>{}</code> + "${foo}" => ""</li>
   *   <li><code>{}</code> + "\\${foo}" => "${foo}"</li>
   *   <li><code>{foo:bar}</code> + "${foo}" => "bar"</li>
   *   <li><code>{}</code> + "${foo:-bar}" => "bar"</li>
   * </ul>
   *
   * @param interpolated the value to interpolate
   * @param context the context
   * @return the interpolated string
   * @throws NullPointerException if the interpolated argument is null
   */
  public static String interpolate(String interpolated, Map<?, ?> context) throws NullPointerException {
    StringBuilder sb = new StringBuilder();
    int status = 0;
    int prev = 0;
    int length = interpolated.length();
    for (int i = 0;i < length;i++) {
      char c = interpolated.charAt(i);
      switch (status) {
        case ESCAPE:
          if (c == '$') {
            sb.append('$');
          } else {
            sb.append('\\').append(c);
          }
          status = SCANNING;
          break;
        case SCANNING:
          if (c == '$') {
            status = DOLLAR;
          } else if (c == '\\') {
            status = ESCAPE;
          } else {
            sb.append(c);
          }
          break;
        case DOLLAR:
          if (c == '{') {
            status = EVALUATING;
            prev = i + 1;
          } else {
            sb.append('$').append(c);
          }
          break;
        case EVALUATING:
          if (c == '}') {
            int j = prev + 1;
            while (j < i) {
              if (j < length && interpolated.charAt(j - 1) == ':' && interpolated.charAt(j) == '-') {
                break;
              } else {
                j++;
              }
            }
            Object value;
            if (j < i) {
              String key = interpolated.substring(prev, j - 1);
              value = context.get(key);
              if (value == null) {
                value = interpolated.substring(j + 1, i);
              }
            } else {
              String key = interpolated.substring(prev, i);
              value = context.get(key);
            }
            if (value != null) {
              sb.append(value);
            }
            status = SCANNING;
          }
          break;
      }
    }
    switch (status) {
      case DOLLAR:
        sb.append('$');
        break;
      case EVALUATING:
        sb.append("${").append(interpolated, prev, interpolated.length());
        break;
    }
    return sb.toString();
  }

  /**
   * @return the current user directory
   */
  public static File getCurrentDirectory() {
    String userDir = System.getProperty("user.dir");
    return new File(userDir);
  }

  /** . */
  public static final Charset UTF_8;

  static {
    UTF_8 = Charset.forName("UTF-8");
  }

}

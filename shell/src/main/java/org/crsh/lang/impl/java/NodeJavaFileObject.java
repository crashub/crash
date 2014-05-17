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

package org.crsh.lang.impl.java;

import org.crsh.util.InputStreamFactory;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

/** @author Julien Viet */
class NodeJavaFileObject implements JavaFileObject {

  /** . */
  final String binaryName;

  /** . */
  private final URI uri;

  /** . */
  private final String name;

  /** . */
  private final long lastModified;

  /** . */
  private final InputStreamFactory stream;

  public NodeJavaFileObject(String binaryName, URI uri, InputStreamFactory stream, long lastModified) {
    this.uri = uri;
    this.binaryName = binaryName;
    this.name = uri.getPath() == null ? uri.getSchemeSpecificPart() : uri.getPath();
    this.lastModified = lastModified;
    this.stream = stream;
  }

  public URI toUri() {
    return uri;
  }

  public String getName() {
    return name;
  }

  public long getLastModified() {
    return lastModified;
  }

  public InputStream openInputStream() throws IOException {
    return stream.open();
  }

  public OutputStream openOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    throw new UnsupportedOperationException();
  }

  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    throw new UnsupportedOperationException();
  }

  public Writer openWriter() throws IOException {
    throw new UnsupportedOperationException();
  }

  public boolean delete() {
    return false;
  }

  public Kind getKind() {
    return Kind.CLASS;
  }

  public boolean isNameCompatible(String simpleName, Kind kind) {
    String baseName = simpleName + kind.extension;
    return kind.equals(getKind())
        && (baseName.equals(getName())
        || getName().endsWith("/" + baseName));
  }

  public NestingKind getNestingKind() {
    return null;
  }

  public Modifier getAccessLevel() {
    return null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[uri=" + uri + "]";
  }
}
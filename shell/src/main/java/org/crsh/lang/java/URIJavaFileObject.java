package org.crsh.lang.java;

import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.lang.model.element.Modifier;
import java.net.URI;

/** @author Julien Viet */
class URIJavaFileObject implements JavaFileObject {

  /** . */
  final String binaryName;

  /** . */
  private final URI uri;

  /** . */
  private final String name;

  /** . */
  private final long lastModified;

  public URIJavaFileObject(String binaryName, URI uri, long lastModified) {
    this.uri = uri;
    this.binaryName = binaryName;
    this.name = uri.getPath() == null ? uri.getSchemeSpecificPart() : uri.getPath();
    this.lastModified = lastModified;
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
    return uri.toURL().openStream();
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
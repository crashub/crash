package org.crsh.term;

import org.crsh.text.CharReader;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AbstractTerm implements Term {

  public int getWidth() {
    throw new UnsupportedOperationException();
  }

  public String getProperty(String name) {
    throw new UnsupportedOperationException();
  }

  public void setEcho(boolean echo) {
    throw new UnsupportedOperationException();
  }

  public TermEvent read() throws IOException {
    throw new UnsupportedOperationException();
  }

  public void write(CharReader reader) throws IOException {
    throw new UnsupportedOperationException();
  }

  public Appendable getInsertBuffer() {
    throw new UnsupportedOperationException();
  }

  public CharSequence getBuffer() {
    throw new UnsupportedOperationException();
  }

  public void addToHistory(CharSequence line) {
    throw new UnsupportedOperationException();
  }

  public void close() {
    throw new UnsupportedOperationException();
  }
}

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

package org.crsh.term;

import org.crsh.text.Chunk;

import java.io.IOException;

public class AbstractTerm implements Term {

  public int getWidth() {
    throw new UnsupportedOperationException();
  }

  public int getHeight() {
    throw new UnsupportedOperationException();
  }

  public String getProperty(String name) {
    throw new UnsupportedOperationException();
  }

  public void setEcho(boolean echo) {
    throw new UnsupportedOperationException();
  }

  public boolean takeAlternateBuffer() throws IOException {
    throw new UnsupportedOperationException();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    throw new UnsupportedOperationException();
  }

  public TermEvent read() throws IOException {
    throw new UnsupportedOperationException();
  }

  public Class<Chunk> getConsumedType() {
    return Chunk.class;
  }

  public void provide(Chunk element) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void write(Chunk chunk) throws IOException {
    provide(chunk);
  }

  public Appendable getDirectBuffer() {
    throw new UnsupportedOperationException();
  }

  public CharSequence getBuffer() {
    throw new UnsupportedOperationException();
  }

  public void addToHistory(CharSequence line) {
    throw new UnsupportedOperationException();
  }

  public void flush() {
    throw new UnsupportedOperationException();
  }

  public void close() {
    throw new UnsupportedOperationException();
  }
}

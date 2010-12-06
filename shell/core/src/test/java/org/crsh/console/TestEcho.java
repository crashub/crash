/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.console;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestEcho extends ClientOutput {

  /** . */
  private final StringBuilder builder = new StringBuilder();

  /** . */
  private final StringBuilder line = new StringBuilder();

  /** . */
  private int position = 0;

  /** . */
  private final boolean supportsCursorMove;

  public TestEcho(boolean supportsCursorMove) {
    this.supportsCursorMove = supportsCursorMove;
  }

  protected void doEchoCRLF() throws IOException {
    builder.append(line.toString());
    line.setLength(0);
    position = 0;
  }

  @Override
  protected void write(CharSequence s) throws IOException {
    line.insert(position++, s);
  }

  @Override
  protected void write(char c) throws IOException {
    line.insert(position++, c);
  }

  @Override
  protected void writeDel() throws IOException {
    line.deleteCharAt(--position);
  }

  @Override
  protected boolean writeMoveRight() {
    if (supportsCursorMove) {
      position++;
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void writeCRLF() throws IOException {
    builder.append(line.toString());
    line.setLength(0);
    position = 0;
  }

  @Override
  protected boolean writeMoveLeft() {
    if (supportsCursorMove) {
      position--;
      return true;
    } else {
      return false;
    }
  }
}

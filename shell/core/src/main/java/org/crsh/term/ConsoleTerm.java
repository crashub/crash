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

package org.crsh.term;

import java.io.Console;
import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ConsoleTerm implements Term {

  /** . */
  private final Console console;

  /** . */
  private boolean echo;

  public ConsoleTerm() {
    this.console = System.console();
    this.echo = true;
  }

  public void setEcho(boolean echo) {
    this.echo = echo;
  }

  public int getWidth() {
    return 10;
  }

  public TermEvent read() throws IOException {
    if (echo) {
      String line = console.readLine();
      return new TermEvent.ReadLine(line);
    } else {
      String line = new String(console.readPassword());
      return new TermEvent.ReadLine(line);
    }
  }

  public void write(CharSequence msg) throws IOException {
    console.writer().print(msg);
    console.flush();
  }

  public CharSequence getBuffer() {
    // Not supported
    return "";
  }

  public void bufferInsert(CharSequence msg) throws IOException {
    // Not supported
  }

  public void addToHistory(CharSequence line) {
    // Not supported
  }

  public void close() {
    // Not supported
  }
}

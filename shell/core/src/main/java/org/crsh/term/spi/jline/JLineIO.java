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

package org.crsh.term.spi.jline;

import jline.ConsoleReader;
import org.crsh.term.CodeType;
import org.crsh.term.spi.TermIO;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JLineIO implements TermIO {

  /** . */
  private final ConsoleReader reader;

  /** . */
  private final short[] keyBindings;

  public JLineIO() throws Exception {
    ConsoleReader reader = new ConsoleReader();
    Method method = ConsoleReader.class.getDeclaredMethod("getKeyForAction", short.class);
    method.setAccessible(true);


    Field f = ConsoleReader.class.getDeclaredField("keybindings");
    f.setAccessible(true);
    short[] keyBindings = (short[])f.get(reader);


    //
    this.reader = reader;
    this.keyBindings = keyBindings;
  }

  public int read() throws IOException {
    int i = reader.readVirtualKey();
    return i;
  }

  public CodeType decode(int code) {
    short action = keyBindings[code];
    switch (action) {
      case ConsoleReader.COMPLETE:
        return CodeType.TAB;
      case ConsoleReader.DELETE_PREV_CHAR:
        return CodeType.DELETE;
      case ConsoleReader.PREV_CHAR:
        return CodeType.LEFT;
      case ConsoleReader.NEXT_CHAR:
        return CodeType.RIGHT;
      case ConsoleReader.EXIT:
        return CodeType.CLOSE;
      default:
        return CodeType.CHAR;
    }
  }

  public void close() {

  }

  public void flush() throws IOException {

  }

  public void write(String s) throws IOException {
    System.out.print(s);
  }

  public void write(char c) throws IOException {
    System.out.print(c);
  }

  public void writeDel() throws IOException {
    System.out.print("\b \b");
  }

  public void writeCRLF() throws IOException {
    System.out.println();
  }

  public boolean moveRight(char c) throws IOException {
    System.out.print(c);
    return true;
  }

  public boolean moveLeft() throws IOException {
    System.out.print("\b");
    return true;
  }
}

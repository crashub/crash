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

package org.crsh.lang.script;

abstract class Token {

  public abstract String toString();

  public static Token EOF = new Token(){
    @Override
    public String toString() {
      return "EOF";
    }
  };

  public static Token PIPE = new Token(){
    @Override
    public String toString() {
      return "PIPE";
    }
  };

  public static class Command extends Token {

    /** . */
    final String line;

    public Command(String line) {
      this.line = line;
    }

    @Override
    public String toString() {
      return "Command[" + line + "]";
    }
  }
}

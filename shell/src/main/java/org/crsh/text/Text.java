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

package org.crsh.text;

/**
 * A textual chunk.
 */
public class Text implements Chunk {

  /**
   * Create a new text chunk wrapping the provided sequence.
   *
   * @param s the sequence
   * @return the created text
   * @throws NullPointerException if the sequence is null
   */
  public static Text create(CharSequence s) throws NullPointerException {
    if (s == null) {
      throw new NullPointerException("No null sequence accepted");
    }
    return new Text(s);
  }

  /** . */
  final CharSequence value;

  private Text(CharSequence s) {
    this.value = s;
  }

  public CharSequence getText() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this || obj instanceof Text && value.toString().equals(((Text)obj).value.toString());
  }

  @Override
  public String toString() {
    return "Text[" + value + "]";
  }
}

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

public enum Decoration {

  /** . */
  bold(0, true, "1"),

  /** . */
  bold_off(0, false, "22"),

  /** . */
  underline(1, true, "4"),

  /** . */
  underline_off(1, false, "24"),

  /** . */
  blink(2, true, "5"),

  /** . */
  blink_off(2, false, "25");

  /** . */
  final int index;

  /** . */
  final boolean on;

  /** . */
  public final String code;

  private Decoration(int index, boolean on, String code) {
    this.index = index;
    this.on = on;
    this.code = code;
  }

  public Style.Composite fg(Color value) {
    return foreground(value);
  }

  public Style.Composite foreground(Color value) {
    return Style.style(this).foreground(value);
  }

  public Style.Composite bg(Color value) {
    return background(value);
  }

  public Style.Composite background(Color value) {
    return Style.style(this).background(value);
  }

  public Style.Composite bold() {
    return bold(true);
  }

  public Style.Composite underline() {
    return underline(true);
  }

  public Style.Composite blink() {
    return blink(true);
  }

  public Style.Composite bold(Boolean value) {
    return Style.style(this).bold(value);
  }

  public Style.Composite underline(Boolean value) {
    return Style.style(this).underline(value);
  }

  public Style.Composite blink(Boolean value) {
    return Style.style(this).blink(value);
  }
}

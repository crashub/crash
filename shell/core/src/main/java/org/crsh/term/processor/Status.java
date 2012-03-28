/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 *
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

package org.crsh.term.processor;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
final class Status {

  /** . */
  private final State state;

  /** . */
  private final boolean available;

  Status(State state, boolean available) {
    this.state = state;
    this.available = available;
  }

  public State getState() {
    return state;
  }

  public boolean isAvailable() {
    return available;
  }

  public boolean isBusy() {
    return !available;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o instanceof Status) {
      Status that = (Status)o;
      return state == that.state && available == that.available;
    } else {
      return false;
    }
  }
}

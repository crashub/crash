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

import java.io.Flushable;
import java.io.IOException;

/**
 * Defines how to send data to a screen.
 *
 * @author Julien Viet
 */
public interface ScreenAppendable extends Appendable, Flushable {

  Appendable append(char c) throws IOException;

  Appendable append(CharSequence s) throws IOException;

  Appendable append(CharSequence csq, int start, int end) throws IOException;

  ScreenAppendable append(Style style) throws IOException;

  ScreenAppendable cls() throws IOException;

}

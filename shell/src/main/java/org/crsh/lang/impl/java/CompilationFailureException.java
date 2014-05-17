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
package org.crsh.lang.impl.java;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;

/** @author Julien Viet */
public class CompilationFailureException extends Exception {

  /** . */
  private final List<Diagnostic<? extends JavaFileObject>> errors;

  public CompilationFailureException(List<Diagnostic<? extends JavaFileObject>> errors) {
    this.errors = errors;
  }

  public List<Diagnostic<? extends JavaFileObject>> getErrors() {
    return errors;
  }

  @Override
  public String getMessage() {
    StringBuilder message = new StringBuilder();
    for (Diagnostic<? extends JavaFileObject> error : errors) {
      message.append(error.getMessage(null));
      message.append("\n");
    }
    return message.toString();
  }
}

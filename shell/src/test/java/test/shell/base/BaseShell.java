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
package test.shell.base;

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.spi.Completion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;

import java.io.IOException;

public class BaseShell implements Shell {

  /** . */
  private BaseProcessFactory factory;

  public BaseShell() {
    this(BaseProcessFactory.NOOP);
  }

  public BaseShell(BaseProcessFactory factory) {
    this.factory = factory;
  }

  public BaseProcessFactory getFactory() {
    return factory;
  }

  public void setFactory(BaseProcessFactory factory) {
    if (factory == null) {
      throw new NullPointerException();
    }
    this.factory = factory;
  }

  /**
   * Returns the empty string.
   *
   * @return the empty string
   */
  public String getWelcome() {
    return "";
  }

  /**
   * Returns the <code>% </code> string.
   *
   * @return the <code>% </code> string
   */
  public String getPrompt() {
    return "% ";
  }

  public ShellProcess createProcess(String request) {
    return factory.create(request);
  }

  /**
   * Returns an empty unmodifiable map.
   */
  public CompletionMatch complete(String prefix) {
    return new CompletionMatch(Delimiter.EMPTY, Completion.create());
  }

  public void close() throws IOException {
  }
}

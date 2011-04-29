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

package org.crsh.shell;

import org.crsh.command.InvocationContext;
import org.crsh.command.CommandInvoker;
import org.crsh.command.ShellCommand;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.LineFeedWriter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestInvocationContext<C, P> implements InvocationContext<C, P> {

  /** . */
  private StringWriter buffer;

  /** . */
  private ShellPrinter writer;

  /** . */
  private LinkedList<P> products;

  /** . */
  private Map<String, Object> attributes;

  public Map<String, Object> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<String, Object>();
    }
    return attributes;
  }

  public int getWidth() {
    return 32;
  }

  public String readLine(String msg, boolean echo) {
    throw new UnsupportedOperationException();
  }

  public ShellPrinter getWriter() {
    if (writer == null) {
      writer = new ShellPrinter(new LineFeedWriter(buffer = new StringWriter(), "\r\n"));
    }
    return writer;
  }

  public Iterable<C> consume() {
    throw new IllegalStateException();
  }

  public boolean isPiped() {
    return false;
  }

  public void produce(P product) {
    if (products == null) {
      products = new LinkedList<P>();
    }
    products.add(product);
  }

  public List<P> getProducts() {
    return products;
  }

  public String execute(ShellCommand command, String... args) throws Exception {
    if (buffer != null) {
      buffer.getBuffer().setLength(0);
    }
    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(arg);
    }
    CommandInvoker<C, P> invoker = (CommandInvoker<C, P>)command.createInvoker(sb.toString());
    invoker.invoke(this);
    return buffer != null ? buffer.toString() : null;
  }
}

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

package org.crsh;

import junit.framework.Assert;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BaseProcessContext implements ShellProcessContext {

  public static BaseProcessContext create(Shell shell, String line) {
    return new BaseProcessContext(shell, line);
  }

  public static BaseProcessContext create(ShellProcess process) {
    return new BaseProcessContext(process);
  }

  /** . */
  private final LinkedList<String> output = new LinkedList<String>();

  /** . */
  private final LinkedList<String> input = new LinkedList<String>();

  /** . */
  private ShellResponse response;

  /** . */
  private final CountDownLatch latch;

  /** . */
  private int width;

  /** . */
  private ShellProcess process;

  private BaseProcessContext(ShellProcess process) {
    this.process = process;
    this.latch = new CountDownLatch(1);
    this.response = null;
    this.width = 32;
  }

  private BaseProcessContext(Shell shell, String line) {
    this(shell.createProcess(line));
  }

  public BaseProcessContext cancel() {
    process.cancel();
    return this;
  }

  public BaseProcessContext execute() {
    process.execute(this);
    return this;
  }

  public ShellProcess getProcess() {
    return process;
  }

  public BaseProcessContext addLineInput(String line) {
    input.add(line);
    return this;
  }

  public BaseProcessContext assertLineOutput(String expected) {
    Assert.assertTrue(output.size() > 0);
    String test = output.removeFirst();
    Assert.assertEquals(expected,  test);
    return this;
  }

  public BaseProcessContext assertNoOutput() {
    Assert.assertEquals(0, output.size());
    return this;
  }

  public BaseProcessContext assertNoInput() {
    Assert.assertEquals(0, input.size());
    return this;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public String getProperty(String name) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    output.addLast(msg);
    return input.isEmpty() ? null : input.removeLast();
  }

  public void end(ShellResponse response) {
    this.response = response;
    this.latch.countDown();
  }

  public ShellResponse getResponse() {
    try {
      latch.await(3, TimeUnit.SECONDS);
      return response;
    }
    catch (InterruptedException e) {
      throw AbstractTestCase.failure(e);
    }
  }
}

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

import junit.framework.Assert;
import org.crsh.AbstractTestCase;
import org.crsh.keyboard.KeyHandler;
import org.crsh.keyboard.KeyType;
import org.crsh.text.Screenable;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
  private int height;

  /** . */
  private ShellProcess process;

  /** . */
  private volatile boolean alternateBuffer = false;

  private BaseProcessContext(ShellProcess process) {
    this.process = process;
    this.latch = new CountDownLatch(1);
    this.response = null;
    this.width = 32;
    this.height = 40;
    this.alternateBuffer = false;
  }

  private BaseProcessContext(Shell shell, String line) {
    this(shell.createProcess(line));
  }

  public boolean getAlternateBuffer() {
    return alternateBuffer;
  }

  public BaseProcessContext cancel() {
    process.cancel();
    return this;
  }

  public BaseProcessContext on(KeyType type, int[] sequence) {
    KeyHandler handler = process.getKeyHandler();
    if (handler != null) {
      handler.handle(type, sequence);
    }
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

  public boolean takeAlternateBuffer() {
    alternateBuffer = true;
    return true;
  }

  public boolean releaseAlternateBuffer() {
    alternateBuffer = false;
    return true;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public String getProperty(String name) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    output.addLast(msg);
    return input.isEmpty() ? null : input.removeLast();
  }

  public Screenable append(CharSequence s) throws IOException {
    if (s.length() > 0) {
      output.add(s.toString());
    }
    return this;
  }

  public Screenable append(char c) throws IOException {
    return append(Character.toString(c));
  }

  public Screenable append(CharSequence csq, int start, int end) throws IOException {
    return append(csq.subSequence(start, end));
  }

  public Screenable append(Style style) throws IOException {
    return this;
  }

  public Screenable cls() throws IOException {
    return this;
  }

  public String getOutput() {
    StringBuilder buffer = new StringBuilder();
    for (String o : output) {
      buffer.append(o);
    }
    return buffer.toString();
  }

  public void flush() {
  }

  public void end(ShellResponse response) {
    this.response = response;
    this.latch.countDown();
  }

  public ShellResponse getResponse() {
    try {
      latch.await(60, TimeUnit.SECONDS);
      return response;
    }
    catch (InterruptedException e) {
      throw AbstractTestCase.failure(e);
    }
  }
}

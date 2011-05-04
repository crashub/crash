/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import groovy.lang.GroovyShell;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.TestPluginContext;
import org.crsh.shell.impl.CRaSH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractCommandTestCase extends TestCase {

  /** . */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  protected CRaSH shell;

  /** . */
  protected GroovyShell groovyShell;

  protected AbstractCommandTestCase() {
  }

  protected AbstractCommandTestCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //
    ShellFactory builder = new ShellFactory(new TestPluginContext());

    //
    shell = builder.build();
    groovyShell = shell.getGroovyShell();
  }

  @Override
  protected void tearDown() throws Exception {
    if (shell != null) {
      shell.close();
      shell = null;
      groovyShell = null;
    }
  }

  protected final ShellResponse evaluate(String s) {
    final AtomicReference<ShellResponse> resp = new AtomicReference<ShellResponse>();
    ShellProcessContext ctx = new ShellProcessContext() {
      public int getWidth() {
        return 32;
      }
      public void begin(ShellProcess process) {
      }
      public String readLine(String msg, boolean echo) {
        throw new UnsupportedOperationException("The command does not have access to console line reading");
      }
      public void end(ShellResponse response) {
        resp.set(response);
      }
    };
    shell.process(s, ctx);
    return resp.get();
  }

  protected final void assertUnknownCommand(String s) {
    ShellResponse resp = evaluate(s);
    assertTrue("Was expecting an ok response instead of " + resp, resp instanceof ShellResponse.UnknownCommand);
    assertEquals(s, ((ShellResponse.UnknownCommand)resp).getName());
  }

  protected final void assertError(String s, Class<? extends Throwable> expectedErrorType) {
    Throwable error = assertError(s);
    if (!expectedErrorType.isInstance(error)) {
      fail("Expected error " + error + " to be of type " + expectedErrorType.getName());
    }
  }

  protected final Throwable assertError(String s) {
    ShellResponse resp = evaluate(s);
    assertTrue("Was expecting an ok response instead of " + resp, resp instanceof ShellResponse.Error);
    return ((ShellResponse.Error)resp).getThrowable();
  }

  protected final ShellResponse.Display assertOk(String expected, String s) {
    ShellResponse.Ok ok = assertOk(s);
    assertTrue("Was not expecting response to be " + ok, ok instanceof ShellResponse.Display);
    ShellResponse.Display display = (ShellResponse.Display)ok;
    assertEquals(expected, display.getText());
    return display;
  }

  protected final ShellResponse.Ok assertOk(String s) {
    ShellResponse resp = evaluate(s);
    if (resp instanceof ShellResponse.Ok) {
      return (ShellResponse.Ok)resp;
    }
    else if (resp instanceof ShellResponse.Error) {
      ShellResponse.Error err = (ShellResponse.Error)resp;
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(err.getThrowable());
      throw afe;
    }
    else {
      throw new AssertionFailedError("Was expecting an ok response instead of " + resp);
    }
  }
}
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
import org.crsh.AbstractTestCase;
import org.crsh.BaseProcessContext;
import org.crsh.TestPluginLifeCycle;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.impl.command.CRaSHSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractCommandTestCase extends AbstractTestCase {

  /** . */
  private final CRaSHPlugin[] NO_PLUGINS = new CRaSHPlugin[0];

  /** . */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  protected CRaSHSession shell;

  /** . */
  protected GroovyShell groovyShell;

  /** . */
  protected TestPluginLifeCycle lifeCycle;

  protected AbstractCommandTestCase() {
  }

  protected AbstractCommandTestCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //
    TestPluginLifeCycle lifeCycle = new TestPluginLifeCycle(NO_PLUGINS);

    //
    lifeCycle.start();

    //
    this.shell = lifeCycle.createShell();
    this.groovyShell = shell.getGroovyShell();
    this.lifeCycle = lifeCycle;
  }

  @Override
  protected void tearDown() throws Exception {
    if (shell != null) {
      shell.close();
      shell = null;
      groovyShell = null;
    }
  }

  protected final BaseProcessContext create(String s) {
    return BaseProcessContext.create(shell, s);
  }

  protected final BaseProcessContext execute(String s) {
    return create(s).execute();
  }

  protected final ShellResponse evaluate(String s) {
    return execute(s).getResponse();
  }

  protected final void assertUnknownCommand(String s) {
    ShellResponse resp = evaluate(s);
    assertTrue("Was expecting an ok response instead of " + resp, resp instanceof ShellResponse.UnknownCommand);
    assertEquals(s, ((ShellResponse.UnknownCommand)resp).getName());
  }

  protected final void assertEvalError(String s, Class<? extends Throwable> expectedThrowableType) {
    Throwable error = assertEvalError(s);
    assertType(expectedThrowableType, error);
  }

  protected final Throwable assertEvalError(String s) {
    return assertError(s, ErrorType.EVALUATION);
  }

  protected final void assertInternalError(String s, Class<? extends Throwable> expectedThrowableType) {
    Throwable error = assertInternalError(s);
    assertType(expectedThrowableType, error);
  }

  protected final Throwable assertInternalError(String s) {
    return assertError(s, ErrorType.INTERNAL);
  }

  protected final void assertError(String s, ErrorType expectedErrorType, Class<? extends Throwable> expectedThrowableType) {
    Throwable error = assertError(s, expectedErrorType);
    assertType(expectedThrowableType, error);
  }

  protected final Throwable assertError(String s, ErrorType expectedErrorType) {
    ShellResponse resp = evaluate(s);
    ShellResponse.Error error = assertInstance(ShellResponse.Error.class, resp);
    assertEquals(expectedErrorType, error.getType());
    return error.getThrowable();
  }

  protected final ShellResponse.Display assertDisplay(String expected, String s) {
    ShellResponse.Ok ok = assertOk(s);
    assertTrue("Was not expecting response to be " + ok, ok instanceof ShellResponse.Display);
    assertEquals(expected, ok.getReader().toString());
    return (ShellResponse.Display)ok;
  }

  protected final ShellResponse.Ok assertOk(String expected, String s) {
    ShellResponse.Ok ok = assertOk(s);
    assertEquals(expected, ok.getReader().toString());
    return ok;
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
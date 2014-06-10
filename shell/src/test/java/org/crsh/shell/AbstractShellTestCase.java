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

package org.crsh.shell;

import groovy.lang.GroovyShell;
import junit.framework.AssertionFailedError;
import org.crsh.AbstractTestCase;
import test.shell.base.BaseProcessContext;
import test.plugin.TestPluginLifeCycle;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.lang.impl.groovy.GroovyCompiler;
import org.crsh.lang.impl.groovy.GroovyLanguageProxy;
import org.crsh.lang.impl.java.JavaLanguage;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.impl.command.ShellSession;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractShellTestCase extends AbstractTestCase {

  /** . */
  protected final Logger log = Logger.getLogger(getClass().getName());

  /** . */
  protected Shell shell;

  /** . */
  protected ShellSession session;

  /** . */
  protected GroovyShell groovyShell;

  /** . */
  protected TestPluginLifeCycle lifeCycle;

  protected AbstractShellTestCase() {
  }

  protected AbstractShellTestCase(String name) {
    super(name);
  }

  protected List<CRaSHPlugin<?>> getPlugins() {
    ArrayList<CRaSHPlugin<?>> plugins = new ArrayList<CRaSHPlugin<?>>();
    plugins.add(new GroovyLanguageProxy());
    plugins.add(new JavaLanguage());
    return plugins;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //
    List<CRaSHPlugin<?>> plugins = getPlugins();
    TestPluginLifeCycle lifeCycle = new TestPluginLifeCycle(plugins.toArray(new CRaSHPlugin[plugins.size()]));

    //
    lifeCycle.start();

    //
    this.shell = lifeCycle.createShell();
    this.session = (ShellSession)shell; // A bit nasty but will do for tests :-)
    this.groovyShell = GroovyCompiler.getGroovyShell((ShellSession)shell);
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
    return assertError(s, ErrorKind.EVALUATION);
  }

  protected final void assertInternalError(String s, Class<? extends Throwable> expectedThrowableType) {
    Throwable error = assertInternalError(s);
    assertType(expectedThrowableType, error);
  }

  protected final Throwable assertInternalError(String s) {
    return assertError(s, ErrorKind.INTERNAL);
  }

  protected final void assertError(String s, ErrorKind expectedErrorType, Class<? extends Throwable> expectedThrowableType) {
    Throwable error = assertError(s, expectedErrorType);
    assertInstance(expectedThrowableType, error);
  }

  protected final Throwable assertError(String s, ErrorKind expectedErrorType) {
    ShellResponse resp = evaluate(s);
    ShellResponse.Error error = assertInstance(ShellResponse.Error.class, resp);
    assertEquals(expectedErrorType, error.getKind());
    return error.getThrowable();
  }

  protected final String evalOk(String s) {
    return assertOk("evaluate \"" + s.replaceAll("\"", "\\\"") + "\"");
  }

  protected final CompletionMatch assertComplete(String prefix) {
    return shell.complete(prefix);
  }

  protected final String assertOk(String s) {
    BaseProcessContext ctx = execute(s);
    ShellResponse resp = ctx.getResponse();
    if (resp instanceof ShellResponse.Ok) {
      return ctx.getOutput();
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

  protected final <R extends ShellResponse> R assertResponse(Class<R> expectedResponse, String s) {
    BaseProcessContext ctx = execute(s);
    ShellResponse resp = ctx.getResponse();
    if (expectedResponse.isInstance(resp)) {
      return expectedResponse.cast(resp);
    }
    else if (resp instanceof ShellResponse.Error) {
      ShellResponse.Error err = (ShellResponse.Error)resp;
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(err.getThrowable());
      throw afe;
    }
    else {
      throw new AssertionFailedError("Was expecting an " + expectedResponse.getSimpleName() + " response instead of " + resp);
    }
  }
}
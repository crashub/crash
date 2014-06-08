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
package org.crsh.lang.impl.script;

import org.crsh.shell.AbstractShellTestCase;
import org.crsh.shell.ErrorKind;

/**
 * @author Julien Viet
 */
public class ScriptTestCase extends AbstractShellTestCase {

  public void testBye() {
    lifeCycle.bindScript("test", "bye");
    Throwable t = assertError("test", ErrorKind.EVALUATION);
    assertEquals(Exception.class, t.getClass());
    assertEquals("Was not expecting response UnknownCommand[bye]", t.getMessage());
  }

  public void testCancel() {
    // fail("Implement me");
  }

  public void testExecute() {
    lifeCycle.bindScript("test",
        "echo abc\n" +
            "echo def");
    assertEquals("abcdef", assertOk("test"));
  }

  public void testException() {
    lifeCycle.bindScript("test", "fail this_is_an_exception");
    Throwable t = assertError("test", ErrorKind.EVALUATION);
    assertEquals(Exception.class, t.getClass());
    assertEquals("this_is_an_exception", t.getMessage());
  }

  public void testRuntimeException() {
    lifeCycle.bindScript("test", "fail -t RUNTIME this_is_a_runtime_exception");
    Throwable t = assertError("test", ErrorKind.EVALUATION);
    assertEquals(RuntimeException.class, t.getClass());
    assertEquals("this_is_a_runtime_exception", t.getMessage());
  }

  public void testError() {
    lifeCycle.bindScript("test", "fail -t ERROR this_is_an_error");
    Throwable t = assertError("test", ErrorKind.INTERNAL);
    assertEquals(Error.class, t.getClass());
    assertEquals("this_is_an_error", t.getMessage());
  }
}

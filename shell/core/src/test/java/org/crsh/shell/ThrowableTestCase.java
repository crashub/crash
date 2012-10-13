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

import org.crsh.command.ScriptException;

import javax.management.JMException;
import java.awt.*;
import java.util.EmptyStackException;

public class ThrowableTestCase extends AbstractCommandTestCase {

  public void testThrowScript() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() throws org.crsh.command.ScriptException {\n" +
        "throw new org.crsh.command.ScriptException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", ScriptException.class);
  }

  public void testThrowGroovyScript() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() throws groovy.util.ScriptException {\n" +
        "throw new groovy.util.ScriptException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", ScriptException.class);
  }

  public void testThrowCheckedException() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() throws javax.management.JMException {\n" +
        "throw new javax.management.JMException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", JMException.class);
  }

  public void testThrowRuntimeException() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() throws java.util.EmptyStackException {\n" +
        "throw new java.util.EmptyStackException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", EmptyStackException.class);
  }

  public void testThrowError() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() throws java.awt.AWTError {\n" +
        "throw new java.awt.AWTError()" +
        "}\n" +
        "}\n");
    assertInternalError("a", AWTError.class);
  }

  public void testUndeclaredThrowScript() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new org.crsh.command.ScriptException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", ScriptException.class);
  }

  public void testUndeclaredThrowGroovyScript() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new groovy.util.ScriptException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", ScriptException.class);
  }

  public void testUndeclaredThrowCheckedException() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new javax.management.JMException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", JMException.class);
  }

  public void testUndeclaredThrowRuntimeException() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new java.util.EmptyStackException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", EmptyStackException.class);
  }

  public void testUndeclaredThrowError() throws Exception {
    lifeCycle.setCommand("a", "public class a extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new java.awt.AWTError()" +
        "}\n" +
        "}\n");
    assertInternalError("a", AWTError.class);
  }

  public void testScriptThrowScript() throws Exception {
    lifeCycle.setCommand("a", "throw new org.crsh.command.ScriptException()");
    assertEvalError("a", ScriptException.class);
  }

  public void testScriptThrowGroovyScript() throws Exception {
    lifeCycle.setCommand("a", "throw new groovy.util.ScriptException()");
    assertEvalError("a", ScriptException.class);
  }

  public void testScriptThrowCheckedException() throws Exception {
    lifeCycle.setCommand("a", "throw new javax.management.JMException()");
    assertEvalError("a", JMException.class);
  }

  public void testScriptThrowRuntimeException() throws Exception {
    lifeCycle.setCommand("a", "throw new java.util.EmptyStackException()");
    assertEvalError("a", EmptyStackException.class);
  }

  public void testScriptThrowError() throws Exception {
    lifeCycle.setCommand("a", "throw new java.awt.AWTError()");
    assertInternalError("a", AWTError.class);
  }
}

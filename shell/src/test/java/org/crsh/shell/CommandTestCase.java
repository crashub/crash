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

public class CommandTestCase extends AbstractShellTestCase {

  /** . */
  private final String no_ret = "class no_ret {\n" +
      "@Command\n" +
      "public void main() {\n" +
      "}\n" +
      "}";


  public void testUnknownCommand() throws Exception {
    assertUnknownCommand("bilto");
  }

  public void testFailure() throws Exception {
    Throwable t = assertEvalError("fail");
//    assertEquals(Exception.class, t.getClass());
  }

  public void testGroovyCompilationError() throws Exception {
    assertInternalError("invalid");
  }

  public void testGroovyInvalidCommandDescriptor() throws Exception {
    lifeCycle.bindGroovy("foo",
        "public class foo { @Command public void main(@Option(names = [\"-h\"]) String opt) { } }");
    assertInternalError("foo");
  }

  public void testJavaCompilationError() throws Exception {
    lifeCycle.bindJava("foo",
        "public class foo { @Command public void main( { } }");
    assertInternalError("foo");
  }

  public void testJavaInvalidCommandDescriptor() throws Exception {
    lifeCycle.bindJava("foo",
        "public class foo { @Command public void main(@Option(names = \"-h\") String opt) { } }");
    assertInternalError("foo");
  }

  public void testSimple() throws Exception {
    assertEquals("foo", assertOk("echo foo"));
  }

  public void testSession() throws Exception {
    assertEquals("null", assertOk("attribute foo"));
    lifeCycle.setAttribute("foo", "bar");
    assertEquals("bar", assertOk("attribute foo"));
    lifeCycle.setAttribute("foo", null);
    assertEquals("null", assertOk("attribute foo"));
  }

  public void testInvokeNoRet() throws Exception {
    String foo = "class foo {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "no_ret()\n" +
      "}\n" +
      "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovy("no_ret", no_ret);

    //
    assertEquals("", assertOk("foo"));
  }

  public void testInvokeNoRetInScript() throws Exception {
    String foo = "no_ret()\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovy("no_ret", no_ret);

    //
    assertEquals("", assertOk("foo"));
  }

  public void testInvoke() throws Exception {
    String foo = "class foo {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "echo 'bar'\n" +
      "}\n" +
      "}";
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testInvokeInScript() throws Exception {
    String foo = "echo 'bar'\n";
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testFlush() {
    assertEquals("foobar", assertOk("echo -f 1 foo bar"));
    assertEquals("bar", evalOk("out << 'bar'; out.flush();"));
  }

  public void testResolveOut() {
    String resolve = "class resolve {\n" +
        "@Command\n" +
        "public org.crsh.command.Pipe<Object, Object> main() {\n" +
        "return new org.crsh.command.Pipe<Object, Object>() {\n" +
        "public void open() {\n" +
        "out << 'HELLO'\n" +
        "}\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("resolve", resolve);

    //
    assertEquals("HELLO", assertOk("resolve"));
  }

  public void testContextLeftShift() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "context << 'hello'\n" +
        "context << 3\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("hello3", assertOk("foo"));
  }

  public void testScriptContextLeftShift() {
    String foo = "context << 'hello'\n" +
        "context << 3\n" +
        "return null";
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("hello3", assertOk("foo"));
  }

  public void testThrowScript() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() throws org.crsh.command.ScriptException {\n" +
        "throw new org.crsh.command.ScriptException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", ScriptException.class);
  }

  public void testThrowGroovyScript() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() throws groovy.util.ScriptException {\n" +
        "throw new groovy.util.ScriptException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", groovy.util.ScriptException.class);
  }

  public void testThrowCheckedException() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() throws javax.management.JMException {\n" +
        "throw new javax.management.JMException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", JMException.class);
  }

  public void testThrowRuntimeException() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() throws java.util.EmptyStackException {\n" +
        "throw new java.util.EmptyStackException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", EmptyStackException.class);
  }

  public void testThrowError() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() throws java.awt.AWTError {\n" +
        "throw new java.awt.AWTError()" +
        "}\n" +
        "}\n");
    assertInternalError("a", AWTError.class);
  }

  public void testUndeclaredThrowScript() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new org.crsh.command.ScriptException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", ScriptException.class);
  }

  public void testUndeclaredThrowGroovyScript() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new groovy.util.ScriptException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", groovy.util.ScriptException.class);
  }

  public void testUndeclaredThrowCheckedException() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new javax.management.JMException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", JMException.class);
  }

  public void testUndeclaredThrowRuntimeException() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new java.util.EmptyStackException()" +
        "}\n" +
        "}\n");
    assertEvalError("a", EmptyStackException.class);
  }

  public void testUndeclaredThrowError() throws Exception {
    lifeCycle.bindGroovy("a", "public class a {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "throw new java.awt.AWTError()" +
        "}\n" +
        "}\n");
    assertInternalError("a", AWTError.class);
  }

  public void testScriptThrowScript() throws Exception {
    lifeCycle.bindGroovy("a", "throw new org.crsh.command.ScriptException()");
    assertEvalError("a", ScriptException.class);
  }

  public void testScriptThrowGroovyScript() throws Exception {
    lifeCycle.bindGroovy("a", "throw new groovy.util.ScriptException()");
    assertEvalError("a", ScriptException.class);
  }

  public void testScriptThrowCheckedException() throws Exception {
    lifeCycle.bindGroovy("a", "throw new javax.management.JMException()");
    assertEvalError("a", JMException.class);
  }

  public void testScriptThrowRuntimeException() throws Exception {
    lifeCycle.bindGroovy("a", "throw new java.util.EmptyStackException()");
    assertEvalError("a", EmptyStackException.class);
  }

  public void testScriptThrowError() throws Exception {
    lifeCycle.bindGroovy("a", "throw new java.awt.AWTError()");
    assertInternalError("a", AWTError.class);
  }
}

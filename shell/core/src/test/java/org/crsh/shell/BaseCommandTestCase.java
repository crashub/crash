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

import org.crsh.command.ScriptException;

import javax.management.JMException;
import java.awt.*;
import java.util.EmptyStackException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BaseCommandTestCase extends AbstractCommandTestCase {

  public void testUnknownCommand() throws Exception {
    assertUnknownCommand("bilto");
  }

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

  public void testSession() throws Exception {
    assertOk("null", "attribute foo");
    lifeCycle.setAttribute("foo", "bar");
    assertOk("bar", "attribute foo");
    lifeCycle.setAttribute("foo", null);
    assertOk("null", "attribute foo");
  }

  /** . */
  private final String no_ret = "class no_ret extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public void main() {\n" +
    "}\n" +
    "}";

  public void testInvokeNoRet() throws Exception {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "no_ret()\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("no_ret", no_ret);

    //
    assertOk("", "foo");
  }

  public void testInvokeNoRetInScript() throws Exception {
    String foo = "no_ret()\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("no_ret", no_ret);

    //
    assertOk("", "foo");
  }

  public void testInvoke() throws Exception {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "echo 'bar'\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);

    //
    assertOk("bar", "foo");
  }

  public void testInvokeInScript() throws Exception {
    String foo = "echo 'bar'\n";
    lifeCycle.setCommand("foo", foo);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String compound_command = "class compound_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public String compound() {\n" +
    "return 'bar';" +
    "}\n" +
    "}";

  public void testInvokeCompound() throws Exception {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "compound_command.compound 'bar'\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("compound_command", compound_command);
    lifeCycle.setCommand("foo", foo);

    //
    assertOk("bar", "foo");
  }

  public void testInvokeCompoundInScript() throws Exception {
    String foo = "compound_command.compound 'bar'\n";
    lifeCycle.setCommand("compound_command", compound_command);
    lifeCycle.setCommand("foo", foo);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String option_command = "class option_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public String main(@Option(names=['o','option']) String opt) {\n" +
    "return opt;" +
    "}\n" +
    "}";

  public void testShortOption() throws Exception {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "option_command o:'bar'\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("option_command", option_command);

    //
    assertOk("bar", "foo");
  }

  public void testShortOptionInScript() throws Exception {
    String foo = "option_command o:'bar'\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("option_command", option_command);

    //
    assertOk("bar", "foo");
  }

  public void testLongOption() throws Exception {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "option_command option:'bar'\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("option_command", option_command);

    //
    assertOk("bar", "foo");
  }

  public void testLongOptionInScript() throws Exception {
    String foo = "option_command option:'bar'\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("option_command", option_command);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String checked_exception_command = "class checked_exception_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public String main() {\n" +
    "throw new javax.naming.NamingException();" +
    "}\n" +
    "}";

  public void testCheckedException() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "try {" +
      "checked_exception_command()" +
      "} catch(javax.naming.NamingException e) {\n" +
      "return 'bar'\n" +
      "}\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("checked_exception_command", checked_exception_command);

    //
    assertOk("bar", "foo");
  }

  public void testCheckedExceptionInScript() {
    String foo = "try {" +
      "checked_exception_command()" +
      "} catch(javax.naming.NamingException e) {\n" +
      "return 'bar'\n" +
      "}\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("checked_exception_command", checked_exception_command);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String script_exception_command = "class script_exception_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public String main() {\n" +
    "throw new org.crsh.command.ScriptException();" +
    "}\n" +
    "}";

  public void testScriptException() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "try {" +
      "script_exception_command()" +
      "} catch(org.crsh.command.ScriptException e) {\n" +
      "return 'bar'\n" +
      "}\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("script_exception_command", script_exception_command);

    //
    assertOk("bar", "foo");
  }

  public void testScriptExceptionInScript() {
    String foo = "try {" +
      "script_exception_command()" +
      "} catch(org.crsh.command.ScriptException e) {\n" +
      "return 'bar'\n" +
      "}\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("script_exception_command", script_exception_command);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String groovy_script_exception_command = "class groovy_script_exception_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public String main() {\n" +
    "throw new groovy.util.ScriptException();" +
    "}\n" +
    "}";

  public void testGroovyScriptException() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "try {" +
      "groovy_script_exception_command()" +
      "} catch(org.crsh.command.ScriptException e) {\n" +
      "return 'bar'\n" +
      "}\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("groovy_script_exception_command", groovy_script_exception_command);

    //
    assertOk("bar", "foo");
  }

  public void testGroovyScriptExceptionInScript() {
    String foo = "try {" +
      "groovy_script_exception_command()" +
      "} catch(org.crsh.command.ScriptException e) {\n" +
      "return 'bar'\n" +
      "}\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("groovy_script_exception_command", groovy_script_exception_command);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String runtime_exception_command = "class runtime_exception_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public String main() {\n" +
    "throw new java.lang.SecurityException();" +
    "}\n" +
    "}";

  public void testRuntimeException() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "try {" +
      "runtime_exception_command()" +
      "} catch(java.lang.SecurityException e) {\n" +
      "return 'bar'\n" +
      "}\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("runtime_exception_command", runtime_exception_command);

    //
    assertOk("bar", "foo");
  }

  public void testRuntimeExceptionInScript() {
    String foo = "try {" +
      "runtime_exception_command()" +
      "} catch(java.lang.SecurityException e) {\n" +
      "return 'bar'\n" +
      "}\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("runtime_exception_command", runtime_exception_command);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String error_command = "class error_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public String main() {\n" +
    "throw new java.awt.AWTError();" +
    "}\n" +
    "}";

  public void testError() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "try {" +
      "error_command()" +
      "} catch(java.awt.AWTError e) {\n" +
      "return 'bar'\n" +
      "}\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("error_command", error_command);

    //
    assertOk("bar", "foo");
  }

  public void testErrorInScript() {
    String foo = "try {" +
      "error_command()" +
      "} catch(java.awt.AWTError e) {\n" +
      "return 'bar'\n" +
      "}\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("error_command", error_command);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String cannot_create_command = "class cannot_create_command extends org.crsh.command.CRaSHCommand {\n" +
    "{ throw new RuntimeException(); } \n" +
    "@Command\n" +
    "public String main() {\n" +
    "throw new java.awt.AWTError();" +
    "}\n" +
    "}";

  public void testCannotCreateCommand() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "try {\n" +
      "cannot_create_command()" +
      "} catch (org.crsh.command.NoSuchCommandException e) {\n" +
      "return 'bar';\n" +
      "}\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("cannot_create_command", cannot_create_command);

    //
    assertOk("bar", "foo");
  }

  public void testCannotCreateCommandInScript() {
    String foo = "try {\n" +
      "cannot_create_command()" +
      "} catch (org.crsh.command.NoSuchCommandException e) {\n" +
      "return 'bar';\n" +
      "}\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("cannot_create_command", cannot_create_command);

    //
    assertOk("bar", "foo");
  }

  /** . */
  private final String produce_command = "class produce_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public void main(org.crsh.command.InvocationContext<Void, String> context) {\n" +
    "['foo','bar'].each { context.produce(it) }" +
    "}\n" +
    "}";

  public void testProduce() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main() {\n" +
      "produce_command { out << it }\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("produce_command", produce_command);

    //
    assertOk("foobar", "foo");
  }

  public void testProduceInScript() {
    String foo = "produce_command { out << it }\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("produce_command", produce_command);

    //
    assertOk("foobar", "foo");
  }

  /** . */
  private final String compound_produce_command = "class compound_produce_command extends org.crsh.command.CRaSHCommand {\n" +
    "@Command\n" +
    "public void compound(org.crsh.command.InvocationContext<Void, String> context) {\n" +
    "['foo','bar'].each { context.produce(it) }" +
    "}\n" +
    "}";

  public void testCompoundProduce() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main() {\n" +
      "compound_produce_command.compound { out << it }\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_produce_command", compound_produce_command);

    //
    assertOk("foobar", "foo");
  }

  public void testCompoundProduceInScript() {
    String foo = "compound_produce_command.compound { out << it }\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_produce_command", compound_produce_command);

    //
    assertOk("foobar", "foo");
  }

  public void testClosure() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main() {\n" +
      "def closure = echo\n" +
      "closure 'bar'\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);

    //
    assertOk("bar", "foo");
  }

  public void testClosureInScript() {
    String foo = "def closure = echo\n" +
      "closure 'bar'\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_command", compound_command);

    //
    assertOk("bar", "foo");
  }

  public void testCompoundClosure() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main() {\n" +
      "def closure = compound_command.compound\n" +
      "closure()\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_command", compound_command);

    //
    assertOk("bar", "foo");
  }

  public void testCompoundClosureInScript() {
    String foo = "def closure = compound_command.compound\n" +
      "closure()\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_command", compound_command);

    //
    assertOk("bar", "foo");
  }
}

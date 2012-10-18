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

import java.util.ArrayList;
import java.util.Arrays;

public class CompositionTestCase extends AbstractCommandTestCase {

  /** . */
  public static final ArrayList<?> list = new ArrayList<Object>();

  /** . */
  private final String compound_command = "class compound_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String compound() {\n" +
      "return 'bar';" +
      "}\n" +
      "}";

  /** . */
  private final String compound_produce_command = "class compound_produce_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void compound(org.crsh.command.InvocationContext<String> context) {\n" +
      "['foo','bar'].each { context.provide(it) }" +
      "}\n" +
      "}";

  /** . */
  private final String compound_consume_command = "class compound_consume_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public org.crsh.command.PipeCommand<String> compound() {\n" +
      "return new org.crsh.command.PipeCommand<String>() {\n" +
      "public void provide(String element) {\n" +
      "org.crsh.shell.CompositionTestCase.list.add(element);\n" +
      "}\n" +
      "}\n" +
      "}\n" +
      "}";

  /** . */
  private final String checked_exception_command = "class checked_exception_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "throw new javax.naming.NamingException();" +
      "}\n" +
      "}";

  /** . */
  private final String script_exception_command = "class script_exception_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "throw new org.crsh.command.ScriptException();" +
      "}\n" +
      "}";

  /** . */
  private final String groovy_script_exception_command = "class groovy_script_exception_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "throw new groovy.util.ScriptException();" +
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
    assertEquals("bar", assertOk("foo"));
  }

  /** . */
  private final String runtime_exception_command = "class runtime_exception_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "throw new java.lang.SecurityException();" +
      "}\n" +
      "}";

  /** . */
  private final String error_command = "class error_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "throw new java.awt.AWTError();" +
      "}\n" +
      "}";

  /** . */
  private final String cannot_create_command = "class cannot_create_command extends org.crsh.command.CRaSHCommand {\n" +
      "{ throw new RuntimeException(); } \n" +
      "@Command\n" +
      "public String main() {\n" +
      "throw new java.awt.AWTError();" +
      "}\n" +
      "}";

  public void testInvokeCompoundInScript() throws Exception {
    String foo = "compound_command.compound 'bar'\n";
    lifeCycle.setCommand("compound_command", compound_command);
    lifeCycle.setCommand("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testClosureInScript() {
    String foo = "def closure = echo\n" +
        "closure 'bar'\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_command", compound_command);

    //
    assertEquals("bar", assertOk("foo"));
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
    assertEquals("bar", assertOk("foo"));
  }

  public void testCompoundClosureInScript() {
    String foo = "def closure = compound_command.compound\n" +
        "closure()\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_command", compound_command);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCompoundProduceToClosure() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "compound_produce_command.compound { out << it }\n" +
        "}\n" +
        "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_produce_command", compound_produce_command);
    assertEquals("foobar", assertOk("foo"));

    // Test with wrong type
    String bar = "class bar extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "compound_produce_command.compound { boolean it -> out << it }\n" +
        "}\n" +
        "}";
    lifeCycle.setCommand("bar", bar);
    lifeCycle.setCommand("compound_produce_command", compound_produce_command);
    assertEquals("", assertOk("bar"));
  }

  public void testCompoundProduceToClosureInScript() {
    String foo = "compound_produce_command.compound { out << it }\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_produce_command", compound_produce_command);
    assertEquals("foobar", assertOk("foo"));

    //
    String bar = "compound_produce_command.compound { boolean it -> out << it }\n";
    lifeCycle.setCommand("bar", bar);
    lifeCycle.setCommand("compound_produce_command", compound_produce_command);
    assertEquals("", assertOk("bar"));
  }

  public void testCompoundCommandAsClosure() {
    String foo =
        "def closure = compound_consume_command.compound\n" +
        "compound_produce_command.compound closure\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("compound_produce_command", compound_produce_command);
    lifeCycle.setCommand("compound_consume_command", compound_consume_command);
    list.clear();
    assertEquals("", assertOk("foo"));
    assertEquals(Arrays.asList("foo", "bar"), list);
  }

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
    assertEquals("bar", assertOk("foo"));
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
    assertEquals("bar", assertOk("foo"));
  }

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
    assertEquals("bar", assertOk("foo"));
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
    assertEquals("bar", assertOk("foo"));
  }

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
    assertEquals("bar", assertOk("foo"));
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
    assertEquals("bar", assertOk("foo"));
  }

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
    assertEquals("bar", assertOk("foo"));
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
    assertEquals("bar", assertOk("foo"));
  }

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
    assertEquals("bar", assertOk("foo"));
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
    assertEquals("bar", assertOk("foo"));
  }

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
    assertEquals("bar", assertOk("foo"));
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
    assertEquals("bar", assertOk("foo"));
  }
}

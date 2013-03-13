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

import java.util.Arrays;

/**
 * Various test related to command dispatch.
 */
public class DispatchTestCase extends AbstractCommandTestCase {

  public void testInvokeCompound() throws Exception {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "compound_command.compound 'bar'\n" +
        "}\n" +
        "}";
    lifeCycle.bind("compound_command", Commands.Compound.class);
    lifeCycle.bind("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testProduceToClosure() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "produce_command { out << it }\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("produce_command", Commands.ProduceString.class);

    //
    assertEquals("foobar", assertOk("foo"));
  }

  public void testProduceToCommandAsClosure() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "def closure = consume_command\n" +
        "produce_command closure\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("produce_command", Commands.ProduceString.class);
    lifeCycle.bind("consume_command", Commands.ConsumeString.class);

    //
    Commands.list.clear();
    assertEquals("", assertOk("foo"));
    assertEquals(Arrays.asList("foo", "bar"), Commands.list);
  }

  // Cannot pass at the moment
  public void testProduceToCommandWithOptionAsClosure() {
    String noOpt = "class noOpt {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "def closure = consume_command_with_option\n" +
        "produce_command closure\n" +
        "}\n" +
        "}";
    String opt = "class opt {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "def closure = consume_command_with_option.with(opt:'prefix')\n" +
        "produce_command closure\n" +
        "}\n" +
        "}";
    String args = "class args {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "def closure = consume_command_with_option.with('juu')\n" +
        "produce_command closure\n" +
        "}\n" +
        "}";
    String optArgs = "class args {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "def closure = consume_command_with_option.with(opt:'prefix','juu')\n" +
        "produce_command closure\n" +
        "}\n" +
        "}";

    //
    lifeCycle.bind("noOpt", noOpt);
    lifeCycle.bind("opt", opt);
    lifeCycle.bind("args", args);
    lifeCycle.bind("optArgs", optArgs);
    lifeCycle.bind("produce_command", Commands.ProduceString.class);
    lifeCycle.bind("consume_command_with_option", Commands.ParameterizedConsumeToList.class);

    //
    Commands.list.clear();
    assertEquals("", assertOk("noOpt"));
    assertEquals(Arrays.asList("foo", "bar"), Commands.list);

    //
    Commands.list.clear();
    assertEquals("", assertOk("opt"));
    assertEquals(Arrays.asList("prefixfoo", "prefixbar"), Commands.list);

    //
    Commands.list.clear();
    assertEquals("", assertOk("args"));
    assertEquals(Arrays.asList("juu", "foo", "bar"), Commands.list);

    //
    Commands.list.clear();
    assertEquals("", assertOk("optArgs"));
    assertEquals(Arrays.asList("prefixjuu", "prefixfoo", "prefixbar"), Commands.list);
  }

  public void testProduceToClosureInScript() {
    lifeCycle.bind("foo", "produce_command { out << it }\n");
    lifeCycle.bind("produce_command", Commands.ProduceString.class);

    //
    assertEquals("foobar", assertOk("foo"));
  }

  public void testClosure() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "def closure = echo\n" +
        "closure 'bar'\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testClosureInScript() {
    String foo = "def closure = echo\n" +
        "closure 'bar'\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("compound_command", Commands.Compound.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCompoundClosure() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "def closure = compound_command.compound\n" +
        "closure()\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("compound_command", Commands.Compound.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCompoundClosureInScript() {
    String foo = "def closure = compound_command.compound\n" +
        "closure()\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("compound_command", Commands.Compound.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCompoundProduceToClosure() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "compound_produce_command.compound { out << it }\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("compound_produce_command", Commands.CompoundProduceString.class);
    assertEquals("foobar", assertOk("foo"));

    // Test with wrong type
    String bar = "class bar {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "compound_produce_command.compound { boolean it -> out << it }\n" +
        "}\n" +
        "}";
    lifeCycle.bind("bar", bar);
    lifeCycle.bind("compound_produce_command", Commands.CompoundProduceString.class);
    assertEquals("", assertOk("bar"));
  }

  public void testCompoundProduceToClosureInScript() {
    String foo = "compound_produce_command.compound { out << it }\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("compound_produce_command", Commands.CompoundProduceString.class);
    assertEquals("foobar", assertOk("foo"));

    //
    String bar = "compound_produce_command.compound { boolean it -> out << it }\n";
    lifeCycle.bind("bar", bar);
    lifeCycle.bind("compound_produce_command", Commands.CompoundProduceString.class);
    assertEquals("", assertOk("bar"));
  }

  public void testCompoundCommandAsClosure() {
    String foo =
        "def closure = compound_consume_command.compound\n" +
            "compound_produce_command.compound closure\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("compound_produce_command", Commands.CompoundProduceString.class);
    lifeCycle.bind("compound_consume_command", Commands.CompoundConsumeString.class);
    Commands.list.clear();
    assertEquals("", assertOk("foo"));
    assertEquals(Arrays.asList("foo", "bar"), Commands.list);
  }

  public void testInvokeCompoundInScript() throws Exception {
    String foo = "compound_command.compound 'bar'\n";
    lifeCycle.bind("compound_command", Commands.Compound.class);
    lifeCycle.bind("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCheckedException() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "try {" +
        "checked_exception_command()" +
        "} catch(javax.naming.NamingException e) {\n" +
        "return 'bar'\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("checked_exception_command", Commands.ThrowCheckedException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCheckedExceptionInScript() {
    String foo = "try {" +
        "checked_exception_command()" +
        "} catch(javax.naming.NamingException e) {\n" +
        "return 'bar'\n" +
        "}\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("checked_exception_command", Commands.ThrowCheckedException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testScriptException() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "try {" +
        "script_exception_command()" +
        "} catch(org.crsh.command.ScriptException e) {\n" +
        "return 'bar'\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("script_exception_command", Commands.ThrowScriptException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testScriptExceptionInScript() {
    String foo = "try {" +
        "script_exception_command()" +
        "} catch(org.crsh.command.ScriptException e) {\n" +
        "return 'bar'\n" +
        "}\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("script_exception_command", Commands.ThrowScriptException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testGroovyScriptException() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "try {" +
        "groovy_script_exception_command()" +
        "} catch(org.crsh.command.ScriptException e) {\n" +
        "return 'bar'\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("groovy_script_exception_command", Commands.ThrowGroovyScriptException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testGroovyScriptExceptionInScript() {
    String foo = "try {" +
        "groovy_script_exception_command()" +
        "} catch(org.crsh.command.ScriptException e) {\n" +
        "return 'bar'\n" +
        "}\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("groovy_script_exception_command", Commands.ThrowGroovyScriptException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testRuntimeException() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "try {" +
        "runtime_exception_command()" +
        "} catch(java.lang.SecurityException e) {\n" +
        "return 'bar'\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("runtime_exception_command", Commands.ThrowRuntimeException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testRuntimeExceptionInScript() {
    String foo = "try {" +
        "runtime_exception_command()" +
        "} catch(java.lang.SecurityException e) {\n" +
        "return 'bar'\n" +
        "}\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("runtime_exception_command", Commands.ThrowRuntimeException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testError() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "try {" +
        "error_command()" +
        "} catch(java.awt.AWTError e) {\n" +
        "return 'bar'\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("error_command", Commands.ThrowError.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testErrorInScript() {
    String foo = "try {" +
        "error_command()" +
        "} catch(java.awt.AWTError e) {\n" +
        "return 'bar'\n" +
        "}\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("error_command", Commands.ThrowError.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCannotCreateCommand() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "try {\n" +
        "cannot_create_command()" +
        "} catch (org.crsh.command.NoSuchCommandException e) {\n" +
        "return 'bar';\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("cannot_create_command", Commands.CannotInstantiate.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCannotCreateCommandInScript() {
    String foo = "try {\n" +
        "cannot_create_command()" +
        "} catch (org.crsh.command.NoSuchCommandException e) {\n" +
        "return 'bar';\n" +
        "}\n";
    lifeCycle.bind("foo", foo);
    lifeCycle.bind("cannot_create_command", Commands.CannotInstantiate.class);

    //
    assertEquals("bar", assertOk("foo"));
  }
}

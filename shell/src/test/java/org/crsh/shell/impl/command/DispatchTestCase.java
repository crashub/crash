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

package org.crsh.shell.impl.command;

import org.crsh.lang.impl.groovy.closure.PipeLineClosure;
import org.crsh.shell.AbstractShellTestCase;
import test.command.Commands;

/**
 * Various test related to command dispatch.
 */
public class DispatchTestCase extends AbstractShellTestCase {

/*
  public void testInvokeCompound() throws Exception {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "compound_command.compound 'bar'\n" +
        "}\n" +
        "}";
    lifeCycle.bindClass("compound_command", Commands.Compound.class);
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testProduceToClosure() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "(produce_command { it })()\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("produce_command", Commands.ProduceString.class);

    //
    assertEquals("foobar", assertOk("foo"));
  }

  public void testProduceToClosureInScript() {
    lifeCycle.bindGroovy("foo", "produce_command { it }\n");
    lifeCycle.bindClass("produce_command", Commands.ProduceString.class);

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
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testClosureInScript() {
    String foo = "def closure = echo\n" +
        "closure 'bar'\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("compound_command", Commands.Compound.class);

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
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("compound_command", Commands.Compound.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCompoundClosureInScript() {
    String foo = "def closure = compound_command.compound\n" +
        "closure()\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("compound_command", Commands.Compound.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCompoundProduceToClosure() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "(compound_produce_command.compound | { it })()\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("compound_produce_command", Commands.CompoundProduceString.class);
    assertEquals("foobar", assertOk("foo"));

    // Test with wrong type
    String bar = "class bar {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "(compound_produce_command.compound | { boolean it -> it })()\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("bar", bar);
    lifeCycle.bindClass("compound_produce_command", Commands.CompoundProduceString.class);
    assertEquals("", assertOk("bar"));
  }

  public void testCompoundProduceToClosureInScript() {
    String foo = "(compound_produce_command.compound | { it })()\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("compound_produce_command", Commands.CompoundProduceString.class);
    assertEquals("foobar", assertOk("foo"));

    //
    String bar = "(compound_produce_command.compound | { boolean it -> it })()\n";
    lifeCycle.bindGroovy("bar", bar);
    lifeCycle.bindClass("compound_produce_command", Commands.CompoundProduceString.class);
    assertEquals("", assertOk("bar"));
  }

  public void testInvokeCompoundInScript() throws Exception {
    String foo = "compound_command.compound 'bar'\n";
    lifeCycle.bindClass("compound_command", Commands.Compound.class);
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }
*/

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
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("checked_exception_command", Commands.ThrowCheckedException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCheckedExceptionInScript() {
    String foo = "try {" +
        "checked_exception_command()" +
        "} catch(javax.naming.NamingException e) {\n" +
        "out << 'bar'\n" +
        "}\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("checked_exception_command", Commands.ThrowCheckedException.class);

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
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("script_exception_command", Commands.ThrowScriptException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testScriptExceptionInScript() {
    String foo = "try {" +
        "script_exception_command()" +
        "} catch(org.crsh.command.ScriptException e) {\n" +
        "out << 'bar'\n" +
        "}\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("script_exception_command", Commands.ThrowScriptException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testGroovyScriptException() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "try {" +
        "groovy_script_exception_command()" +
        "} catch(groovy.util.ScriptException e) {\n" +
        "return 'bar'\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovyClass("groovy_script_exception_command", Commands.ThrowGroovyScriptException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testGroovyScriptExceptionInScript() {
    String foo = "try {" +
        "groovy_script_exception_command()" +
        "} catch(groovy.util.ScriptException e) {\n" +
        "out << 'bar'\n" +
        "}\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovyClass("groovy_script_exception_command", Commands.ThrowGroovyScriptException.class);

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
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("runtime_exception_command", Commands.ThrowRuntimeException.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testRuntimeExceptionInScript() {
    String foo = "try {" +
        "runtime_exception_command()" +
        "} catch(java.lang.SecurityException e) {\n" +
        "out << 'bar'\n" +
        "}\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("runtime_exception_command", Commands.ThrowRuntimeException.class);

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
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("error_command", Commands.ThrowError.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testErrorInScript() {
    String foo = "try {" +
        "error_command()" +
        "} catch(java.awt.AWTError e) {\n" +
        "out << 'bar'\n" +
        "}\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("error_command", Commands.ThrowError.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCannotCreateCommand() {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "try {\n" +
        "cannot_create_command()" +
        "} catch (" + RuntimeException.class.getName() + " e) {\n" +
        "return 'bar';\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("cannot_create_command", Commands.CannotInstantiate.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testCannotCreateCommandInScript() {
    String foo = "try {\n" +
        "cannot_create_command()" +
        "} catch (" + RuntimeException.class.getName() + " e) {\n" +
        "out << 'bar';\n" +
        "}\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindClass("cannot_create_command", Commands.CannotInstantiate.class);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public static Object bound;

  public void testBindCommandInCommand() {
    lifeCycle.bindClass("bound", Commands.Parameterized.class);
    lifeCycle.bindGroovy("container", "public class container {\n" +
        "@Command public void main() {\n" +
        DispatchTestCase.class.getName() + ".bound = bound { }\n" +
        "}\n" +
        "}\n");
    bound = null;
    assertOk("container");
    assertNotNull(bound);
    assertInstance(PipeLineClosure.class, bound);
  }

  public void testBindCommandInScript() {
    lifeCycle.bindClass("bound", Commands.Parameterized.class);
    lifeCycle.bindGroovy("container",  DispatchTestCase.class.getName() + ".bound = bound { }\n");
    bound = null;
    assertOk("container");
    assertNotNull(bound);
    assertInstance(PipeLineClosure.class, bound);
  }
}

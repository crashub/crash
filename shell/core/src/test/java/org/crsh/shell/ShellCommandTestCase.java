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

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import junit.framework.TestCase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.command.ShellCommand;
import org.crsh.command.GroovyScriptCommand;
import org.crsh.command.SyntaxException;

import java.util.Arrays;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellCommandTestCase extends TestCase {


  /** . */
  private GroovyClassLoader loader;

  /** . */
  private GroovyShell shell;

  @Override
  protected void setUp() throws Exception {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(GroovyScriptCommand.class.getName());

    //
    loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
    shell = new GroovyShell(loader);
  }

  public void testOut() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@Command\n" +
      "public void main() {" +
      "out.print(\"abc\");" +
      "}" +
      "}");

    //
    ShellCommand command = (ShellCommand)clazz.newInstance();
    assertEquals("abc", new TestInvocationContext().execute(command));
  }

  public void testOptionInjectionInCommandClassCmdLine() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@Option(names=\"s\") @Required def String str = 'default value';" +
      "@Command\n" +
      "public Object main() {" +
      "return str;" +
      "}" +
      "}");

    //
    ShellCommand command = (ShellCommand)clazz.newInstance();
    assertEquals("abc", new TestInvocationContext().execute(command, "-s", "abc"));
    try {
      new TestInvocationContext().execute(command);
      fail();
    }
    catch (SyntaxException e) {
    }
  }

  public void testContextAccessFromCommandClassCmdLine() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@Command\n" +
      "public Object main() {" +
      "return juu;" +
      "}" +
      "}");

    //
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    TestInvocationContext<Void, Void> ctx = new TestInvocationContext();
    ctx.getSession().put("juu", "daa");
    assertEquals("daa", ctx.execute(cmd));
  }

  public void testArgumentInjectionInCommandCmdLine() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@Command\n" +
      "public Object main(@Argument String str) {" +
      "return str;" +
      "}" +
      "}");

    //
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    assertEquals("b", new TestInvocationContext().execute(cmd, "b"));
  }

  public void testMainInCommandCmdLine() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@Command\n" +
      "public Object main() {" +
      "return 'foo';" +
      "}" +
      "}");

    //
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    assertEquals("foo", new TestInvocationContext().execute(cmd));
  }

  public void testContextAccessInCommandClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@Command\n" +
      "public Object main() {" +
      "return bar;" +
      "}" +
      "}");

    //
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.getSession().put("bar", "bar_value");

    // Execute directly
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    assertEquals("bar_value", ctx.execute(cmd));
  }

  public void testClosureInvocationInClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@Command\n" +
      "public Object main() {" +
      "return bar();" +
      "}" +
      "}");

    //
    TestInvocationContext ctx = new TestInvocationContext();
    Closure closure = (Closure)shell.evaluate("{ -> return 'from_closure'; }");
    ctx.getSession().put("bar", closure);

    // Execute directly
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    assertEquals("from_closure", ctx.execute(cmd));
  }

  public void testArgumentQuoteInClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public Object main(@org.crsh.cmdline.annotations.Argument List<String> arguments) {\n" +
      "return arguments;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    assertEquals("" + Arrays.asList("foo"), new TestInvocationContext().execute(cmd, "'foo'"));
  }

  public void testArgumentQuoteInClass2() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public Object main(@org.crsh.cmdline.annotations.Argument(unquote = false) List<String> arguments) {\n" +
      "return arguments;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    assertEquals("" + Arrays.asList("'foo'"), new TestInvocationContext().execute(cmd, "'foo'"));
  }

  public void testContextAccessInScript() throws Exception {
    Class clazz = loader.parseClass("System.out.println('bar:' + bar) ; return bar;");
    ShellCommand script = (ShellCommand)clazz.newInstance();
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.getSession().put("bar", "bar_value");
    assertEquals("bar_value", ctx.execute(script));
  }

  public void testArgumentAccessInScript() throws Exception {
    Class clazz = loader.parseClass("return args[0];");
    ShellCommand script = (ShellCommand)clazz.newInstance();
    assertEquals("arg_value", new TestInvocationContext().execute(script, "arg_value"));
  }

  public void testArgumentAccessInClosure() throws Exception {
    Class clazz = loader.parseClass("{ arg -> return arg };");
    ShellCommand script = (ShellCommand)clazz.newInstance();
    assertEquals("arg_value", new TestInvocationContext().execute(script, "arg_value"));
  }

  public void testResolveContext() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public Object main() {\n" +
      "return context.class.name;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    TestInvocationContext context = new TestInvocationContext();
    assertEquals(context.getClass().getName(), context.execute(cmd));
  }

  public void testResolveContextInScript() throws Exception {
    Class clazz = loader.parseClass("return context.class.name");
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    TestInvocationContext context = new TestInvocationContext();
    assertEquals(context.getClass().getName(), context.execute(cmd));
  }
}

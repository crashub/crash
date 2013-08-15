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

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import junit.framework.TestCase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.command.BaseCommand;
import org.crsh.command.ShellCommand;
import org.crsh.lang.groovy.command.GroovyScriptCommand;
import org.crsh.command.SyntaxException;

import java.util.Arrays;

public class InvocationContextTestCase extends TestCase {


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
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
      "@Command\n" +
      "public void main() {" +
      "out.print(\"abc\");" +
      "}" +
      "}");

    //
    assertEquals("abc", new TestInvocationContext().execute(clazz));
  }

  public void testOptionInjectionInCommandClassCmdLine() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
      "@Option(names=\"s\") @Required def String str = 'default value';" +
      "@Command\n" +
      "public Object main() {" +
      "return str;" +
      "}" +
      "}");

    //
    assertEquals("abc", new TestInvocationContext().execute(clazz, "-s", "abc"));
    try {
      new TestInvocationContext().execute(clazz);
      fail();
    }
    catch (SyntaxException e) {
    }
  }

  public void testContextAccessFromCommandClassCmdLine() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
      "@Command\n" +
      "public Object main() {" +
      "return juu;" +
      "}" +
      "}");

    //
    TestInvocationContext<Void> ctx = new TestInvocationContext<Void>();
    ctx.getSession().put("juu", "daa");
    assertEquals("daa", ctx.execute(clazz));
  }

  public void testArgumentInjectionInCommandCmdLine() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
      "@Command\n" +
      "public Object main(@Argument String str) {" +
      "return str;" +
      "}" +
      "}");

    //
    assertEquals("b", new TestInvocationContext().execute(clazz, "b"));
  }

  public void testMainInCommandCmdLine() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
      "@Command\n" +
      "public Object main() {" +
      "return 'foo';" +
      "}" +
      "}");

    //
    assertEquals("foo", new TestInvocationContext().execute(clazz));
  }

  public void testContextAccessInCommandClass() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
      "@Command\n" +
      "public Object main() {" +
      "return bar;" +
      "}" +
      "}");

    //
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.getSession().put("bar", "bar_value");

    // Execute directly
    assertEquals("bar_value", ctx.execute(clazz));
  }

  public void testClosureInvocationInClass() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
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
    assertEquals("from_closure", ctx.execute(clazz));
  }

  public void testArgumentQuoteInClass() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo {\n" +
      "@Command\n" +
      "public Object main(@org.crsh.cli.Argument List<String> arguments) {\n" +
      "return arguments;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    assertEquals("" + Arrays.asList("foo"), new TestInvocationContext().execute(clazz, "'foo'"));
  }

  public void testArgumentQuoteInClass2() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo {\n" +
      "@Command\n" +
      "public Object main(@org.crsh.cli.Argument(unquote = false) List<String> arguments) {\n" +
      "return arguments;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    assertEquals("" + Arrays.asList("'foo'"), new TestInvocationContext().execute(clazz, "'foo'"));
  }

  public void testContextAccessInScript() throws Exception {
    Class<? extends GroovyScriptCommand> clazz = loader.parseClass("System.out.println('bar:' + bar) ; return bar;");
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.getSession().put("bar", "bar_value");
    assertEquals("bar_value", ctx.execute2(clazz));
  }

  public void testArgumentAccessInScript() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("return args[0];");
    assertEquals("arg_value", new TestInvocationContext().execute2(clazz, "arg_value"));
  }

  public void testArgumentAccessInClosure() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("{ arg -> return arg };");
    assertEquals("arg_value", new TestInvocationContext().execute2(clazz, "arg_value"));
  }

  public void testResolveContext() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo {\n" +
      "@Command\n" +
      "public Object main() {\n" +
      "return context.class.name;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    TestInvocationContext context = new TestInvocationContext();
    assertNotNull(context.execute(clazz));
  }

  public void testResolveContextInScript() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("return context.class.name");
    TestInvocationContext context = new TestInvocationContext();
    assertNotNull(context.execute2(clazz));
  }

  public void testScriptUseReturnValue() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("return 'def'");
    assertEquals("def", new TestInvocationContext().execute2(clazz));
  }

  public void testScriptDiscardReturnValue() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("out << 'abc'\nreturn 'def'");
    assertEquals("abc", new TestInvocationContext().execute2(clazz));
  }
}

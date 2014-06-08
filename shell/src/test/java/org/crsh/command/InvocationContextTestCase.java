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
package org.crsh.command;

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import junit.framework.Assert;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.AbstractTestCase;
import org.crsh.cli.impl.SyntaxException;
import test.command.TestInvocationContext;
import org.crsh.lang.impl.groovy.command.GroovyScriptCommand;
import org.crsh.shell.impl.command.spi.CommandException;

import java.util.Arrays;

public class InvocationContextTestCase extends AbstractTestCase {


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
    Assert.assertEquals("abc", new TestInvocationContext().execute(clazz));
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
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute(clazz, "-s", "abc");
    assertEquals(Arrays.asList("abc"), ctx.getProducedItems());
    try {
      new TestInvocationContext().execute(clazz);
      fail();
    }
    catch (CommandException e) {
      assertInstance(SyntaxException.class, e.getCause());
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
    ctx.execute(clazz);
    assertEquals(Arrays.<Object>asList("daa"), ctx.getProducedItems());
  }

  public void testArgumentInjectionInCommandCmdLine() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
      "@Command\n" +
      "public Object main(@Argument String str) {" +
      "return str;" +
      "}" +
      "}");

    //
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute(clazz, "b");
    assertEquals(Arrays.asList("b"), ctx.getProducedItems());
  }

  public void testMainInCommandCmdLine() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo { " +
      "@Command\n" +
      "public Object main() {" +
      "return 'foo';" +
      "}" +
      "}");

    //
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute(clazz);
    assertEquals(Arrays.asList("foo"), ctx.getProducedItems());
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
    ctx.execute(clazz);
    assertEquals(Arrays.asList("bar_value"), ctx.getProducedItems());
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
    ctx.execute(clazz);
    assertEquals(Arrays.asList("from_closure"), ctx.getProducedItems());
  }

  public void testArgumentQuoteInClass() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo {\n" +
      "@Command\n" +
      "public Object main(@org.crsh.cli.Argument List<String> arguments) {\n" +
      "return arguments;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute(clazz, "'foo'");
    assertEquals(Arrays.<Object>asList(Arrays.asList("foo")), ctx.getProducedItems());
  }

  public void testArgumentQuoteInClass2() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo {\n" +
      "@Command\n" +
      "public Object main(@org.crsh.cli.Argument(unquote = false) List<String> arguments) {\n" +
      "return arguments;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute(clazz, "'foo'");
    assertEquals(Arrays.<Object>asList(Arrays.asList("'foo'")), ctx.getProducedItems());
  }

  public void testContextAccessInScript() throws Exception {
    Class<? extends GroovyScriptCommand> clazz = loader.parseClass("System.out.println('bar:' + bar) ; return bar;");
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.getSession().put("bar", "bar_value");
    ctx.execute2(clazz);
    assertEquals(Arrays.asList("bar_value"), ctx.getProducedItems());
  }

  public void testArgumentAccessInScript() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("return args[0];");
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute2(clazz, "arg_value");
    assertEquals(Arrays.asList("arg_value"), ctx.getProducedItems());
  }

  public void testArgumentAccessInClosure() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("{ arg -> context.provide(arg) };");
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute2(clazz, "arg_value");
    assertEquals(Arrays.asList("arg_value"), ctx.getProducedItems());
  }

  public void testResolveContext() throws Exception {
    Class<? extends BaseCommand> clazz = loader.parseClass("class foo {\n" +
      "@Command\n" +
      "public Object main() {\n" +
      "return context;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    TestInvocationContext context = new TestInvocationContext();
    context.execute(clazz);
    assertEquals(1, context.getProducedItems().size());
    assertInstance(InvocationContext.class, context.getProducedItems().get(0));
  }

  public void testResolveContextInScript() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("return context");
    TestInvocationContext context = new TestInvocationContext();
    context.execute2(clazz);
    assertEquals(1, context.getProducedItems().size());
    assertInstance(InvocationContext.class, context.getProducedItems().get(0));
  }

  public void testScriptUseReturnValue() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("return 'def'");
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute2(clazz);
    assertEquals(Arrays.asList("def"), ctx.getProducedItems());
  }

  public void testScriptDiscardImplicitReturnValue() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("def a = 'def'");
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute2(clazz);
    assertEquals(0, ctx.getProducedItems().size());
  }

  public void testScriptReturnExplicitReturnValue() throws Exception {
    Class<? extends GroovyScriptCommand>  clazz = loader.parseClass("return 'def'");
    TestInvocationContext ctx = new TestInvocationContext();
    ctx.execute2(clazz);
    assertEquals(Arrays.asList("def"), ctx.getProducedItems());
  }
}

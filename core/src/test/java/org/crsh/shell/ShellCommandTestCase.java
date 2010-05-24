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
import org.crsh.command.ClassCommand;
import org.crsh.command.CommandContext;
import org.crsh.command.ScriptCommand;

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
    config.setScriptBaseClass(ScriptCommand.class.getName());

    //
    loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
    shell = new GroovyShell(loader);
  }

  public void testOptionInjectionInCommandClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "@org.kohsuke.args4j.Option(name=\"-str\") def String str = 'default value';" +
      "public Object execute() {" +
      "return str;" +
      "}" +
      "}");

    //
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("abc", cmd.execute(new CommandContext(), "-str","abc"));
  }

  public void testArgumentInjectionInCommandClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "@org.kohsuke.args4j.Argument def String str = 'default value';" +
      "public Object execute() {" +
      "return str;" +
      "}" +
      "}");

    //
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("b", cmd.execute(new CommandContext(), "b"));
  }

  public void testContextAccessInCommandClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "public Object execute() {" +
      "return bar;" +
      "}" +
      "}");

    //
    CommandContext ctx = new CommandContext();
    ctx.put("bar", "bar_value");

    // Execute directly
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("bar_value", cmd.execute(ctx));
  }

  public void testClosureInvocationInClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "public Object execute() {" +
      "return bar();" +
      "}" +
      "}");

    //
    CommandContext ctx = new CommandContext();
    Closure closure = (Closure)shell.evaluate("{ -> return 'from_closure'; }");
    ctx.put("bar", closure);

    // Execute directly
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("from_closure", cmd.execute(ctx));
  }

  public void testArgumentQuoteInClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.AnyArgumentClassCommand { " +
      "public Object execute() {" +
      "return arguments;" +
      "}" +
      "}");

    // Execute directly
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals(Arrays.asList("'foo'"), cmd.execute(new CommandContext(), "'foo'"));
  }

  public void testContextAccessInScript() throws Exception {
    Class clazz = loader.parseClass("return bar;");
    ScriptCommand script = (ScriptCommand)clazz.newInstance();
    CommandContext ctx = new CommandContext();
    ctx.put("bar", "bar_value");
    assertEquals("bar_value", script.execute(ctx));
  }

  public void testArgumentAccessInScript() throws Exception {
    Class clazz = loader.parseClass("return args[0];");
    ScriptCommand script = (ScriptCommand)clazz.newInstance();
    CommandContext ctx = new CommandContext();
    assertEquals("arg_value", script.execute(ctx, "arg_value"));
  }

  public void testArgumentAccessInClosure() throws Exception {
    Class clazz = loader.parseClass("{ arg -> return arg };");
    ScriptCommand script = (ScriptCommand)clazz.newInstance();
    CommandContext ctx = new CommandContext();
    assertEquals("arg_value", script.execute(ctx, "arg_value"));
  }
}

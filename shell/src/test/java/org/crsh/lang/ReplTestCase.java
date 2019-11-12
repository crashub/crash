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
package org.crsh.lang;

import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.command.BaseCommand;
import org.crsh.lang.impl.groovy.GroovyLanguageProxy;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.AbstractShellTestCase;
import test.command.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author Julien Viet */
public class ReplTestCase extends AbstractShellTestCase {

  /** . */
  public static final ArrayList<Object> list = new ArrayList<Object>();

  @Override
  protected List<CRaSHPlugin<?>> getPlugins() {
    List<CRaSHPlugin<?>> plugins = super.getPlugins();
    plugins.add(new GroovyLanguageProxy());
    return plugins;
  }

  public void testResolveContext() {
    Object context = groovyShell.evaluate("context");
    assertNotNull(context);
  }

  public void testConfigureOptionWithClosure() {
    lifeCycle.bindClass("parameterized", Commands.Parameterized.class);
    assertOk("repl groovy");
    list.clear();
    Commands.Parameterized.reset();
    assertOk("(parameterized { opt = 'toto_opt'; })()");
    assertEquals("toto_opt", Commands.Parameterized.opt);
  }

  public void testConfigureArgumentWithClosure() {
    lifeCycle.bindClass("parameterized", Commands.Parameterized.class);
    assertOk("repl groovy");
    list.clear();
    Commands.Parameterized.reset();
    assertOk("(parameterized { 'toto_arg'; })()");
    assertEquals(null, Commands.Parameterized.opt);
    assertEquals(Arrays.asList("toto_arg"), Commands.Parameterized.args);
  }

  public void testConfigureArgumentListWithClosure() {
    lifeCycle.bindClass("parameterized", Commands.Parameterized.class);
    assertOk("repl groovy");
    list.clear();
    Commands.Parameterized.reset();
    assertOk("(parameterized { ['toto_arg_1', 'toto_arg_2']; })()");
    assertEquals(null, Commands.Parameterized.opt);
    assertEquals(Arrays.asList("toto_arg_1", "toto_arg_2"), Commands.Parameterized.args);
  }

  public void testConfigureArgumentArrayWithClosure() {
    lifeCycle.bindClass("parameterized", Commands.Parameterized.class);
    assertOk("repl groovy");
    list.clear();
    Commands.Parameterized.reset();
    assertOk("(parameterized { ['toto_arg_1', 'toto_arg_2'] as Object[]; })()");
    assertEquals(null, Commands.Parameterized.opt);
    assertEquals(Arrays.asList("toto_arg_1", "toto_arg_2"), Commands.Parameterized.args);
  }

  public void testResolveContextInClosure() {
    lifeCycle.bindClass("produce", Commands.ProduceString.class);
    assertOk("repl groovy");
    String result = assertOk("(produce | { String it -> context.provide(it) })()");
    assertEquals("foobar", result);
  }

  public void testReturnValueInClosure() {
    lifeCycle.bindClass("produce", Commands.ProduceString.class);
    assertOk("repl groovy");
    String result = assertOk("(produce | { String it -> it })()");
    assertEquals("foobar", result);
  }

  public void testClosureInPipe() {
    lifeCycle.bindClass("produce", Commands.ProduceString.class);
    lifeCycle.bindClass("consume", Commands.ConsumeString.class);
    assertOk("repl groovy");
    Commands.list.clear();
    assertOk("(produce | { String it -> '_' + it + '_' } | consume)()");
    assertEquals(Arrays.<Object>asList("_foo_", "_bar_"), Commands.list);
  }

  public void testCommandInClosure() {
    lifeCycle.bindClass("produce", Commands.ProduceString.class);
    lifeCycle.bindClass("value", Commands.ProduceInteger.class);
    lifeCycle.bindClass("consume", Commands.ConsumeInteger.class);
    assertOk("repl groovy");
    Commands.list.clear();
    assertOk("(produce | { String it -> value(); } | consume)()");
    assertEquals(Arrays.<Object>asList(3, 3), Commands.list);
  }

  public void testSubordinateCommandInClosure() {
    lifeCycle.bindClass("produce", Commands.ProduceString.class);
    lifeCycle.bindClass("value", Commands.SubordinateProduceInteger.class);
    lifeCycle.bindClass("consume", Commands.ConsumeInteger.class);
    assertOk("repl groovy");
    Commands.list.clear();
    assertOk("(produce | { String it -> value.sub(); } | consume)()");
    assertEquals(Arrays.<Object>asList(3, 3), Commands.list);
  }

  public void testSubCommandInClosure() {
    lifeCycle.bindClass("produce", Commands.ProduceInteger.class);
    lifeCycle.bindGroovy("toto", "public class toto {\n" +
        "@Command\n" +
        "public void sub(InvocationContext<String> c) {\n" +
        "c.provide('foo');\n" +
        "}\n" +
        "}");
    lifeCycle.bindClass("consume", Commands.ConsumeString.class);
    assertOk("repl groovy");
    Commands.list.clear();
    assertOk("(produce | { Integer it -> toto.sub(); } | consume)()");
    assertEquals(Arrays.<Object>asList("foo"), Commands.list);
  }

  public static class Toto extends BaseCommand {
    @Command
    public String sub() {
      return "invoked";
    }
    @Command
    public String find() {
      return "find_invoked";
    }
  }

  public void testSubCommand() {
    lifeCycle.bindClass("toto", Toto.class);
    assertOk("repl groovy");
    String result = assertOk("toto.sub()");
    assertEquals("invoked", result);
  }

  public void testSubCommandOverridesGDK() {
    lifeCycle.bindClass("toto", Toto.class);
    assertOk("repl groovy");
    String result = assertOk("toto.find()");
    assertEquals("find_invoked", result);
    assertOk("toto.find");
  }

  public void testProvideToContext() {
    assertOk("repl groovy");
    String result = assertOk("context << 'hello'");
    assertTrue(result.startsWith("hello"));
  }

  public void testPipe() {
    lifeCycle.bindClass("produce", Commands.ProduceString.class);
    lifeCycle.bindClass("consume", Commands.ConsumeString.class);
    assertOk("repl groovy");
    Commands.list.clear();
    assertOk("(produce | consume)()");
    assertEquals(Arrays.<Object>asList("foo", "bar"), Commands.list);
  }

  public void testMethodOptionBinding() {
    lifeCycle.bindClass("parameterized", Commands.Parameterized.class);
    assertOk("repl groovy");
    Commands.Parameterized.reset();
    assertOk("a = parameterized { opt = 'foo_opt' }");
    assertEquals(null, Commands.Parameterized.opt);
    assertEquals(null, Commands.Parameterized.args);
    assertOk("a()");
    assertEquals("foo_opt", Commands.Parameterized.opt);
    assertEquals(null, Commands.Parameterized.args);
  }

  public void testMethodArgumentBinding() {
    lifeCycle.bindClass("parameterized", Commands.Parameterized.class);
    assertOk("repl groovy");
    Commands.Parameterized.reset();
    assertOk("a = parameterized { ['arg1', 'arg2'] }");
    assertEquals(null, Commands.Parameterized.opt);
    assertEquals(null, Commands.Parameterized.args);
    assertOk("a()");
    assertEquals(null, Commands.Parameterized.opt);
    assertEquals(Arrays.asList("arg1", "arg2"), Commands.Parameterized.args);
  }

  public void testMethodOptionBindingMethodArgumentBinding() {
    lifeCycle.bindClass("parameterized", Commands.Parameterized.class);
    assertOk("repl groovy");
    Commands.Parameterized.reset();
    assertOk("a = parameterized { opt = 'foo_opt'; ['arg1', 'arg2'] }");
    assertEquals(null, Commands.Parameterized.opt);
    assertEquals(null, Commands.Parameterized.args);
    assertOk("a()");
    assertEquals("foo_opt", Commands.Parameterized.opt);
    assertEquals(Arrays.asList("arg1", "arg2"), Commands.Parameterized.args);
  }

  public static class ClassOptionBindingSubordinate extends BaseCommand {

    /** . */
    public static String opt;

    @Option(names = "o")
    public String option;

    public ClassOptionBindingSubordinate() {
      System.out.println("zeezf");
    }

    @Command
    public void sub() {
      opt = option;
    }
  }

  public void testClassOptionBindingSubordinate() {
    lifeCycle.bindClass("cmd", ClassOptionBindingSubordinate.class);
    assertOk("repl groovy");
    assertOk("a = cmd { o = 'foo_opt'; }");
    //assertOk("a.sub()");
    //assertEquals("foo_opt", Commands.Parameterized.opt);
  }

  public static class ClassOptionBinding extends BaseCommand {

    /** . */
    public static String opt;

    @Option(names = "o")
    public String option;

    public ClassOptionBinding() {
      System.out.println("zeezf");
    }

    @Command
    public void main() {
      opt = option;
    }
  }

  public void testClassOptionBinding() {
    lifeCycle.bindClass("cmd", ClassOptionBinding.class);
    assertOk("repl groovy");
    assertOk("a = cmd { o = 'foo_opt'; }");
    assertOk("a()");
    //assertNotEquals("foo_opt", Commands.Parameterized.opt);
  }

  public void testInClosure() {
    lifeCycle.bindClass("cmd", Commands.ProduceString.class);
    assertOk("repl groovy");
    String s = assertOk("[0].each { cmd() }");
    assertEquals("foobar[0]", s);
  }

  public void testSubordinateInClosure() {
    lifeCycle.bindClass("cmd", Commands.SubordinateProduceInteger.class);
    assertOk("repl groovy");
    String s = assertOk("[0].each { cmd.sub() }");
    assertEquals("3[0]", s);
  }
}

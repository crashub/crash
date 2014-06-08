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
package org.crsh.command.base;

import org.crsh.shell.AbstractShellTestCase;
import org.crsh.text.renderers.BindingRenderer;

import javax.naming.Context;
import java.util.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class JNDICommandTestCase extends AbstractShellTestCase {

  private String defaultFactory;
  public static List<BindingRenderer.BindingData> output = new ArrayList<BindingRenderer.BindingData>();

  private final String consume_command = "class consume_command {\n" +
      "@Command\n" +
      "public org.crsh.command.Pipe<org.crsh.text.renderers.BindingRenderer.BindingData, Object> main() {\n" +
      "return new org.crsh.command.Pipe<org.crsh.text.renderers.BindingRenderer.BindingData, Object>() {\n" +
      "public void provide(org.crsh.text.renderers.BindingRenderer.BindingData element) {\n" +
      "org.crsh.command.base.JNDICommandTestCase.output.add(element)\n" +
      "}\n" +
      "}\n" +
      "}\n" +
      "}";

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if (defaultFactory == null) {
      System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
    } else {
      System.setProperty(Context.INITIAL_CONTEXT_FACTORY, defaultFactory);
    }
  }

  public void testSimple() throws Exception {
    setFactory("org.crsh.command.base.factory.SimpleInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find | consume_command");
    assertEquals(2, output.size());
    assertEquals("Foo", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
    assertEquals("java:global/Foo", output.get(1).name);
    assertEquals("Bar", output.get(1).type);
  }

  public void testNested() throws Exception {
    setFactory("org.crsh.command.base.factory.NestedInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find | consume_command");
    assertEquals(3, output.size());
    assertEquals("java:global/Foo", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
    assertEquals("java:global/Foo2", output.get(1).name);
    assertEquals("Bar2", output.get(1).type);
    assertEquals("java:global/Foo2/Foo", output.get(2).name);
    assertEquals("Bar", output.get(2).type);
  }

  public void testError() throws Exception {
    setFactory("org.crsh.command.base.factory.ErrorInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find | consume_command");
    assertEquals(2, output.size());
    assertEquals("Empty", output.get(0).name);
    assertEquals("Empty2", output.get(0).type);
    assertEquals("java:module/Module", output.get(1).name);
    assertEquals("Module2", output.get(1).type);
  }

  public void testFilter() throws Exception {
    setFactory("org.crsh.command.base.factory.TypedInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -f java.lang.String | consume_command");
    assertEquals(1, output.size());
    assertEquals("String", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
  }

  public void testFilterMany() throws Exception {
    setFactory("org.crsh.command.base.factory.TypedInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -f java.lang.String -f java.util.List | consume_command");
    assertEquals(2, output.size());
    assertEquals("String", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
    assertEquals("ArrayList", output.get(1).name);
    assertEquals("Bar", output.get(1).type);
  }

  public void testFilterInterface() throws Exception {
    setFactory("org.crsh.command.base.factory.TypedInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -f java.util.List | consume_command");
    assertEquals(1, output.size());
    assertEquals("ArrayList", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
  }

  public void testFilterSuperType() throws Exception {
    setFactory("org.crsh.command.base.factory.TypedInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -f java.util.AbstractList | consume_command");
    assertEquals(1, output.size());
    assertEquals("ArrayList", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
  }

  public void testNameExact() throws Exception {
    setFactory("org.crsh.command.base.factory.SimpleInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -n Foo | consume_command");
    assertEquals(1, output.size());
    assertEquals("Foo", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
  }

  public void testNameBegin() throws Exception {
    setFactory("org.crsh.command.base.factory.SimpleInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -n F* | consume_command");
    assertEquals(1, output.size());
    assertEquals("Foo", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
  }

  public void testNameEnd() throws Exception {
    setFactory("org.crsh.command.base.factory.SimpleInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -n *o | consume_command");
    assertEquals(2, output.size());
    assertEquals("Foo", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
    assertEquals("java:global/Foo", output.get(1).name);
    assertEquals("Bar", output.get(1).type);
  }

  public void testNameNoBeginEnd() throws Exception {
    setFactory("org.crsh.command.base.factory.SimpleInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -n *global* | consume_command");
    assertEquals(1, output.size());
    assertEquals("java:global/Foo", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
  }

  public void testNameWildcard() throws Exception {
    setFactory("org.crsh.command.base.factory.SimpleInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -n java:*/Foo | consume_command");
    assertEquals(1, output.size());
    assertEquals("java:global/Foo", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
  }

  public void testNameWildcardBeginEnd() throws Exception {
    setFactory("org.crsh.command.base.factory.SimpleInitialContextFactory");
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("jndi find -n *:*/* | consume_command");
    assertEquals(1, output.size());
    assertEquals("java:global/Foo", output.get(0).name);
    assertEquals("Bar", output.get(0).type);
  }

  private void setFactory(String name) {
    defaultFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, name);
  }

}

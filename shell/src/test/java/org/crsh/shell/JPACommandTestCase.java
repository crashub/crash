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

import org.crsh.shell.entities.Bar;
import org.crsh.shell.entities.Foo;
import org.crsh.shell.entities.Foo2;
import org.crsh.text.renderers.EntityTypeRenderable;

import javax.naming.Context;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class JPACommandTestCase extends AbstractCommandTestCase {

  private String defaultFactory;
  public static List<EntityTypeRenderable.EntityTypeData> output_entity = new ArrayList<EntityTypeRenderable.EntityTypeData>();
  public static List<Map> output_value = new ArrayList<Map>();

  private final String consume_command_entity = "class consume_command_entity {\n" +
      "@Command\n" +
      "public org.crsh.command.PipeCommand<org.crsh.text.renderers.EntityTypeRenderable.EntityTypeData, Object> main() {\n" +
      "return new org.crsh.command.PipeCommand<org.crsh.text.renderers.EntityTypeRenderable.EntityTypeData, Object>() {\n" +
      "public void provide(org.crsh.text.renderers.EntityTypeRenderable.EntityTypeData element) {\n" +
      "org.crsh.shell.JPACommandTestCase.output_entity.add(element)\n" +
      "}\n" +
      "}\n" +
      "}\n" +
      "}";

  private final String consume_command_value = "class consume_command_value {\n" +
      "@Command\n" +
      "public org.crsh.command.PipeCommand<Map, Object> main() {\n" +
      "return new org.crsh.command.PipeCommand<Map, Object>() {\n" +
      "public void provide(Map element) {\n" +
      "org.crsh.shell.JPACommandTestCase.output_value.add(element)\n" +
      "}\n" +
      "}\n" +
      "}\n" +
      "}";

  private final Comparator entityComparator = new Comparator<EntityTypeRenderable.EntityTypeData>() {
    public int compare(EntityTypeRenderable.EntityTypeData o1, EntityTypeRenderable.EntityTypeData o2) {
      return o1.name.compareTo(o2.name);
    }
  };
  private final Comparator attributeComparator = new Comparator<EntityTypeRenderable.AttributeData>() {
    public int compare(EntityTypeRenderable.AttributeData o1, EntityTypeRenderable.AttributeData o2) {
      return o1.name.compareTo(o2.name);
    }
  };

  @Override
  public void setUp() throws Exception {
    super.setUp();
    defaultFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.crsh.shell.factory.JPAInitialContextFactory");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if (defaultFactory == null) {
      System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
    } else {
      System.setProperty(Context.INITIAL_CONTEXT_FACTORY, defaultFactory);
    }
  }

  public void testOpenClose() throws Exception {
    
    //
    assertError("jpa close", ErrorType.EVALUATION);
    
    //
    assertError("jpa open none", ErrorType.EVALUATION);
    assertOk("jpa open testEmf");
    assertError("jpa open testEmf", ErrorType.EVALUATION);

    //
    assertOk("jpa close");
    assertError("jpa close", ErrorType.EVALUATION);
    
  }

  public void testEntities() throws Exception {
    output_entity.clear();
    lifeCycle.bindGroovy("consume_command_entity", consume_command_entity);
    assertOk("jpa open testEmf");
    assertOk("jpa entities | consume_command_entity");
    Collections.sort(output_entity, entityComparator);
    assertEquals(3, output_entity.size());
    assertTrue(output_entity.get(0).name.endsWith("Bar"));
    assertEquals("org.crsh.shell.entities.Bar", output_entity.get(0).type);
    assertEquals("ENTITY", output_entity.get(0).mapping);
    assertEquals(false, output_entity.get(0).verbose);
    assertTrue(output_entity.get(1).name.endsWith("Foo"));
    assertEquals("org.crsh.shell.entities.Foo", output_entity.get(1).type);
    assertEquals("ENTITY", output_entity.get(1).mapping);
    assertEquals(false, output_entity.get(1).verbose);
    assertOk("jpa close");
  }

  public void testEntity() throws Exception {
    output_entity.clear();
    lifeCycle.bindGroovy("consume_command_entity", consume_command_entity);
    assertOk("jpa open testEmf");
    assertError("jpa entity None", ErrorType.EVALUATION);
    assertOk("jpa entity " + Foo.class.getName() + " | consume_command_entity");
    assertEquals(1, output_entity.size());
    assertTrue(output_entity.get(0).name.endsWith("Foo"));
    assertEquals("org.crsh.shell.entities.Foo", output_entity.get(0).type);
    assertEquals("ENTITY", output_entity.get(0).mapping);
    assertEquals(true, output_entity.get(0).verbose);
    Collections.sort(output_entity.get(0).attributes, attributeComparator);
    assertEquals(4, output_entity.get(0).attributes.size());
    assertEquals("created", output_entity.get(0).attributes.get(1).name);
    assertEquals(Calendar.class.getName(), output_entity.get(0).attributes.get(1).type);
    assertEquals("BASIC", output_entity.get(0).attributes.get(1).mapping);
    assertEquals(false, output_entity.get(0).attributes.get(1).association.booleanValue());
    assertEquals(false, output_entity.get(0).attributes.get(1).collection.booleanValue());
    assertEquals("id", output_entity.get(0).attributes.get(2).name);
    assertEquals(Long.class.getName(), output_entity.get(0).attributes.get(2).type);
    assertEquals("BASIC", output_entity.get(0).attributes.get(2).mapping);
    assertEquals(false, output_entity.get(0).attributes.get(2).association.booleanValue());
    assertEquals(false, output_entity.get(0).attributes.get(2).collection.booleanValue());
    assertEquals("name", output_entity.get(0).attributes.get(3).name);
    assertEquals(String.class.getName(), output_entity.get(0).attributes.get(3).type);
    assertEquals("BASIC", output_entity.get(0).attributes.get(3).mapping);
    assertEquals(false, output_entity.get(0).attributes.get(3).association.booleanValue());
    assertEquals(false, output_entity.get(0).attributes.get(3).collection.booleanValue());
    assertOk("jpa close");
  }

  public void testSelect() throws Exception {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("testPU");
    EntityManager em = emf.createEntityManager();
    em.getTransaction().begin();
    em.persist(new Bar());
    em.persist(new Bar());
    em.getTransaction().commit();

    em.getTransaction().begin();
    em.persist(new Foo("foo", Calendar.getInstance()));
    Foo foo = new Foo("bar", Calendar.getInstance());
    foo.setBar(em.find(Bar.class, 1L));
    em.persist(foo);
    Foo2 foo2 = new Foo2();
    foo2.setBars(Arrays.asList(em.find(Bar.class, 1L), em.find(Bar.class, 2L)));
    em.persist(foo2);
    em.persist(new Foo2());
    em.getTransaction().commit();
    em.close();

    output_value.clear();
    lifeCycle.bindGroovy("consume_command_value", consume_command_value);
    assertOk("jpa open testEmf");
    assertOk("jpa select f FROM Foo f order by f.id | consume_command_value");
    assertEquals(2, output_value.size());
    assertEquals("1", output_value.get(0).get("*id").toString());
    assertEquals("foo", output_value.get(0).get("name"));
    assertEquals("<null>", output_value.get(0).get("bar"));
    assertNotNull(output_value.get(0).get("created"));
    assertEquals("2", output_value.get(1).get("*id").toString());
    assertEquals("bar", output_value.get(1).get("name"));
    assertNotNull(output_value.get(1).get("created"));
    assertNotNull(output_value.get(1).get("bar"));
    assertEquals(Bar.class.getName() + "[id=1]", output_value.get(1).get("bar"));

    output_value.clear();
    assertOk("jpa select f FROM Foo2 f order by f.id | consume_command_value");
    assertEquals(2, output_value.size());
    assertEquals("1", output_value.get(0).get("*id".toString()));
    assertEquals("{" + Bar.class.getName() + "[id=1]," + Bar.class.getName() + "[id=2]}", output_value.get(0).get("bars"));
    assertEquals("2", output_value.get(1).get("*id".toString()));
    assertEquals("{}", output_value.get(1).get("bars"));


    assertOk("jpa close");
  }
  
}

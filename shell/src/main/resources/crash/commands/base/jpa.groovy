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

package crash.commands.base

import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.command.InvocationContext
import org.crsh.util.Utils

import javax.naming.InitialContext
import org.crsh.cli.Argument
import org.crsh.text.renderers.EntityTypeRenderer
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion
import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.util.JNDIHandler

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@Usage("Java persistance API")
class jpa implements Completer {

  @Usage("Open a JPA session")
  @Command
  Object open(@Argument(completer = jpa.EmfCompleter.class) String jndiName) {

    if (em != null) {
      throw new ScriptException("Already connected");
    }

    if (jndiName == null) {
      throw new ScriptException("JNDI resource is required");
    }

    InitialContext ic = new InitialContext();
    def emf = ic.lookup(jndiName);

    if (emf == null) {
      throw new ScriptException("${jndiName} doesn't exist");
    }
    
    em = emf.createEntityManager();

    return "Using $jndiName entity manager factory\n";
  }

  @Usage("Close the current JPA session")
  @Command
  Object close() {
    if (em == null) {
      throw new ScriptException("Not connected");
    } else {
      em = null;
      return "Connection closed\n";
    }
  }

  @Usage("List JPA entities")
  @Command
  void entities(InvocationContext<EntityTypeRenderer.EntityTypeData> context) {
    em.metamodel.entities.each { e ->
      context.provide(new EntityTypeRenderer.EntityTypeData(e.name, e.javaType.name, e.persistenceType.toString()));
    }
  }

  @Usage("Display JPA entity")
  @Command
  void entity(InvocationContext<EntityTypeRenderer.EntityTypeData> context, @Argument String name) {
    def en;
      em.metamodel.entities.each { e ->
      if (e.name.equals(name)) {
        en = e;
      }
    }
    if (en == null) {
      throw new ScriptException("${name} is not an entity");
    }
    
    def etd = new EntityTypeRenderer.EntityTypeData(en.name, en.javaType.name, en.persistenceType.toString(), true)
    en.attributes.each { a ->
      etd.add(new EntityTypeRenderer.AttributeData(a.name, a.javaType.name, a.association, a.collection, a.persistentAttributeType.toString()));
    }
    context.provide(etd);
  }

  @Usage("Execute select JPA query")
  @Command
  void select(InvocationContext<Map> context, @Argument List<String> statements) {

    def query = "SELECT";
    statements.each { s ->
      query += " " + s
    }

    q = em.createQuery(query);

    q.resultList.each { r ->
      type = em.metamodel.entity(r.class);
      Map idColumn = new LinkedHashMap();
      Map valueColumn = new LinkedHashMap();
      type.attributes.each { a ->
        if (!a.collection && a.id) {
          addValue(a, r, idColumn)
        } else {
          addValue(a, r, valueColumn)
        }
      }
      idColumn.putAll(valueColumn);
      context.provide(idColumn);
    }

  }

  void addValue(attribute, row, result) {
    if (!attribute.collection) {
      if (Utils.instanceOf(attribute.type.class, "javax.persistence.metamodel.EntityType")) {
        if (row."${attribute.name}" != null) {
          result.put(attribute.name, formatEntity(attribute.type, row."${attribute.name}"))
        } else {
          result.put(attribute.name, "<null>");
        }
      } else {
        def value = String.valueOf(row."${attribute.name}");
        if (value.length() > 50) {
          value = value.substring(0, 47) + "...";
        }
        if (attribute.id) {
          result.put("*" + attribute.name, value)
        } else {
          result.put(attribute.name, value)
        }
      }
    } else {
      result.put(attribute.name, formatEntities(attribute.elementType, row."${attribute.name}"))
    }
  }

  String formatEntity(entity, instance) {
    def ids = "";
    entity.attributes.each { a ->
      if (Utils.instanceOf(a.class, "javax.persistence.metamodel.SingularAttribute") && a.id) {
        ids += a.name + "=" + instance."${a.name}" + ","
      }
    }
    return "${entity.name}[${ids.substring(0, ids.length() - 1)}]";
  }

  String formatEntities(entity, collection) {
    if (collection.size() == 0) {
      return "{}";
    }
    def entities = "";
    collection.each { instance ->
      entities += formatEntity(entity, instance) + ","
    }
    return "{${entities.substring(0, entities.length() - 1)}}";
  }

  public static class EmfCompleter implements Completer  {
    Completer c = new JNDIHandler.JNDICompleter("javax.persistence.EntityManagerFactory");
    Completion complete(ParameterDescriptor parameter, String prefix) {
      return c.complete(parameter, prefix);
    }
  }

  Completion complete(ParameterDescriptor parameter, java.lang.String prefix) throws Exception {
    def builder = new Completion.Builder(prefix);
      em.metamodel.entities.each { e ->
        if (prefix == null || prefix.length() == 0) {
          builder.add(e.name, true);
        } else if (e.name.startsWith(prefix)) {
          builder.add(e.name.substring(prefix.length()), true);
        }
      }
      return builder.build()
    }
  
}
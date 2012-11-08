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

import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import javax.naming.InitialContext
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Argument
import org.crsh.text.formatter.EntityTypeRenderable
import org.crsh.cmdline.spi.Completer
import org.crsh.cmdline.spi.Completion
import org.crsh.cmdline.ParameterDescriptor
import org.crsh.util.JNDIHandler
import com.sun.tools.jdi.LinkedHashMap

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@Usage("Java persistance API")
class jpa extends CRaSHCommand implements Completer {

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
  void entities(InvocationContext<EntityTypeRenderable.EntityTypeData> context) {
    em.metamodel.entities.each { e ->
      context.provide(new EntityTypeRenderable.EntityTypeData(e.name, e.javaType.name, e.persistenceType.toString()));
    }
  }

  @Usage("Display JPA entity")
  @Command
  void entity(InvocationContext<EntityTypeRenderable.EntityTypeData> context, @Argument(completer = jpa.class) String name) {
    def en;
    em.metamodel.entities.each { e ->
      if (e.name.equals(name)) {
        en = e;
      }
    }
    if (en == null) {
      throw new ScriptException("${name} is not an entity");
    }
    
    def etd = new EntityTypeRenderable.EntityTypeData(en.name, en.javaType.name, en.persistenceType.toString(), true)
    en.attributes.each { a ->
      etd.add(new EntityTypeRenderable.AttributeData(a.name, a.javaType.name, a.association, a.collection, a.persistentAttributeType.toString()));
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
      Map result = new LinkedHashMap();
      type.attributes.each { a ->
        result.put(a.name, r."${a.name}");
      }
      context.provide(result);
    }

  }

  public static class EmfCompleter implements Completer {
    Completion complete(ParameterDescriptor<?> parameter, java.lang.String prefix) {
      def builder = new Completion.Builder(prefix);
      def pattern = (prefix != null && prefix.length() > 0 ? prefix + "*" : null);
      JNDIHandler.lookup(["javax.persistence.EntityManagerFactory"], pattern, null).each { d ->
        if (pattern == null) {
          builder.add(d.name, true)
        } else {
          builder.add(d.name.substring(pattern.length() - 1), true)
        }
      }
      return builder.build();
    }
  }

  Completion complete(ParameterDescriptor<?> parameter, java.lang.String prefix) {
    def builder = new Completion.Builder(prefix);
      em.metamodel.entities.each { e ->
        if (prefix == null) {
          builder.add(e.name, true);
        } else if (e.name.startWith(prefix)) {
          builder.add(e.name.substring(prefix.length() - 1), true);
        }
      }
      return builder.build()
    }
  
}
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
import javax.naming.Context
import org.crsh.text.formatter.BindingRenderable
import org.crsh.text.formatter.BindingRenderable.BindingData
import org.crsh.util.TypeResolver
import org.crsh.cmdline.annotations.Option

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@Usage("Java Naming and Directory Interface")
class jndi extends CRaSHCommand {

    @Usage("List JNDI resources")
    @Command
    void ls(InvocationContext<BindingRenderable.BindingData> context, @Option(names=["f","filter"]) String filter) {
        print(context, filter);
    }
    
    @Usage("List DataSource resources")
    @Command
    void datasources(InvocationContext<BindingRenderable.BindingData> context) {
        print(context, "javax.sql.DataSource");
    }
    
    @Usage("List Entity Manager Factory resources")
    @Command
    void emf(InvocationContext<BindingRenderable.BindingData> context) {
        print(context, "javax.persistence.EntityManagerFactory");
    }
    
    @Usage("List Mail resources")
    @Command
    void mail(InvocationContext<BindingRenderable.BindingData> context) {
        print(context, "javax.mail.Session");
    }
    
    void print(InvocationContext<BindingRenderable.BindingData> context, String filter) {
        add(context, filter, "");
        add(context, filter, "java:/");
        add(context, filter, "java:comp/env/jdbc");
        add(context, filter, "java:jboss");
        add(context, filter, "java:global");
        add(context, filter, "java:app");
        add(context, filter, "java:module");
    }

    void add(InvocationContext<BindingRenderable.BindingData> context, String filter, String path) {
        add(context, filter, path, path, null)
    }

    void add(InvocationContext<BindingRenderable.BindingData> context, String filter, String path, String search, Context ctx) {
        try {
            if (ctx == null) {
                ctx = new InitialContext();
            }
            if (path.length() > 0) {
                path += "/";
            }

            ctx.listBindings(search).each { instance ->
                try {
                    if (filter == null || TypeResolver.instanceOf(instance.object.class, filter)) {
                        context.provide(new BindingData(path + instance.name, instance.className));
                    }
                } catch (ClassCastException e) {}
                if (instance.object instanceof Context) {
                    add(context, filter, path + instance.name, "", instance.object);
                }
            }
        } catch(Exception e) {}
    }

}

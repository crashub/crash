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
import org.crsh.cli.Option
import org.crsh.text.renderers.BindingRenderer
import org.crsh.util.JNDIHandler

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@Usage("Java Naming and Directory Interface")
class jndi {

    @Usage("List JNDI resources")
    @Command
    void find(
            InvocationContext<BindingRenderer.BindingData> context,
            @Usage("Filter displayed resources using FQN type'") @Option(names=["f","filter"]) List<String> filters,
            @Usage("Filter displayed resources using name'") @Option(names=["n","name"]) String name,
            @Usage("Display resource type'") @Option(names=["v", "verbose"]) Boolean verbose,
            @Usage("Apply a filter on 'javax.sql.DataSource'") @Option(names=["d", "datasources"]) Boolean datasources,
            @Usage("Apply a filter on 'javax.persistence.EntityManagerFactory'") @Option(names=["e", "emf"]) Boolean emf,
            @Usage("Apply a filter on 'javax.mail.Session'") @Option(names=["m", "mail"]) Boolean mail) {

        if (datasources) {
            if (filters == null) filters = new ArrayList<String>();
            filters.add("javax.sql.DataSource");
        }

        if (emf) {
            if (filters == null) filters = new ArrayList<String>();
            filters.add("javax.persistence.EntityManagerFactory");
        }

        if (mail) {
            if (filters == null) filters = new ArrayList<String>();
            filters.add("javax.mail.Session");
        }

        JNDIHandler.lookup(filters, name, verbose).each { d -> context.provide(d) }

    }

}

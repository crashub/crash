/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.crsh.netty;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import org.crsh.jcr.NodeMetaClass;
import org.crsh.servlet.ServletShellContext;
import org.crsh.shell.ShellBuilder;
import org.crsh.netty.TelnetPipelineFactory;
import org.crsh.netty.TelnetServerHandler;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import javax.jcr.Node;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.beans.IntrospectionException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CrshLifeCycle implements ServletContextListener
{

   /** . */
   private Channel channel;

   public void contextInitialized(ServletContextEvent sce)
   {
      // Integrate
      try
      {
         MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
         Class<? extends Node> eXoNode = (Class<Node>)Thread.currentThread().getContextClassLoader().loadClass("org.exoplatform.services.jcr.impl.core.NodeImpl");
         NodeMetaClass mc2 = new NodeMetaClass(registry, eXoNode);
         mc2.initialize();
         registry.setMetaClass(eXoNode, mc2);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
      catch (IntrospectionException e)
      {
         throw new RuntimeException(e);
      }


      //
      ServerBootstrap bootstrap = new ServerBootstrap(
         new NioServerSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool()
         )
      );

      //
      ServletShellContext context = new ServletShellContext(sce.getServletContext(), Thread.currentThread().getContextClassLoader());

      //
      ShellBuilder builder = new ShellBuilder(context);

      //
      TelnetServerHandler handler = new TelnetServerHandler(builder);
      bootstrap.setPipelineFactory(new TelnetPipelineFactory(handler));

      // Bind and start to accept incoming connections.
      channel = bootstrap.bind(new InetSocketAddress(5000));
   }


   public void contextDestroyed(ServletContextEvent sce)
   {
      if (channel != null)
      {
         channel.close();
      }
   }
}
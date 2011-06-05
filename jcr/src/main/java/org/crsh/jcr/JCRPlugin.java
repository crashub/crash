/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.jcr;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.Service;

import javax.jcr.Node;
import java.beans.IntrospectionException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JCRPlugin extends CRaSHPlugin<JCRPlugin> implements Service
{

   /** . */
   private static final Object LOCK = new Object();

   /** . */
   private static boolean integrated = false;

  @Override
  public JCRPlugin getImplementation() {
    return this;
  }

  @Override
   public void init()
   {
      // Force integration of node meta class
      NodeMetaClass.setup();

      //
      synchronized (LOCK) {
        if (!integrated) {
          try {
            MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
            Class<? extends Node> eXoNode = (Class<Node>)Thread.currentThread().getContextClassLoader().loadClass("org.exoplatform.services.jcr.impl.core.NodeImpl");
            NodeMetaClass mc2 = new NodeMetaClass(registry, eXoNode);
            mc2.initialize();
            registry.setMetaClass(eXoNode, mc2);
          }
          catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
          catch (IntrospectionException e) {
            throw new RuntimeException(e);
          }
        }
        integrated = true;
      }
   }
}

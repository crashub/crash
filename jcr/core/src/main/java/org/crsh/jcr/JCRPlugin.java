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
package org.crsh.jcr;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import org.crsh.jcr.groovy.NodeMetaClass;
import org.crsh.plugin.CRaSHPlugin;

import javax.jcr.Node;
import java.beans.IntrospectionException;
import java.util.*;
import javax.jcr.Repository;

public abstract class JCRPlugin<T extends JCRPlugin> extends CRaSHPlugin<T> {

  public static Repository findRepository(Map<String, String> properties) throws Exception {
    for (JCRPlugin plugin : ServiceLoader.load(JCRPlugin.class)) {
      Repository repository = plugin.getRepository(properties);
      if (repository != null) {
        return repository;
      }
    }
    return null;
  }

  public static Iterable<JCRPlugin> findRepositories() throws Exception {
    return ServiceLoader.load(JCRPlugin.class);
  }

  /** . */
  private static final Collection<String> NODES = Arrays.asList(
      "org.exoplatform.services.jcr.impl.core.NodeImpl",
      "org.apache.jackrabbit.core.NodeImpl", "org.apache.jackrabbit.rmi.client.ClientNode",
      "org.apache.jackrabbit.rmi.server.ServerNode");

  /** . */
  private static final Object LOCK = new Object();

  /** . */
  private static boolean integrated = false;

  public Collection<String> getNodeClassNames() {
    return NODES;
  }
  
  public abstract Repository getRepository(Map<String, String> properties) throws Exception;

  public abstract String getName();

  public abstract String getDisplayName();

  public abstract String getUsage();

  @Override
  public void init() {
    // Force integration of node meta class
    NodeMetaClass.setup();
    synchronized (LOCK) {
      if (!integrated) {
        try {
          MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
          Collection<Class<? extends Node>> nodes = loadAvailablesNodeImplementations(getNodeClassNames());
          for (Class<? extends Node> nodeClass : nodes) {
            registerNodeImplementation(registry, nodeClass);
          }
        } catch (IntrospectionException e) {
          throw new RuntimeException(e);
        }
      }
      integrated = true;
    }
  }

  private Collection<Class<? extends Node>> loadAvailablesNodeImplementations(
      Collection<String> classNames) {
    List<Class<? extends Node>> classes = new ArrayList<Class<? extends Node>>(classNames.size());
    for (String className : classNames) {
      Class<? extends Node> nodeClass = loadNodeImplementation(className);
      if (nodeClass != null) {
        classes.add(nodeClass);
      }
    }
    return classes;
  }

  private Class<? extends Node> loadNodeImplementation(String className) {
    try {
      return (Class<? extends Node>) Thread.currentThread().getContextClassLoader().loadClass(
          className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private void registerNodeImplementation(MetaClassRegistry registry,
      Class<? extends Node> nodeClass) throws IntrospectionException {
    NodeMetaClass mc2 = new NodeMetaClass(registry, nodeClass);
    mc2.initialize();
    registry.setMetaClass(nodeClass, mc2);
  }
}

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

package org.crsh.jcr.command;

import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.completers.AbstractPathCompleter;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;
import org.crsh.command.CRaSHCommand;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class JCRCommand extends CRaSHCommand implements PathCompleter {

  protected JCRCommand() throws IntrospectionException {
  }

  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    if (parameter.getCompleterType() == PathCompleter.class) {

      final Path path = (Path)getProperty("currentPath");
      final Session session = (Session)getProperty("session");

      //
      if (session != null) {

        AbstractPathCompleter<Node> pc = new AbstractPathCompleter<Node>() {
          @Override
          protected String getCurrentPath() throws Exception {
            return path != null ? path.getValue() : "/";
          }

          @Override
          protected Node getPath(String path) throws Exception {
            try {
              return (Node)session.getItem(path);
            }
            catch (RepositoryException e) {
              return null;
            }
          }

          @Override
          protected boolean exists(Node path) throws Exception {
            return path != null;
          }

          @Override
          protected boolean isDirectory(Node path) throws Exception {
            return true;
          }

          @Override
          protected boolean isFile(Node path) throws Exception {
            return false;
          }

          @Override
          protected Collection<Node> getChilren(Node path) throws Exception {
            List<Node> children = new ArrayList<Node>();
            for (NodeIterator i = path.getNodes();i.hasNext();) {
              Node child = i.nextNode();
              children.add(child);
            }
            return children;
          }

          @Override
          protected String getName(Node path) throws Exception {
            return path.getName();
          }
        };

        //
        return pc.complete(parameter, prefix);
      }
    }

    //
    return Completion.create();
  }
}

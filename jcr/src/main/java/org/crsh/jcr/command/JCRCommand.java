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

package org.crsh.jcr.command;

import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.spi.Completer;
import org.crsh.command.CRaSHCommand;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class JCRCommand extends CRaSHCommand implements Completer {

  protected JCRCommand() throws IntrospectionException {
  }

  public Map<String, Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {
    if (parameter.getAnnotation() instanceof Path) {

      String path = (String)getProperty("currentPath");
      Session session = (Session)getProperty("session");

      //
      if (session != null) {

        Node relative = null;

        if (prefix.length() == 0 || prefix.charAt(0) != '/') {
          if (path != null) {
            Item item = session.getItem(path);
            if (item instanceof Node) {
              relative = (Node)item;
            }
          }
        } else {
          relative = session.getRootNode();
          prefix = prefix.substring(1);
        }

        // Now navigate using the prefix
        if (relative != null) {
          for (int index = prefix.indexOf('/');index != -1;index = prefix.indexOf('/')) {
            String name = prefix.substring(0, index);
            if (relative.hasNode(name)) {
              relative = relative.getNode(name);
              prefix = prefix.substring(index + 1);
            } else {
              return Collections.emptyMap();
            }
          }
        }

        // Compute the next possible completions
        Map<String, Boolean> completions = new HashMap<String, Boolean>();
        for (NodeIterator i = relative.getNodes(prefix + '*');i.hasNext();) {
          Node child = i.nextNode();
          String suffix = child.getName().substring(prefix.length());
          if (child.hasNodes()) {
            completions.put(suffix + '/', false);

          } else {
            completions.put(suffix, true);
          }
        }

        //
        return completions;
      }
    }

    //
    return Collections.emptyMap();
  }
}

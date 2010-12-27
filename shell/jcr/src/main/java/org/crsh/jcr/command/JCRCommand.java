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

import javax.jcr.Session;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class JCRCommand extends CRaSHCommand implements Completer {

  protected JCRCommand() throws IntrospectionException {
  }

  public List<String> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {
    if (parameter.getAnnotation() instanceof PathArg) {

      String path = (String)getProperty("currentPath");
      Session session = (Session)getProperty("session");

      //
      System.out.println("Computing completion from prefix " + prefix + " in context of path " + path + " with session " + session);

    }

    //
    return Collections.emptyList();
  }
}

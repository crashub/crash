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
package org.crsh.servlet;

import org.crsh.shell.ShellContext;
import org.crsh.util.IO;

import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ServletShellContext implements ShellContext
{

   /** . */
   private final ServletContext servletContext;

   /** . */
   private final ClassLoader loader;

   public ServletShellContext(ServletContext servletContext, ClassLoader loader)
   {
      if (servletContext == null)
      {
         throw new NullPointerException();
      }
      if (loader == null)
      {
         throw new NullPointerException();
      }
      
      //
      this.servletContext = servletContext;
      this.loader = loader;
   }

   public String loadScript(String scriptURI)
   {
      InputStream in = servletContext.getResourceAsStream("/WEB-INF/groovy/" + scriptURI);
      return in != null ? IO.readAsUTF8(in) : null;
   }

   public ClassLoader getLoader()
   {
      return loader;
   }
}

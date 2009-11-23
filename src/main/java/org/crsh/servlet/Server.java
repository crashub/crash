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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Server
{

   /** . */
   private ServerSocket server;
   
   /** . */
   private Thread acceptor;

   public Server() 
   {
   }
   
   public void start() throws Exception
   {
      this.server = new ServerSocket(5000);
      this.acceptor = new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               Socket socket = server.accept();
            }
            catch (IOException e)
            {
               
            }
         }
      };
      
      //
   }
   
   public void stop()
   {
      
   }
   
}

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
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Command
import org.crsh.command.PipeCommand
import org.crsh.cmdline.annotations.Option

/** 
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@Usage("Apply a filter on a map")
class filter extends CRaSHCommand {

  @Usage("Filter map before formatting")
  @Command
  PipeCommand<Map, Map> main(
       @Usage("format <key>:<value>")
       @Option(names=['e','equals']) List<String> eq) {

       return new PipeCommand<Map, Map>() {
           @Override
           void provide(Map element) {

               if (eq == null || eq.size() == 0) {
                   context.provide(element);
                   return;
               }

               for(String e : eq) {
                   if (e.contains(":")) {
                       e = e.trim();
                       int pos = e.indexOf(":");
                       String key = e.substring(0, pos);
                       String value = e.substring(pos + 1);
                       if (value.equals(element.get(key))) {
                           context.provide(element);
                           return;
                       }
                   } else {
                       context.provide(element);
                           return;
                   }
               }
           }

       }

  }

}
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

import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Usage
import org.crsh.command.Pipe

class sort {

  @Usage("sort a map")
  @Command
  Pipe<Map, Map> main(
      @Usage("Filed used to sort")
      @Option(names = ['f', 'fields']) List<String> fields) {
    return new Pipe<Map, Map>() {
      List<Map> d = new ArrayList<Map>();

      @Override
      void provide(Map element) {
        d.add(element);
      }

      @Override
      void flush() {
        Collections.sort(d, new EntryComparator(fields))
        d.each { m ->
          context.provide(m);
        }
        d.clear();
        super.flush();
      }
    }
  }

  class EntryComparator implements Comparator<Map> {

    List<String> fields;

    EntryComparator(List<String> fields) {
      this.fields = fields
    }

    int compare(Map o1, Map o2) {

      for (String field : fields) {

        int order = 1;
        if (field.endsWith(":asc")) {
          field = field.substring(0, field.length() - 4)
        }
        if (field.endsWith(":desc")) {
          field = field.substring(0, field.length() - 5)
          order = -1;
        }

        if (o1.containsKey(field) && o2.containsKey(field)) {
          def v1 = o1.get(field);
          def v2 = o2.get(field);
          if (v1 instanceof Comparable && v2 instanceof Comparable) {
            int r = v1.compareTo(v2);
            if (r != 0) {
              return r * order;
            }
          }
        }
        else {
          return 0;
        }
      }

      return 0;
    }

  }

}
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

import org.crsh.cli.Usage
import org.crsh.command.ScriptException
import org.crsh.cli.Command
import org.crsh.command.Pipe
import org.crsh.cli.Option
import java.util.regex.Pattern
import org.crsh.util.Utils

class filter {

  /** . */
  private static final String[] EMPTY_KEYS = new String[0];

  /** . */
  private static final Pattern[] EMPTY_VALUES = new Pattern[0];

  @Command
  @Usage("a filter for a stream of map")
  Pipe<Map, Map> main(
    @Usage("format <key>:<value>")
    @Option(names=['p','pattern']) List<String> patterns) {

    //
    String[] keys;
    Pattern[] values;
    if (patterns == null || patterns.empty) {
      keys = EMPTY_KEYS;
      values = EMPTY_VALUES;
    } else {
      HashMap<String, StringBuilder> tmp = new HashMap<String, StringBuilder>();
      patterns.each { pattern ->
        pattern = pattern.trim();
        int pos = pattern.indexOf(":");
        if (pos == -1) {
          throw new ScriptException("Bad pattern " + pattern);
        } else {
          String key = pattern.substring(0, pos);
          String value = pattern.substring(pos + 1);
          StringBuilder previous = tmp[key];
          if (previous == null) {
            tmp[key] = previous = new StringBuilder('(?:^');
          } else {
            previous.append('|(?:^');
          }
          previous.append(Utils.globexToRegex(value));
          previous.append('$)');
        }
      }
      keys = new String[tmp.size()];
      values = new Pattern[tmp.size()];
      tmp.eachWithIndex { key, value, i ->
        keys[i] = key;
        values[i] = Pattern.compile(value.toString());
      }
    }

    //
    return new Pipe<Map, Map>() {
      @Override
      void provide(Map element) {
        for (int i = 0;i < keys.length;i++) {
          Object value = element[keys[i]];
          if (value != null) {
            def chars = value instanceof CharSequence ? value : value.toString();;
            if (!values[i].matcher(chars).matches()) {
              return;
            }
          }
        }
        context.provide(element);
      }
    }
  }
}
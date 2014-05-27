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
package org.crsh.cli.completers;

import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;
import org.crsh.cli.type.ValueType;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Julien Viet
 */
public class ObjectNameCompleter implements Completer {

  private static String[] parseKeyValue(String s) {
    int eq = s.indexOf('=');
    if (eq == -1) {
      return new String[]{s, null};
    } else {
      return new String[]{s.substring(0, eq), s.substring(eq + 1, s.length())};
    }
  }

  @Override
  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    if (parameter.getType() == ValueType.OBJECT_NAME) {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      int colon = prefix.indexOf(':');
      if (colon == -1) {
        Completion.Builder b = new Completion.Builder(prefix);
        LinkedHashSet<String> domains = new LinkedHashSet<String>();
        for (ObjectName name : server.queryNames(null, null)) {
          domains.add(name.getDomain());
        }
        for (String domain : domains) {
          if (domain.startsWith(prefix)) {
            b.add(domain.substring(prefix.length()) + ":", false);
          }
        }
        return b.build();
      } else {
        String domain = prefix.substring(0, colon);
        String rest = prefix.substring(colon + 1);
        int prev = 0;
        Hashtable<String, String> keyValues = new Hashtable<String, String>();
        while (true) {
          int next = rest.indexOf(',', prev);
          if (next == -1) {
            String[] keyValue = parseKeyValue(rest.substring(prev));
            Set<ObjectName> completions = new HashSet<ObjectName>();
            for (ObjectName name : server.queryNames(null, null)) {
              if (name.getDomain().equals(domain) && name.getKeyPropertyList().entrySet().containsAll(keyValues.entrySet())) {
                completions.add(name);
              }
            }
            if (keyValue[1] == null) {
              Completion.Builder b = new Completion.Builder(keyValue[0]);
              for (ObjectName name : completions) {
                for (String key : name.getKeyPropertyList().keySet()) {
                  if (!keyValues.containsKey(key) && key.startsWith(keyValue[0])) {
                    b.add(key.substring(keyValue[0].length()) + "=", false);
                  }
                }
              }
              return b.build();
            } else {
              Completion.Builder b = new Completion.Builder(keyValue[1]);
              for (ObjectName completion : completions) {
                String value = completion.getKeyProperty(keyValue[0]);
                if (value != null && value.startsWith(keyValue[1])) {
                  Hashtable<String, String> a = completion.getKeyPropertyList();
                  a.remove(keyValue[0]);
                  a.keySet().removeAll(keyValues.keySet());
                  if (a.isEmpty()) {
                    b.add(value.substring(keyValue[1].length()), true);
                  } else {
                    b.add(value.substring(keyValue[1].length()) + ",", false);
                  }
                }
              }
              return b.build();
            }
          } else {
            String[] keyValue = parseKeyValue(rest.substring(prev, next));
            keyValues.put(keyValue[0], keyValue[1]);
            prev = next + 1;
          }
        }
      }
    } else {
      return Completion.create();
    }
  }
}

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

package org.crsh.command.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ArgumentParser<T> {

  /** . */
  private final CommandInfo<T> command;

  /** . */
  private List<OptionInfo> list;

  /** . */
  private Pattern optionPattern;

  public ArgumentParser(CommandInfo<T> command) {

    StringBuilder re = new StringBuilder("^(");
    ArrayList<OptionInfo> list = new ArrayList<OptionInfo>();
    int index = 0;
    for (OptionInfo option : command.getOptions()) {
      if (index > 0) {
        re.append('|');
      }
      re.append("(?:\\s*\\-([");
      for (Character opt : option.getOpts()) {
        re.append(opt);
      }
      re.append("])");

      //
      for (int i = 0;i < option.getArity();i++) {
        re.append("\\s*").append("([A-Aa-z0-9]*)");
      }

      //
      re.append(')');
      list.add(option);
      index++;
    }
    re.append(").*");

    //
    String regex = re.toString();
    System.out.println("regex = " + regex);

    //
    this.command = command;
    this.list = list;
    this.optionPattern = Pattern.compile(regex);
  }

  public MatchIterator parse(final String s) {
    //
    return new MatchIterator() {

      String rest = s;
      Match next = null;

      public String getRest() {
        return rest;
      }

      public boolean hasNext() {
        if (next == null) {
          Matcher matcher = optionPattern.matcher(rest);
          if (matcher.matches()) {
            OptionInfo matched = null;
            int index = 2;
            for (OptionInfo option : command.getOptions()) {
              if (matcher.group(index) != null) {
                matched = option;
                break;
              } else {
                index += 1 + option.getArity();
              }
            }

            //
            if (matched == null) {
              throw new AssertionError("Should not happen");
            }

            //
            String name = matcher.group(index++);
            List<String> values = Collections.emptyList();
            for (int j = 0;j < matched.getArity();j++) {
              if (values.isEmpty()) {
                values = new ArrayList<String>();
              }
              values.add(matcher.group(index++));
            }
            if (matched.getArity() > 0) {
              values = new ArrayList<String>(values);
            }

            //
            next = new Match(name, values);
            rest = rest.substring(matcher.end(1));
          } else {
            // Do nothing ?
          }
        }
        return next != null;
      }

      public Match next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        Match tmp = next;
        next = null;
        return tmp;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

}

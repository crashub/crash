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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MatchIterator implements Iterator<Match> {

  /** . */
  private final ArgumentParser<?> parser;

  /** . */
  private String rest;

  /** . */
  private Iterator<? extends Match<?>> i;

  public MatchIterator(ArgumentParser parser, String s) {
    this.parser = parser;
    this.rest = s;
    this.i = new OptionIterator();
  }

  public boolean hasNext() {
    if (i.hasNext()) {
      return true;
    } else {
      if (i instanceof OptionIterator) {
        i = arguments(rest).iterator();
        return i.hasNext();
      } else {
        return false;
      }
    }
  }

  public Match next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return i.next();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public String getRest() {
    return rest;
  }

  private List<Match.Argument> arguments(String s) {

    List<String> values = Arrays.asList(s.split("\\s+"));
    List<ArgumentInfo> arguments = parser.command.getArguments();

    // Attempt to match all arguments until we find a list argument
    LinkedList<Match.Argument> list = new LinkedList<Match.Argument>();
    ListIterator<Match.Argument> bilto = list.listIterator();
    ListIterator<String> headValues = values.listIterator();
    out:
    for (ListIterator<ArgumentInfo> i = arguments.listIterator();i.hasNext();) {

      // Get head argument
      ArgumentInfo head = i.next();

      //
      if (head.getType().getMultiplicity() == Multiplicity.SINGLE) {
        if (headValues.hasNext()) {
          String value = headValues.next();
          bilto.add(new Match.Argument(head, Collections.singletonList(value)));
          bilto.next();
        } else {
          break;
        }
      } else {
        ListIterator<String> tailValues = values.listIterator(values.size());
        ListIterator<ArgumentInfo> r = arguments.listIterator(arguments.size());
        while (r.hasPrevious()) {
          ArgumentInfo tail = r.previous();
          if (tail == head) {
            List<String> foo = new ArrayList<String>();
            while (headValues.nextIndex() < tailValues.previousIndex()) {
              foo.add(headValues.next());
            }
            bilto.add(new Match.Argument(head, foo));
            break out;
          } else {
            if (tailValues.previousIndex() == headValues.nextIndex()) {
              throw new UnsupportedOperationException("todo: not enough");
            } else {
              String value = tailValues.previous();
              bilto.add(new Match.Argument(tail, Collections.singletonList(value)));
            }
          }
        }
      }
    }

    //

    return list;
  }

  private class OptionIterator implements Iterator<Match.Option> {

    /** . */
    private Match.Option next = null;

    public boolean hasNext() {
      if (next == null) {
        Matcher matcher = parser.optionsPattern.matcher(rest);
        if (matcher.matches()) {
          OptionInfo matched = null;
          int index = 2;
          for (OptionInfo option : parser.command.getOptions()) {
            if (matcher.group(index) != null) {
              matched = option;
              break;
            } else {
              index += 1 + option.getArity();
            }
          }

          //
          if (matched != null) {
            String name = matcher.group(index++);
            List<String> values = Collections.emptyList();
            for (int j = 0;j < matched.getArity();j++) {
              if (values.isEmpty()) {
                values = new ArrayList<String>();
              }
              String value = matcher.group(index++);
              values.add(value);
            }
            if (matched.getArity() > 0) {
              values = Collections.unmodifiableList(values);
            }

            //
            next = new Match.Option(matched, name, values);
            rest = rest.substring(matcher.end(1));
          }
          else
          {
            //
          }
        } else {
          //
        }
      }
      return next != null;
    }

    public Match.Option next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Match.Option tmp = next;
      next = null;
      return tmp;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}

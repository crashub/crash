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

package org.crsh.command.info.analyzer;

import org.crsh.command.info.ArgumentInfo;
import org.crsh.command.info.Multiplicity;
import org.crsh.command.info.OptionInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MatchIterator implements Iterator<Match> {

  /** . */
  private final ArgumentParser<?> parser;

  /** . */
  private StringBuilder done;

  /** The rest. */
  private String rest;

  /** . */
  private Iterator<? extends Match<?>> i;

  public MatchIterator(ArgumentParser parser, String s) {
    this.parser = parser;
    this.rest = s;
    this.i = new OptionIterator();
    this.done = new StringBuilder();
  }

  private void skipRestTo(int to) {
    skipRestBy(to - done.length());
  }

  private void skipRestBy(int diff) {
    if (diff < 0) {
      throw new AssertionError();
    }
    done.append(rest.substring(0, diff));
    rest = rest.substring(diff);
  }

  public boolean hasNext() {
    if (i.hasNext()) {
      return true;
    } else {
      if (i instanceof OptionIterator) {
        i = arguments(rest);
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

  private Iterator<Match.Argument> arguments(String s) {
    ArrayList<Chunk> values = new ArrayList<Chunk>();
    Matcher matcher = Pattern.compile("\\S+").matcher(s);
    while (matcher.find()) {
      values.add(new Chunk(matcher.group(0), done.length() + matcher.start()));
    }

    //
    List<? extends ArgumentInfo<?>> arguments = parser.command.getArguments();

    // Attempt to match all arguments until we find a list argument
    final LinkedList<Match.Argument> list = new LinkedList<Match.Argument>();
    ListIterator<Match.Argument> bilto = list.listIterator();
    ListIterator<Chunk> headValues = values.listIterator();
    Chunk headLast = null;
    out:
    for (ListIterator<? extends ArgumentInfo<?>> i = arguments.listIterator();i.hasNext();) {

      // Get head argument
      ArgumentInfo head = i.next();

      //
      if (head.getType().getMultiplicity() == Multiplicity.SINGLE) {
        if (headValues.hasNext()) {
          Chunk chunk = headValues.next();
          bilto.add(new Match.Argument(head, chunk.getStart(), chunk.getEnd(), Collections.singletonList(chunk.getValue())));
          headLast = chunk;
        } else {
          break;
        }
      } else {
        ListIterator<Chunk> tailValues = values.listIterator(values.size());
        ListIterator<? extends ArgumentInfo<?>> r = arguments.listIterator(arguments.size());
        while (r.hasPrevious()) {
          ArgumentInfo tail = r.previous();
          if (tail == head) {
            LinkedList<String> foo = new LinkedList<String>();
            Chunk first = null;
            Chunk last = null;
            while (headValues.nextIndex() <= tailValues.previousIndex()) {
              last = headValues.next();
              first = first == null ? last : first;
              foo.add(last.getValue());
            }
            int begin;
            int end;
            if (first == null) {
              if (headLast == null) {
                begin = end = 0;
              } else {
                begin = end = headLast.getEnd();
              }
            } else {
              begin = first.getStart();
              end = last.getEnd();
            }
            bilto.add(new Match.Argument(head, begin, end, foo));
            break out;
          } else {
            if (tailValues.previousIndex() < headValues.nextIndex()) {
              // We cannot satisfy so we don't consume the value and we just continue backward
            } else {
              Chunk chunk = tailValues.previous();
              bilto.add(new Match.Argument(tail, chunk.getStart(), chunk.getEnd(), Collections.singletonList(chunk.getValue())));
              bilto.previous();
            }
          }
        }
      }
    }

    //
    return new Iterator<Match.Argument>() {

      /** . */
      private final Iterator<Match.Argument> iterator = list.iterator();

      public boolean hasNext() {
        return iterator.hasNext();
      }

      public Match.Argument next() {
        Match.Argument next = iterator.next();

        //

        skipRestTo(next.getEnd());

        return next;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
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
            skipRestBy(matcher.end(1));
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

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

package org.crsh.cmdline.matcher;

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.EmptyCompleter;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.binding.TypeBinding;
import org.crsh.cmdline.spi.Completer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
final class MatcherFactory<T, B extends TypeBinding> {

  /** . */
  final CommandDescriptor<T, B> command;

  /** . */
  final Pattern optionsPattern;

  /** . */
  final List<Pattern> argumentsPatterns;

  MatcherFactory(CommandDescriptor<T, B> command) {

    //
    StringBuilder optionsRE = buildOptions(new StringBuilder(), command.getOptions());
    List<Pattern> argumentPatterns = buildArguments(command.getArguments());

    //
    this.command = command;
    this.optionsPattern = Pattern.compile(optionsRE.toString());
    this.argumentsPatterns = Collections.unmodifiableList(argumentPatterns);
  }

  List<ArgumentMatch<B>> analyzeArguments(StringCursor cursor) {
    LinkedList<ArgumentMatch<B>> argumentMatches = new LinkedList<ArgumentMatch<B>>();
    for (Pattern p : argumentsPatterns) {
      java.util.regex.Matcher matcher = p.matcher(cursor.getValue());
      if (matcher.find()) {

        for (int i = 1;i <= matcher.groupCount();i++) {

          ArrayList<String> values = new ArrayList<String>();

          //
          java.util.regex.Matcher m2 = Pattern.compile("\\S+").matcher(matcher.group(i));
          while (m2.find()) {
            values.add(m2.group(0));
          }

          //
          if (values.size() > 0) {
            ArgumentMatch<B> match = new ArgumentMatch<B>(
              command.getArguments().get(i - 1),
              cursor.getIndex() + matcher.start(i),
              cursor.getIndex()  + matcher.end(i),
              values
            );

            //
            argumentMatches.add(match);
          }
        }
        break;
      }
    }

    //
    if (argumentMatches.size() > 0) {
      cursor.seek(argumentMatches.getLast().getEnd());
    }

    //
    return argumentMatches;
  }

  List<String> completeArguemnts(StringCursor cursor) {

    //
    List<ArgumentMatch<B>> matches = analyzeArguments(cursor);

    //
    return null;



  }

  List<String> completeOptions(StringCursor cursor) {

    //
    List<OptionMatch<B>> matches = analyzeOptions(cursor);

    //
    if (cursor.isEmpty()) {
      if (matches.size() > 0) {
        OptionMatch<?> last = matches.get(matches.size() - 1);
        List<String> values = last.getValues();
        Class<? extends Completer> completerType = last.getParameter().getCompleterType();
        if (completerType != EmptyCompleter.class) {
          if (values.size() > 0) {
            String prefix = values.get(values.size() - 1);
            if (prefix != null) {
              try {
                Completer completer = completerType.newInstance();
                return completer.complete(last.getParameter(), prefix);
              }
              catch (Exception e) {
                e.printStackTrace();
              }
            } else {
              //
            }
          } else {
            //
          }
        } else {
          //
        }
      } else {
        //
      }
    } else {
      //
    }

    //
    return null;
  }

  List<OptionMatch<B>> analyzeOptions(StringCursor cursor) {
    List<OptionMatch<B>> optionMatches = new ArrayList<OptionMatch<B>>();
    while (true) {
      java.util.regex.Matcher matcher = optionsPattern.matcher(cursor.getValue());
      if (matcher.matches()) {
        OptionDescriptor<B> matched = null;
        int index = 2;
        for (OptionDescriptor<B> option : command.getOptions()) {
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
          optionMatches.add(new OptionMatch<B>(matched, name.substring(name.length() == 2 ? 1 : 2), values));
          cursor.skip(matcher.end(1));
        }
        else {
          break;
        }
      } else {
        break;
      }
    }

    //
    return optionMatches;
  }

  static List<Pattern> buildArguments(List<? extends ArgumentDescriptor<?>> arguments) {
    List<Pattern> argumentPatterns = new ArrayList<Pattern>();

    //
    for (int i = arguments.size();i > 0;i--) {
      StringBuilder argumentsRE = new StringBuilder("^");
      for (int j = 0;j < i;j++) {
        ArgumentDescriptor<?> argument = arguments.get(j);
        if (j == i - 1) {
          if (argument.getMultiplicity() == Multiplicity.SINGLE) {
            argumentsRE.append("\\s*(?<!\\S)(\\S+|$)");
          }
          else {
            argumentsRE.append("\\s*(?<!\\S)((?:\\s*(?:\\S+))*)");
          }
        } else {
          if (argument.getMultiplicity() == Multiplicity.SINGLE) {
            argumentsRE.append("\\s*(?<!\\S)(\\S+)");
          }
          else {
            argumentsRE.append("\\s*(?<!\\S)((?:\\s*(?:\\S+))*)");
          }
        }
      }
      argumentPatterns.add(Pattern.compile(argumentsRE.toString()));
    }

    //
    return argumentPatterns;
  }

  static StringBuilder buildOptions(StringBuilder sb, Iterable<? extends OptionDescriptor<?>> options) {
    sb.append("^(");

    //
    boolean hasPrevious = false;
    for (OptionDescriptor<?> option : options) {

      //
      if (hasPrevious) {
        sb.append('|');
      }

      //
      sb.append("(?:\\s*(");
      boolean needOr = false;
      for (String name : option.getNames()) {
        if (needOr) {
          sb.append('|');
        }
        if (name.length() == 1) {
          sb.append("\\-").append(name);
        } else {
          sb.append("\\-\\-").append(name);
        }
        needOr = true;
      }
      sb.append(")");

      //
      for (int i = 0;i < option.getArity();i++) {
        sb.append("(?:\\s+((?:(?!\\-)\\S+)|$))?");
      }

      //
      sb.append(')');

      //
      hasPrevious = true;
    }

    //
    sb.append(").*");

    //
    return sb;
  }
}

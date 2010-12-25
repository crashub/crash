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
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.EmptyCompleter;
import org.crsh.cmdline.binding.ClassFieldBinding;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.binding.MethodArgumentBinding;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.binding.TypeBinding;
import org.crsh.cmdline.spi.Completer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Matcher<T> {

  /** . */
  private final CommandAnalyzer<T, ClassFieldBinding> analyzer;

  /** . */
  private final ClassDescriptor<T> descriptor;

  /** . */
  private final String mainName;

  public Matcher(ClassDescriptor<T> descriptor) {
    this(null, descriptor);
  }

  public Matcher(String mainName, ClassDescriptor<T> descriptor) {
    this.analyzer = new CommandAnalyzer<T, ClassFieldBinding>(descriptor);
    this.descriptor = descriptor;
    this.mainName = mainName;
  }

  public List<String> complete(String s) {

    //
    StringCursor cursor = new StringCursor(s);

    // Read all common options we are able to
    List<String> completions = analyzer.completeOptions(cursor);

    //
    if (completions != null) {
      return completions;
    }

    //
    MethodDescriptor<T> method = null;
    Pattern p = Pattern.compile("^\\s*(\\S+|$)");
    java.util.regex.Matcher m = p.matcher(cursor.getValue());
    if (m.find()) {
      String name = m.group(1);
      method = descriptor.getMethod(name);
      if (method == null) {
        ArrayList<String> a = new ArrayList<String>();
        for (MethodDescriptor<T> candidate : descriptor.getMethods()) {
          if (candidate.getName().startsWith(name)) {
            a.add(candidate.getName().substring(name.length()));
          }
        }
        if (a.size() > 0) {
          return a;
        }
      } else {
        cursor.skip(m.end(1));
        if (cursor.isEmpty()) {
          return Collections.singletonList("");
        }
      }
    }

    //
    if (method != null) {

      CommandAnalyzer<T, MethodArgumentBinding> methodAnalyzer = new CommandAnalyzer<T, MethodArgumentBinding>(method);

      completions = methodAnalyzer.completeOptions(cursor);



    }

    //
    if (completions == null) {
      completions = Collections.emptyList();
    }

    //
    return completions;
  }

  public CommandMatch<T, ?, ?> match(String s) {

    //
    StringCursor cursor = new StringCursor(s);

    // Read all common options we are able to
    List<OptionMatch<ClassFieldBinding>> options = analyzer.analyzeOptions(cursor);

    List<OptionMatch<MethodArgumentBinding>> methodOptions = null;
    List<ArgumentMatch<MethodArgumentBinding>> methodArguments = null;
    MethodDescriptor<T> method = null;
    Pattern p = Pattern.compile("^\\s*(\\S+)");
    java.util.regex.Matcher m = p.matcher(cursor.getValue());
    if (m.find()) {
      String f = m.group(1);
      method = descriptor.getMethod(f);
      if (method != null) {
        cursor.skip(m.end(1));
      }
    }

    // Try to consume with main method name then
    if (method == null) {
      method = descriptor.getMethod(mainName);
    }

    //
    if (method != null) {
      ClassMatch<T> owner = new ClassMatch<T>(descriptor, options, Collections.<ArgumentMatch<ClassFieldBinding>>emptyList(), cursor.getValue());
      CommandAnalyzer<T, MethodArgumentBinding> methodAnalyzer = new CommandAnalyzer<T, MethodArgumentBinding>(method);
      methodOptions = methodAnalyzer.analyzeOptions(cursor);
      methodArguments = methodAnalyzer.analyzeArguments(cursor);
      return new MethodMatch<T>(owner, method, methodOptions, methodArguments, cursor.getValue());
    } else {
      List<ArgumentMatch<ClassFieldBinding>> arguments = analyzer.analyzeArguments(cursor);
      return new ClassMatch<T>(descriptor, options, arguments, cursor.getValue());
    }
  }

  private static class CommandAnalyzer<T, B extends TypeBinding> {

    /** . */
    final CommandDescriptor<T, B> command;

    /** . */
    final Pattern findOptionsPattern;

    /** . */
    final List<Pattern> argumentsPatterns;

    public CommandAnalyzer(CommandDescriptor<T, B> command) {

      //
      StringBuilder findOptionsRE = buildOptions(new StringBuilder(), command.getOptions());

      //
      List<Pattern> argumentPatterns = new ArrayList<Pattern>();
      List<ArgumentDescriptor<B>> arguments = command.getArguments();
      for (int i = arguments.size();i > 0;i--) {
        StringBuilder argumentsRE = new StringBuilder("^");
        for (ArgumentDescriptor<B> argument : arguments.subList(0, i)) {
          if (argument.getMultiplicity() == Multiplicity.SINGLE) {
            argumentsRE.append("\\s*(?<!\\S)(\\S+)");
          }
          else {
            argumentsRE.append("\\s*(?<!\\S)((?:\\s*(?:\\S+))*)");
          }
        }
        argumentPatterns.add(Pattern.compile(argumentsRE.toString()));
      }

      //
      this.command = command;
      this.findOptionsPattern = Pattern.compile(findOptionsRE.toString());
      this.argumentsPatterns = Collections.unmodifiableList(argumentPatterns);
    }

    public List<ArgumentMatch<B>> analyzeArguments(StringCursor bilto) {
      LinkedList<ArgumentMatch<B>> argumentMatches = new LinkedList<ArgumentMatch<B>>();
      for (Pattern p : argumentsPatterns) {
        java.util.regex.Matcher matcher = p.matcher(bilto.getValue());
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
                bilto.getIndex() + matcher.start(i),
                bilto.getIndex()  + matcher.end(i),
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
        bilto.seek(argumentMatches.getLast().getEnd());
      }

      //
      return argumentMatches;
    }

    public List<String> completeOptions(StringCursor cursor) {

      //
      List<OptionMatch<B>> options = analyzeOptions(cursor);

      //
      if (cursor.isEmpty()) {
        if (options.size() > 0) {
          OptionMatch<?> last = options.get(options.size() - 1);
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

    public List<OptionMatch<B>> analyzeOptions(StringCursor bilto) {
      List<OptionMatch<B>> optionMatches = new ArrayList<OptionMatch<B>>();
      while (true) {
        java.util.regex.Matcher matcher = findOptionsPattern.matcher(bilto.getValue());
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
            bilto.skip(matcher.end(1));
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

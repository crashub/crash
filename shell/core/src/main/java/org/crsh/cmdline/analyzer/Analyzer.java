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

package org.crsh.cmdline.analyzer;

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.ParameterBinding;
import static org.crsh.util.Utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Analyzer<T> {

  /** . */
  private final CommandAnalyzer<T, ClassDescriptor<T>, ParameterBinding.ClassField> analyzer;

  /** . */
  private final ClassDescriptor<T> descriptor;

  public Analyzer(ClassDescriptor<T> descriptor) {
    this.analyzer = new CommandAnalyzer<T, ClassDescriptor<T>, ParameterBinding.ClassField>(descriptor);
    this.descriptor = descriptor;
  }

  public CommandMatch<T, ?, ?> analyze(String s) {
    Bilto bilto = new Bilto(s);
    List<OptionMatch<ParameterBinding.ClassField>> options = analyzer.analyzeOptions(bilto);
    Pattern p = Pattern.compile("^\\s*(\\S+)");
    Matcher m = p.matcher(bilto.rest);
    if (m.find()) {
      String f = m.group(1);
      MethodDescriptor<T> sub = descriptor.getMethod(f);
      if (sub != null) {
        CommandAnalyzer<T, MethodDescriptor<T>, ParameterBinding.MethodArgument> methodAnalyzer = new CommandAnalyzer<T, MethodDescriptor<T>, ParameterBinding.MethodArgument>(sub);
        List<OptionMatch<ParameterBinding.MethodArgument>> methodOptions = methodAnalyzer.analyzeOptions(bilto);
        List<ArgumentMatch<ParameterBinding.MethodArgument>> methodArguments = methodAnalyzer.analyzeArguments(bilto);
        return new MethodMatch<T>(sub, options, methodOptions, methodArguments, bilto.rest);
      }
    }
    List<ArgumentMatch<ParameterBinding.ClassField>> arguments = analyzer.analyzeArguments(bilto);
    return new ClassMatch<T>(descriptor, options, arguments, bilto.rest);
  }

  static class Bilto {

    /** . */
    private StringBuilder done;

    /** The rest. */
    private String rest;

    Bilto(String rest) {
      this.rest = rest;
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
  }

  private class CommandAnalyzer<T, C extends CommandDescriptor<T, B>, B extends ParameterBinding> {

    /** . */
    final CommandDescriptor<T, B> command;

    /** . */
    final Pattern optionsPattern;

    /** . */
    final List<Pattern> argumentsPatterns;

    public CommandAnalyzer(CommandDescriptor<T, B> command) {
      StringBuilder optionsRE = new StringBuilder("^(");
      Collection<OptionDescriptor<B>> options = command.getOptions();
      for (Iterator<OptionDescriptor<B>> it = options.iterator();it.hasNext();) {
        OptionDescriptor<B> option = it.next();
        optionsRE.append("(?:\\s*(");
        boolean needOr = false;
        for (String name : option.getNames()) {
          if (needOr) {
            optionsRE.append('|');
          }
          if (name.length() == 1) {
            optionsRE.append("\\-").append(name);
          } else {
            optionsRE.append("\\-\\-").append(name);
          }
          needOr = true;
        }
        optionsRE.append(")");

        //
        for (int i = 0;i < option.getArity();i++) {
          optionsRE.append("(?:\\s+").append("(?!\\-)(\\S+))?");
        }

        //
        optionsRE.append(')');

        //
        if (it.hasNext()) {
          optionsRE.append('|');
        }
      }
      optionsRE.append(").*");

      //
      List<Pattern> argumentPatterns = new ArrayList<Pattern>();
      List<ArgumentDescriptor<B>> arguments = command.getArguments();
      for (int i = arguments.size();i > 0;i--) {
        StringBuilder argumentsRE = new StringBuilder("^");
        for (ArgumentDescriptor<B> argument : arguments.subList(0, i)) {
          if (argument.getType().getMultiplicity() == Multiplicity.SINGLE) {
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
      this.optionsPattern = Pattern.compile(optionsRE.toString());
      this.argumentsPatterns = Collections.unmodifiableList(argumentPatterns);
    }

    public List<ArgumentMatch<B>> analyzeArguments(Bilto bilto) {
      LinkedList<ArgumentMatch<B>> argumentMatches = newLinkedList();
      for (Pattern p : argumentsPatterns) {
        Matcher matcher = p.matcher(bilto.rest);
        if (matcher.find()) {

          for (int i = 1;i <= matcher.groupCount();i++) {

            ArrayList<String> values = new ArrayList<String>();

            //
            Matcher m2 = Pattern.compile("\\S+").matcher(matcher.group(i));
            while (m2.find()) {
              values.add(m2.group(0));
            }

            //
            if (values.size() > 0) {
              ArgumentMatch<B> match = new ArgumentMatch<B>(
                command.getArguments().get(i - 1),
                bilto.done.length() + matcher.start(i),
                bilto.done.length() + matcher.end(i),
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
        bilto.skipRestTo(argumentMatches.getLast().getEnd());
      }

      //
      return argumentMatches;
    }

    public List<OptionMatch<B>> analyzeOptions(Bilto bilto) {
      List<OptionMatch<B>> optionMatches = new ArrayList<OptionMatch<B>>();
      while (true) {
        Matcher matcher = optionsPattern.matcher(bilto.rest);
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
            bilto.skipRestBy(matcher.end(1));
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
}

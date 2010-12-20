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
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.ParameterBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Analyzer<T, B extends ParameterBinding> {

  /** . */
  final CommandDescriptor<T, B> command;

  /** . */
  final Pattern optionsPattern;

  /** . */
  final List<Pattern> argumentsPatterns;

  public Analyzer(CommandDescriptor<T, B> command) {

    //
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

  public Pattern getOptionsPattern() {
    return optionsPattern;
  }

  public List<Pattern> getArgumentsPatterns() {
    return argumentsPatterns;
  }

  public MatchIterator<T, B> analyzer(String s) {
    return new MatchIterator<T, B>(this, s);
  }
}

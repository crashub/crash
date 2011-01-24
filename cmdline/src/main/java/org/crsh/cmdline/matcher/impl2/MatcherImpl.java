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

package org.crsh.cmdline.matcher.impl2;

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.binding.ClassFieldBinding;
import org.crsh.cmdline.binding.MethodArgumentBinding;
import org.crsh.cmdline.matcher.ArgumentMatch;
import org.crsh.cmdline.matcher.ClassMatch;
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.Matcher;
import org.crsh.cmdline.matcher.MethodMatch;
import org.crsh.cmdline.matcher.OptionMatch;
import org.crsh.cmdline.matcher.ParameterMatch;
import org.crsh.cmdline.spi.Completer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MatcherImpl<T> extends Matcher<T> {

  /** . */
  private final ClassDescriptor<T> descriptor;

  /** . */
  private final String mainName;

  public MatcherImpl(ClassDescriptor<T> descriptor) {
    this(null, descriptor);
  }

  public MatcherImpl(String mainName, ClassDescriptor<T> descriptor) {
    this.mainName = mainName;
    this.descriptor = descriptor;
  }

  @Override
  public CommandMatch<T, ?, ?> match(String s) {

    Tokenizer tokenizer = new Tokenizer(s);
    Parser<T> parser = new Parser<T>(tokenizer, descriptor, mainName, true);

    //
    List<OptionMatch<ClassFieldBinding>> classOptions = new ArrayList<OptionMatch<ClassFieldBinding>>();
    List<ArgumentMatch<ClassFieldBinding>> classArguments = new ArrayList<ArgumentMatch<ClassFieldBinding>>();
    List<OptionMatch<MethodArgumentBinding>> methodOptions = new ArrayList<OptionMatch<MethodArgumentBinding>>();
    List<ArgumentMatch<MethodArgumentBinding>> methodArguments = new ArrayList<ArgumentMatch<MethodArgumentBinding>>();
    MethodDescriptor<T> method = null;


    Integer methodEnd = null;
    Integer classEnd = null;
    while (true) {
      Event event = parser.bilto();
      if (event instanceof Event.Separator) {
        // We don't care
      } else if (event instanceof Event.End) {
        // We are done
        // Check error status and react to it maybe
        Event.End end = (Event.End)event;
        int endIndex = end.getIndex();

        // We try to match the main if none was found
        if (method == null) {
          classEnd = endIndex;
          if (mainName != null) {
            method = descriptor.getMethod(mainName);
          }
          if (method != null) {
            methodEnd = classEnd;
          }
        } else {
          methodEnd = classEnd = endIndex;
        }
        break;
      } else if (event instanceof Event.Option) {
        Event.Option option = (Event.Option)event;
        OptionDescriptor<?> desc = option.getDescriptor();
        OptionMatch match = new OptionMatch(desc, option.getToken().getName(), option.getStrings());
        if (desc.getOwner() instanceof ClassDescriptor<?>) {
          classOptions.add(match);
        } else {
          methodOptions.add(match);
        }
      } else if (event instanceof Event.Method) {
        if (event instanceof Event.Method.Implicit) {
          Event.Method.Implicit implicit = (Event.Method.Implicit)event;
          classEnd = implicit.getTrigger().getFrom();
          method = (MethodDescriptor<T>)implicit.getDescriptor();
        } else {
          Event.Method.Explicit explicit = (Event.Method.Explicit)event;
          classEnd = explicit.getToken().getFrom();
          method = (MethodDescriptor<T>)explicit.getDescriptor();
        }
      } else if (event instanceof Event.Argument) {
        Event.Argument argumentEvent = (Event.Argument)event;
        List<Token.Literal> values = argumentEvent.getValues();
        ArgumentMatch match = new ArgumentMatch(
          argumentEvent.getDescriptor(),
          values.get(0).getFrom(),
          values.get(argumentEvent.getValues().size() - 1).getTo(),
          argumentEvent.getStrings()
        );
        if (argumentEvent.getDescriptor().getOwner() instanceof ClassDescriptor<?>) {
          classArguments.add(match);
        } else {
          methodArguments.add(match);
        }
      }
    }

    //
    ClassMatch classMatch = new ClassMatch(descriptor, classOptions, classArguments, s.substring(classEnd));
    if (method != null) {
      return new MethodMatch(classMatch, method, false, methodOptions, methodArguments, s.substring(methodEnd));
    } else {
      return classMatch;
    }
  }

  @Override
  public Map<String, String> complete(Completer completer, String s) throws CmdCompletionException {
    throw new UnsupportedOperationException();
  }
}

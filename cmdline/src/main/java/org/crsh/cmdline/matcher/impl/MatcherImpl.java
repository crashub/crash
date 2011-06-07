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

package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.binding.ClassFieldBinding;
import org.crsh.cmdline.binding.MethodArgumentBinding;
import org.crsh.cmdline.matcher.ArgumentMatch;
import org.crsh.cmdline.matcher.ClassMatch;
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.LiteralValue;
import org.crsh.cmdline.matcher.Matcher;
import org.crsh.cmdline.matcher.MethodMatch;
import org.crsh.cmdline.matcher.OptionMatch;
import org.crsh.cmdline.matcher.tokenizer.Termination;
import org.crsh.cmdline.matcher.tokenizer.Token;
import org.crsh.cmdline.matcher.tokenizer.Tokenizer;
import org.crsh.cmdline.spi.Completer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
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

  private List<LiteralValue> bilto(List<? extends Token.Literal> literals) {
    List<LiteralValue> values = new ArrayList<LiteralValue>(literals.size());
    for (Token.Literal literal : literals) {
      values.add(new LiteralValue(literal.getRaw(), literal.getValue()));
    }
    return values;
  }

  @Override
  public CommandMatch<T, ?, ?> match(String s) {

    Tokenizer tokenizer = new Tokenizer(s);
    Parser<T> parser = new Parser<T>(tokenizer, descriptor, mainName, Mode.INVOKE);

    //
    List<OptionMatch<ClassFieldBinding>> classOptions = new ArrayList<OptionMatch<ClassFieldBinding>>();
    List<ArgumentMatch<ClassFieldBinding>> classArguments = new ArrayList<ArgumentMatch<ClassFieldBinding>>();
    List<OptionMatch<MethodArgumentBinding>> methodOptions = new ArrayList<OptionMatch<MethodArgumentBinding>>();
    List<ArgumentMatch<MethodArgumentBinding>> methodArguments = new ArrayList<ArgumentMatch<MethodArgumentBinding>>();
    MethodDescriptor<T> method = null;

    //
    Integer methodEnd = null;
    Integer classEnd;
    Event previous = null;
    while (true) {
      Event event = parser.next();
      if (event instanceof Event.Separator) {
        //
      } else if (event instanceof Event.Stop) {
        // We are done
        // Check error status and react to it maybe
        Event.Stop end = (Event.Stop)event;
        int endIndex;
        if (previous instanceof Event.Separator) {
          endIndex = ((Event.Separator)previous).getToken().getFrom();
        } else {
          endIndex = end.getIndex();
        }

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
        Event.Option optionEvent = (Event.Option)event;
        OptionDescriptor<?> desc = optionEvent.getDescriptor();
        List options;
        if (desc.getOwner() instanceof ClassDescriptor<?>) {
          options = classOptions;
        } else {
          options = methodOptions;
        }
        boolean done = false;
        for (ListIterator<OptionMatch> i = options.listIterator();i.hasNext();) {
          OptionMatch om = i.next();
          if (om.getParameter().equals(desc)) {
            List<LiteralValue> v = new ArrayList<LiteralValue>(om.getValues());
            v.addAll(bilto(optionEvent.getValues()));
            List<String> names = new ArrayList<String>(om.getNames());
            names.add(optionEvent.getToken().getName());
            i.set(new OptionMatch(desc, names, v));
            done = true;
            break;
          }
        }
        if (!done) {
          OptionMatch match = new OptionMatch(desc, optionEvent.getToken().getName(), bilto(optionEvent.getValues()));
          options.add(match);
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
        ArgumentMatch match;
        if (values.size() > 0) {
          match = new ArgumentMatch(
            argumentEvent.getDescriptor(),
            argumentEvent.getFrom(),
            argumentEvent.getTo(),
            bilto(argumentEvent.getValues())
          );
          if (argumentEvent.getDescriptor().getOwner() instanceof ClassDescriptor<?>) {
            classArguments.add(match);
          } else {
            methodArguments.add(match);
          }
        }
      }
      previous = event;
    }

    //
    ClassMatch classMatch = new ClassMatch(descriptor, classOptions, classArguments, s.substring(classEnd));
    if (method != null) {
      return new MethodMatch(classMatch, method, false, methodOptions, methodArguments, s.substring(methodEnd));
    } else {
      return classMatch;
    }
  }

  private Completion argument(MethodDescriptor<?> method, Completer completer) {
    List<? extends ArgumentDescriptor<?>> arguments = method.getArguments();
    if (arguments.isEmpty()) {
      return new EmptyCompletion();
    } else {
      ArgumentDescriptor<?> argument = arguments.get(0);
      return new ParameterCompletion("", Termination.DETERMINED, argument, completer);
    }
  }

  @Override
  public Map<String, String> complete(Completer completer, String s) throws CmdCompletionException {
    return getCompletion(completer, s).complete();
  }

  private Completion getCompletion(Completer completer, String s) throws CmdCompletionException {

    Tokenizer tokenizer = new Tokenizer(s);
    Parser<T> parser = new Parser<T>(tokenizer, descriptor, mainName, Mode.COMPLETE);

    // Last non separator event
    Event last = null;
    Event.Separator separator = null;
    MethodDescriptor<?> method = null;
    Event.Stop stop;

    //
    while (true) {
      Event event = parser.next();
      if (event instanceof Event.Separator) {
        separator = (Event.Separator)event;
      } else if (event instanceof Event.Stop) {
        stop = (Event.Stop)event;
        break;
      } else if (event instanceof Event.Option) {
        last = event;
        separator = null;
      } else if (event instanceof Event.Method) {
        method = ((Event.Method)event).getDescriptor();
        last = event;
        separator = null;
      } else if (event instanceof Event.Argument) {
        last = event;
        separator = null;
      } else if (event instanceof Event.DoubleDash) {
        last = event;
        separator = null;
      }
    }

    //
    if (stop instanceof Event.Stop.Unresolved.NoSuchOption) {
      Event.Stop.Unresolved.NoSuchOption nso = (Event.Stop.Unresolved.NoSuchOption)stop;
      return new OptionCompletion<T>(method != null ? (CommandDescriptor<T, ?>)method : descriptor, nso.getToken());
    } else if (stop instanceof Event.Stop.Unresolved) {
      if (stop instanceof Event.Stop.Unresolved.TooManyArguments) {
        if (method == null) {
          Event.Stop.Unresolved.TooManyArguments tma = (Event.Stop.Unresolved.TooManyArguments)stop;
          return new MethodCompletion<T>(descriptor, mainName, s.substring(stop.getIndex()), tma.getToken().getTermination());
        } else {
          return new EmptyCompletion();
        }
      } else {
        return new EmptyCompletion();
      }
    } else if (stop instanceof Event.Stop.Done.Option) {
      // to use ?
    } else if (stop instanceof Event.Stop.Done.Arg) {
      // to use ?
    }

    //
    if (last == null) {
      if (method == null) {
        if (descriptor.getSubordinates().keySet().equals(Collections.singleton(mainName))) {
          method = descriptor.getMethod(mainName);
          List<ArgumentDescriptor<MethodArgumentBinding>> args = method.getArguments();
          if (args.size() > 0) {
            return new ParameterCompletion("", Termination.DETERMINED, args.get(0), completer);
          } else {
            return new EmptyCompletion();
          }
        } else {
          return new MethodCompletion<T>(descriptor, mainName, s.substring(stop.getIndex()), Termination.DETERMINED);
        }
      } else {
        return new EmptyCompletion();
      }
    }

    //
    if (last instanceof Event.DoubleDash) {
      Event.DoubleDash dd = (Event.DoubleDash)last;
      return new OptionCompletion<T>(method != null ? (CommandDescriptor<T, ?>)method : descriptor, dd.token);
    } else if (last instanceof Event.Option) {
      Event.Option optionEvent = (Event.Option)last;
      List<Token.Literal.Word> values = optionEvent.getValues();
      OptionDescriptor<?> option = optionEvent.getDescriptor();
      if (separator == null) {
        if (values.size() == 0) {
          return new SpaceCompletion();
        } else if (values.size() <= option.getArity()) {
          Token.Literal.Word word = optionEvent.peekLast();
          return new ParameterCompletion(word.getValue(), word.getTermination(), option, completer);
        } else {
          return new EmptyCompletion();
        }
      } else {
        if (values.size() < option.getArity()) {
          return new ParameterCompletion("", Termination.DETERMINED, option, completer);
        } else {
          if (method == null) {
            return new MethodCompletion<T>(descriptor, mainName, s.substring(stop.getIndex()), Termination.DETERMINED);
          } else {
            return argument(method, completer);
          }
        }
      }
    } else if (last instanceof Event.Argument) {
      Event.Argument eventArgument = (Event.Argument)last;
      ArgumentDescriptor<?> argument = eventArgument.getDescriptor();
      if (separator != null) {
        switch (argument.getMultiplicity()) {
          case SINGLE:
            List<? extends ArgumentDescriptor<?>> arguments = argument.getOwner().getArguments();
            int index = arguments.indexOf(argument) + 1;
            if (index < arguments.size()) {
              throw new UnsupportedOperationException("Need to find next argument and use it for completion");
            } else {
              return new EmptyCompletion();
            }
          case MULTI:
            return new ParameterCompletion("", Termination.DETERMINED, argument, completer);
          default:
            throw new AssertionError();
        }
      } else {
        Token.Literal value = eventArgument.peekLast();
        return new ParameterCompletion(value.getValue(), value.getTermination(), argument, completer);
      }
    } else if (last instanceof Event.Method) {
      if (separator != null) {
        return argument(method, completer);
      } else {
        return new SpaceCompletion();
      }
    } else {
      throw new AssertionError();
    }
  }
}

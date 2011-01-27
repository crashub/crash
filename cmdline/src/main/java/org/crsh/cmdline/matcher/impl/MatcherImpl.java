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
import org.crsh.cmdline.EmptyCompleter;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.binding.ClassFieldBinding;
import org.crsh.cmdline.binding.MethodArgumentBinding;
import org.crsh.cmdline.matcher.ArgumentMatch;
import org.crsh.cmdline.matcher.ClassMatch;
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.Matcher;
import org.crsh.cmdline.matcher.MethodMatch;
import org.crsh.cmdline.matcher.OptionMatch;
import org.crsh.cmdline.matcher.Value;
import org.crsh.cmdline.spi.Completer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private List<Value> bilto(List<? extends Token.Literal> literals) {
    List<Value> values = new ArrayList<Value>(literals.size());
    for (Token.Literal literal : literals) {
      values.add(new Value(literal.raw, literal.value));
    }
    return values;
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

    //
    Integer methodEnd = null;
    Integer classEnd;
    Event previous = null;
    while (true) {
      Event event = parser.bilto();
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
        OptionMatch match = new OptionMatch(desc, optionEvent.getToken().getName(), bilto(optionEvent.getValues()));
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

  @Override
  public Map<String, String> complete(Completer completer, String s) throws CmdCompletionException {

    Tokenizer tokenizer = new Tokenizer(s);
    Parser<T> parser = new Parser<T>(tokenizer, descriptor, mainName, false);

    // Last non separator event
    Event last = null;
    Event.Separator separator = null;
    MethodDescriptor<?> method = null;
    Event.Stop stop;

    //
    while (true) {
      Event event = parser.bilto();
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
      }
    }

    //
    if (stop instanceof Event.Stop.Unresolved.NoSuchOption) {
      Event.Stop.Unresolved.NoSuchOption nso = (Event.Stop.Unresolved.NoSuchOption)stop;
      String prefix = nso.getToken().value;
      CommandDescriptor<T, ?> cmd = method != null ? (CommandDescriptor<T, ?>)method : descriptor;
      Map<String, String> completions = new HashMap<String, String>();
      Set<String> optionNames = nso.getToken() instanceof Token.Literal.Option.Short ? cmd.getShortOptionNames() : cmd.getLongOptionNames();
      for (String optionName : optionNames) {
        if (optionName.startsWith(prefix)) {
          completions.put(optionName.substring(prefix.length()), " ");
        }
      }
      return completions;
    } else if (stop instanceof Event.Stop.Unresolved) {
      if (stop instanceof Event.Stop.Unresolved.TooManyArguments) {
        if (method == null) {

          // Copy / paste
          String prefix = s.substring(stop.getIndex());
          Map<String, String> completions = new HashMap<String, String>();
          for (MethodDescriptor<?> m : descriptor.getMethods()) {
            String name = m.getName();
            if (name.startsWith(prefix)) {
              if (!name.equals(mainName)) {
                completions.put(name.substring(prefix.length()), " ");
              }
            }
          }
          return completions;

        } else {
          return Collections.emptyMap();
        }
      } else {
        return Collections.emptyMap();
      }
    }

    //
    if (last == null) {
      if (method == null) {
        String prefix = s.substring(stop.getIndex());
        Map<String, String> completions = new HashMap<String, String>();
        for (MethodDescriptor<?> m : descriptor.getMethods()) {
          String name = m.getName();
          if (name.startsWith(prefix)) {
            if (!name.equals(mainName)) {
              completions.put(name.substring(prefix.length()), " ");
            }
          }
        }
        return completions;
      } else {
        return Collections.emptyMap();
      }
    }

    //
    String prefix;
    Termination termination;
    ParameterDescriptor<?> parameter;

    //
    if (last instanceof Event.Option) {
      Event.Option optionEvent = (Event.Option)last;
      List<Token.Literal.Word> values = optionEvent.getValues();
      OptionDescriptor<?> option = optionEvent.getDescriptor();
      if (separator == null) {
        if (values.size() == 0) {
          return Collections.singletonMap("", " ");
        } else if (values.size() <= option.getArity()) {
          Token.Literal.Word word = optionEvent.peekLast();
          prefix = word.value;
          termination = word.termination;
        } else {
          return Collections.emptyMap();
        }
      } else {
        if (values.size() < option.getArity()) {
          prefix = "";
          termination = Termination.DETERMINED;
        } else {
          if (method == null) {

            // Copy paste from above
            String _prefix = s.substring(stop.getIndex());
            Map<String, String> _completions = new HashMap<String, String>();
            for (MethodDescriptor<?> _m : descriptor.getMethods()) {
              String _name = _m.getName();
              if (_name.startsWith(_prefix)) {
                if (!_name.equals(mainName)) {
                  _completions.put(_name.substring(_prefix.length()), " ");
                }
              }
            }
            return _completions;

          } else {
            // IT COULD BE AN ARGUMENT CHECK
            return Collections.emptyMap();
          }
        }
      }
      parameter = option;
    } else if (last instanceof Event.Argument) {
      Event.Argument eventArgument = (Event.Argument)last;
      ArgumentDescriptor<?> argument = eventArgument.getDescriptor();
      List<Token.Literal> values = eventArgument.getValues();
      if (separator != null) {
        switch (argument.getMultiplicity()) {
          case ZERO_OR_ONE:
          case ONE:
            List<? extends ArgumentDescriptor<?>> arguments = argument.getOwner().getArguments();
            int index = arguments.indexOf(argument) + 1;
            if (index < arguments.size()) {
              throw new UnsupportedOperationException("Need to find next argument and use it for completion");
            } else {
              return Collections.emptyMap();
            }
          case ZERO_OR_MORE:
            prefix = "";
            termination = Termination.DETERMINED;
            parameter = argument;
            break;
          default:
            throw new AssertionError();
        }
      } else {
        Token.Literal value = eventArgument.peekLast();
        prefix = value.value;
        termination = value.termination;
        parameter = argument;
      }
    } else if (last instanceof Event.Method) {
      if (separator != null) {
        List<? extends ArgumentDescriptor<?>> arguments = method.getArguments();
        if (arguments.isEmpty()) {
          return Collections.emptyMap();
        } else {
          ArgumentDescriptor<?> argument = arguments.isEmpty() ? null : arguments.get(0);
          prefix = "";
          termination = Termination.DETERMINED;
          parameter = argument;
        }
      } else {
        return Collections.singletonMap("", " ");
      }
    } else {
      throw new AssertionError();
    }

    //
    Class<? extends Completer> completerType = parameter.getCompleterType();

    // Use the most adapted completer
    if (completerType != EmptyCompleter.class) {
      try {
        completer = completerType.newInstance();
      }
      catch (Exception e) {
        throw new CmdCompletionException(e);
      }
    }

    //
    if (completer != null) {

      //
      String foo;
      switch (termination) {
        case DETERMINED:
          foo = " ";
          break;
        case DOUBLE_QUOTE:
          foo = "\"";
          break;
        case SINGLE_QUOTE:
          foo = "'";
          break;
        default:
          throw new AssertionError();
      }

      //
      try {
        Map<String, Boolean> res = completer.complete(parameter, prefix);
        Map<String, String> delimiter = new HashMap<String, String>();
        for (Map.Entry<String, Boolean> entry : res.entrySet()) {
          delimiter.put(entry.getKey(), entry.getValue() ? "" + foo : "");
        }
        return delimiter;
      }
      catch (Exception e) {
        throw new CmdCompletionException(e);
      }
    } else {
      return Collections.emptyMap();
    }
  }
}

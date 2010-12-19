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

import org.crsh.command.Argument;
import org.crsh.command.Description;
import org.crsh.command.Option;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandInfo<T, B extends ParameterBinding> {

  public static <T> CommandInfo<T, ParameterBinding.ClassField> create(Class<T> type) throws IntrospectionException {
    return new ClassCommandInfo<T>(type);
  }

  /** . */
  private final String name;

  /** . */
  private final String description;

  /** . */
  private final Map<String, OptionInfo<B>> options;

  /** . */
  private final List<ArgumentInfo<B>> arguments;

  CommandInfo(String name, String description, List<ParameterInfo<B>> parameters) throws IntrospectionException {

    Map<String, OptionInfo<B>> options = Collections.emptyMap();
    List<ArgumentInfo<B>> arguments = Collections.emptyList();
    boolean listArgument = false;
    for (ParameterInfo<B> parameter : parameters) {
      if (parameter instanceof OptionInfo) {
        OptionInfo<B> option = (OptionInfo<B>)parameter;
        for (Character opt : option.getOpts()) {
          if (options.isEmpty()) {
            options = new HashMap<String, OptionInfo<B>>();
          }
          options.put("-" + opt, option);
        }
      } else if (parameter instanceof ArgumentInfo) {
        ArgumentInfo<B> argument = (ArgumentInfo<B>)parameter;
        if (argument.getType().getMultiplicity() == Multiplicity.LIST) {
          if (listArgument) {
            throw new IntrospectionException();
          }
          listArgument = true;
        }
        if (arguments.isEmpty()) {
          arguments = new ArrayList<ArgumentInfo<B>>();
        }
        arguments.add(argument);
      }
    }

    //
    this.description = description;
    this.options = options.isEmpty() ? options : Collections.unmodifiableMap(options);
    this.arguments = arguments.isEmpty() ? arguments : Collections.unmodifiableList(arguments);
    this.name = name;
  }

  public abstract Class<T> getType();

  public Collection<OptionInfo<B>> getOptions() {
    return options.values();
  }

  public OptionInfo<B> getOption(String name) {
    return options.get(name);
  }

  public List<ArgumentInfo<B>> getArguments() {
    return arguments;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  protected static String description(Description descriptionAnn) {
    return descriptionAnn != null ? descriptionAnn.value() : "";
  }

  protected static <B extends ParameterBinding> ParameterInfo<B> create(
    B binding,
    Type type,
    Description descriptionAnn,
    Argument argumentAnn,
    Option optionAnn) throws IntrospectionException {
    if (argumentAnn != null) {
      if (optionAnn != null) {
        throw new IntrospectionException();
      }
      return new ArgumentInfo<B>(
        binding,
        type,
        description(descriptionAnn),
        argumentAnn.required(),
        argumentAnn.password());
    } else if (optionAnn != null) {

      List<Character> opt = new ArrayList<Character>();
      for (char c : optionAnn.opt()) {
        opt.add(c);
      }

      return new OptionInfo<B>(
        binding,
        type,
        Collections.unmodifiableList(opt),
        description(descriptionAnn),
        optionAnn.required(),
        optionAnn.arity(),
        optionAnn.password());
    } else {
      return null;
    }
  }
}

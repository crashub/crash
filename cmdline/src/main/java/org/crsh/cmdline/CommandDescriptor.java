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

package org.crsh.cmdline;

import org.crsh.cmdline.binding.TypeBinding;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandDescriptor<T, B extends TypeBinding> {

  public static <T> ClassDescriptor<T> create(Class<T> type) throws IntrospectionException {

    //
    return new ClassDescriptor<T>(type);
  }

  /** . */
  private final String name;

  /** . */
  private final InfoDescriptor info;

  /** . */
  private final Map<String, OptionDescriptor<B>> optionMap;

  /** . */
  private final Set<OptionDescriptor<B>> options;

  /** . */
  private final List<ArgumentDescriptor<B>> arguments;

  CommandDescriptor(String name, InfoDescriptor info, List<ParameterDescriptor<B>> parameters) throws IntrospectionException {

    Map<String, OptionDescriptor<B>> options = Collections.emptyMap();
    List<ArgumentDescriptor<B>> arguments = Collections.emptyList();
    boolean listArgument = false;
    for (ParameterDescriptor<B> parameter : parameters) {
      if (parameter instanceof OptionDescriptor) {
        OptionDescriptor<B> option = (OptionDescriptor<B>)parameter;
        for (String optionName : option.getNames()) {
          if (options.isEmpty()) {
            options = new LinkedHashMap<String, OptionDescriptor<B>>();
          }
          options.put((optionName.length() == 1 ? "-" : "--") + optionName, option);
        }
      } else if (parameter instanceof ArgumentDescriptor) {
        ArgumentDescriptor<B> argument = (ArgumentDescriptor<B>)parameter;
        if (argument.getMultiplicity() == Multiplicity.LIST) {
          if (listArgument) {
            throw new IntrospectionException();
          }
          listArgument = true;
        }
        if (arguments.isEmpty()) {
          arguments = new ArrayList<ArgumentDescriptor<B>>();
        }
        arguments.add(argument);
      }
    }

    //
    this.info = info;
    this.optionMap = options.isEmpty() ? options : Collections.unmodifiableMap(options);
    this.arguments = arguments.isEmpty() ? arguments : Collections.unmodifiableList(arguments);
    this.options = options.isEmpty() ? Collections.<OptionDescriptor<B>>emptySet() : Collections.unmodifiableSet(new LinkedHashSet<OptionDescriptor<B>>(options.values()));
    this.name = name;
  }

  public abstract Class<T> getType();

  public abstract void printUsage(PrintWriter writer);

  public abstract void printMan(PrintWriter writer);

  public Set<String> getOptionNames() {
    return optionMap.keySet();
  }

  public Collection<OptionDescriptor<B>> getOptions() {
    return options;
  }

  public OptionDescriptor<B> getOption(String name) {
    return optionMap.get(name);
  }

  public List<ArgumentDescriptor<B>> getArguments() {
    return arguments;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return info != null ? info.getDisplay() : "";
  }

  public InfoDescriptor getInfo() {
    return info;
  }

  protected static InfoDescriptor info(Annotation[] annotations) {
    for (Annotation ann : annotations) {
      if (ann instanceof  Description) {
        Description descriptionAnn = (Description)ann;
        return new InfoDescriptor(
          descriptionAnn.display(),
          descriptionAnn.usage(),
          descriptionAnn.man()
        );
      }
    }
    return null;
  }

  protected static <B extends TypeBinding> ParameterDescriptor<B> create(
    B binding,
    Type type,
    Argument argumentAnn,
    Option optionAnn,
    InfoDescriptor info,
    Annotation ann) throws IntrospectionException {

    //
    if (argumentAnn != null) {
      if (optionAnn != null) {
        throw new IntrospectionException();
      }

      //
      return new ArgumentDescriptor<B>(
        binding,
        argumentAnn.name(),
        type,
        info,
        argumentAnn.required(),
        argumentAnn.password(),
        argumentAnn.completer(),
        ann);
    } else if (optionAnn != null) {
      return new OptionDescriptor<B>(
        binding,
        type,
        Collections.unmodifiableList(Arrays.asList(optionAnn.names())),
        info,
        optionAnn.required(),
        optionAnn.arity(),
        optionAnn.password(),
        optionAnn.completer(),
        ann);
    } else {
      return null;
    }
  }

  /**
   * Jus grouping some data for conveniency
   */
  protected static class Tuple {
    final Argument argumentAnn;
    final Option optionAnn;
    final InfoDescriptor descriptionAnn;
    final Annotation ann;
    private Tuple(Argument argumentAnn, Option optionAnn, InfoDescriptor info, Annotation ann) {
      this.argumentAnn = argumentAnn;
      this.optionAnn = optionAnn;
      this.descriptionAnn = info;
      this.ann = ann;
    }
  }

  protected static Tuple get(Annotation... ab) {
    Argument argumentAnn = null;
    Option optionAnn = null;
    InfoDescriptor descriptionAnn = null;
    Annotation info = null;
    for (Annotation parameterAnnotation : ab) {
      if (parameterAnnotation instanceof Option) {
        optionAnn = (Option)parameterAnnotation;
      } else if (parameterAnnotation instanceof Argument) {
        argumentAnn = (Argument)parameterAnnotation;
      } else if (parameterAnnotation instanceof Description) {
        descriptionAnn = new InfoDescriptor((Description)parameterAnnotation);
      } else {

        // Look at annotated annotations
        Class<? extends Annotation> a = parameterAnnotation.annotationType();
        if (a.getAnnotation(Option.class) != null) {
          optionAnn = a.getAnnotation(Option.class);
          info = parameterAnnotation;
        } else if (a.getAnnotation(Argument.class) != null) {
          argumentAnn =  a.getAnnotation(Argument.class);
          info = parameterAnnotation;
        }

        //
        if (info != null) {
          Description annotationDescriptionAnn = a.getAnnotation(Description.class);
          if (annotationDescriptionAnn != null) {
            InfoDescriptor annotationInfo = new InfoDescriptor(annotationDescriptionAnn);
            if (descriptionAnn == null) {
              descriptionAnn = annotationInfo;
            } else {
              descriptionAnn = new InfoDescriptor(descriptionAnn, annotationInfo);
            }
          }
        }
      }
    }

    return new Tuple(argumentAnn, optionAnn, descriptionAnn, info);
  }
}

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

package org.crsh.command;

import com.beust.jcommander.JCommander;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.crsh.cmdline.ClassCommandDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.ParameterBinding;
import org.crsh.cmdline.processor.CmdLineProcessor;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.TypeResolver;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * A base command that should be subclasses by Groovy commands. For this matter it inherits the
 * {@link GroovyObjectSupport} class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <C> the consumed type
 * @param <P> the produced type
 */
public abstract class BaseCommand<C, P> extends GroovyObjectSupport implements ShellCommand<C, P> {

  private static final class MetaData {

    /** . */
    private static final ConcurrentHashMap<String, MetaData> metaDataCache = new ConcurrentHashMap<String, MetaData>();

    static MetaData getMetaData(Class<?> clazz) {
      MetaData metaData = metaDataCache.get(clazz.getName());
      if (metaData == null || !metaData.isValid(clazz)) {
        metaData = new MetaData(clazz);
        metaDataCache.put(clazz.getName(), metaData);
      }
      return metaData;
    }

    /** . */
    private static final Pattern ARGS4J = Pattern.compile("^org\\.kohsuke\\.args4j\\.?$");

    /** . */
    private static final Pattern JCOMMANDER = Pattern.compile("^com\\.beust\\.jcommander\\.?$");

    /** . */
    private static final Pattern CMDLINE = Pattern.compile("^org\\.crsh\\.cmdline\\.?$");

    /** . */
    private final int descriptionFramework;

    /** . */
    private final String fqn;

    /** . */
    private final int identityHashCode;

    private MetaData(Class<?> clazz) {
      this.descriptionFramework = findDescriptionFramework(clazz);
      this.fqn = clazz.getName();
      this.identityHashCode = System.identityHashCode(clazz);
    }

    private boolean isValid(Class<?> clazz) {
      return identityHashCode == System.identityHashCode(clazz) && fqn.equals(clazz.getName());
    }

    private int findDescriptionFramework(Class<?> clazz) {
      if (clazz == null) {
        throw new NullPointerException();
      }
      Class<?> superClazz = clazz.getSuperclass();
      int bs;
      if (superClazz != null) {
        bs = findDescriptionFramework(superClazz);
      } else {
        bs = 0;
      }
      for (Field f : clazz.getDeclaredFields()) {
        for (Annotation annotation : f.getDeclaredAnnotations()) {
          String packageName = annotation.annotationType().getPackage().getName();
          if (ARGS4J.matcher(packageName).matches()) {
            bs |= 0x01;
          } else if (JCOMMANDER.matcher(packageName).matches()) {
            bs |= 0x02;
          } else if (CMDLINE.matcher(packageName).matches()) {
            bs |= 0x04;
          }
        }
      }
      return bs;
    }
  }

  /** . */
  private CommandContext<C, P> context;

  /** . */
  private boolean unquoteArguments;

  /** . */
  private Class<C> consumedType;

  /** . */
  private Class<P> producedType;

  /** . */
  private final MetaData metaData;

  protected BaseCommand() {
    this.context = null;
    this.unquoteArguments = true;
    this.consumedType = (Class<C>)TypeResolver.resolve(getClass(), ShellCommand.class, 0);
    this.producedType = (Class<P>)TypeResolver.resolve(getClass(), ShellCommand.class, 1);
    this.metaData = MetaData.getMetaData(getClass());
  }

  public Class<P> getProducedType() {
    return producedType;
  }

  public Class<C> getConsumedType() {
    return consumedType;
  }

  @Override
  public final Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {
      Object o = context.getAttributes().get(name);
      if (o instanceof Closure) {
        Closure closure = (Closure)o;
        if (args instanceof Object[]) {
          Object[] array = (Object[])args;
          if (array.length == 0) {
            return closure.call();
          } else {
            return closure.call(array);
          }
        } else {
          return closure.call(args);
        }
      } else {
        throw e;
      }
    }
  }

  @Override
  public final Object getProperty(String property) {
    try {
      return super.getProperty(property);
    }
    catch (MissingPropertyException e) {
      return context.getAttributes().get(property);
    }
  }

  @Override
  public final void setProperty(String property, Object newValue) {
    try {
      super.setProperty(property, newValue);
    }
    catch (MissingPropertyException e) {
      context.getAttributes().put(property, newValue);
    }
  }

  /**
   * Returns true if the command wants its arguments to be unquoted.
   *
   * @return true if arguments must be unquoted
   */
  public final boolean getUnquoteArguments() {
    return unquoteArguments;
  }

  public final void setUnquoteArguments(boolean unquoteArguments) {
    this.unquoteArguments = unquoteArguments;
  }

  protected final String readLine(String msg) {
    return readLine(msg, true);
  }

  protected final String readLine(String msg, boolean echo) {
    if (context == null) {
      throw new IllegalStateException("No current context");
    }
    return context.readLine(msg, echo);
  }

  private <T extends BaseCommand> void handle(T cmd, String[] args) {
    try {
      // WTF
      Class<T> clazz = (Class<T>)cmd.getClass();
      CommandDescriptor<T, ParameterBinding.ClassField> descriptor = CommandDescriptor.create(clazz);
      CmdLineProcessor.Clazz<T> processor = new CmdLineProcessor.Clazz<T>(descriptor);
      StringBuilder s = new StringBuilder();
      for (String arg : args) {
        if (s.length() > 0) {
          s.append(" ");
        }
        s.append(arg);
      }
      processor.process(cmd, s.toString());
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new ScriptException(e.getMessage(), e);
    }
  }

  public final void execute(CommandContext<C, P> context, String... args) throws ScriptException {
    if (context == null) {
      throw new NullPointerException();
    }
    if (args == null) {
      throw new NullPointerException();
    }

    //
    if (args.length > 0 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
      ShellPrinter out = context.getWriter();

      //
      Description description = getClass().getAnnotation(Description.class);
      if (description != null) {
        out.write(description.value());
        out.write("\n");
      }

      //
      switch (metaData.descriptionFramework) {
        default:
          System.out.println("Not only one description framework");
        case 0:
          break;
        case 1:
          CmdLineParser parser = new CmdLineParser(this);
          parser.printUsage(out, null);
          break;
        case 2:
          throw new UnsupportedOperationException();
        case 4:
          try {
            Class<?> clazz = getClass();
            ClassCommandDescriptor<?> descriptor = CommandDescriptor.create(clazz);
            out.print(descriptor.getUsage());
          }
          catch (IntrospectionException e) {
            throw new ScriptException(e.getMessage(), e);
          }
          break;
      }

      //
    } else {
      // Remove surrounding quotes if there are
      if (unquoteArguments) {
        String[] foo = new String[args.length];
        for (int i = 0;i < args.length;i++) {
          String arg = args[i];
          if (arg.charAt(0) == '\'') {
            if (arg.charAt(arg.length() - 1) == '\'') {
              arg = arg.substring(1, arg.length() - 1);
            }
          } else if (arg.charAt(0) == '"') {
            if (arg.charAt(arg.length() - 1) == '"') {
              arg = arg.substring(1, arg.length() - 1);
            }
          }
          foo[i] = arg;
        }
        args = foo;
      }

      //
      switch (metaData.descriptionFramework) {
        default:
          System.out.println("Not only one description framework");
        case 0:
          break;
        case 1:
          try {
            CmdLineParser parser = new CmdLineParser(this);
            parser.parseArgument(args);
          }
          catch (CmdLineException e) {
            throw new ScriptException(e.getMessage(), e);
          }
           break;
        case 2:
          try {
            JCommander jc = new JCommander(this);
            jc.parse(args);
          }
          catch (Exception e) {
            throw new ScriptException(e.getMessage(), e);
          }
          break;
        case 4:
          handle(this, args);
          break;
      }

      //
      try {
        this.context = context;

        //
        execute(context);
      }
      finally {
        this.context = null;
      }
    }
  }

  protected abstract void execute(CommandContext<C, P> context) throws ScriptException;

}

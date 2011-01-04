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
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.Strings;
import org.crsh.util.TypeResolver;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * The base command.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <C> the consumed type
 * @param <P> the produced type
 */
public abstract class BaseCommand<C, P> extends GroovyCommand implements ShellCommand, CommandInvoker<C, P> {

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
          }
        }
      }
      return bs;
    }
  }

  /** . */
  private InvocationContext<C, P> context;

  /** . */
  private boolean unquoteArguments;

  /** . */
  private Class<C> consumedType;

  /** . */
  private Class<P> producedType;

  /** . */
  private final MetaData metaData;

  /** . */
  private String[] args;

  protected BaseCommand() {
    this.context = null;
    this.unquoteArguments = true;
    this.consumedType = (Class<C>)TypeResolver.resolve(getClass(), CommandInvoker.class, 0);
    this.producedType = (Class<P>)TypeResolver.resolve(getClass(), CommandInvoker.class, 1);
    this.metaData = MetaData.getMetaData(getClass());
    this.args = null;
  }

  public Class<P> getProducedType() {
    return producedType;
  }

  public Class<C> getConsumedType() {
    return consumedType;
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

  @Override
  protected final InvocationContext<?, ?> getContext() {
    return context;
  }

  public final Map<String, String> complete(CommandContext context, String line) {
    return Collections.emptyMap();
  }

  public String describe(String line, DescriptionMode mode) {
    return null;
  }

  public final void usage(ShellPrinter printer) {
      //
      Description description = getClass().getAnnotation(Description.class);
      if (description != null) {
        printer.write(description.value());
        printer.write("\n");
      }

      //
      switch (metaData.descriptionFramework) {
        default:
          System.out.println("Not only one description framework");
        case 0:
          break;
        case 1:
          CmdLineParser parser = new CmdLineParser(this);
          parser.printUsage(printer, null);
          break;
        case 2:
          throw new UnsupportedOperationException();
      }
  }

  public final CommandInvoker<?, ?> createInvoker(String line) {
    List<String> chunks = Strings.chunks(line);
    this.args = chunks.toArray(new String[chunks.size()]);
    return this;
  }

  public final void invoke(InvocationContext<C, P> context) throws ScriptException {
    if (context == null) {
      throw new NullPointerException();
    }
    if (args == null) {
      throw new NullPointerException();
    }

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

  protected abstract void execute(InvocationContext<C, P> context) throws ScriptException;

}

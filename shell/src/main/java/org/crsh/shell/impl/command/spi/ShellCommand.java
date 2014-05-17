/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.crsh.shell.impl.command.spi;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.Format;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionException;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.completion.CompletionMatcher;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.Util;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;
import org.crsh.command.RuntimeContext;
import org.crsh.command.SyntaxException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A command as seen by the shell.
 */
public abstract class ShellCommand<T> {

  /**
   * Returns the command descriptor.
   *
   * @return the descriptor
   */
  public abstract CommandDescriptor<T> getDescriptor();

  /**
   * Returns a completer for this command.
   *
   * @param context the related runtime context
   * @return the completer
   * @throws CreateCommandException anything that would prevent completion to happen
   */
  protected abstract Completer getCompleter(RuntimeContext context) throws CreateCommandException;

  /**
   * Resolve the real match for a specified invocation match.
   *
   * @param match the match
   * @return the command
   */
  protected abstract CommandMatch<?, ?> resolve(InvocationMatch<T> match);

  public final String describe(final InvocationMatch<T> match, Format format) {

    //
    final CommandMatch<?, ?> commandMatch = resolve(match);

    //
    if (format instanceof Format.Man) {
      final Format.Man man = (Format.Man)format;
      format = new Format.Man() {
        @Override
        public void printSynopsisSection(CommandDescriptor<?> descriptor, Appendable stream) throws IOException {
          man.printSynopsisSection(descriptor, stream);

          // Extra stream section
          if (match.getDescriptor().getSubordinates().isEmpty()) {
            stream.append("STREAM\n");
            stream.append(Util.MAN_TAB);
            printFQN(descriptor, stream);
            stream.append(" <").append(commandMatch.getConsumedType().getName()).append(", ").append(commandMatch.getProducedType().getName()).append('>');
            stream.append("\n\n");
          }
        }
      };
    }

    //
    try {
      StringBuffer buffer = new StringBuffer();
      match.getDescriptor().print(format, buffer);
      return buffer.toString();
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Provide completions for the specified arguments.
   *
   * @param context the command context
   * @param line the original command line arguments
   * @return the completions
   */
  public final CompletionMatch complete(RuntimeContext context, String line) throws CreateCommandException {
    CompletionMatcher matcher = getDescriptor().completer();
    Completer completer = getCompleter(context);
    try {
      return matcher.match(completer, line);
    }
    catch (CompletionException e) {
      // command.log.log(Level.SEVERE, "Error during completion of line " + line, e);
      return new CompletionMatch(Delimiter.EMPTY, Completion.create());
    }
  }

  /**
   * Returns a description of the command or null if none can be found.
   *
   * @param line the usage line
   * @param format the description format
   * @return the description
   */
  public final String describe(String line, Format format) {
    InvocationMatcher<T> analyzer = getDescriptor().matcher();
    InvocationMatch<T> match;
    try {
      match = analyzer.parse(line);
    }
    catch (org.crsh.cli.SyntaxException e) {
      throw new SyntaxException(e.getMessage());
    }
    return describe(match, format);
  }

  /**
   * Provides an invoker for the command line specified as a command line to parse.
   *
   * @param line the command line arguments
   * @return the command
   */
  public final CommandInvoker<?, ?> resolveInvoker(String line) throws CreateCommandException {
    return resolveCommand(line).getInvoker();
  }

  public final CommandMatch<?, ?> resolveCommand(String line) throws CreateCommandException {
    CommandDescriptor<T> descriptor = getDescriptor();
    InvocationMatcher<T> analyzer = descriptor.matcher();
    InvocationMatch<T> match;
    try {
      match = analyzer.parse(line);
    }
    catch (org.crsh.cli.SyntaxException e) {
      throw new SyntaxException(e.getMessage());
    }
    return resolve(match);
  }

  /**
   * Provides an invoker for the command line specified in a detyped manner.
   *
   * @param options the base options
   * @param subordinate the subordinate command name, might null
   * @param subordinateOptions the subordinate options
   * @param arguments arguments
   * @return the command
   */
  public final CommandMatch<?, ?> resolveCommand(Map<String, ?> options, String subordinate, Map<String, ?> subordinateOptions, List<?> arguments) throws CreateCommandException {
    InvocationMatcher<T> matcher = getDescriptor().matcher();

    //
    if (options != null && options.size() > 0) {
      for (Map.Entry<String, ?> option : options.entrySet()) {
        matcher = matcher.option(option.getKey(), Collections.singletonList(option.getValue()));
      }
    }

    //
    if (subordinate != null && subordinate.length() > 0) {
      matcher = matcher.subordinate(subordinate);

      // Minor : remove that and use same signature
      if (subordinateOptions != null && subordinateOptions.size() > 0) {
        for (Map.Entry<String, ?> option : subordinateOptions.entrySet()) {
          matcher = matcher.option(option.getKey(), Collections.singletonList(option.getValue()));
        }
      }
    }

    //
    InvocationMatch<T> match = matcher.arguments(arguments != null ? arguments : Collections.emptyList());

    //
    return resolve(match);
  }
}

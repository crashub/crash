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

package org.crsh.command;

import org.crsh.cli.impl.completion.CompletionMatch;

import java.util.List;
import java.util.Map;

/**
 * A shell command.
 */
public interface ShellCommand {

  /**
   * Provide completions for the specified arguments.
   *
   * @param context the command context
   * @param line the original command line arguments
   * @return the completions
   */
  CompletionMatch complete(RuntimeContext context, String line) throws CommandCreationException;

  /**
   * Returns a description of the command or null if none can be found.
   *
   * @param line the usage line
   * @param mode the description mode
   * @return the description
   */
  String describe(String line, DescriptionFormat mode) throws CommandCreationException;

  /**
   * Provides an invoker for the command line specified as a command line to parse.
   *
   * @param line the command line arguments
   * @return the command
   */
  CommandInvoker<?, ?> resolveInvoker(String line) throws CommandCreationException;

  /**
   * Provides an invoker for the command line specified in a detyped manner.
   *
   * @param options the base options
   * @param subordinate the subordinate command name, might null
   * @param subordinateOptions the subordinate options
   * @param arguments arguments
   * @return the command
   */
  CommandInvoker<?, ?> resolveInvoker(Map<String, ?> options, String subordinate, Map<String, ?> subordinateOptions, List<?> arguments) throws CommandCreationException;

}

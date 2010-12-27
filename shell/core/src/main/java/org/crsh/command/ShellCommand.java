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

import java.util.List;

/**
 * <p>The shell command allows a single source to provide a customized invoker according to the context
 * of the arguments. More importantly it allows to decouple the obtention of a command related to its
 * arguments from the actual execution of the command. This somewhat matters because the command execution
 * pipeline has notion of consumed and produced types, thanks to this, the consumed and produced
 * types can vary according to the arguments.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ShellCommand {

  /**
   * Provide completions for the specified arguments.
   *
   * @param context the command context
   * @param line the original submitted line
   * @param chunks the line splitted in chunks
   * @return the completions
   */
  List<String> complete(CommandContext context, String line, String... chunks);

  /**
   * Provides an invoker for the specified arguments.
   *
   * @param args the arguments
   * @return the command to provide
   */
  CommandInvoker<?, ?> createInvoker(String... args);

}

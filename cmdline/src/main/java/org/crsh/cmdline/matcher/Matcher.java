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

package org.crsh.cmdline.matcher;

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.EmptyCompleter;
import org.crsh.cmdline.matcher.impl.MatcherImpl;
import org.crsh.cmdline.spi.Completer;
import org.crsh.cmdline.spi.CompletionResult;

import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Matcher<T> {

  public static <T> Matcher<T> createMatcher(String mainName, ClassDescriptor<T> descriptor) {
    return new MatcherImpl<T>(mainName, descriptor);
  }

  public static <T> Matcher<T> createMatcher(ClassDescriptor<T> descriptor) {
    return new MatcherImpl<T>(descriptor);
  }

  public final CompletionResult<String> complete(String s) throws CmdCompletionException {
    return complete(EmptyCompleter.getInstance(), s);
  }

  public abstract CompletionResult<String> complete(Completer completer, String s) throws CmdCompletionException;


  public abstract CommandMatch<T, ?, ?> match(String s);

}

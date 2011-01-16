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

package org.crsh.cmdline.matcher.impl2;

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.Matcher;
import org.crsh.cmdline.spi.Completer;

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
    if (descriptor == null) {
      throw new NullPointerException();
    }

    //
    this.mainName = mainName;
    this.descriptor = descriptor;
  }

  @Override
  public Map<String, String> complete(Completer completer, String s) throws CmdCompletionException {
    throw new UnsupportedOperationException();
  }

  @Override
  public CommandMatch<T, ?, ?> match(String s) {
    throw new UnsupportedOperationException();
  }

  private static final int A = 0;

  public void foo(String s) {

    Tokenizer tokenizer = new Tokenizer(s);

    while (tokenizer.hasNext()) {

      Token token = tokenizer.next();

      switch (token.type) {
        case LONG_OPTION:
        case SHORT_OPTION:

          break;


      }



    }



  }
}

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
package org.crsh.command.pipeline;

import org.crsh.command.CommandContext;
import org.crsh.text.Chunk;

import java.io.IOException;

/** @author Julien Viet */
public class ConvertingPipe<C, P, CONS extends CommandContext<? super P>> extends AbstractPipe<C, P, CONS> {

  /** . */
  private final Class<C> consumedType;

  /** . */
  private final Class<P> producedType;

  public ConvertingPipe(Class<C> consumedType, Class<P> producedType, boolean piped) {
    super(piped);

    //
    this.consumedType = consumedType;
    this.producedType = producedType;
  }

  public Class<C> getConsumedType() {
    return consumedType;
  }

  public Class<P> getProducedType() {
    return producedType;
  }

  public void write(Chunk chunk) throws IOException {
    if (producedType.equals(Void.class)) {
      //
    } else if (producedType.isAssignableFrom(Chunk.class)) {
      P p = producedType.cast(chunk);
      consumer.provide(p);
    } else {
      consumer.write(chunk);
    }
  }

  public void provide(C element) throws IOException {
    if (producedType.isInstance(element)) {
      P p = producedType.cast(element);
      consumer.provide(p);
    } else {
      // Discarded for now...
    }
  }
}

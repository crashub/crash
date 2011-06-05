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

package org.crsh.cmdline.annotations;

import org.crsh.cmdline.EmptyCompleter;
import org.crsh.cmdline.spi.Completer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

  String[] names();

  /**
   * The option arity. The default value -1 indicates that the annotated java type should imply the arity and the multiplicity.
   * <ul>
   * <li>A single valued type will have an arity of 1 and a multiplicity of {@link org.crsh.cmdline.Multiplicity#ZERO_OR_ONE}
   * or {@link org.crsh.cmdline.Multiplicity#ZERO_OR_ONE} according to the related {@link Required} annotation</li>
   * <li>A multi valued type will have an unbounded arity and {@link org.crsh.cmdline.Multiplicity#MULTI} multiplicity.</li>
   * </ul>
   *
   * @return arity
   */
  int arity() default -1;

  boolean password() default false;

  /**
   * Indicates whether or not the value should be unquoted.
   *
   * @return the unquote value
   */
  boolean unquote() default true;

  Class<? extends Completer> completer() default EmptyCompleter.class;

}

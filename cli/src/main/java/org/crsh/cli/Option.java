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

package org.crsh.cli;

import org.crsh.cli.completers.EmptyCompleter;
import org.crsh.cli.spi.Completer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An option command parameter.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

  /**
   * The option names, when an option name has a single letter it will be used as a short switch, when it has
   * two letters or more it is considered as a long switch.
   *
   * @return the option names
   */
  String[] names();

  /**
   * Indicates whether or not the value should be unquoted.
   *
   * @return the unquote value
   */
  boolean unquote() default true;

  /**
   * The completer type to complete this option value.
   *
   * @return the completer type
   */
  Class<? extends Completer> completer() default EmptyCompleter.class;

}

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

package org.crsh.command.introspector;

import org.crsh.command.Description;
import org.crsh.command.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandInfo<T> {

  /** . */
  private final String name;

  /** . */
  private final String description;

  /** . */
  private final Map<String, ParameterInfo> parameterMap;

  public CommandInfo(String name, String description, List<ParameterInfo> parameters) {
    HashMap<String, ParameterInfo> parameterMap = new HashMap<String, ParameterInfo>();
    for (ParameterInfo parameter : parameters) {
      for (String parameterName : parameter.getNames()) {
        parameterMap.put(parameterName, parameter);
      }
    }

    //
    this.description = description;
    this.parameterMap = parameterMap;
    this.name = name;
  }

  public abstract Class<T> getType();

  public Iterable<ParameterInfo> getParameters() {
    return parameterMap.values();
  }

  public ParameterInfo getParameter(String name) {
    return parameterMap.get(name);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  protected static String description(Description descriptionAnn) {
    return descriptionAnn != null ? descriptionAnn.value() : "";
  }

  protected static ParameterInfo create(Description descriptionAnn, Parameter parameterAnn) {
    return new ParameterInfo(
      description(descriptionAnn),
      Collections.unmodifiableList(Arrays.asList(parameterAnn.names())),
      parameterAnn.required(),
      parameterAnn.arity(),
      parameterAnn.password());
  }
}

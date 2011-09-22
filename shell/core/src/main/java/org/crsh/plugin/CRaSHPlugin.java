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

package org.crsh.plugin;

import org.crsh.util.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Base class for a plugin, that consist of a subclass of this class and the implementation
 * of the business interface of the plugin. The business interface of the plugin is simply
 * represented by the P generic parameter and its associated class <code>Class&lt;P&gt;></code>.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <P> the plugin type
 */
public abstract class CRaSHPlugin<P> {

  /** . */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  static final int FAILED = -1;

  /** . */
  static final int CONSTRUCTED = 0;

  /** . */
  static final int INITIALIZING = 1;

  /** . */
  static final int INITIALIZED = 2;

  /** . */
  PluginContext context;

  /** . */
  int status;

  /** . */
  private final Class<P> type;

  protected CRaSHPlugin() {
    this.type = (Class<P>)TypeResolver.resolveToClass(getClass(), CRaSHPlugin.class, 0);
    this.status = CONSTRUCTED;
    this.context = null;
  }

  protected final PluginContext getContext() {
    return context;
  }

  public final Class<P> getType() {
    return type;
  }

  /**
   * Returns the implementation.
   *
   * @return the implementation
   */
  public abstract P getImplementation();

  public void init() {
  }

  public void destroy() {
  }

  @Override
  public String toString() {
    return "Plugin[type=" + getClass().getSimpleName() + ",interface=" + type.getSimpleName() + "]";
  }
}

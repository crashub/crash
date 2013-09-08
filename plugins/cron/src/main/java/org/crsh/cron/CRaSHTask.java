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
package org.crsh.cron;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import org.crsh.shell.ShellFactory;

/** @author Benjamin Prato */
class CRaSHTask extends Task {

  /** . */
  final CRaSHTaskDef def;

  /** . */
  final CronPlugin plugin;

  /** . */
  final ShellFactory factory;

  CRaSHTask(CronPlugin plugin, ShellFactory factory, CRaSHTaskDef def) {
    this.plugin = plugin;
    this.def = def;
    this.factory = factory;
  }

  @Override
  public void execute(TaskExecutionContext context) throws RuntimeException {
    CRaSHTaskProcess process = new CRaSHTaskProcess(this);
    process.run();
  }
}

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

import it.sauronsoftware.cron4j.SchedulingPattern;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Screenable;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Julien Viet */
public class CRaSHTaskProcess {

  /** . */
  private static final Logger log = Logger.getLogger(CronPlugin.class.getName());

  /** . */
  private final CRaSHTask task;

  /** . */
  private final StringBuilder msg = new StringBuilder();

  /** . */
  private final long time;

  /** . */
  private volatile boolean active;

  CRaSHTaskProcess(CRaSHTask task) {
    this.task = task;
    this.time = System.currentTimeMillis();

    //
    msg.append("Terminated task ").append(task.def.getLine()).append(" started at ").append(new Date(time)).append(" with buffer ");
  }

  public boolean isActive() {
    return active;
  }

  public long getTime() {
    return time;
  }

  public SchedulingPattern getSchedulingPattern() {
    return task.def.getSchedullingPattern();
  }

  public String getLine() {
    return task.def.getLine();
  }

  void run() {
    Shell sh = task.factory.create(null);
    ShellProcess sp = sh.createProcess(task.def.getLine());
    task.plugin.processes.add(this);
    task.plugin.history.add(this);
    while (task.plugin.history.size() > 100) {
      task.plugin.history.remove();
    }
    active = true;
    log.log(Level.FINE, "Started task with id=" + task.def.hashCode() + " pattern=" + task.def.getSchedullingPattern() +  " : "  + task.def.getLine());
    sp.execute(context);
  }

  /** . */
  private final ShellProcessContext context = new ShellProcessContext() {

    public void end(ShellResponse response) {
      active = false;
      task.plugin.processes.remove(CRaSHTaskProcess.this);
      log.log(Level.FINE, "Terminated task with id=" + task.def.hashCode() + " pattern=" + task.def.getSchedullingPattern() +  " : "  + task.def.getLine());
      log.log(Level.FINEST, msg.toString());
    }

    public boolean takeAlternateBuffer() throws IOException {
      return false;
    }

    public boolean releaseAlternateBuffer() throws IOException {
      return false;
    }

    public String getProperty(String propertyName) {
      return null;
    }

    public String readLine(String msg, boolean echo) {
      return null;
    }

    public int getWidth() {
      return 120;
    }

    public int getHeight() {
      return 40;
    }

    @Override
    public Appendable append(char c) throws IOException {
      msg.append(c);
      return this;
    }

    @Override
    public Appendable append(CharSequence s) throws IOException {
      msg.append(s);
      return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
      msg.append(csq, start, end);
      return this;
    }

    @Override
    public Screenable append(Style style) throws IOException {
      return this;
    }

    @Override
    public Screenable cls() throws IOException {
      return this;
    }

    public void flush() throws IOException {
    }
  };
}

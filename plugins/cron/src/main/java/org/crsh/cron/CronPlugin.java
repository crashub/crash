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

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.ShellFactory;
import org.crsh.util.Utils;
import org.crsh.vfs.Resource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Benjamin Prato */
public class CronPlugin extends CRaSHPlugin<CronPlugin> implements TaskCollector {

  public static PropertyDescriptor<String> CRON_CONFIG_PATH = new PropertyDescriptor<String>(String.class, "cron.config.path", null, "The contrab file path") {
    @Override
    protected String doParse(String s) throws Exception {
      return s;
    }
  };

  /** Logger */
  protected final Logger log = Logger.getLogger(getClass().getName());

  /** . */
  private final Scheduler scheduler = new Scheduler();

  /** . */
  final ConcurrentLinkedQueue<CRaSHTaskProcess> history = new ConcurrentLinkedQueue<CRaSHTaskProcess>();

  /** . */
  final CopyOnWriteArrayList<CRaSHTaskProcess> processes = new CopyOnWriteArrayList<CRaSHTaskProcess>();

  @Override
  public CronPlugin getImplementation() {
    return this;
  }

  public Scheduler getScheduler() {
    return scheduler;
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Collections.<PropertyDescriptor<?>>singleton(CRON_CONFIG_PATH);
  }

  @Override
  public void init() {
    scheduler.addTaskCollector(this);
    scheduler.start();
  }

  @Override
  public void destroy() {
    scheduler.stop();
  }

  /**
   * Returns the processes currently running.
   *
   * @return the running processes
   */
  public List<CRaSHTaskProcess> getProcesses() {
    return processes;
  }

  /**
   * Returns the processes history.
   *
   * @return the running processes
   */
  public Queue<CRaSHTaskProcess> getHistory() {
    return history;
  }

  /**
   * Spawn the job immediatly.
   * @return true when the jobs have been spawned successfully
   */
  public boolean spawn() {
    try {
      Method spawn = Scheduler.class.getDeclaredMethod("spawnLauncher", long.class);
      spawn.setAccessible(true);
      spawn.invoke(scheduler, System.currentTimeMillis());
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This method is protected so it can be overriden for unit tests
   *
   * @return read the config file and return it
   */
  protected Resource getConfig() {

    //
    Resource config = null;
    String configPath = getContext().getProperty(CRON_CONFIG_PATH);
    if (configPath != null) {
      File configFile = new File(configPath);
      if (configFile.exists()) {
        log.log(Level.FINE, "Found crontab file " + configPath);
        if (configFile.isFile()) {
          try {
            config = new Resource("crontab", configFile.toURI().toURL());
          }
          catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Could not retrieve cron config file from " + configPath, e);
          }
          catch (IOException e) {
            log.log(Level.FINE, "Could not load cron config file from " + configPath, e);
          }
        } else {
          log.log(Level.FINE, "Crontab file " + configPath + " is not a file");
        }
      } else {
        log.log(Level.FINE, "Crontab file " + configPath + " does not exist");
      }
    } else {
      // Override from config if any
      Resource res = getContext().loadResource("crontab", ResourceKind.CONFIG);
      if (res != null) {
        config = res;
        log.log(Level.FINE, "Found crontab config url " + res);
      }
    }

    //
    if (config == null) {
      log.log(Level.INFO, "No crontab configuration");
    }

    //
    return config;
  }

  public TaskTable getTasks() {
    if (getConfig() == null) {
      return new TaskTable();
    }
    //
    Resource res = getConfig();
    List<String> lines = null;
    try {
      lines = new ArrayList<String>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(res.getContent()), "UTF-8"));
      while (true) {
        String cronLine = reader.readLine();
        if (cronLine == null) {
          break;
        } else {
          lines.add(cronLine);
        }
      }
    }
    catch (IOException e) {
      log.log(Level.FINE, "Could not read cron file", e);
    }

    //
    ShellFactory factory = getContext().getPlugin(ShellFactory.class);
    TaskTable table = new TaskTable();
    for (String cronLine : lines) {
      CRaSHTaskDef crshTask = validateAndParseCronLine(cronLine);
      if (crshTask != null) {
        table.add(crshTask.getSchedullingPattern(), new CRaSHTask(this, factory, crshTask));
      }
      else {
        log.log(Level.FINE, "Cannot parse cron line " + cronLine);
      }
    }
    return table;
  }

  private CRaSHTaskDef validateAndParseCronLine(String cronLine) {

    //
    cronLine = cronLine.trim();
    if (cronLine.length() == 0 || cronLine.charAt(0) == '#') {
      return null;
    }

    //
    List<String> cronLineParts = Arrays.asList(cronLine.split("\\s+"));
    if (cronLineParts.size() < 6) {
      return null;
    }

    //
    String schedulePart = Utils.join(cronLineParts.subList(0, 5), " ");
    String commandPart = Utils.join(cronLineParts.subList(5, cronLineParts.size()), " ");

    try {
      return new CRaSHTaskDef(new SchedulingPattern(schedulePart), commandPart);
    }
    catch (InvalidPatternException e) {
      // ?
      return null;
    }
  }
}

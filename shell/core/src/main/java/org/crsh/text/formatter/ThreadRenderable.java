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

package org.crsh.text.formatter;

import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Renderable;
import org.crsh.text.Renderer;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;
import org.crsh.util.Utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ThreadRenderable extends Renderable<Thread> {

  /** . */
  private static final EnumMap<Thread.State, Color> colorMapping = new EnumMap<Thread.State, Color>(Thread.State.class);

  static {
    colorMapping.put(Thread.State.NEW, Color.cyan);
    colorMapping.put(Thread.State.RUNNABLE, Color.green);
    colorMapping.put(Thread.State.BLOCKED, Color.red);
    colorMapping.put(Thread.State.WAITING, Color.yellow);
    colorMapping.put(Thread.State.TIMED_WAITING, Color.magenta);
    colorMapping.put(Thread.State.TERMINATED, Color.blue);
  }

  @Override
  public Class<Thread> getType() {
    return Thread.class;
  }

  @Override
  public Renderer renderer(Iterator<Thread> stream) {

    //
    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    //
    List<Thread> threads = Utils.list(stream);

    // Sample CPU
    Map<Long, Long> times1 = new HashMap<Long, Long>();
    for (Thread thread : threads) {
      long cpu = threadMXBean.getThreadCpuTime(thread.getId());
      times1.put(thread.getId(), cpu);
    }

    try {
      // Sleep 100ms
      Thread.sleep(100);
    }
    catch (InterruptedException e) {
    }

    // Resample
    Map<Long, Long> times2 = new HashMap<Long, Long>(threads.size());
    for (Thread thread : threads) {
      long cpu = threadMXBean.getThreadCpuTime(thread.getId());
      times2.put(thread.getId(), cpu);
    }

    // Compute delta map and total time
    long total = 0;
    Map<Long, Long> deltas = new HashMap<Long, Long>(threads.size());
    for (Long id : times2.keySet()) {
      long time1 = times2.get(id);
      long time2 = times1.get(id);
      if (time1 == -1) {
        time1 = time2;
      } else if (time2 == -1) {
        time2 = time1;
      }
      long delta = time2 - time1;
      System.out.print("Thread ID :" + id + " delta:" + delta);
      deltas.put(id, delta);
      total += delta;
    }

    //
    TableElement table = new TableElement(1,1,1,1,1,1,1,2,5);

    // Header
    RowElement header = new RowElement();
    header.style(Decoration.bold.fg(Color.black).bg(Color.white));
    header.add(new LabelElement("ID"));
    header.add(new LabelElement("PRIORITY"));
    header.add(new LabelElement("STATE"));
    header.add(new LabelElement("%CPU"));
    header.add(new LabelElement("TIME"));
    header.add(new LabelElement("INTERRUPTED"));
    header.add(new LabelElement("DAEMON"));
    header.add(new LabelElement("GROUP"));
    header.add(new LabelElement("NAME"));
    table.add(header);

    //
    for (Thread thread : threads) {
      Color c = colorMapping.get(thread.getState());
      long seconds = times2.get(thread.getId()) / 1000000000;
      long min = seconds / 60;
      String time = min + ":" + (seconds % 60);

      long threadDelta = deltas.get(thread.getId());
      long cpu = 0;
      if (threadDelta != 0) {
          Math.round((threadDelta * 100) / total);
      }



      //
      ThreadGroup group = thread.getThreadGroup();

      //
      RowElement row = new RowElement();
      row.style(c.bg().fg(Color.black));
      row.add(new LabelElement(thread.getId()));
      row.add(new LabelElement(thread.getPriority()));
      row.add(new LabelElement(thread.getState()));
      row.add(new LabelElement(cpu));
      row.add(new LabelElement(time));
      row.add(new LabelElement(thread.isInterrupted()));
      row.add(new LabelElement(thread.isDaemon()));
      row.add(new LabelElement(group == null ? "" : group.getName()));
      row.add(new LabelElement(thread.getName()));
      table.add(row);
    }

    //
    return table.renderer();
  }
}

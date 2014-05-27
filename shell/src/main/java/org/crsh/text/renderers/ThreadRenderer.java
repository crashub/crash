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

package org.crsh.text.renderers;

import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.Overflow;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;
import org.crsh.util.Utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ThreadRenderer extends Renderer<Thread> {

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
  public LineRenderer renderer(Iterator<Thread> stream) {

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
      Thread.currentThread().interrupt();
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
      deltas.put(id, delta);
      total += delta;
    }

    // Compute cpu
    final HashMap<Thread, Long> cpus = new HashMap<Thread, Long>(threads.size());
    for (Thread thread : threads) {
      long cpu = total == 0 ? 0 : Math.round((deltas.get(thread.getId()) * 100) / total);
      cpus.put(thread, cpu);
    }

    // Sort by CPU time : should be a rendering hint...
    Collections.sort(threads, new Comparator<Thread>() {
      public int compare(Thread o1, Thread o2) {
        long l1 = cpus.get(o1);
        long l2 = cpus.get(o2);
        if (l1 < l2) {
          return 1;
        } else if (l1 > l2) {
          return -1;
        } else {
          return 0;
        }
      }
    });

    //
    TableElement table = new TableElement(1,3,2,1,1,1,1,1,1).overflow(Overflow.HIDDEN).rightCellPadding(1);

    // Header
    table.add(
      new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add(
        "ID",
        "NAME",
        "GROUP",
        "PRIORITY",
        "STATE",
        "%CPU",
        "TIME",
        "INTERRUPTED",
        "DAEMON"
      )
    );

    //
    for (Thread thread : threads) {
      Color c = colorMapping.get(thread.getState());
      long seconds = times2.get(thread.getId()) / 1000000000;
      long min = seconds / 60;
      String time = min + ":" + (seconds % 60);
      long cpu = cpus.get(thread);
      ThreadGroup group = thread.getThreadGroup();
      table.row(
          new LabelElement(thread.getId()),
          new LabelElement(thread.getName()),
          new LabelElement(group == null ? "" : group.getName()),
          new LabelElement(thread.getPriority()),
          new LabelElement(thread.getState()).style(c.fg()),
          new LabelElement(cpu),
          new LabelElement(time),
          new LabelElement(thread.isInterrupted()),
          new LabelElement(thread.isDaemon())
      );
    }

    //
    return table.renderer();
  }
}

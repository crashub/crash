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

package crash.commands.base

import org.crsh.command.CRaSHCommand;
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import java.lang.management.ManagementFactory
import org.crsh.text.ui.UIBuilder
import org.crsh.command.InvocationContext;

@Usage("JVM informations")
class jvm extends CRaSHCommand {

  /**
   * Show JMX data about os.
   */
  @Usage("Show JVM operating system")
  @Command
  public void system(InvocationContext<Map> context) {
    out << "OPERATING SYSTEM\n";
    def os = ManagementFactory.operatingSystemMXBean;
    context.provide([name:"architecture",value:os?.arch]);
    context.provide([name:"name",value:os?.name]);
    context.provide([name:"version",value:os?.version]);
    context.provide([name:"processors",value:os?.availableProcessors]);
  }

  /**
   * Show JMX data about Runtime.
   */
  @Usage("Show JVM runtime")
  @Command
  public void runtime() {

    UIBuilder ui = new UIBuilder()

    ui.table() {

      def rt = ManagementFactory.runtimeMXBean
      row(decoration: bold, foreground: black, background: white) {
        label("RUNTIME", foreground: red); label("")
      }
      row() {
        label("name", foreground: red); label(rt?.name)
      }
      row() {
        label("spec name", foreground: red); label(rt?.specName)
      }
      row() {
        label("vendor", foreground: red); label(rt?.specVendor)
      }
      row() {
        label("management spec version", foreground: red); label(rt?.managementSpecVersion)
      }
    }
    out << ui;
  }

  /**
   * Show JMX data about Class Loading System.
   */
  @Usage("Show JVM classloding")
  @Command
  public void classloading() {
    UIBuilder ui = new UIBuilder()
    ui.table() {
      def cl = ManagementFactory.classLoadingMXBean

      row(decoration: bold, foreground: black, background: white) {
        label("CLASS LOADING SYSTEM", foreground: red); label("")
      }
      row() {
        label("isVerbose", foreground: red); label(cl?.isVerbose())
      }
      row() {
        label("loadedClassCount", foreground: red); label(cl?.loadedClassCount)
      }
      row() {
        label("totalLoadedClassCount", foreground: red); label(cl?.totalLoadedClassCount)
      }
      row() {
        label("unloadedClassCount", foreground: red); label(cl?.unloadedClassCount)
      }
    }
    out << ui;
  }

  /**
   * Show JMX data about Compilation.
   */
  @Usage("Show JVM compilation")
  @Command
  public void compilation() {
    UIBuilder ui = new UIBuilder()
    ui.table() {
      def comp = ManagementFactory.compilationMXBean
      row(decoration: bold, foreground: black, background: white) {
        label("COMPILATION", foreground: red); label("")
      }
      row() {
        label("totalCompilationTime", foreground: red); label(comp.totalCompilationTime)
      }
    }
    out << ui;
  }

  /**
   * Show JMX data about Memory.
   */
  @Usage("Show JVM memory")
  @Command
  public void mem() {
    UIBuilder ui = new UIBuilder()
    ui.table() {
      def mem = ManagementFactory.memoryMXBean
      def heapUsage = mem.heapMemoryUsage
      def nonHeapUsage = mem.nonHeapMemoryUsage
      row(decoration: bold, foreground: black, background: white) {
        label("MEMORY", foreground: red); label("")
      }
      row(decoration: bold) {
        label("HEAP STORAGE", foreground: red); label("")
      }
      row() {
        label("committed", foreground: red); label(heapUsage?.committed)
      }
      row() {
        label("init", foreground: red); label(heapUsage?.init)
      }
      row() {
        label("max", foreground: red); label(heapUsage?.max)
      }
      row() {
        label("used", foreground: red); label(heapUsage?.used)
      }
      row(decoration: bold) {
        label("NON-HEAP STORAGE", foreground: red); label("")
      }
      row() {
        label("committed", foreground: red); label(nonHeapUsage?.committed)
      }
      row() {
        label("init", foreground: red); label(nonHeapUsage?.init)
      }
      row() {
        label("max", foreground: red); label(nonHeapUsage?.max)
      }
      row() {
        label("used", foreground: red); label(nonHeapUsage?.used)
      }
    }
    out << ui;

    ui = new UIBuilder()
    ui.table() {
      row(decoration: bold, foreground: black, background: white) {
        label("MEMORY", foreground: red); label(""); label("")
      }
      ManagementFactory.memoryPoolMXBeans.each{ mp ->
        row() {
          label("name", foreground: red); label(mp?.name); label("")
        }

        String[] mmnames = mp.memoryManagerNames
        mmnames.each{ mmname ->
          row() {
            label(""); label("Manager Name", foreground: red); label(mmname)
          }
        }
        row() {
          label(""); label("Type", foreground: red); label(mp?.type)
        }
        row() {
          label(""); label("Usage threshold supported", foreground: red); label(mp?.isUsageThresholdSupported())
        }
        row() {
          label("", foreground: red); label(""); label("")
        }
      }
    }
    out << ui;
  }

  /**
   * Show JMX data about Thread.
   */
  @Usage("Show JVM garbage collection")
  @Command
  public void gc() {
    UIBuilder ui = new UIBuilder()
    ui.table() {
      row(decoration: bold, foreground: black, background: white) {
        label("GARBAGE COLLECTION", foreground: red); label(""); label("")
      }
      ManagementFactory.garbageCollectorMXBeans.each { gc ->
        row() {
          label("name", foreground: red); label(gc?.name); label("")
        }
        row() {
          label("") ;label("collection count", foreground: red); label(gc?.collectionCount)
        }
        row() {
          label("") ;label("collection time", foreground: red); label(gc?.collectionTime)
        }
        String[] mpoolNames = gc.memoryPoolNames
        mpoolNames.each { mpoolName ->
          row() {
            label("") ;label("mpool name", foreground: red); label(mpoolName)
          }
        }
      }
    }
    out << ui;
  }
}
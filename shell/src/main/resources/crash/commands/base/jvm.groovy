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

import org.crsh.cli.Command
import org.crsh.cli.Usage

import java.lang.management.ManagementFactory

import org.crsh.command.InvocationContext
import org.crsh.cli.Argument
import org.crsh.command.Pipe
import java.lang.management.MemoryPoolMXBean
import java.lang.management.MemoryUsage;

@Usage("JVM information")
class jvm {

  /**
   * Show JMX data about os.
   */
  @Usage("Show JVM operating system")
  @Command
  public void system(InvocationContext<Map> context) {
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
    def rt = ManagementFactory.runtimeMXBean
    context.provide([name:"name",value:rt?.name]);
    context.provide([name:"specName",value:rt?.specName]);
    context.provide([name:"specVendor",value:rt?.specVendor]);
    context.provide([name:"managementSpecVersion",value:rt?.managementSpecVersion]);
  }

  /**
   * Show JMX data about Class Loading System.
   */
  @Usage("Show JVM classloading")
  @Command
  public void classloading() {
    def cl = ManagementFactory.classLoadingMXBean
    context.provide([name:"isVerbose ",value:cl?.isVerbose()]);
    context.provide([name:"loadedClassCount ",value:cl?.loadedClassCount]);
    context.provide([name:"totalLoadedClassCount ",value:cl?.totalLoadedClassCount]);
    context.provide([name:"unloadedClassCount ",value:cl?.unloadedClassCount]);
  }

  /**
   * Show JMX data about Compilation.
   */
  @Usage("Show JVM compilation")
  @Command
  public void compilation() {
    def comp = ManagementFactory.compilationMXBean
    context.provide([name:"totalCompilationTime ",value:comp.totalCompilationTime]);
  }

  /**
   * Show memory heap.
   */
  @Usage("Show JVM memory heap")
  @Command
  public void heap(InvocationContext<MemoryUsage> context) {
    def mem = ManagementFactory.memoryMXBean
    def heap = mem.heapMemoryUsage
    context.provide(heap);
//    context.provide([name:"commited ",value:heap?.committed]);
//    context.provide([name:"init ",value:heap?.init]);
//    context.provide([name:"max ",value:heap?.max]);
//    context.provide([name:"used ",value:heap?.used]);
  }

  /**
   * Show memory non heap.
   */
  @Usage("Show JVM memory non heap")
  @Command
  public void nonheap(InvocationContext<MemoryUsage> context) {
    def mem = ManagementFactory.memoryMXBean
    def nonHeap = mem.nonHeapMemoryUsage
    context.provide(nonHeap);
//    context.provide([name:"commited ",value:nonHeap?.committed]);
//    context.provide([name:"init ",value:nonHeap?.init]);
//    context.provide([name:"max ",value:nonHeap?.max]);
//    context.provide([name:"used ",value:nonHeap?.used]);
  }

  /**
   * Show JMX data about Memory.
   */
  @Usage("Show JVM memory pools")
  @Command
  public void pools(InvocationContext<String> context) {
    ManagementFactory.memoryPoolMXBeans.each { pool ->
      context.provide(pool.name);
    }
  }

  @Command
  public void top(InvocationContext<MemoryUsage> context) {
    while (!Thread.currentThread().interrupted()) {
      out.cls();
      heap(context);
      out.flush();
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Show JMX data about Memory.
   */
  @Usage("Show JVM memory pool")
  @Command
  public Pipe<String, MemoryUsage> pool(@Argument List<String> pools) {
    def mem = ManagementFactory.memoryPoolMXBeans
    return new Pipe<String, MemoryUsage>() {
      @Override
      void open() {
        for (String pool : pools) {
          provide(pool);
        }
      }

      @Override
      void provide(String element) {

        MemoryPoolMXBean found = null;
        for (MemoryPoolMXBean pool : mem) {
          if (pool.getName().equals(element)) {
            found = pool;
            break;
          }
        }

        //
        if (found != null) {
//          context.provide(found.peakUsage)
          context.provide(found.usage)
        }
      }
    }

/*
    def nonHeapUsage = mem.nonHeapMemoryUsage

    out << "\nNON-HEAP STORAGE\n\n";
    out << "committed:" + nonHeapUsage?.committed + "\n";
    out << "init:" + nonHeapUsage?.init + "\n";
    out << "max:" + nonHeapUsage?.max + "\n\n";
*/


/*
    ManagementFactory.memoryPoolMXBeans.each{ mp ->
      out << "name :" + mp?.name + "\n";
      String[] mmnames = mp.memoryManagerNames
      mmnames.each{ mmname ->
              context.provide([name:"Manager Name",value:mmname]);
      }
      context.provide([name:"Type ",value:mp?.type]);
      context.provide([name:"Usage threshold supported ",value:mp?.isUsageThresholdSupported()]);
      out << "\n";
    }
*/
  }



  /**
   * Show JMX data about Thread.
   */
  @Usage("Show JVM garbage collection")
  @Command
  public void gc() {

      out << "\nGARBAGE COLLECTION\n";
      ManagementFactory.garbageCollectorMXBeans.each { gc ->
          out << "name :" + gc?.name + "\n";
          context.provide([name:"collection count ",value:gc?.collectionCount]);
          context.provide([name:"collection time ",value:gc?.collectionTime]);


          String[] mpoolNames = gc.memoryPoolNames
          mpoolNames.each { mpoolName ->
              context.provide([name:"mpool name ",value:mpoolName]);
          }
          out << "\n\n";
      }
  }
}

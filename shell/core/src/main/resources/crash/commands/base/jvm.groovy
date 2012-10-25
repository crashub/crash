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
    out << "\nOPERATING SYSTEM\n";
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
    out << "\nJVM runtime\n";
    def rt = ManagementFactory.runtimeMXBean
    context.provide([name:"name",value:rt?.name]);
    context.provide([name:"specName",value:rt?.specName]);
    context.provide([name:"specVendor",value:rt?.specVendor]);
    context.provide([name:"managementSpecVersion",value:rt?.managementSpecVersion]);
  }

  /**
   * Show JMX data about Class Loading System.
   */
  @Usage("Show JVM classloding")
  @Command
  public void classloading() {
    out << "JVM classloding\n";
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
   * Show JMX data about Memory.
   */
  @Usage("Show JVM memory")
  @Command
  public void mem() {

    def mem = ManagementFactory.memoryMXBean
    def heapUsage = mem.heapMemoryUsage
    def nonHeapUsage = mem.nonHeapMemoryUsage
    out << "\nMEMORY:\n";
    out << "\nHEAP STORAGE\n\n";
    out << "committed:" + heapUsage?.committed + "\n";
    out << "init:" + heapUsage?.init + "\n";
    out << "max:" + heapUsage?.max + "\n";
    out << "used:" + heapUsage?.used + "\n";

    out << "\nNON-HEAP STORAGE\n\n";
    out << "committed:" + nonHeapUsage?.committed + "\n";
    out << "init:" + nonHeapUsage?.init + "\n";
    out << "max:" + nonHeapUsage?.max + "\n\n";


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
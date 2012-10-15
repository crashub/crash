package crash.commands.base

import org.crsh.cmdline.annotations.Command;
import org.crsh.command.CRaSHCommand;
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Option
import java.lang.management.ManagementFactory
import org.crsh.text.ui.UIBuilder

@Usage("JVM informations")
class jvm extends CRaSHCommand {
	
	
	@Usage("Show JVM informations ")
	@Command
	void info(
		@Usage("Show JMX data about os") @Option(names=["os"]) String osParam,
		@Usage("Show JMX data about Runtime") @Option(names=["rt"]) String rtParam,
		@Usage("Show JMX data about Class Loading System") @Option(names=["cl"]) String clParam,
		@Usage("Show JMX data about Compilation") @Option(names=["comp"]) String compParam,
		@Usage("Show JMX data about Memory") @Option(names=["mem"]) String memParam,
		@Usage("Show JMX data about Thread") @Option(names=["td"]) String tdParam
		) {
		if (null != osParam) {
			showOsInfo();
		}
		if (null != rtParam) {
			showRtInfo();
		}
		if (null != clParam) {
			showClInfo();
		}
		if (null != compParam) {
			showCompInfo();
		}
		if (null != memParam) {
			showMemInfo();
		}
		if (null != tdParam) {
			showTdInfo();
		}
		if ((null == osParam) && (null == rtParam) && (null == clParam) && (null == compParam) && (null == memParam) && (null == tdParam) ) {
			showOsInfo();
			showRtInfo();
			showClInfo();
			showCompInfo();
			showMemInfo();
			showTdInfo();
		}
		
	}

  /**
   * Show JMX data about os.
   */
  void showOsInfo() {

      UIBuilder ui = new UIBuilder()

      ui.table() {
          def os = ManagementFactory.operatingSystemMXBean;
          row(decoration: bold, foreground: black, background: white) {
            label(value: "OPERATING SYSTEM", foreground: red); label("")
          }
          row() {
            label(value: "architecture", foreground: red); label(os?.arch)
          }
          row() {
            label(value: "name", foreground: red); label(os?.name)
          }
          row() {
            label(value: "version", foreground: red); label(os?.version)
          }
          row() {
            label(value: "processors", foreground: red); label(os?.availableProcessors)
          }
      }
      out << ui;
  }

  /**
   * Show JMX data about Runtime.
   */
  void showRtInfo() {

      UIBuilder ui = new UIBuilder()

      ui.table() {

          def rt = ManagementFactory.runtimeMXBean
          row(decoration: bold, foreground: black, background: white) {
            label(value: "RUNTIME", foreground: red); label("")
          }
          row() {
            label(value: "name", foreground: red); label(rt?.name)
          }
          row() {
            label(value: "spec name", foreground: red); label(rt?.specName)
          }
          row() {
            label(value: "vendor", foreground: red); label(rt?.specVendor)
          }
          row() {
            label(value: "management spec version", foreground: red); label(rt?.managementSpecVersion)
          }
      }
      out << ui;
  }

  /**
   * Show JMX data about Class Loading System.
   */
  void showClInfo() {
      UIBuilder ui = new UIBuilder()
      ui.table() {
	      def cl = ManagementFactory.classLoadingMXBean

          row(decoration: bold, foreground: black, background: white) {
            label(value: "CLASS LOADING SYSTEM", foreground: red); label("")
          }
          row() {
            label(value: "isVerbose", foreground: red); label(cl?.isVerbose())
          }
          row() {
            label(value: "loadedClassCount", foreground: red); label(cl?.loadedClassCount)
          }
          row() {
            label(value: "totalLoadedClassCount", foreground: red); label(cl?.totalLoadedClassCount)
          }
          row() {
            label(value: "unloadedClassCount", foreground: red); label(cl?.unloadedClassCount)
          }
      }
      out << ui;
  }

  /**
   * Show JMX data about Compilation.
   */
  void showCompInfo() {
      UIBuilder ui = new UIBuilder()
      ui.table() {
          def comp = ManagementFactory.compilationMXBean
          row(decoration: bold, foreground: black, background: white) {
            label(value: "COMPILATION", foreground: red); label("")
          }
          row() {
            label(value: "totalCompilationTime", foreground: red); label(comp.totalCompilationTime)
          }
      }
      out << ui;
  }

  /**
   * Show JMX data about Memory.
   */
  void showMemInfo() {
      UIBuilder ui = new UIBuilder()
      ui.table() {
          def mem = ManagementFactory.memoryMXBean
	      def heapUsage = mem.heapMemoryUsage
	      def nonHeapUsage = mem.nonHeapMemoryUsage
          row(decoration: bold, foreground: black, background: white) {
            label(value: "MEMORY", foreground: red); label("")
          }
          row(decoration: bold) {
            label(value: "HEAP STORAGE", foreground: red); label("")
          }
          row() {
            label(value: "committed", foreground: red); label(heapUsage?.committed)
          }
          row() {
            label(value: "init", foreground: red); label(heapUsage?.init)
          }
          row() {
            label(value: "max", foreground: red); label(heapUsage?.max)
          }
          row() {
            label(value: "used", foreground: red); label(heapUsage?.used)
          }
          row(decoration: bold) {
            label(value: "NON-HEAP STORAGE", foreground: red); label("")
          }
          row() {
            label(value: "committed", foreground: red); label(nonHeapUsage?.committed)
          }
          row() {
            label(value: "init", foreground: red); label(nonHeapUsage?.init)
          }
          row() {
            label(value: "max", foreground: red); label(nonHeapUsage?.max)
          }
          row() {
            label(value: "used", foreground: red); label(nonHeapUsage?.used)
          }
      }
      out << ui;

      ui = new UIBuilder()
      ui.table() {
          row(decoration: bold, foreground: black, background: white) {
            label(value: "MEMORY", foreground: red); label(""); label("")
          }
          ManagementFactory.memoryPoolMXBeans.each{ mp ->
                row() {
                  label(value: "name", foreground: red); label(mp?.name); label("")
                }

		        String[] mmnames = mp.memoryManagerNames
		        mmnames.each{ mmname ->
                  row() {
                    label(""); label(value: "Manager Name", foreground: red); label(mmname)
                  }
                }
                row() {
                  label(""); label(value: "Type", foreground: red); label(mp?.type)
                }
                row() {
                  label(""); label(value: "Usage threshold supported", foreground: red); label(mp?.isUsageThresholdSupported())
                }
                row() {
                  label(value: "", foreground: red); label(""); label("")
                }
          }
      }
      out << ui;
  }

  /**
   * Show JMX data about Thread.
   */
  void showTdInfo() {
      UIBuilder ui = new UIBuilder()
      ui.table() {
          def td = ManagementFactory.threadMXBean
          row(decoration: bold, foreground: black, background: white) {
            label(value: "THREADS", foreground: red); label("") ; label("")
          }
          td.allThreadIds.each { tid ->
            row() {
              label(value: "Thread name", foreground: red); label(td.getThreadInfo(tid).threadName); label("")
            }
	      }
          row(decoration: bold, foreground: black, background: white) {
            label(value: "GARBAGE COLLECTION", foreground: red); label(""); label("")
          }
          ManagementFactory.garbageCollectorMXBeans.each { gc ->
            row() {
              label("") ;label(value: "name", foreground: red); label(gc?.name)
            }
            row() {
              label("") ;label(value: "collection count", foreground: red); label(gc?.collectionCount)
            }
            row() {
              label("") ;label(value: "collection time", foreground: red); label(gc?.collectionTime)
            }
            String[] mpoolNames = gc.memoryPoolNames
            mpoolNames.each { mpoolName ->
              row() {
                label("") ;label(value: "mpool name", foreground: red); label(mpoolName)
              }
		    }
          }
      }
      out << ui;
  }
}
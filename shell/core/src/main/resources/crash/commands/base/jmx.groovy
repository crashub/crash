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

package crash.command.base

import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.CRaSHCommand
import org.crsh.shell.ui.UIBuilder
import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.ObjectInstance
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Option
import javax.management.MBeanInfo
import org.crsh.cmdline.annotations.Argument
import javax.management.ObjectName

@Usage("Java Management Extensions")
class jmx extends CRaSHCommand {

  @Usage("todo")
  @Command
  void ls(
      InvocationContext<Void, ObjectName> context,
      @Usage("classname e.g:java.util.logging.Logging ") @Option(names=["c","classname"]) String classNameParam,      
      @Usage("ObjectName e.g:java.util.logging:type=Logging") @Option(names=["o","objectname"]) String objectNameParam,
	  @Usage("desc") @Option(names=["desc"]) String desc,
	  @Usage("attr") @Option(names=["attr"]) String attr,
	  @Usage("op") @Option(names=["op"]) String op
    ) {
	out.println("classname:" + classNameParam + " objectname:" + objectNameParam)
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectInstance> instances = server.queryMBeans(null, null);
    if (context.piped) {
      instances.each { instance ->
        context.produce(instance.objectName)
      }
	  //TODO : voir
	  out << ui;
    } else {
      UIBuilder ui = new UIBuilder()
	  // No args
	  if ((classNameParam == null) && (objectNameParam == null)) {
		  out.println("No args !")
	      ui.table() {
	        row(decoration: bold, foreground: black, background: white) {
	          label("CLASS NAME"); label("OBJECT NAME")
	        }
	        instances.each { instance ->
		          row() {
		            label(value: instance.className, foreground: red); label(instance.objectName)
		          }
	        }
	      }
		  out << ui;
	  }
	  // One arg : className 
	  if ((classNameParam != null) && (objectNameParam == null)) {
		  ui.table() {
			row(decoration: bold, foreground: black, background: white) {
			  label("OBJECT NAME")
			}
			instances.each { instance ->
				if (classNameParam == instance.className) {
				  row() {
					label(instance.objectName)
				  }
				}
			}
		  }
		  out << ui;
	  }
	  // Two Args
	  if ((classNameParam != null) && (objectNameParam != null)) {
		  instances.each { instance ->
			  if ((classNameParam != null) && (objectNameParam != null)) {
				  showManagedBeanDetails(server, instance.objectName)
			  }
		  }
		  
	  }

    }
  }
	
	void showManagedBeanDetails(MBeanServer server, ObjectName instance) {
		UIBuilder ui = new UIBuilder()
		
		// Show description table
//		ui.table() {
//			MBeanInfo info = server.getMBeanInfo(instance);
//			row() {
//				label(value: "DOMAIN", foreground: red); label(info.description)
//			}
//		}
//		out << ui;
		
		
		// Show attributes table
//		ui.table() {
//			row(decoration: bold, foreground: black, background: white) {
//			  label("ATTRIBUTE NAME"); label("ACCESS"); label("TYPE"); label("DESCRIPTION")
//			}
			MBeanInfo info = server.getMBeanInfo(instance);
			

//			info.attributes.each { attribute ->
//				row() {
//					String attr = "";
//					if (attribute.readable) {
//						attr += "R";
//					}
//					if (attribute.writable) {
//						attr += "W";
//					}
//					label(value: attribute.name, foreground: red); label(attr); label(attribute.type); label(attribute.description)
//				  }
//			  }
//		}
//		out << ui;
			
//-----------------------------------------	
//			ui.table() {
//				header {
//				  label("ATTRIBUTE NAME"); label("ACCESS"); label("TYPE"); label("DESCRIPTION")
//				}
//				info.attributes.each { attribute ->
//					row() {
//						String attr = "";
//						if (attribute.readable) {
//							attr += "R";
//						}
//						if (attribute.writable) {
//							attr += "W";
//						}
//						label(value: attribute.name, foreground: red); label(attr); label(attribute.type); label(attribute.description)
//					  }
//				  }
//
//			}
//			out << ui;
//---------------------------------------------------			
			info.attributes.each { attribute ->
				String attr = "";
				if (attribute.readable) {
					attr += "R";
				}
				if (attribute.writable) {
					attr += "W";
				}
				out.println(attribute.name + " " + attr + " " + attribute.type + " " + attribute.description)
			}
			out << ui;
		// Show operations table
//		ui.table() {
//			row(decoration: bold, foreground: black, background: white) {
//			  label("OPERATIONS"); label("RETURN TYPE"); label("DESCRIPTION"); label("PARAMETERS")
//			}
//			MBeanInfo info = server.getMBeanInfo(instance);
//		   
//			info.operations.each { operation ->
//				row() {
//					label(value: operation.name, foreground: red); label(attr); operation.returnType; operation.description
//				  }
//			  }
//		}

		
	}
	
//void showResult(Set<ObjectInstance> instances)

  @Usage("todo")
  @Command
  void get(
      @Option(names=['a','attributes']) List<String> attributes,
      @Argument List<String> names) {

    MBeanServer server = ManagementFactory.getPlatformMBeanServer();


    // Determine attributes from names
    if (attributes == null) {
      HashSet<String> tmp = [] as HashSet;
      names.each { name ->
        ObjectName on = ObjectName.getInstance(name);
        MBeanInfo info = server.getMBeanInfo(on);
        info.attributes.each { attribute ->
          tmp.add(attribute.name);
        }
      }
      attributes = new ArrayList<String>(tmp);
    }

    UIBuilder ui = new UIBuilder()
    ui.table() {
      row(decoration: bold, foreground: black, background: white) {
        label("OBJECT NAME");
        attributes.each { attribute ->
          label(attribute)
        }
      }
      names.each { name ->
        ObjectName on = ObjectName.getInstance(name);
        row() {
          label(value: on.getCanonicalName(), foreground: red)
          attributes.each { attribute ->
            label(String.valueOf(server.getAttribute(on, attribute)))
          }
        }
      }
    }
    out << ui;
  }

}
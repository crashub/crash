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

import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.CRaSHCommand
import org.crsh.text.ui.UIBuilder
import java.lang.management.ManagementFactory
import javax.management.Descriptor
import javax.management.MBeanAttributeInfo
import javax.management.MBeanOperationInfo
import javax.management.MBeanServer
import javax.management.ObjectInstance
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Option
import javax.management.MBeanInfo
import javax.management.MBeanParameterInfo
import org.crsh.cmdline.annotations.Argument
import javax.management.ObjectName
import javax.management.openmbean.ArrayType
import javax.management.openmbean.CompositeData
import javax.management.openmbean.CompositeType
import javax.management.openmbean.OpenType
import javax.management.openmbean.SimpleType

@Usage("Java Management Extensions")
class jmx extends CRaSHCommand {

	
  @Usage("Show informations on JMX MBean ")
  @Command
  void ls(
	  InvocationContext<Void, ObjectName> context,
	  @Usage("classname e.g:java.util.logging.Logging ") @Option(names=["c","classname"]) String classNameParam,
	  @Usage("ObjectName e.g:java.util.logging:type=Logging") @Option(names=["o","objectname"]) String objectNameParam,
	  @Usage("desc") @Option(names=["desc"]) String desc,
	  @Usage("attr") @Option(names=["attr"]) String attr,
	  @Usage("op") @Option(names=["op"]) String op
	) {

	UIBuilder ui = new UIBuilder();
	MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	Set<ObjectInstance> instances = server.queryMBeans(null, null);
	if (context.piped) {
	  instances.each { instance ->
		context.produce(instance.objectName)
	  }
	  out << ui;
	} else {

	  // No args
	  if ((classNameParam == null) && (objectNameParam == null)) {
		  showClassNamesObjectNames(instances);
	  }

	  // One arg : className
	  if ((classNameParam != null) && (objectNameParam == null)) {
		  showObjectNames(instances, classNameParam);
	  }

	  // Two Args
	  if ((classNameParam != null) && (objectNameParam != null)) {
		  instances.each { instance ->
				  if ((classNameParam == instance.className ) && (objectNameParam == instance.objectName.toString())) {
					  MBeanInfo info = server.getMBeanInfo(instance.objectName);
					  if (null != desc) {
						  out.println("");
						  showDescriptions(info, instance.objectName);
					  }
					  if (null != attr) {
						  out.println("");
						  showAttributes(info, server.getAttribute(instance.objectName,attribute.getName()),instance.objectName);
					  }
					  if (null != op) {
						  out.println("");
						  showOperations(info);
					  }
					  if ((null == attr) && (null == op) && (null == desc)) {
						  out.println("");
						  showDescriptions(info, instance.objectName);
						  out.println("");
						  showAttributes(info, server, instance.objectName);
						  out.println("");
						  showOperations(info);
					  }
				  }
		  }
		  
	  }

	}
  }
	/**
	 * Show all classNames and ObjectNames.
	 * eg : jmx ls
	 * @param instances Set<ObjectInstance>.
	 */
	void showClassNamesObjectNames(Set<ObjectInstance> instances) {
		UIBuilder ui = new UIBuilder();
		ui.table() {
		  row(decoration: bold, foreground: black, background: white) {
			label("CLASS NAME"); label("OBJECT NAME")
		  }
		  instances?.each { instance ->
			  if (null != instance) {
				row() {
				  label(value: instance.className, foreground: red); label(instance.objectName)
				}
			  }
		  }
		}
		out << ui;
	}
	
	/**
	 * Show ObjectName of a className.
	 * e.g : jmx ls -c java.util.logging.Logging.
	 * @param instances Set<ObjectInstance>
	 * @param classNameParam String
	 */
	void showObjectNames(Set<ObjectInstance> instances, String classNameParam) {
		UIBuilder ui = new UIBuilder();
		ui.table() {
			row(decoration: bold, foreground: black, background: white) {
			  label("OBJECT NAME")
			}
			instances?.each { instance ->
				if ((null != instance) && (classNameParam == instance.className)) {
				  row() {
					label(instance.objectName)
				  }
				}
			}
		  }
		  out << ui;
	}
	
	/**
	 * Show description.
	 * @param info MBeanInfo
	 * @param objectName ObjectName
	 */
	void showDescriptions(MBeanInfo info, ObjectName objectName) {
		UIBuilder ui = new UIBuilder();
		
		ui.table() {
			header(decoration: bold, foreground: black, background: white) {
				label("Description");label("Value")
			  }
			if ((null == objectName) && (null == info)) {
				row() {
					label(value: "", foreground: red); label("")
				  }
			}
			if (null != objectName) {
				row() {
					label(value: "Domain", foreground: red); label(objectName.getDomain())
				  }
				row() {
					label(value: "Type", foreground: red); label(objectName.getKeyPropertyListString())
				  }
			}
			if (null != info) {
				row() {
					label(value: "Java Class", foreground: red); label(info.className)
				  }
				row() {
					label(value: "Description", foreground: red); label(info.description)
				  }
			}
		}
		out << ui;
	}
	
	/**
	 * Show all operations of MBeanInfo.
	 * @param info MBeanInfo
	 */
	void showOperations(MBeanInfo info) {
		UIBuilder ui = new UIBuilder();
		ui.table() {
			row(decoration: bold, foreground: black, background: white) {
			  label("OPERATIONS"); label("RETURN TYPE"); label("DESCRIPTION"); label("PARAMETERS")
			}

			info?.operations?.each { operation ->
				if (null != operation) {
					row() {
						label(value: operation.name, foreground: red); label(operation.returnType); label(operation.description);
						 label(retreiveMethodParameters(operation))
					  }
				}
			}
		}
		out << ui;
	}
		
	/**
	 * Retreive all method parameters.
	 * @param operation MBeanOperationInfo
	 * @return String (p0:java.lang.String p1:java.lang.String)
	 */
	String retreiveMethodParameters(MBeanOperationInfo operation) {
		String result = "";
		if (null != operation) {
			MBeanParameterInfo[] params = operation.getSignature();
			for(int p = 0; p < params.length; p++)  {
				MBeanParameterInfo paramInfo = params[p];
				if (null != paramInfo) {
					String pname = paramInfo.getName();
					String type  = paramInfo.getType();
					result += pname + ":" + type + " ";
				}
			}
		}
		return result;
	}
		
	
	/**
	 * Retreive attributes name,type,description and values.
	 * @param info MBeanInfo.
	 * @param server MBeanServer.
	 * @param objectName ObjectName.
	 * @return Set<Attr>.
	 */
	Set<Attr> retreiveAttributes(MBeanInfo info, MBeanServer server, ObjectName objectName) {
		Set<Attr> lst = new TreeSet<Attr>();
		info?.attributes?.each { attribute ->
			if (null != attribute) {
				Object attrs = server?.getAttribute(objectName,attribute.name);
				String attr = "";
				if (attribute.readable) {
					attr += "R";
				}
				if (attribute.writable) {
					attr += "W";
				}
				lst.add(new Attr(attribute.name, attribute.type, attribute.description, attr, attrs));
			}
		}
		return lst;
	}
	
	/**
	 * Show attributes list on screen.
	 * @param server MBeanServer
	 * @param info MBeanInfo
	 * @param objectName ObjectName
	 */
	void showAttributes(MBeanInfo info, MBeanServer server, ObjectName objectName) {
		Set<Attr> lst = retreiveAttributes(info, server, objectName);
		UIBuilder ui = new UIBuilder();

		ui.table() {
			header(decoration: bold, foreground: black, background: white) {
			  label("ATTRIBUTE NAME"); label("ACCESS"); label("TYPE"); label("DESCRIPTION"); label("ATTRIBUTE VALUE")
			}
			
			for(Attr tmpAttr : lst) {
				if (null != tmpAttr) {
					row() {
						label(value: tmpAttr.name, foreground: red);
						label(tmpAttr.access);
						label(tmpAttr.type);
						label(tmpAttr.desc);
						label(tmpAttr.attrs.toString());
					}
				}
			}
		}
		out << ui;
	}
	
	/**
	 * Store attribute information temporary.
	 * @author drieu
	 *
	 */
	class Attr implements Comparable {
		def name,type,desc,access,attrs
		
		Attr(String n, String t, String d, String a, Object attrs) {
			this.name = n;
			this.type = t;
			this.desc = d;
			this.access = a;
			this.attrs = attrs;
		}
		
		@Override
		public boolean equals(Object obj) {
			boolean bCmp = false;
			if ((obj instanceof Attr)) {
				Attr attr = (Attr)obj;
				if ((this.name == attr.name) && (this.type == attr.type) && (this.desc == attr.desc)) {
					bCmp = true;
				}
			}
			return bCmp;
		}
		
		@Override
		public int compareTo(Object obj) {
			int cmp = 1;
			if ((obj instanceof Attr)) {
				Attr attr = (Attr)obj;
				if ((this.name == attr.name) && (this.type == attr.type) && (this.desc == attr.desc)) {
					cmp = 0;
				}
			}
			return cmp;
		}
	}
	

  @Usage("Retreive attribute value (jmx get -a Name java.lang:type=Compilation)")
  @Command
  void get(
	  @Usage("Attribute name e.g:Name") @Option(names=['a','attributes']) List<String> attributes,
	  @Usage("Name list e.g:java.lang:type=Compilation") @Argument List<String> names) {

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
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Required
import org.crsh.text.ui.UIBuilder

import javax.management.MBeanServerFactory

@Usage("mule commands")
class mule extends CRaSHCommand {
  @Usage("print information about a mule broker")
  @Command
  void info() {
      def muleContextMBean = getFirstMuleContextMBean();
      if (muleContextMBean == null) {
          out << "Failed to locate any MuleContext MBean - report this issue to the developers of this command\n"
      } else {
          out << "Mule Version: $muleContextMBean.Version\n"
          out << "Build Number: $muleContextMBean.BuildNumber\n"
          out << "Build Date  : $muleContextMBean.BuildDate\n"
          out << "Host        : $muleContextMBean.HostIp $muleContextMBean.Hostname\n"
          out << "OS          : $muleContextMBean.OsVersion\n"
          out << "JDK         : $muleContextMBean.JdkVersion\n"
      }
  }

  // TODO list apps
  // TODO control one app
  // TODO list flows and connectors in app
  // TODO control one flow/connector
  
  private GroovyMBean getFirstMuleContextMBean() {
      def mbeanServers = MBeanServerFactory.findMBeanServer(null)
      for (mbeanServer in mbeanServers) {
          for (domain in mbeanServer.domains) {
              if (domain ==~ /Mule\..+/) {
                  return new GroovyMBean(mbeanServer, "$domain:name=MuleContext")
              }
          }
      }
      return null
  }
}
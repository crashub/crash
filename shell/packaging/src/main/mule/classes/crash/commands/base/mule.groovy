import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Required
import org.crsh.text.ui.UIBuilder

import javax.management.MBeanServerFactory
import javax.management.MBeanServer

@Usage("mule commands")
class mule extends CRaSHCommand {
  MBeanServer mbeanServer = getMBeanServer()

  @Usage("print information about the broker")
  @Command
  void info() {
      def muleContextMBean = getFirstMuleContextMBean()
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

  @Usage("list the names of all deployed applications")
  @Command
  void apps() {
      mbeanServer.domains.each { domain ->
          if (isMuleApplicationDomain(domain)) {
              out << "${domain.substring(5)}\n"
          }
      }
  }

  // TODO control one app
  // TODO list flows and connectors in app
  // TODO control one flow/connector

  private GroovyMBean getFirstMuleContextMBean() {
      for (domain in mbeanServer.domains) {
          if (isMuleApplicationDomain(domain)) {
              return new GroovyMBean(mbeanServer, "$domain:name=MuleContext")
          }
      }
      return null
  }

  private boolean isMuleApplicationDomain(String domain) {
      domain ==~ /Mule\..+/
  }

  private MBeanServer getMBeanServer() {
      MBeanServerFactory.findMBeanServer(null)[0]
  }
}
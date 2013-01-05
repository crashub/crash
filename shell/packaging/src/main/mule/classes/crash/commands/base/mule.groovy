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
          context.provide([name:"Mule Version",value:muleContextMBean.Version])
          context.provide([name:"Build Number",value:muleContextMBean.BuildNumber])
          context.provide([name:"Build Date",value:muleContextMBean.BuildDate])
          context.provide([name:"Host IP",value:muleContextMBean.HostIp])
          context.provide([name:"Hostname",value:muleContextMBean.Hostname])
          context.provide([name:"OS",value:muleContextMBean.OsVersion])
          context.provide([name:"JDK",value:muleContextMBean.JdkVersion])
      }
  }

  @Usage("list the names of all deployed applications")
  @Command
  void apps() {
      mbeanServer.domains.each { domain ->
          if (isMuleApplicationDomain(domain)) {
              def muleContextMBean = new GroovyMBean(mbeanServer, "$domain:name=MuleContext")
              context.provide([name:domain.substring(5),'start time':muleContextMBean.StartTime])
          }
      }
  }

  @Usage("print an application's statistics")
  @Command
  void stats(@Usage("The application name") @Required @Argument String applicationName) {
      def statisticsMBean = new GroovyMBean(mbeanServer, "Mule.$applicationName:type=org.mule.Statistics,Application=application totals")
      if (statisticsMBean == null) {
          out << "No application named $applicationName has been found\n"
      } else {
          statisticsMBean.listAttributeNames().each { statisticName ->
              context.provide([name:statisticName,value:statisticsMBean[statisticName]])
          }
      }
  }

  // TODO control one app
  // TODO list flows, connectors and endpoints in app
  // TODO get one flow stats
  // TODO control one flow, connector or endpoint

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
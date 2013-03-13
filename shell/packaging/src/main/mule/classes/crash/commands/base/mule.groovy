import org.crsh.command.ScriptException
import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.cli.Argument
import org.crsh.cli.Option
import org.crsh.cli.Required
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion
import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.cli.descriptor.OptionDescriptor

import javax.management.MBeanServerFactory
import javax.management.MBeanServer
import javax.management.ObjectName

@Usage("mule commands")
class mule implements Completer {
  static final ObjectName CE_WRAPPER_MANAGER_ON = new ObjectName('Mule:name=WrapperManager')
  static final ObjectName EE_WRAPPER_MANAGER_ON = new ObjectName('org.tanukisoftware.wrapper:type=WrapperManager')
  MBeanServer mbeanServer = getMBeanServer()

  @Usage("print information about the broker")
  @Command
  void info() {
      def muleContextMBean = getFirstMuleContextMBean()
      if (muleContextMBean == null) {
          throw new ScriptException("No MuleContext MBean located");
      } else {
          def muleWrapperManagerMBean = getMuleWrapperManagerMBean()
          context.provide([name:"Mule Version",value:muleContextMBean.Version])
          context.provide([name:"Build Number",value:muleContextMBean.BuildNumber])
          context.provide([name:"Build Date",value:muleContextMBean.BuildDate])
          context.provide([name:"Host IP",value:muleContextMBean.HostIp])
          context.provide([name:"Hostname",value:muleContextMBean.Hostname])
          context.provide([name:"OS",value:muleContextMBean.OsVersion])
          context.provide([name:"JDK",value:muleContextMBean.JdkVersion])
          context.provide([name:"Launched As Service",value:muleWrapperManagerMBean.LaunchedAsService])
          context.provide([name:"Debug Enabled",value:muleWrapperManagerMBean.DebugEnabled])
          context.provide([name:"Java PID",value:muleWrapperManagerMBean.JavaPID])
          context.provide([name:"JVM Id",value:muleWrapperManagerMBean.JVMId])
      }
  }

  @Usage("list the names of all deployed applications")
  @Command
  void apps() {
      getApplicationNames().each { appName ->
          def contextMBean = new GroovyMBean(mbeanServer, "Mule.$appName:name=MuleContext")
          context.provide([name:appName,'start time':contextMBean.StartTime,initialized:contextMBean.Initialised,stopped:contextMBean.Stopped])
      }
  }

  @Usage("print the statistics for an application or a flow within an application")
  @Command
  void stats(@Usage("The application name") @Required @Option(names=["a"], completer=mule.class) String applicationName,
             @Usage("The flow name") @Option(names=["f"]) String flowName) {

      try {
          def statisticsMBeanObjectName =
              "Mule.$applicationName:type=org.mule.Statistics,"
              .plus(flowName != null ? "Flow=$flowName" : 'Application=application totals')

          def statisticsMBean = new GroovyMBean(mbeanServer, statisticsMBeanObjectName)
          statisticsMBean.listAttributeNames().each { statisticName ->
              context.provide([name:statisticName,value:statisticsMBean[statisticName]])
          }
      } catch (Exception e) {
          out << "Failed to locate statistics for the provided parameters ($e.message)\n"
      }
  }

  @Usage("list all the flows of an application")
  @Command
  void flows(@Usage("The application name") @Required @Option(names=["a"], completer=mule.class) String applicationName) {
      listMBeans(applicationName, 'Flow', { mBean -> 
          [name:mBean.Name,type:mBean.Type]
      })
  }

  @Usage("list all the connectors of an application")
  @Command
  void connectors(@Usage("The application name") @Required @Option(names=["a"], completer=mule.class) String applicationName) {
      listMBeans(applicationName, 'Connector', { mBean -> 
          [name:mBean.Name, protocol:mBean.Protocol,started:mBean.Started,disposed:mBean.Disposed]
      })
  }

  @Usage("list all the endpoints of an application")
  @Command
  void endpoints(@Usage("The application name") @Required @Option(names=["a"], completer=mule.class) String applicationName) {
      listMBeans(applicationName, 'Endpoint', { mBean -> 
          [name:mBean.Name,address:mBean.Address,flow:mBean.ComponentName,inbound:mBean.Inbound,outbound:mBean.Outbound,connected:mBean.Connected]
      })
  }

  public static enum EndpointAction {connect, disconnect}

  @Usage("control an endpoint")
  @Command
  void endpoint(@Usage("The application name") @Required @Option(names=["a"], completer=mule.class) String applicationName,
                 @Usage("The endpoint name") @Required @Option(names=["e"]) String endpointName,
                 @Usage("The endpoint action to run") @Required @Argument EndpointAction endpointAction) {

      for (ObjectName endpointObjectName : getObjectNames(applicationName, "Endpoint")) {
          def endpointMBean = new GroovyMBean(mbeanServer, endpointObjectName)
          if (endpointMBean.Name == endpointName) {
              try {
                  endpointMBean.invokeMethod(endpointAction.toString(), [] as Object[])
                  out << "Action $endpointAction successfully run. Endpoint connected: $endpointMBean.Connected\n"
              } catch (Exception e) {
                  out << "Failed to $endpointAction the endpoint ($e.message)\n"
              }
              return
          }
      }

      out << "Failed to locate an endpoint for the provided parameters\n"
  }

  public static enum ConnectorAction {
      initialize('initialise'), start('startConnector'), stop('stopConnector'), dispose('dispose')
      private final String value
      ConnectorAction(String value) { this.value = value }
      public String value() { return value }
  }

  @Usage("control a connector")
  @Command
  void connector(@Usage("The application name") @Required @Option(names=["a"], completer=mule.class) String applicationName,
                 @Usage("The connector name") @Required @Option(names=["c"]) String connectorName,
                 @Usage("The connector action to run") @Required @Argument ConnectorAction connectorAction) {

      try {
          def connectorMBean = new GroovyMBean(mbeanServer, "Mule.$applicationName:type=Connector,name=\"$connectorName\"")
          try {
              connectorMBean.invokeMethod(connectorAction.value(), [] as Object[])
              out << "Action $connectorAction successfully run. Connector started: $connectorMBean.Started, disposed: $connectorMBean.Disposed\n"
          } catch (Exception e) {
              out << "Failed to $connectorAction the connector ($e.message)\n"
          }
      }
      catch (Exception e) {
          out << "Failed to locate a connector for the provided parameters\n"
      }
  }

  public static enum ApplicationAction {start, stop, dispose}

  @Usage("control an application")
  @Command
  void app(@Usage("The application name") @Required @Option(names=["a"], completer=mule.class) String applicationName,
           @Usage("The application action to run") @Required @Argument ApplicationAction applicationAction) {

      try {
          def contextMBean = new GroovyMBean(mbeanServer, "Mule.$applicationName:name=MuleContext")
          try {
              contextMBean.invokeMethod(applicationAction.toString(), [] as Object[])
              out << "Action $applicationAction successfully run. Application intialized: $contextMBean.Initialised, stopped: $contextMBean.Stopped\n"
          } catch (Exception e) {
              out << "Failed to $applicationAction the application ($e.message)\n"
          }
      }
      catch (Exception e) {
          out << "Failed to locate an application for the provided parameters\n"
      }
  }

  public static enum BrokerAction {
      restart('restart'), stop('stop'), thread_dump('requestThreadDump')
      private final String value
      BrokerAction(String value) { this.value = value }
      public String value() { return value }
  }

  @Usage("control the broker")
  @Command
  void broker(@Usage("The broker action to run") @Required @Argument BrokerAction brokerAction,
              @Usage("The exit code for the stop action (default is 0)") @Option(names=["x"]) Integer exitCode) {
      try {
          if (brokerAction == BrokerAction.stop) {
              getMuleWrapperManagerMBean().invokeMethod(brokerAction.value(), [exitCode == null?0:exitCode] as Object[])
          } else {
              getMuleWrapperManagerMBean().invokeMethod(brokerAction.value(), [] as Object[])
          }
          out << "Action $brokerAction successfully run.\n"
      } catch (Exception e) {
          out << "Failed to $brokerAction the broker ($e.message)\n"
      }
  }

  private GroovyMBean getMuleWrapperManagerMBean() {
      if (mbeanServer.isRegistered(CE_WRAPPER_MANAGER_ON)) {
          new GroovyMBean(mbeanServer, CE_WRAPPER_MANAGER_ON)
      } else {
          new GroovyMBean(mbeanServer, EE_WRAPPER_MANAGER_ON)
      }
  }

  private void listMBeans(String applicationName, String type, Closure extractor) {
      getObjectNames(applicationName, type).sort().each { objectName ->
          def mBean = new GroovyMBean(mbeanServer, objectName)
          context.provide(extractor(mBean))
      }
  }

  private Collection<ObjectName> getObjectNames(String applicationName, String type) {
      def mBeanPattern = new ObjectName("Mule.$applicationName:type=$type,*")
      mbeanServer.queryNames(mBeanPattern, null)
  }

  Completion complete(ParameterDescriptor parameter, String prefix) {
      if (parameter instanceof OptionDescriptor && parameter.names.contains("a")) {
          def completionBuilder = Completion.builder(prefix)
          getApplicationNames().findAll { appName ->
              appName.startsWith(prefix)
          }.each { appName ->
              completionBuilder.add(appName.substring(prefix.length()), true)
          }
          completionBuilder.build()
      } else {
          Completion.create(prefix);
      }
  }

  private List<String> getApplicationNames() {
      mbeanServer.domains.findAll { domain ->
          isMuleApplicationDomain(domain)
      }.collect { domain ->
          domain.substring(5)
      }.sort()
  }

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
      for (MBeanServer mbeanServer : MBeanServerFactory.findMBeanServer(null)) {
          if ((mbeanServer.isRegistered(CE_WRAPPER_MANAGER_ON)) || (mbeanServer.isRegistered(EE_WRAPPER_MANAGER_ON))) {
              return mbeanServer
          }
      }
      throw new ScriptException("No Mule wrapper manager located");
  }
}
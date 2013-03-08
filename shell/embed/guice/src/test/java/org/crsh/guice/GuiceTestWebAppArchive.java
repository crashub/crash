package org.crsh.guice;

import org.crsh.telnet.TelnetPlugin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;

import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceTestWebAppArchive {

	public static WebArchive buildInstance() {
		WebArchive archive = ShrinkWrap.create(WebArchive.class)
				.addClass(GuiceApplication.class)
				.addClass(SampleService.class)
				.addClass(SampleServlet.class)
				.addClass(CrashGuiceSupport.class)
				.addClass(CrashGuiceSupport.InjectorHolder.class)
				.addClass(GuiceServletContextListener.class)
				.addClass(TelnetPlugin.class)
				.addAsLibraries(Maven.resolver().addDependencies(
						MavenDependencies.createDependency("org.crsh:crsh.shell.core:1.2.0-cr7-SNAPSHOT", ScopeType.COMPILE, false))
						.resolve().withTransitivity().asFile())
				.addAsDirectories("/crash/commands")
				.setWebXML(GuiceTestWebAppArchive.class.getResource("web.xml"));
		return archive;
	}
	
}

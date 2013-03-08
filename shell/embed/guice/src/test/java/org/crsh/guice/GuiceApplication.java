package org.crsh.guice;

import java.util.Random;

import org.crsh.plugin.PropertyDescriptor;

import com.google.common.collect.ImmutableMap;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

public class GuiceApplication extends ServletModule {
	
	@Override
	protected void configureServlets() {
		super.configureServlets();
		int telnetPortValue = new Random().nextInt(10000) + 1025;
		bind(Integer.class).annotatedWith(Names.named("telnet.port")).toInstance(telnetPortValue);
		PropertyDescriptor telnetPort = PropertyDescriptor.create("telnet.port", telnetPortValue, "whatever");
		install(new CrashGuiceSupport(ImmutableMap.<PropertyDescriptor<Object>, Object>of(telnetPort, telnetPortValue)));
		serve("/").with(SampleServlet.class);
	}
	
}

package org.crsh.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceServletContextListener extends com.google.inject.servlet.GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new GuiceApplication());
	}
	
}

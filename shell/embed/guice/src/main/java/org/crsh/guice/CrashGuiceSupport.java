package org.crsh.guice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginDiscovery;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class CrashGuiceSupport extends AbstractModule {

	public static class InjectorHolder extends PluginLifeCycle {

		private final Injector injector;
		private ClassLoader loader;
		private final Map<PropertyDescriptor<Object>, Object> configuration;

		@Inject
		public InjectorHolder(Injector injector, @Named("crashConfiguration") Map<PropertyDescriptor<Object>, Object> configuration) throws IOException, URISyntaxException {
			this.injector = injector;
			this.configuration = configuration;
			this.loader = getClass().getClassLoader();
			PluginDiscovery discovery = new ServiceLoaderDiscovery(loader);
			FS cmdFS = createCommandFS();
			FS confFS = createConfFS();

			PluginContext context = new PluginContext(
					discovery,
					buildGuiceMap(),
					cmdFS,
					confFS,
					loader);

			for (Map.Entry<PropertyDescriptor<Object>, Object> property: configuration.entrySet()) {
				context.setProperty(property.getKey(), property.getValue());
			}
			
			context.refresh();
			start(context);
		}

		private Map<String, Object> buildGuiceMap() {
			return ImmutableMap.of(
					"factory", injector,
					"properties", configuration,
					"beans", new GuiceMap(injector)
					);
		}

		protected FS createCommandFS() throws IOException, URISyntaxException {
			FS cmdFS = new FS();
			cmdFS.mount(loader, Path.get("/crash/commands/"));
			return cmdFS;
		}

		protected FS createConfFS() throws IOException, URISyntaxException {
			FS confFS = new FS();
			confFS.mount(loader, Path.get("/crash/"));
			return confFS;
		}
		
		public void destroy() throws Exception {
			stop();
		}
	}

	private final Map<PropertyDescriptor<Object>, Object> configuration;
	
	public CrashGuiceSupport() {
		this(ImmutableMap.<PropertyDescriptor<Object>, Object>of());
	}
	
	public CrashGuiceSupport(Map<PropertyDescriptor<Object>, Object> configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void configure() {
		bind(new TypeLiteral<Map<PropertyDescriptor<Object>, Object>>(){}).annotatedWith(Names.named("crashConfiguration")).toInstance(configuration);
		bind(InjectorHolder.class).asEagerSingleton();
	}

}

package org.crsh.guice;

import java.util.Iterator;

import org.crsh.util.SimpleMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.spi.DefaultBindingScopingVisitor;

class GuiceMap extends SimpleMap<String, Object> {

	private final Injector injector;

	GuiceMap(Injector injector) {
		this.injector = injector;
	}

	@Override
	protected Iterator<String> keys() {
		Builder<String> types = ImmutableList.<String>builder();
		for (Entry<Key<?>, Binding<?>> entry: injector.getAllBindings().entrySet()) {
			if (isSingleton(entry)) {
				types.add(entry.getKey().getTypeLiteral().toString());
			}
		}
		return types.build().iterator();
	}

	private boolean isSingleton(Entry<Key<?>, Binding<?>> entry) {
		return Boolean.TRUE.equals(entry.getValue().acceptScopingVisitor(new DefaultBindingScopingVisitor<Boolean>() {
			@Override
			public Boolean visitScope(Scope scope) {
				return scope == Scopes.SINGLETON;
			}
		}));
	}

	@Override
	public Object get(Object key)  {
		if (key instanceof String) {
			String className = (String) key;
			try {
				Class<?> clazz = Class.forName(className);
				if (clazz != null) {
					return injector.getInstance(clazz);
				}
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return null;
	}
}

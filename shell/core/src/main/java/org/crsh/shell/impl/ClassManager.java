package org.crsh.shell.impl;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.command.CommandInvoker;
import org.crsh.command.GroovyScriptCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.ErrorType;
import org.crsh.util.TimestampedObject;
import org.crsh.vfs.Resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ClassManager<T> {

  /** . */
  private final Map<String, TimestampedObject<Class<? extends T>>> classes = new ConcurrentHashMap<String, TimestampedObject<Class<? extends T>>>();

  /** . */
  private final PluginContext context;

  /** . */
  private final Class<? extends Script> baseScriptClass;

  /** . */
  private final CompilerConfiguration config;

  /** . */
  private final Class<T> baseClass;

  /** . */
  private final ResourceKind kind;

  ClassManager(PluginContext context, ResourceKind kind, Class<T> baseClass, Class<? extends Script> baseScriptClass) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setRecompileGroovySource(true);
    config.setScriptBaseClass(GroovyScriptCommand.class.getName());

    //
    this.context = context;
    this.baseScriptClass = baseScriptClass;
    this.config = config;
    this.baseClass = baseClass;
    this.kind = kind;
  }

  Class<? extends T> getClass(String name) throws CreateCommandException, NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null argument allowed");
    }

    TimestampedObject<Class<? extends T>> providerRef = classes.get(name);

    //
    Resource script = context.loadResource(name, kind);

    //
    if (script != null) {
      if (providerRef != null) {
        if (script.getTimestamp() != providerRef.getTimestamp()) {
          providerRef = null;
        }
      }

      //
      if (providerRef == null) {

        Class<?> clazz;
        try {
          GroovyCodeSource gcs = new GroovyCodeSource(script.getContent(), name, "/groovy/shell");
          GroovyClassLoader gcl = new GroovyClassLoader(context.getLoader(), config);
          clazz = gcl.parseClass(gcs, false);
        }
        catch (CompilationFailedException e) {
          throw new CreateCommandException(ErrorType.INTERNAL, "Could not compile command script " + name, e);
        }

        //
        if (baseClass.isAssignableFrom(clazz)) {
          Class<? extends T> providerClass = clazz.asSubclass(baseClass);
          providerRef = new TimestampedObject<Class<? extends T>>(script.getTimestamp(), providerClass);
          classes.put(name, providerRef);
        } else {
          throw new CreateCommandException(ErrorType.INTERNAL, "Parsed script " + clazz.getName() +
            " does not implements " + CommandInvoker.class.getName());
        }
      }
    }

    //
    if (providerRef == null) {
      return null;
    }

    //
    return providerRef.getObject();
  }

  T getInstance(String name) throws CreateCommandException, NullPointerException {
    Class<? extends T> clazz = getClass(name);
    if (clazz == null) {
      return null;
    }

    //
    try {
      return clazz.newInstance();
    }
    catch (Exception e) {
      throw new CreateCommandException(ErrorType.INTERNAL, "Could not create command " + name + " instance", e);
    }
  }
}

package org.crsh.util;

import org.crsh.cli.completers.AbstractPathCompleter;
import org.crsh.text.renderers.BindingRenderer;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import java.util.*;
import java.util.regex.Pattern;

/** @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
public class JNDIHandler {

  public static List<BindingRenderer.BindingData> lookup(List<String> filters, String name, Boolean verbose) {

    Pattern pattern = null;
    if (name != null) {
      pattern = Pattern.compile("^" + Utils.globexToRegex(name) + "$");
    }

    List<BindingRenderer.BindingData> data = new ArrayList<BindingRenderer.BindingData>();

    data.addAll(get(filters, pattern, verbose, ""));
    data.addAll(get(filters, pattern, verbose, "java:/"));
    data.addAll(get(filters, pattern, verbose, "java:comp/env/jdbc"));
    data.addAll(get(filters, pattern, verbose, "java:jboss"));
    data.addAll(get(filters, pattern, verbose, "java:global"));
    data.addAll(get(filters, pattern, verbose, "java:app"));
    data.addAll(get(filters, pattern, verbose, "java:module"));

    return data;

  }

  static List<BindingRenderer.BindingData> get(
      List<String> filters,
      Pattern pattern,
      Boolean verbose,
      String path) {

    return get(filters, pattern, verbose, path, path, null);
  }

  static List<BindingRenderer.BindingData> get(
      List<String> filters,
      Pattern pattern,
      Boolean verbose,
      String path,
      String search,
      Context ctx) {

    List<BindingRenderer.BindingData> data = new ArrayList<BindingRenderer.BindingData>();

    try {
      if (ctx == null) {
        ctx = new InitialContext();
      }
      if (path.length() > 0) {
        path += "/";
      }

      NamingEnumeration<Binding> e = ctx.listBindings(search);
      while (e.hasMoreElements()) {
        Binding instance = e.next();

        String fullName = path + instance.getName();

        if (
            filters == null ||
                filters.size() == 0 ||
                Utils.instanceOf(instance.getObject().getClass(), filters)) {
          if (pattern == null || pattern.matcher(fullName).find()) {
            data.add(new BindingRenderer.BindingData(fullName, instance.getClassName(), instance, verbose));
          }
        }
        if (instance.getObject() instanceof Context) {
          data.addAll(get(filters, pattern, verbose, fullName, "", (Context) instance.getObject()));
        }

      }

    }
    catch (Exception e) {
    }

    return data;
  }

  public static class JNDICompleter extends AbstractPathCompleter<String> {

    private final String[] filters;
    private List<BindingRenderer.BindingData> bindings;

    public JNDICompleter(String... filters) {
      this.filters = filters;
      bindings = JNDIHandler.lookup(Arrays.asList(filters), null, true);
    }

    @Override
    protected String getCurrentPath() {
      return "";
    }

    @Override
    protected String getPath(String path) {
      return path;
    }

    @Override
    protected boolean exists(String path) {
      if (path.equals("/") || path.endsWith("/")) {
        return true;
      } else {
        for (BindingRenderer.BindingData binding : bindings) {
          if (binding.name.startsWith(path.substring(1) + "/")) {
            return true;
          }
        }
      }
      return false;
    }

    @Override
    protected boolean isDirectory(String path) {
      if (path.equals("/")) {
        return true;
      }
      if (path.startsWith("/")) {
        path = path.substring(1);
      }
      for (BindingRenderer.BindingData binding : bindings) {
        if (binding.name.startsWith(path + "/")) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected boolean isFile(String path) {
      if (path.equals("/")) {
        return false;
      }
      if (path.startsWith("/")) {
        path = path.substring(1);
      }
      if (path.endsWith("/")) {
        for (BindingRenderer.BindingData binding : bindings) {
          if (binding.name.equals(path.substring(0, path.length() - 1))) {
            return true;
          }
        }
      }
      return false;
    }

    @Override
    protected Collection<String> getChilren(String path) {
      List<String> l = new ArrayList<String>();
      for (BindingRenderer.BindingData binding : bindings) {
        if (path.equals("/") || binding.name.startsWith(path.substring(1))) {
          String completion = binding.name.substring(path.substring(1).length());
          if (completion.startsWith("/")) {
            completion = completion.substring(1);
          }
          l.add(completion);
        }
      }
      return l;
    }

    @Override
    protected String getName(String path) {
      return path;
    }
  }

}

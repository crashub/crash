package org.crsh.util;

import org.crsh.text.formatter.BindingRenderable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class JNDIHandler {

  public static List<BindingRenderable.BindingData> lookup(List<String> filters, String name, Boolean verbose) {

        Pattern pattern = null;
        if (name != null) {
            name = Utils.applyRegex(name);
            pattern = Pattern.compile(name);
        }

        List<BindingRenderable.BindingData> data = new ArrayList<BindingRenderable.BindingData>();

        data.addAll(get(filters, pattern, verbose, ""));
        data.addAll(get(filters, pattern, verbose, "java:/"));
        data.addAll(get(filters, pattern, verbose, "java:comp/env/jdbc"));
        data.addAll(get(filters, pattern, verbose, "java:jboss"));
        data.addAll(get(filters, pattern, verbose, "java:global"));
        data.addAll(get(filters, pattern, verbose, "java:app"));
        data.addAll(get(filters, pattern, verbose, "java:module"));

        return data;

    }

    static List<BindingRenderable.BindingData> get(
            List<String> filters,
            Pattern pattern,
            Boolean verbose,
            String path) {

        return get(filters, pattern, verbose, path, path, null);
    }

    static List<BindingRenderable.BindingData> get(
            List<String> filters,
            Pattern pattern,
            Boolean verbose,
            String path,
            String search,
            Context ctx) {

        List<BindingRenderable.BindingData> data = new ArrayList<BindingRenderable.BindingData>();

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
                    TypeResolver.instanceOf(instance.getObject().getClass(), filters)) {
                    if (pattern == null || pattern.matcher(fullName).find()) {
                        data.add(new BindingRenderable.BindingData(fullName, instance.getClassName(), verbose));
                    }
                }
                if (instance.getObject() instanceof Context) {
                    data.addAll(get(filters, pattern, verbose, fullName, "", (Context) instance.getObject()));
                }

          }

        } catch(Exception e) {}

        return data;
    }

}

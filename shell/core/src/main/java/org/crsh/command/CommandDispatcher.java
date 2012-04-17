package org.crsh.command;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Tuple;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.crsh.cmdline.Delimiter;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
final class CommandDispatcher extends Closure {

  /** . */
  final InvocationContext ic;

  /** . */
  final ShellCommand command;

  CommandDispatcher(ShellCommand command, InvocationContext ic) {
    super(new Object());

    //
    this.command = command;
    this.ic = ic;
  }

  @Override
  public Object getProperty(String property) {
    try {
      return super.getProperty(property);
    }
    catch (MissingPropertyException e) {
      return new CommandClosure(this, property);
    }
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {
      return dispatch(name, args);
    }
  }

  /**
   * Closure invocation.
   *
   * @param arguments the closure arguments
   */
  public Object call(Object[] arguments) {
    return dispatch("", arguments);
  }

  Object dispatch(String methodName, Object arguments) {
    if (arguments == null) {
      return dispatch(methodName, MetaClassHelper.EMPTY_ARRAY);
    } else if (arguments instanceof Tuple) {
      Tuple tuple = (Tuple) arguments;
      return dispatch(methodName, tuple.toArray());
    } else if (arguments instanceof Object[]) {
      return dispatch(methodName, (Object[])arguments);
    } else {
      return dispatch(methodName, new Object[]{arguments});
    }
  }

  Object dispatch(String name, Object[] args) {
    StringBuilder line = new StringBuilder();
    if (name.length() > 0) {
      line.append(name).append(" ");
    }

    //
    Closure closure;
    int to = args.length;
    if (to > 0 && args[to - 1] instanceof Closure) {
      closure = (Closure)args[--to];
    } else {
      closure = null;
    }

    //
    if (to > 0) {
      Object first = args[0];
      int from;
      try {
        if (first instanceof Map<?, ?>) {
          from = 1;
          Map<?, ?> options = (Map<?, ?>)first;
          for (Map.Entry<?, ?> option : options.entrySet()) {
            String optionName = option.getKey().toString();
            Object optionValue = option.getValue();

            boolean printName;
            boolean printValue;
            if (optionValue instanceof Boolean) {
              printName = Boolean.TRUE.equals(optionValue);
              printValue = false;
            } else {
              printName = true;
              printValue = true;
            }

            //
            if (printName) {
              line.append(" ");
              line.append(optionName.length() == 1 ? "-" : "--");
              line.append(optionName);
              if (printValue) {
                line.append(" ");
                line.append("\"");
                Delimiter.DOUBLE_QUOTE.escape(optionValue.toString(), line);
                line.append("\"");
              }
            }
          }
        } else {
          from = 0;
        }
        while (from < to) {
          Object o = args[from++];
          line.append(" ");
          line.append("\"");
          Delimiter.DOUBLE_QUOTE.escape(o.toString(), line);
          line.append("\"");
        }
      }
      catch (IOException e) {
        throw new AssertionError(e);
      }
    }

    //
    try {
      CommandInvoker<Void, Void> invoker = (CommandInvoker<Void, Void>)command.createInvoker(line.toString());
      Class producedType = invoker.getProducedType();
      InnerInvocationContext inc = new InnerInvocationContext(ic, producedType);
      invoker.invoke(inc);

      //
      if (closure != null) {
        for (Object o : inc.products) {
          closure.call(o);
        }
      }

      //
      return null;
    }
    catch (ScriptException e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        throw new InvokerInvocationException(cause);
      } else {
        throw e;
      }
    }
  }
}

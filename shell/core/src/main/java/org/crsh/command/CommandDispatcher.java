package org.crsh.command;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.Tuple;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.crsh.cmdline.Delimiter;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
final class CommandDispatcher extends GroovyObjectSupport {

  /** . */
  final InnerInvocationContext ic;

  /** . */
  final ShellCommand command;

  CommandDispatcher(ShellCommand command, InnerInvocationContext ic) {
    this.command = command;
    this.ic = ic;
  }

  @Override
  public Object invokeMethod(String methodName, Object arguments) {
    if (arguments == null) {
      return invokeMethod(methodName, MetaClassHelper.EMPTY_ARRAY);
    } else if (arguments instanceof Tuple) {
      Tuple tuple = (Tuple) arguments;
      return invokeMethod(methodName, tuple.toArray());
    } else if (arguments instanceof Object[]) {
      return invokeMethod(methodName, (Object[]) arguments);
    } else {
      return invokeMethod(methodName, new Object[]{arguments});
    }
  }

  public Object invokeMethod(String methodName, Object[] arguments) {
    StringBuilder line = new StringBuilder();
    if (methodName.length() > 0) {
      line.append(methodName).append(" ");
    }
    if (arguments.length > 0) {
      Object first = arguments[0];
      int index;
      try {
        if (first instanceof Map<?, ?>) {
          index = 1;
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
          index = 0;
        }
        while (index < arguments.length) {
          Object o = arguments[index++];
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
      invoker.invoke(ic);
    }
    catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      } else {
        // ?
        throw new InvokerInvocationException(e);
      }
    }

    //
    return null;
  }
}

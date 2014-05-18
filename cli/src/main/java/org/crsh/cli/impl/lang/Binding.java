package org.crsh.cli.impl.lang;

import org.crsh.cli.impl.invocation.InvocationException;

/**
 * @author Julien Viet
 */
interface Binding {

  void set(Object o, Object[] args, Object value) throws InvocationException;

}

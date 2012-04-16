package org.crsh.command;

import groovy.lang.Closure;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InvokeCommandClosure extends Closure {

  /** . */
  private final ShellCommand command;

  public InvokeCommandClosure(Object owner, ShellCommand command) {
    super(owner);

    //
    this.command = command;
  }

  @Override
  public Object call(Object[] args) {
    throw new UnsupportedOperationException();
  }
}

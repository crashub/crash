package org.crsh.command;

import groovy.lang.Closure;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class CommandClosure extends Closure {


  /** . */
  private final CommandDispatcher dispatcher;

  /** . */
  private final String name;

  CommandClosure(CommandDispatcher dispatcher, String name) {
    super(dispatcher);

    //
    this.dispatcher = dispatcher;
    this.name = name;
  }

  @Override
  public Object call(Object[] args) {
    return dispatcher.dispatch(name, args);
  }
}




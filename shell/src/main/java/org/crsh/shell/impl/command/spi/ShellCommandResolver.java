package org.crsh.shell.impl.command.spi;

/**
 * @author Julien Viet
 */
public interface ShellCommandResolver {

  Iterable<String> getCommandNames();

  /**
   * Attempt to obtain a command instance. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command instance
   * @throws org.crsh.shell.impl.command.spi.CommandCreationException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  CommandResolution resolveCommand(final String name) throws CommandCreationException, NullPointerException;
}

package org.crsh.shell.impl.command;

import java.util.HashMap;
import java.util.Map;

import org.crsh.cli.descriptor.Format;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.command.BaseCommand;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.lang.impl.java.ClassShellCommand;
import org.crsh.lang.spi.CommandResolution;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandResolver;


public class ExternalResolver implements CommandResolver
{
	public static final ExternalResolver INSTANCE = new ExternalResolver();
	private final HashMap<String, Class<? extends BaseCommand>> commands;
	private final HashMap<String, String> descriptions;

	private ExternalResolver()
	{
		commands = new HashMap<String, Class<? extends BaseCommand>>();
		descriptions = new HashMap<String, String>();
	}

	public void addCommand(String command, String description, Class<? extends BaseCommand> clazz)
	{
		commands.put(command, clazz);
		descriptions.put(command, description);
	}

	@Override
	public Iterable<Map.Entry<String, String>> getDescriptions()
	{
		return descriptions.entrySet();
	}

	@Override
	public Command<?> resolveCommand(String name, ShellSafety shellSafety) throws CommandException, NullPointerException
	{
		final Class<? extends BaseCommand> systemCommand = commands.get(name);
		if (systemCommand != null)
		{
			return createCommand(systemCommand).getCommand();
		}
		return null;
	}

	private <C extends BaseCommand> CommandResolution createCommand(final Class<C> commandClass) throws CommandException
	{
		final ClassShellCommand<C> shellCommand;
		final String description;
		try
		{
			shellCommand = new ClassShellCommand<C>(commandClass, ShellSafetyFactory.getCurrentThreadShellSafety());
			description = shellCommand.describe(commandClass.getSimpleName(), Format.DESCRIBE);
		}
		catch (IntrospectionException e)
		{
			throw new CommandException(ErrorKind.SYNTAX, "Invalid cli annotation in command " + commandClass.getSimpleName(), e);
		}
		return new CommandResolution()
		{
			@Override
			public String getDescription()
			{
				return description;
			}

			@Override
			public Command<?> getCommand() throws CommandException
			{
				return shellCommand;
			}
		};
	}
}

package me.cursedblackcat.dajibot2;

/**
 * Represents a bot command that was received in chat, with a command name and potential arguments.
 * @author Darren Yip
 */
public class ExecutedCommand {
	private String commandName;
	private String[] arguments;
	
	public ExecutedCommand(String c, String[] a) {
		commandName = c;
		arguments = a;
	}
	
	public String getCommand() {
		return commandName;
	}
	
	public String[] getArguments() {
		return arguments;
	}
}

package me.cursedblackcat.dajibot2;

/**
 * Represents a bot command, with a command and potential arguments.
 * @author Darren Yip
 *
 */
public class Command {
	private String command;
	private String[] arguments;
	
	public Command(String c, String[] a) {
		command = c;
		arguments = a;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String[] getArguments() {
		return arguments;
	}
}

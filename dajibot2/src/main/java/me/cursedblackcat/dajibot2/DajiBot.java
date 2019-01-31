package me.cursedblackcat.dajibot2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Messageable;

/**
 * The main class of the program.
 * @author Darren Yip
 *
 */
public class DajiBot {
	private	static String prefix = "$";

	private static DiscordApi api;
	private static final Logger logger = LogManager.getLogger(DajiBot.class);

	public static Command parseCommand(String message) {
		String[] parts = message.split("\\s+");
		String command = parts[0].substring(1);
		String[] arguments = ArrayUtils.remove(parts, 0);
		
		return new Command(command, arguments);
	}
	
	public static void handleCommand(Command c, Messageable channel) {
		String command = c.getCommand().toLowerCase();
		
		switch (command) { //TODO implement command actions
		case "help":
			channel.sendMessage("Command: `" + command + "`\nArguments: " + Arrays.toString(c.getArguments()));
			break;
		case "diamondseal":
			channel.sendMessage("Command: `" + command + "`\nArguments: " + Arrays.toString(c.getArguments()));
			break;
		}
	}

	public static void main(String[] args) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader("token.txt"));
		String token = bufferedReader.readLine();
		bufferedReader.close();

		api = new DiscordApiBuilder().setToken(token).login().join();

		api.addMessageCreateListener(event -> {
			if(event.getMessageContent().startsWith(prefix)) {
				handleCommand(parseCommand(event.getMessageContent()), event.getChannel());
			}
		});
	}
}

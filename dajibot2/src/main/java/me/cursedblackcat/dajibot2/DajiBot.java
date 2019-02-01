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
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

/**
 * The main class of the program.
 * @author Darren Yip
 *
 */
public class DajiBot {
	private static String helpText = "DajiBot v2 - a rewrite of the original DajiBot by CursedBlackCat#7801 \n" + 
			"\n" + 
			"**These are the commands I recognize:**\n" + 
			"```\n" +
			"~~~General commands~~~\n" + 
			"help - Shows this menu\n\n" + 
			"diamondseal - Everything to do with the diamond seal simulator\n\n" +
			"~~~Admin commands~~~\n" + 
			"changeprefix - Change the bot's command prefix. Can only be run by people with admin permissions.\n\n" + 
			"createseal - Create a new diamond seal machine. Can only be run by people with admin permissions.\n\n" + 
			"deleteseal - Delete a diamond seal machine. Can only be run by people with admin permissions.\n\n" + 
			"```";
	
	private	static String prefix = "$";

	private static DiscordApi api;

	private static final long[] privilegedRoleIDs =
		{
				541008546280505344L
		};

	private static final long ownerID = 226767560211693568L;

	public static ExecutedCommand parseCommand(String message) {
		String[] parts = message.split("\\s+");
		String command = parts[0].substring(1);
		String[] arguments = ArrayUtils.remove(parts, 0);

		return new ExecutedCommand(command, arguments);
	}

	/**
	 * Perform actions based on a received command.
	 * @param c The command that was received.
	 * @param channel The channel in which the command was sent
	 * @param privileged Whether or not the command should be executed with admin permissions on the bot.
	 * @param user The user executing the command.
	 */
	public static void handleCommand(ExecutedCommand c, Messageable channel, boolean privileged, User user) {
		String command = c.getCommand().toLowerCase();

		switch (command) { //TODO implement command actions
		case "help":
			if (c.getArguments().length == 0) {
				channel.sendMessage(user.getMentionTag() + "\n" + helpText);
			} else {
				switch (c.getArguments()[0]) {
				case "diamondseal":
					//TODO
					break;
				default:
					channel.sendMessage(user.getMentionTag() + " there is no such command `" + c.getArguments()[0] + "`");
				}
			}
			break;
		case "diamondseal":
			//TODO
			channel.sendMessage("Command: `" + command + "`\nArguments: " + Arrays.toString(c.getArguments()));
			break;
		case "changeprefix":
			if (privileged) {
				prefix = c.getArguments()[0];
				channel.sendMessage(user.getMentionTag() + " Prefix has been set to " + c.getArguments()[0]);
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "createseal":
			if (privileged) {
				//TODO create a seal
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "deleteseal":
			if (privileged) {
				//TODO delete a seal
				channel.sendMessage(user.getMentionTag() + " Seal machine " + c.getArguments()[0] + " has been deleted.");
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		}
	}

	/**
	 * Checks if the user has admin privileges on the bot.
	 * @return
	 */
	private static boolean isPrivileged(User user, Server server) {
		if (user.getId() == ownerID) {
			return true;
		}

		for (Role role : user.getRoles(server)){
			for (long id : privilegedRoleIDs) {
				if (role.getId() == id) {
					return true;
				}
			}
		}

		return false;
	}

	public static void main(String[] args) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader("token.txt"));
		String token = bufferedReader.readLine();
		bufferedReader.close();

		api = new DiscordApiBuilder().setToken(token).login().join();

		api.addMessageCreateListener(event -> {
			if(event.getMessageContent().startsWith(prefix)) {
				try {
					User author = event.getMessageAuthor().asUser().get();
					Server server = event.getServer().get();
					if (isPrivileged(author, server)) {
						handleCommand(parseCommand(event.getMessageContent()), event.getChannel(), true, author);
					} else {
						handleCommand(parseCommand(event.getMessageContent()), event.getChannel(), false, author);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}

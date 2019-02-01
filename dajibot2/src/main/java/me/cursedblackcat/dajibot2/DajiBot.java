package me.cursedblackcat.dajibot2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.event.ListenerManager;

import me.cursedblackcat.dajibot2.diamondseal.Card;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSeal;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSealBuilder;

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
			"diamondseal - Pull a card from the diamond seal simulator\n\n" +
			"listseals - List all diamond seal banners in the diamond seal simulator\n\n" +
			"~~~Admin commands~~~\n" + 
			"changeprefix - Change the bot's command prefix. Can only be run by people with admin permissions.\n\n" + 
			"createseal - Create a new diamond seal machine. Can only be run by people with admin permissions.\n\n" + 
			"deleteseal - Delete a diamond seal machine. Can only be run by people with admin permissions.\n\n" + 
			"```";
	
	private	static String prefix = "$";

	private static DiscordApi api;
	static ListenerManager<MessageCreateListener> listenerManager = null;

	private static final long[] privilegedRoleIDs =
		{
				541008546280505344L
		};

	private static final long ownerID = 226767560211693568L;
	
	private static ArrayList<DiamondSeal> diamondSeals = new ArrayList<DiamondSeal>();

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
			DiamondSeal seal = getDiamondSealByCommandName(c.getArguments()[0]);
			try {
				channel.sendMessage(user.getMentionTag() + " You pulled `" + seal.drawFromMachine().getName() + "` from " + seal.getName() +  "!");
				
			} catch (NullPointerException e) {
				channel.sendMessage(user.getMentionTag() + " No such diamond seal `" + c.getArguments()[0] + "`");
			}
			break;
		case "listseals":
			String response = "";
			for (DiamondSeal s : diamondSeals) {
				response += s.getCommandName();
				response += ", ";
			}
			response = response.substring(0, response.length() - 2);
			channel.sendMessage(user.getMentionTag() + ", here are all the diamond seal banners available:\n\n" + response);

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
				//TODO put the seal in database for persistence
				DiamondSealBuilder builder = new DiamondSealBuilder();
				//Get seal name
				channel.sendMessage(user.getMentionTag() + " What should the seal be called?");
				listenerManager = api.addMessageCreateListener(event -> {
					if(event.getMessageAuthor().asUser().get() == user) {
						builder.withName(event.getMessageContent());
						listenerManager.remove();
						channel.sendMessage(user.getMentionTag() + " What seal name should users type in a command to pull from this seal?");
						listenerManager = api.addMessageCreateListener(event1 -> {
							if(event1.getMessageAuthor().asUser().get() == user) {
								builder.withCommandName(event1.getMessageContent());
								listenerManager.remove();
								channel.sendMessage(user.getMentionTag() + " Please enter a comma-separated list of cards in the seal.");
								listenerManager = api.addMessageCreateListener(event2 -> {
									if(event2.getMessageAuthor().asUser().get() == user) {
										String[] cardNames = event2.getMessageContent().split("\\s*,\\s*");
										for (String name : cardNames) {
											builder.withCard(new Card(name));
										}
										listenerManager.remove();
										channel.sendMessage(user.getMentionTag() + " Please enter a comma-separated list of each card's percent rate in the seal, multiplied by 10, in the same order that you entered the cards above.");
										listenerManager = api.addMessageCreateListener(event3 -> {
											if(event3.getMessageAuthor().asUser().get() == user) {
												String[] cardRates = event3.getMessageContent().split("\\s*,\\s*");
												for (String rate : cardRates) {
													builder.withRate(Integer.parseInt(rate));
												}
												listenerManager.remove();
												try {
													DiamondSeal newSeal = builder.build();
													diamondSeals.add(newSeal);
													channel.sendMessage(user.getMentionTag() + " Seal created! Pull from your new seal by running `diamondseal " + event1.getMessageContent() + "`");
												} catch (IllegalStateException e) {
													channel.sendMessage(user.getMentionTag() + " Invalid entry. Amount of cards and amount of rates should be equal, and all rates should sum up to 1000, or 100%.");
												}
											}
										});
									}
								});
							}
						});
					}
				});
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "deleteseal":
			if (privileged) {
				diamondSeals.remove(getDiamondSealByCommandName(c.getArguments()[0]));
				channel.sendMessage(user.getMentionTag() + " Seal machine " + c.getArguments()[0] + " has been deleted.");
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		}
	}
	
	private static DiamondSeal getDiamondSealByCommandName(String name) {
		for (DiamondSeal seal : diamondSeals) {
			if (seal.getCommandName().equals(name)) {
				return seal;
			}
		}
		return null;
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

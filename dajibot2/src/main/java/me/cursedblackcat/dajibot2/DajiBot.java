package me.cursedblackcat.dajibot2;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.ArrayUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.event.ListenerManager;

import me.cursedblackcat.dajibot2.diamondseal.DiamondSealCard;
import me.cursedblackcat.dajibot2.accounts.Account;
import me.cursedblackcat.dajibot2.accounts.AccountDatabaseHandler;
import me.cursedblackcat.dajibot2.accounts.InsufficientCurrencyException;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSeal;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSealBuilder;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSealDatabaseHandler;
import me.cursedblackcat.dajibot2.rewards.ItemType;
import me.cursedblackcat.dajibot2.rewards.Reward;
import me.cursedblackcat.dajibot2.rewards.RewardsDatabaseHandler;

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
			"~~~Diamond seal simulator commands~~~\n" + 
			"diamondseal <sealname> - Use 5 diamonds from your account to pull a card from the diamond seal simulator.\n\n" +
			"simulateseal <sealname> - Simulates pulling a card from the diamond seal simulator. Does not cost you diamonds, but doesn't give you the card.\n\n" +
			"listseals - List all diamond seal banners in the diamond seal simulator\n\n" +
			"sealinfo <sealname> - Check available cards/series and their rates in a seal banner.\n\n" +
			"~~~Account commands~~~\n" + 
			"register - Register your Discord account with DajiBot.\n\n" +
			"rewards - List all of your unclaimed rewards.\n\n" +
			"collect <n> - Collect reward number <n> in your rewards inbox." +
			"accountinfo - View your account info.\n\n" +
			"inventory <pagenumber> - View your card inventory.\n\n" +
			"~~~Admin commands~~~\n" + 
			"changeprefix - Change the bot's command prefix. Can only be run by people with admin permissions.\n\n" + 
			"createseal - Create a new diamond seal machine. Can only be run by people with admin permissions.\n\n" + 
			"deleteseal - Delete a diamond seal machine. Can only be run by people with admin permissions.\n\n" + 
			"~~~Bot owner commands~~~\n" + 
			"addcurrency <@user> <type> <amount> - Add currency to a user. Can only be run by the bot owner.\n\n" + 
			"```";

	private	static String prefix = "$";

	private static DiscordApi api;
	static ListenerManager<MessageCreateListener> listenerManager = null;

	private static final long[] privilegedRoleIDs =
		{
				541008546280505344L,
				294141628057124864L,
				294141638958120970L,
				304076853977677825L
		};

	private static final long ownerID = 226767560211693568L;

	private static ArrayList<DiamondSeal> diamondSeals = new ArrayList<DiamondSeal>();

	private static DiamondSealDatabaseHandler sealDBHandler;
	private static AccountDatabaseHandler accountDBHandler;
	private static RewardsDatabaseHandler rewardsDBHandler;

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

		switch (command) {
		case "help":
			channel.sendMessage(user.getMentionTag() + "\n" + helpText);
			break;
		case "diamondseal":
			if (!accountDBHandler.userAlreadyExists(user)) {
				channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
				return;
			}

			try {
				accountDBHandler.deductCurrencyFromAccount(user, ItemType.DIAMOND, 5);
				DiamondSeal seal = getDiamondSealByCommandName(c.getArguments()[0]);
				DiamondSealCard result = seal.drawFromMachine();
				accountDBHandler.addCardToAccount(user, result);
				channel.sendMessage(user.getMentionTag() + " You pulled `" + result.getName() + "` from " + seal.getName() +  "! The card has been added to your inventory.");
			} catch (NullPointerException e) {
				channel.sendMessage(user.getMentionTag() + " No such diamond seal `" + c.getArguments()[0] + "`");
			} catch (ArrayIndexOutOfBoundsException e) {
				channel.sendMessage(user.getMentionTag() + " Please specify a diamond seal banner. Run `" + "listseals" + "` for a list of diamond seal banners.");
			} catch (InsufficientCurrencyException e) {
				channel.sendMessage(user.getMentionTag() + " You have insufficient diamonds to perform a diamond seal.");
			}
			break;
		case "simulateseal":
			try {
				DiamondSeal seal = getDiamondSealByCommandName(c.getArguments()[0]);
				channel.sendMessage(user.getMentionTag() + " You pulled `" + seal.drawFromMachine().getName() + "` from " + seal.getName() +  "! As this was a free simulated pull, the card was not added to your inventory.");

			} catch (NullPointerException e) {
				channel.sendMessage(user.getMentionTag() + " No such diamond seal `" + c.getArguments()[0] + "`");
			} catch (ArrayIndexOutOfBoundsException e) {
				channel.sendMessage(user.getMentionTag() + " Please specify a diamond seal banner. Run `" + "listseals" + "` for a list of diamond seal banners.");
			}
			break;
		case "sealinfo":
			try {
				DiamondSeal sealBanner = getDiamondSealByCommandName(c.getArguments()[0]);
				channel.sendMessage(user.getMentionTag() + "\n" + sealBanner.getInfo());

			} catch (NullPointerException e) {
				channel.sendMessage(user.getMentionTag() + " No such diamond seal `" + c.getArguments()[0] + "`");
			} catch (ArrayIndexOutOfBoundsException e) {
				channel.sendMessage(user.getMentionTag() + " Please specify a diamond seal banner. Run `" + "listseals" + "` for a list of diamond seal banners.");
			}
			break;
		case "listseals":
			try {
				String response = "";
				for (DiamondSeal s : diamondSeals) {
					response += s.getCommandName();
					response += ", ";
				}
				response = response.substring(0, response.length() - 2);
				channel.sendMessage(user.getMentionTag() + ", here are all the diamond seal banners available:\n\n" + response);

			} catch (StringIndexOutOfBoundsException e) {
				channel.sendMessage(user.getMentionTag() + " There are no diamond seal banners available.");
			}
			break;
		case "register":
			if (accountDBHandler.userAlreadyExists(user)) {
				channel.sendMessage(user.getMentionTag() + " You have already registered. View your account info by running `accountinfo`");
			} else {
				if (accountDBHandler.registerNewUser(new Account(user))) {
					channel.sendMessage(user.getMentionTag() + " Successfully registered. Welcome, summoner! View your account info by running `accountinfo`");
				} else {
					channel.sendMessage(user.getMentionTag() + " An error occurred while registering.");
				}
			}
			break;
		case "rewards":
			if (!accountDBHandler.userAlreadyExists(user)) {
				channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
				return;
			}

			ArrayList<Reward> rewards = accountDBHandler.getUserAccount(user).getRewards();
			
			if (rewards.size() == 0) {
				channel.sendMessage(user.getMentionTag() + " You do not currently have any rewards to be collected.");
				return;
			}

			int rewardsPage = -1;

			try {
				rewardsPage = Integer.parseInt(c.getArguments()[0]);
			} catch (Exception e) {
				rewardsPage = 1;
			}

			int rewardsMaxPage = (int) Math.round(Math.ceil(rewards.size() / 5.0));

			EmbedBuilder rewardsEmbed = new EmbedBuilder();
			rewardsEmbed.setAuthor(user)
			.setTitle("Rewards - " + rewards.size() + " rewards total")
			.setColor(Color.MAGENTA) //TODO check which attribute role user has
			.setFooter("Page " + rewardsPage  + " of " + rewardsMaxPage +" | DajiBot v2", "https://cdn.discordapp.com/app-icons/293148175013773312/9ec4cdaabd88f0902a7ea2eddab5a827.png");
			int rewardsStartBound = 5 * (rewardsPage - 1);
			int rewardsEndBound = rewardsStartBound + 5;

			for (int i = rewardsStartBound; i < rewardsEndBound; i++) {
				try{
					Reward reward = rewards.get(i);
					if (reward.getItemType() == ItemType.CARD) {
						rewardsEmbed.addField((i + 1) + ". " + reward.getText(), DiamondSealCard.getCardNameFromID(reward.getCardID()) + " x" + reward.getAmount());
					} else {
						String type = "";
						switch (reward.getItemType()) {
						case DIAMOND:
							type = "Diamond";
							break;
						case COIN:
							type = "Coin";
							break;
						case FRIEND_POINT:
							type = "Friend Point";
							break;
						case SOUL:
							type = "Soul";
							break;
						default:
							type = "An error occurred";
							break;
						}
						rewardsEmbed.addField((i + 1) + ". " + reward.getText(), type + " x" + reward.getAmount());
					}
				} catch (IndexOutOfBoundsException e) {
					//end of list was reached, pass
				}
			}

			if (!(rewardsPage > rewardsMaxPage)){
				channel.sendMessage(rewardsEmbed);
			}
			break;
		case "collect":
		case "claim":
			if (!accountDBHandler.userAlreadyExists(user)) {
				channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
				return;
			}

			ArrayList<Reward> userRewards = accountDBHandler.getUserAccount(user).getRewards();
			
			if (userRewards.size() == 0) {
				channel.sendMessage(user.getMentionTag() + " You do not currently have any rewards to be collected.");
				return;
			}
			
			try {
				int targetRewardIndex = Integer.parseInt(c.getArguments()[0]) - 1;
				
				Reward claimTargetReward = userRewards.get(targetRewardIndex);
				
				boolean a = rewardsDBHandler.removeReward(claimTargetReward);
				boolean b = accountDBHandler.claimReward(user, claimTargetReward);
				if (!(a && b)){
					channel.sendMessage(user.getMentionTag() + " An error was encountered while claiming your reward (SQLException).");
				} else {
					String type = "";
					switch (claimTargetReward.getItemType()) {
					case DIAMOND:
						type = "Diamond";
						break;
					case COIN:
						type = "Coin";
						break;
					case FRIEND_POINT:
						type = "Friend Point";
						break;
					case SOUL:
						type = "Soul";
						break;
					case CARD:
						type = DiamondSealCard.getCardNameFromID(claimTargetReward.getCardID());
						break;
					default:
						type = "An error occurred";
						break;
					}
					channel.sendMessage(user.getMentionTag() + " You have successfully claimed " + type + " x" + claimTargetReward.getAmount() + ".");
				}
			} catch (NumberFormatException e) {
				channel.sendMessage(user.getMentionTag() + " Please enter the number of the reward you want to claim. Run the `rewards` command to see all your rewards.");
			} catch (ArrayIndexOutOfBoundsException e) {
				channel.sendMessage(user.getMentionTag() + " Please enter the number of the reward you want to claim. Run the `rewards` command to see all your rewards.");
			}
			break;
		case "info":
		case "accountinfo":
			if (!accountDBHandler.userAlreadyExists(user)) {
				channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
				return;
			}
			Account account = accountDBHandler.getUserAccount(user);
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setAuthor(user)
			.setTitle("Account Info")
			.setColor(Color.MAGENTA) //TODO check which attribute role user has
			.addField("Diamonds", String.valueOf(account.getDiamonds()))
			.addField("Coins", String.valueOf(account.getCoins()))
			.addField("Friend Points", String.valueOf(account.getFriendPoints()))
			.addField("Souls", String.valueOf(account.getSouls()))
			.setFooter("DajiBot v2", "https://cdn.discordapp.com/app-icons/293148175013773312/9ec4cdaabd88f0902a7ea2eddab5a827.png");
			channel.sendMessage(embedBuilder);
			break;
		case "inv":
		case "viewinventory":
		case "inventory":
			if (!accountDBHandler.userAlreadyExists(user)) {
				channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
				return;
			}

			ArrayList<Integer> inventory = accountDBHandler.getUserAccount(user).getInventory();

			int page = -1;

			try {
				page = Integer.parseInt(c.getArguments()[0]);
			} catch (Exception e) {
				page = 1;
			}

			int maxPage = (int) Math.round(Math.ceil(inventory.size() / 5.0));

			EmbedBuilder inventoryEmbed = new EmbedBuilder();
			inventoryEmbed.setAuthor(user)
			.setTitle("Inventory - " + inventory.size() + " cards total")
			.setColor(Color.MAGENTA) //TODO check which attribute role user has
			.setFooter("Page " + page + " of " + maxPage +" | DajiBot v2", "https://cdn.discordapp.com/app-icons/293148175013773312/9ec4cdaabd88f0902a7ea2eddab5a827.png");
			int startBound = 5 * (page - 1);
			int endBound = startBound + 5;

			for (int i = startBound; i < endBound; i++) {
				try{
					if (inventory.get(i) == 0) {
						inventoryEmbed.addField(DiamondSealCard.getCardNameFromID(inventory.get(i)), "Lv. MAX (99), Skill Lv. 15");

					} else {
						inventoryEmbed.addField(DiamondSealCard.getCardNameFromID(inventory.get(i)), "Lv. 1, Skill Lv. 1"); //TODO implement card leveling
					}
				} catch (IndexOutOfBoundsException e) {
					//end of list was reached, pass
				}
			}

			if (!(page > maxPage)){
				channel.sendMessage(inventoryEmbed);
			}
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
											builder.withCard(new DiamondSealCard(name));
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
													boolean result = sealDBHandler.addSeal(newSeal.getName(), newSeal.getCommandName(), newSeal.getEntityNames(), "Card", newSeal.getRates());
													if (result) {
														diamondSeals.add(newSeal);
														channel.sendMessage(user.getMentionTag() + " Seal created! Pull from your new seal by running `diamondseal " + event1.getMessageContent() + "`");
													} else {
														channel.sendMessage(user.getMentionTag() + " An error occurred when creating the seal (SQLException). The seal was not created.");
													}
												} catch (IllegalStateException e) {
													channel.sendMessage(user.getMentionTag() + " Invalid entry. Amount of cards and amount of rates should be equal, and all rates should sum up to 1000, or 100%.");
												} catch (IllegalArgumentException e) {
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
		case "removeseal":
			if (privileged) {
				if (sealDBHandler.removeSeal(c.getArguments()[0])) {
					diamondSeals.remove(getDiamondSealByCommandName(c.getArguments()[0]));
					channel.sendMessage(user.getMentionTag() + " Seal banner " + c.getArguments()[0] + " has been deleted.");
				} else {
					channel.sendMessage(user.getMentionTag() + " An error occurred when deleting the seal (SQLException). The seal was not deleted.");
				}		
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "addcurrency":
		case "addcurr":
		case "addcur":
			if (user.isBotOwner()) {
				//user type amt
				try {
					User targetUser = api.getUserById(c.getArguments()[0].replaceAll("[^0-9]", "")).get();
					ItemType type;
					int amount = Integer.parseInt(c.getArguments()[2]);
					switch (c.getArguments()[1].toLowerCase()) {
					case "diamond":
					case "diamonds":
						type = ItemType.DIAMOND;
						break;
					case "coin":
					case "coins":
						type = ItemType.COIN;
						break;
					case "friendpoint":
					case "friendpoints":
						type = ItemType.FRIEND_POINT;
						break;
					case "soul":
					case "souls":
						type = ItemType.SOUL;
						break;
					default:
						channel.sendMessage(user.getMentionTag() + " Please specify a currency type, one of `diamond coin friendpoint soul`");
						return;
					}

					accountDBHandler.addCurrencyToAccount(targetUser, type, amount);
					channel.sendMessage(user.getMentionTag() + " Currency successfully added.");
				} catch (InterruptedException e) {
					e.printStackTrace();
					channel.sendMessage(user.getMentionTag() + " Error occurred: InterruptedException. See stack trace for more info");
				} catch (ExecutionException e) {
					e.printStackTrace();
					channel.sendMessage(user.getMentionTag() + " Error occurred: ExecutionException. See stack trace for more info");
				} catch (NumberFormatException e) {
					e.printStackTrace();
					channel.sendMessage(user.getMentionTag() + " Please enter a proper amount.");
				}
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

	public static User getUserById(long id){
		try {
			return api.getUserById(id).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}	

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader("token.txt"));
		String token = bufferedReader.readLine();
		bufferedReader.close();

		sealDBHandler = new DiamondSealDatabaseHandler();
		accountDBHandler = new AccountDatabaseHandler();
		rewardsDBHandler = new RewardsDatabaseHandler();
		diamondSeals = sealDBHandler.getAllDiamondSeals();

		api = new DiscordApiBuilder().setToken(token).login().join();

		api.addMessageCreateListener(event -> {
			if (event.getMessageAuthor().asUser().get().isBotOwner() && event.getMessageContent().startsWith(prefix)) {
				handleCommand(parseCommand(event.getMessageContent()), event.getChannel(), true, event.getMessageAuthor().asUser().get());
			} else if(event.getMessageContent().startsWith(prefix)) {

				try {
					User author = event.getMessageAuthor().asUser().get();
					Server server = event.getServer().get();
					if (isPrivileged(author, server)) {
						handleCommand(parseCommand(event.getMessageContent()), event.getChannel(), true, author);
					} else {
						handleCommand(parseCommand(event.getMessageContent()), event.getChannel(), false, author);
					}
				} catch (NoSuchElementException e) {
					try {
						event.getServer().get();
					} catch (NoSuchElementException e1) {
						event.getChannel().sendMessage("Sorry, DajiBot doesn't work in DMs. Please try your command again in <#300630118932414464> on the Tower of Saviors Discord server.");
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}

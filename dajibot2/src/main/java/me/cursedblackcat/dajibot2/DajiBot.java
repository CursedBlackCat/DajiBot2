package me.cursedblackcat.dajibot2;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
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
import me.cursedblackcat.dajibot2.shop.ShopDatabaseHandler;
import me.cursedblackcat.dajibot2.shop.ShopItem;
import me.cursedblackcat.dajibot2.rewards.DailyRewardsCron;

/**
 * The main class of the program.
 * @author Darren Yip
 *
 */
public class DajiBot {
	private static String helpText = "DajiBot v2 - a rewrite of the original DajiBot by CursedBlackCat#7801 \n" + 
			"\n" + 
			"The help command is broken into subcategories. Run the command `help <category>` for a list of commands in a category. **These are the valid categories:**\n" + 
			"\n" +
			"diamondseal, account, shop, admin, owner";

	private static String helpTextDiamondSeal = "**Diamond seal simulator commands**\n" + 
			"```diamondseal <sealname> - Use 5 diamonds from your account to pull a card from the diamond seal simulator.\n\n" +
			"simulateseal <sealname> - Simulates pulling a card from the diamond seal simulator. Does not cost you diamonds, but doesn't give you the card.\n\n" +
			"listseals - List all diamond seal banners in the diamond seal simulator\n\n" +
			"sealinfo <sealname> - Check available cards/series and their rates in a seal banner.```";

	private static String helpTextAccount = "**Account commands**\n" + 
			"```register - Register your Discord account with DajiBot.\n\n" +
			"daily - Collect your daily diamond reward.\n\n" +
			"rewards - List all of your unclaimed rewards.\n\n" +
			"collect <n> - Collect reward number <n> in your rewards inbox.\n\n" +
			"accountinfo - View your account info.\n\n" +
			"inventory <pagenumber> - View your card inventory.```";

	private static String helpTextShop = "**Shop commands**\n" + 
			"```shop - List all items available for sale in the shop.\n\n" +
			"buy <n> - Buy item number n in the shop.```";

	private static String helpTextAdmin = "**Admin commands**\n" + 
			"```changeprefix - Change the bot's command prefix. Can only be run by people with admin permissions.\n\n" + 
			"createseal - Create a new diamond seal machine. Can only be run by people with admin permissions.\n\n" + 
			"deleteseal - Delete a diamond seal machine. Can only be run by people with admin permissions.```";

	private static String helpTextOwner = "**Bot owner commands**\n" + 
			"```addcurrency <@user> <type> <amount> - Add currency to a user. Can only be run by the bot owner.\n\n" + 
			"resetdaily - Manually resets everyone's daily reward status. Can only be run by the bot owner.\n\n" + 
			"rewardall <type> <amount> <rewardtext> - Manually adds a specified reward for all registered users. Can only be run by the bot owner.\n\n" +
			"cleanrewards - Clears expired rewards from the rewards database. Can only be run by the bot owner.\n\n" +
			"addshopitem <itemtype> <amount> <costcurrency> <price> <cardID> - Create a new item for sale in the shop. Ignore cardID if the item for sale is not a card. Can only be run by the bot owner.\n\n" + 
			"removeshopitem <n> - Remove item number n for sale in the shop. Can only be run by the bot owner." +
			"```";

	private	static String prefix = "$";

	public static TextChannel botChannel;

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

	public static DiamondSealDatabaseHandler sealDBHandler;
	public static AccountDatabaseHandler accountDBHandler;
	public static RewardsDatabaseHandler rewardsDBHandler;
	public static ShopDatabaseHandler shopDBHandler;

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
			try {
				switch(c.getArguments()[0].toLowerCase()) {
				case "diamondseal":
					channel.sendMessage(user.getMentionTag() + "\n" + helpTextDiamondSeal);
					break;
				case "account":
					channel.sendMessage(user.getMentionTag() + "\n" + helpTextAccount);
					break;
				case "shop":
					channel.sendMessage(user.getMentionTag() + "\n" + helpTextShop);
					break;
				case "admin":
					if (privileged) {
						channel.sendMessage(user.getMentionTag() + "\n" + helpTextAdmin);
					} else {
						channel.sendMessage(user.getMentionTag() + " You cannot use admin commands.");
					}
					break;
				case "owner":
					if (user.isBotOwner()) {
						channel.sendMessage(user.getMentionTag() + "\n" + helpTextOwner);
					} else {
						channel.sendMessage(user.getMentionTag() + " You cannot use bot owner commands.");
					}
					break;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				channel.sendMessage(user.getMentionTag() + "\n" + helpText);
			}
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
		case "daily":
			if (!accountDBHandler.userAlreadyExists(user)) {
				channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
				return;
			}

			if (accountDBHandler.dailyRewardAlreadyCollected(user)) {
				channel.sendMessage(user.getMentionTag() + " You have already collected your daily reward.");
				return;
			}

			accountDBHandler.collectDailyReward(user);
			Date now = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long millisecondsUntilMidnight = (cal.getTimeInMillis()-System.currentTimeMillis());
			Date ninetyDays = new Date(now.getTime() + millisecondsUntilMidnight + 7776000000L);

			Reward dailyDiamondReward = new Reward(user, ItemType.DIAMOND, 1, ninetyDays, -1, "Daily Login Reward - " + dateFormat.format(now));
			rewardsDBHandler.addReward(dailyDiamondReward);

			Reward dailyFPReward = new Reward(user, ItemType.FRIEND_POINT, 200, ninetyDays, -1, "Daily Login Reward - " + dateFormat.format(now));
			rewardsDBHandler.addReward(dailyFPReward);

			channel.sendMessage(user.getMentionTag() + " Your daily rewards of Diamond x1 and Friend Point x200 has been sent to your rewards inbox. Run the `rewards` command to see your rewards inbox.");
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
			.setColor(Color.MAGENTA)
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
						rewardsEmbed.addField((i + 1) + ". " + reward.getText(), type + " x" + reward.getAmount() + "\nExpires " + reward.getExpiryDate());
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

				Reward claimTargetReward = null;
				try {
					claimTargetReward = userRewards.get(targetRewardIndex);
				} catch (IndexOutOfBoundsException e) {
					channel.sendMessage(user.getMentionTag() + " Invalid reward number. Run the `rewards` command to see your rewards.");
				}

				if (accountDBHandler.claimReward(user, claimTargetReward)) {
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

				} else {
					channel.sendMessage(user.getMentionTag() + " An error occurred (SQLException).");
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
			.setColor(Color.MAGENTA)
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
			.setColor(Color.MAGENTA)
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
		case "listitems":
		case "shop":
		case "listshop":
			try {
				if (!accountDBHandler.userAlreadyExists(user)) {
					channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
					return;
				}

				ArrayList<ShopItem> allItems = shopDBHandler.getAllItemsInShop();

				int shopPage = -1;

				try {
					shopPage = Integer.parseInt(c.getArguments()[0]);
				} catch (Exception e) {
					shopPage = 1;
				}

				int maxShopPage = (int) Math.round(Math.ceil(allItems.size() / 5.0));

				EmbedBuilder shopEmbed = new EmbedBuilder();
				shopEmbed.setAuthor(user)
				.setTitle("Shop - " + allItems.size() + " items total")
				.setColor(Color.MAGENTA)
				.setFooter("Page " + shopPage + " of " + maxShopPage +" | DajiBot v2", "https://cdn.discordapp.com/app-icons/293148175013773312/9ec4cdaabd88f0902a7ea2eddab5a827.png");
				int shopStartBound = 5 * (shopPage - 1);
				int shopEndBound = shopStartBound + 5;

				for (int i = shopStartBound; i < shopEndBound; i++) {
					try{
						String costTypeString = "";
						ShopItem shopItem = allItems.get(i);

						switch (shopItem.getCostType()) {
						case DIAMOND:
							costTypeString = "Diamond";
							break;
						case COIN:
							costTypeString = "Coin";
							break;
						case FRIEND_POINT:
							costTypeString = "Friend Point";
							break;
						case SOUL:
							costTypeString = "Soul";
							break;
						default:
							costTypeString = "An error occurred";
							break;
						}


						if (shopItem.getItemType().equals(ItemType.CARD)) {
							shopEmbed.addField((i + 1) + ". " + DiamondSealCard.getCardNameFromID(shopItem.getCardID()) + " x" + shopItem.getItemAmount(), costTypeString + " x" + shopItem.getCostAmount());
						} else {
							String shopItemTypeString = "";

							switch (shopItem.getItemType()) {
							case DIAMOND:
								shopItemTypeString = "Diamond";
								break;
							case COIN:
								shopItemTypeString = "Coin";
								break;
							case FRIEND_POINT:
								shopItemTypeString = "Friend Point";
								break;
							case SOUL:
								shopItemTypeString = "Soul";
								break;
							default:
								shopItemTypeString = "An error occurred";
								break;
							}

							shopEmbed.addField((i + 1) + ". " + shopItemTypeString + " x" + shopItem.getItemAmount(), costTypeString + " x" + shopItem.getCostAmount());
						}						
					} catch (IndexOutOfBoundsException e) {
						//end of list was reached, pass
					}
				}

				if (!(shopPage > maxShopPage)){
					channel.sendMessage(shopEmbed);
				}
			} catch (SQLException e) {
				channel.sendMessage("An exception occurred while listing shop items: SQLException. See stack trace for more info.");
				e.printStackTrace();
			}
			break;
		case "buy":
			try {
				ArrayList<ShopItem> allItems = shopDBHandler.getAllItemsInShop();
				ShopItem buyTargetItem = allItems.get(Integer.parseInt(c.getArguments()[0]) - 1);
				ItemType costType = buyTargetItem.getCostType();
				int costAmount = buyTargetItem.getCostAmount();
				
				if(!accountDBHandler.deductCurrencyFromAccount(user, costType, costAmount)) {
					channel.sendMessage(user.getMentionTag() + " An unknown error occurred. Please try again, and if the issue persists, contact the bot owner.");
					return;
				}
				
				if (buyTargetItem.getItemType().equals(ItemType.CARD)) {
					accountDBHandler.addCardToAccount(user, new DiamondSealCard(DiamondSealCard.getCardNameFromID(buyTargetItem.getCardID())));
				} else {
					accountDBHandler.addCurrencyToAccount(user, buyTargetItem.getItemType(), buyTargetItem.getItemAmount());
				}
				
				channel.sendMessage(user.getMentionTag() + " Item successfully purchased.");
				} catch (SQLException e) {
				channel.sendMessage(user.getMentionTag() + " An exception occurred while listing shop items: SQLException. See stack trace for more info.");
				e.printStackTrace();
			} catch (NumberFormatException e) {
				channel.sendMessage(user.getMentionTag() + " Please enter the number of the item you wish to buy.");
			} catch (ArrayIndexOutOfBoundsException e) {
				channel.sendMessage(user.getMentionTag() + " Please enter the number of the item you wish to buy.");
			} catch (InsufficientCurrencyException e) {
				channel.sendMessage(user.getMentionTag() + " You can't afford that.");
			}

			break;
		case "changeprefix":
			if (privileged) {
				prefix = c.getArguments()[0];
				channel.sendMessage(user.getMentionTag() + " Prefix has been set to " + c.getArguments()[0]);
				api.updateActivity("Tower of Saviors | " + prefix + "help");
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
											name.replaceAll("'", "''");
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
					channel.sendMessage(user.getMentionTag() + " Please enter a proper amount.");
				}
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "resetdaily":
			if (user.isBotOwner()) {
				accountDBHandler.resetDailyRewards();
				channel.sendMessage(user.getMentionTag() + " Daily rewards reset.");
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "rewardall":
			if (user.isBotOwner()) {
				try {
					ItemType type;
					int amount = Integer.parseInt(c.getArguments()[1]);
					switch (c.getArguments()[0].toLowerCase()) {
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
						channel.sendMessage(user.getMentionTag() + " Please specify a reward type, one of `diamond coin friendpoint soul`");
						return;
					}

					ArrayList<Long> allUsers = accountDBHandler.getAllUsers();

					String rewardText = "";

					for (int i = 2; i < c.getArguments().length; i++) {
						rewardText += c.getArguments()[i] + " ";
					}

					rewardText = rewardText.substring(0, rewardText.length() - 1);

					for (Long id : allUsers) {
						rewardsDBHandler.addReward(new Reward(api.getUserById(id).get(), type, amount, new Date(221876928000000L), -1, rewardText));
					}

					channel.sendMessage(user.getMentionTag() + " Rewards successfully added.");
				} catch (NumberFormatException e) {
					channel.sendMessage(user.getMentionTag() + " Please enter a proper amount.");
				} catch (InterruptedException e) {
					e.printStackTrace();
					channel.sendMessage(user.getMentionTag() + " Error occurred: InterruptedException. See stack trace for more info");
				} catch (ExecutionException e) {
					e.printStackTrace();
					channel.sendMessage(user.getMentionTag() + " Error occurred: ExecutionException. See stack trace for more info");
				} catch (SQLException e) {
					e.printStackTrace();
					channel.sendMessage(user.getMentionTag() + " Error occurred: SQLException. See stack trace for more info");
				}
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "purgeoldrewards":
		case "purgerewards":
		case "cleanrewards":
			if (user.isBotOwner()) {
				if (rewardsDBHandler.clearExpiredRewards()) {
					channel.sendMessage(user.getMentionTag() + " Expired rewards cleared from rewards database.");
				} else {
					channel.sendMessage(user.getMentionTag() + " Unable to clear expired rewards from rewards database.");
				}
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "addshopitem":
			if (user.isBotOwner()) {
				try {
					String[] arguments = c.getArguments();
					ItemType newShopItemType;
					int newShopItemAmount;
					ItemType costType;
					int costAmount;
					int shopCardID = -1;
					switch (arguments[0].toLowerCase()) {
					case "diamond":
					case "diamonds":
						newShopItemType = ItemType.DIAMOND;
						break;
					case "coin":
					case "coins":
						newShopItemType = ItemType.COIN;
						break;
					case "friendpoint":
					case "friendpoints":
						newShopItemType = ItemType.FRIEND_POINT;
						break;
					case "soul":
					case "souls":
						newShopItemType = ItemType.SOUL;
						break;
					case "card":
					case "cards":
						newShopItemType = ItemType.CARD;
						shopCardID = Integer.parseInt(arguments[4]);
						break;
					default:
						channel.sendMessage(user.getMentionTag() + " Please specify an item type, one of `diamond coin friendpoint soul card`");
						return;
					}

					switch (arguments[2].toLowerCase()) {
					case "diamond":
					case "diamonds":
						costType = ItemType.DIAMOND;
						break;
					case "coin":
					case "coins":
						costType = ItemType.COIN;
						break;
					case "friendpoint":
					case "friendpoints":
						costType = ItemType.FRIEND_POINT;
						break;
					case "soul":
					case "souls":
						costType = ItemType.SOUL;
						break;
					default:
						channel.sendMessage(user.getMentionTag() + " Please specify a cost type, one of `diamond coin friendpoint soul`");
						return;
					}

					newShopItemAmount = Integer.parseInt(arguments[1]);
					costAmount = Integer.parseInt(arguments[3]);

					if(shopDBHandler.addItemToShop(new ShopItem(newShopItemType, shopCardID, newShopItemAmount, costType, costAmount))) {
						channel.sendMessage(user.getMentionTag() + " Successfully added item to shop.");
					} else {
						channel.sendMessage(user.getMentionTag() + " An exception occurred while creating new shop item: SQLException.");
					}

				} catch (NumberFormatException e) {
					channel.sendMessage(user.getMentionTag() + " Please enter a valid integer.");
					return;
				}				
			} else {
				channel.sendMessage(user.getMentionTag() + " You do not have permissions to run this command!");
			}
			break;
		case "removeshopitem":
			if (user.isBotOwner()) {
				try {
					if (shopDBHandler.removeItemFromShop(shopDBHandler.getAllItemsInShop().get(Integer.parseInt(c.getArguments()[0]) - 1))) {
						channel.sendMessage(user.getMentionTag() + " Successfully removed item from shop.");
					} else {
						channel.sendMessage(user.getMentionTag() + " An exception occurred while deleting shop item: SQLException.");
					}
				} catch (SQLException e) {
					e.printStackTrace();
					channel.sendMessage(user.getMentionTag() + " An exception occurred while deleting shop item: SQLException. See stack trace for more info.");
				} catch (NumberFormatException e) {
					channel.sendMessage(user.getMentionTag() + " Please enter a valid integer.");
				} catch (ArrayIndexOutOfBoundsException e) {
					channel.sendMessage(user.getMentionTag() + " Please enter the index of the item you would like to remove from the shop.");
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
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		DailyRewardsCron task = new DailyRewardsCron("Daily rewards");

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long millisecondsUntilMidnight = (c.getTimeInMillis()-System.currentTimeMillis());

		//Run the task starting at the moment of the upcoming midnight, then subsequently run the task again every midnight afterwards
		ScheduledFuture<?> scheduledFuture = ses.scheduleAtFixedRate(task, millisecondsUntilMidnight, 86400000, TimeUnit.MILLISECONDS);

		System.out.println("Daily reward collection flag reset task scheduled! Next execution is " + new Date(new Date().getTime() + scheduledFuture.getDelay(TimeUnit.MILLISECONDS)));


		BufferedReader bufferedReader = new BufferedReader(new FileReader("token.txt"));
		String token = bufferedReader.readLine();
		bufferedReader.close();

		sealDBHandler = new DiamondSealDatabaseHandler();
		accountDBHandler = new AccountDatabaseHandler();
		rewardsDBHandler = new RewardsDatabaseHandler();
		shopDBHandler = new ShopDatabaseHandler();
		diamondSeals = sealDBHandler.getAllDiamondSeals();

		api = new DiscordApiBuilder().setToken(token).login().join();

		try {
			botChannel = api.getTextChannelById(300630118932414464L).get();
			botChannel.sendMessage("DajiBot online! Next daily reset is scheduled for " + new Date(new Date().getTime() + scheduledFuture.getDelay(TimeUnit.MILLISECONDS)) + ".");

		} catch (NoSuchElementException e) {
			System.out.println("Unable to get bot channel!");
		}

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

		api.updateActivity("Tower of Saviors | " + prefix + "help");
	}
}

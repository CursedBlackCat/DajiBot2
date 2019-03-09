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

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import me.cursedblackcat.dajibot2.accounts.Account;
import me.cursedblackcat.dajibot2.accounts.AccountDatabaseHandler;
import me.cursedblackcat.dajibot2.accounts.InsufficientCurrencyException;
import me.cursedblackcat.dajibot2.commands.AccountCommandExecutor;
import me.cursedblackcat.dajibot2.commands.AdminCommandExecutor;
import me.cursedblackcat.dajibot2.commands.DiamondSealCommandExecutor;
import me.cursedblackcat.dajibot2.commands.HelpCommand;
import me.cursedblackcat.dajibot2.commands.OwnerCommandExecutor;
import me.cursedblackcat.dajibot2.commands.ShopCommandExecutor;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSeal;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSealBuilder;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSealCard;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSealDatabaseHandler;
import me.cursedblackcat.dajibot2.rewards.DailyRewardsCron;
import me.cursedblackcat.dajibot2.rewards.ItemType;
import me.cursedblackcat.dajibot2.rewards.Reward;
import me.cursedblackcat.dajibot2.rewards.RewardsDatabaseHandler;
import me.cursedblackcat.dajibot2.shop.ShopDatabaseHandler;
import me.cursedblackcat.dajibot2.shop.ShopItem;

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

	public static String prefix = "$";

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
		case "diamondseal": //done
			break;
		case "simulateseal": //done
			break;
		case "sealinfo": //done
			break;
		case "listseals"://done
			break;
		case "register":
			break;
		case "daily":
			break;
		case "rewards":
			break;
		case "collect":
		case "claim":
			break;
		case "info":
		case "accountinfo":
			break;
		case "inv":
		case "viewinventory":
		case "inventory":
			break;
		case "listitems":
		case "shop":
		case "listshop":
			
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
														getDiamondSeals().add(newSeal);
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
					getDiamondSeals().remove(getDiamondSealByCommandName(c.getArguments()[0]));
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

	public static DiamondSeal getDiamondSealByCommandName(String name) {
		for (DiamondSeal seal : getDiamondSeals()) {
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
	public static boolean isPrivileged(User user, Server server) {
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

	public static ArrayList<DiamondSeal> getDiamondSeals() {
		return diamondSeals;
	}

	private static void setDiamondSeals(ArrayList<DiamondSeal> diamondSeals) {
		DajiBot.diamondSeals = diamondSeals;
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
		setDiamondSeals(sealDBHandler.getAllDiamondSeals());

		api = new DiscordApiBuilder().setToken(token).login().join();

		try {
			botChannel = api.getTextChannelById(300630118932414464L).get();
			botChannel.sendMessage("DajiBot online! Next daily reset is scheduled for " + new Date(new Date().getTime() + scheduledFuture.getDelay(TimeUnit.MILLISECONDS)) + ".");

		} catch (NoSuchElementException e) {
			System.out.println("Unable to get bot channel!");
		}

//		api.addMessageCreateListener(event -> {
//			if (event.getMessageAuthor().asUser().get().isBotOwner() && event.getMessageContent().startsWith(prefix)) {
//				handleCommand(parseCommand(event.getMessageContent()), event.getChannel(), true, event.getMessageAuthor().asUser().get());
//			} else if(event.getMessageContent().startsWith(prefix)) {
//
//				try {
//					User author = event.getMessageAuthor().asUser().get();
//					Server server = event.getServer().get();
//					if (isPrivileged(author, server)) {
//						handleCommand(parseCommand(event.getMessageContent()), event.getChannel(), true, author);
//					} else {
//						handleCommand(parseCommand(event.getMessageContent()), event.getChannel(), false, author);
//					}
//				} catch (NoSuchElementException e) {
//					try {
//						event.getServer().get();
//					} catch (NoSuchElementException e1) {
//						event.getChannel().sendMessage("Sorry, DajiBot doesn't work in DMs. Please try your command again in <#300630118932414464> on the Tower of Saviors Discord server.");
//					}
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
		
		CommandHandler diamondSealCommandHandler = new JavacordHandler(api);
		CommandHandler accountCommandHandler = new JavacordHandler(api);
		CommandHandler shopCommandHandler = new JavacordHandler(api);
		CommandHandler adminCommandHandler = new JavacordHandler(api);
		CommandHandler ownerCommandHandler = new JavacordHandler(api);
		
		diamondSealCommandHandler.registerCommand(new DiamondSealCommandExecutor());
		accountCommandHandler.registerCommand(new AccountCommandExecutor());
		shopCommandHandler.registerCommand(new ShopCommandExecutor());
		adminCommandHandler.registerCommand(new AdminCommandExecutor());
		ownerCommandHandler.registerCommand(new OwnerCommandExecutor());
		
		CommandHandler helpHandler = new JavacordHandler(api);
		helpHandler.registerCommand(new HelpCommand(diamondSealCommandHandler, accountCommandHandler, shopCommandHandler, adminCommandHandler, ownerCommandHandler));

		api.updateActivity("Tower of Saviors | $help");
	}
}

package me.cursedblackcat.dajibot2.commands;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import me.cursedblackcat.dajibot2.DajiBot;
import me.cursedblackcat.dajibot2.accounts.Account;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSealCard;
import me.cursedblackcat.dajibot2.rewards.ItemType;
import me.cursedblackcat.dajibot2.rewards.Reward;

public class AccountCommandExecutor implements CommandExecutor {
	/*
	 TEMPLATE:
	@Command(aliases = "$command",
			description = "desc",
			usage = "$command",
			privateMessages = false,
			async = true)
	public String onCommandCommand(String[] args, User user) {
		
	}
	 */
	
	@Command(aliases = "$register",
			description = "Register your Discord account with DajiBot.",
			usage = "$register",
			privateMessages = false,
			async = true)
	public String onRegisterCommand(String[] args, User user) {
		if (DajiBot.accountDBHandler.userAlreadyExists(user)) {
			return user.getMentionTag() + " You have already registered. View your account info by running `accountinfo`";
		} else {
			if (DajiBot.accountDBHandler.registerNewUser(new Account(user))) {
				return user.getMentionTag() + " Successfully registered. Welcome, summoner! View your account info by running `accountinfo`";
			} else {
				return user.getMentionTag() + " An error occurred while registering.";
			}
		}
	}
	
	@Command(aliases = "$daily",
			description = "Collect your daily login rewards. Resets at midnight UTC.",
			usage = "$daily",
			privateMessages = false,
			async = true)
	public String onDailyCommand(String[] args, User user) {
		if (!DajiBot.accountDBHandler.userAlreadyExists(user)) {
			return user.getMentionTag() + " Please register first by running  the `register` command.";
		}

		if (DajiBot.accountDBHandler.dailyRewardAlreadyCollected(user)) {
			return user.getMentionTag() + " You have already collected your daily reward.";
		}

		DajiBot.accountDBHandler.collectDailyReward(user);
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
		DajiBot.rewardsDBHandler.addReward(dailyDiamondReward);

		Reward dailyFPReward = new Reward(user, ItemType.FRIEND_POINT, 200, ninetyDays, -1, "Daily Login Reward - " + dateFormat.format(now));
		DajiBot.rewardsDBHandler.addReward(dailyFPReward);

		return user.getMentionTag() + " Your daily rewards of Diamond x1 and Friend Point x200 has been sent to your rewards inbox. Run the `rewards` command to see your rewards inbox.";
	}
	
	@Command(aliases = "$rewards",
			description = "List all of your unclaimed rewards.",
			usage = "$rewards <page>",
			privateMessages = false,
			async = true)
	public void onRewardsCommand(String[] args, User user, ServerTextChannel channel) {
		if (!DajiBot.accountDBHandler.userAlreadyExists(user)) {
			channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
			return;
		}

		ArrayList<Reward> rewards = DajiBot.accountDBHandler.getUserAccount(user).getRewards();

		if (rewards.size() == 0) {
			channel.sendMessage(user.getMentionTag() + " You do not currently have any rewards to be collected.");
			return;
		}

		int rewardsPage = -1;

		try {
			rewardsPage = Integer.parseInt(args[0]);
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
	}
	
	@Command(aliases = {"$collect", "$claim"},
			description = "Collect reward number `<n>` in your rewards inbox.",
			usage = "$collect <n>",
			privateMessages = false,
			async = true)
	public String onCollectCommand(String[] args, User user) {
		if (!DajiBot.accountDBHandler.userAlreadyExists(user)) {
			return user.getMentionTag() + " Please register first by running  the `register` command.";
		}

		ArrayList<Reward> userRewards = DajiBot.accountDBHandler.getUserAccount(user).getRewards();

		if (userRewards.size() == 0) {
			return user.getMentionTag() + " You do not currently have any rewards to be collected.";
		}

		try {
			int targetRewardIndex = Integer.parseInt(args[0]) - 1;

			Reward claimTargetReward = null;
			try {
				claimTargetReward = userRewards.get(targetRewardIndex);
			} catch (IndexOutOfBoundsException e) {
				return user.getMentionTag() + " Invalid reward number. Run the `rewards` command to see your rewards.";
			}

			if (DajiBot.accountDBHandler.claimReward(user, claimTargetReward)) {
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
				return user.getMentionTag() + " You have successfully claimed " + type + " x" + claimTargetReward.getAmount() + ".";

			} else {
				return user.getMentionTag() + " An error occurred (SQLException).";
			}
		} catch (NumberFormatException e) {
			return user.getMentionTag() + " Please enter the number of the reward you want to claim. Run the `rewards` command to see all your rewards.";
		} catch (ArrayIndexOutOfBoundsException e) {
			return user.getMentionTag() + " Please enter the number of the reward you want to claim. Run the `rewards` command to see all your rewards.";
		}
	}
	
	@Command(aliases = {"$accountinfo", "$info"},
			description = "View your account info.",
			usage = "$accountinfo",
			privateMessages = false,
			async = true)
	public void onAccountInfoCommand(String[] args, User user, ServerTextChannel channel) {
		if (!DajiBot.accountDBHandler.userAlreadyExists(user)) {
			channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
			return;
		}
		Account account = DajiBot.accountDBHandler.getUserAccount(user);
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
	}
	
	@Command(aliases = {"$inventory", "$inv", "$viewinventory"},
			description = "View your card inventory.",
			usage = "$inventory <page>",
			privateMessages = false,
			async = true)
	public void onInventoryCommand(String[] args, User user, ServerTextChannel channel) {
		if (!DajiBot.accountDBHandler.userAlreadyExists(user)) {
			channel.sendMessage(user.getMentionTag() + " Please register first by running  the `register` command.");
			return;
		}

		ArrayList<Integer> inventory = DajiBot.accountDBHandler.getUserAccount(user).getInventory();

		int page = -1;

		try {
			page = Integer.parseInt(args[0]);
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
	}
}

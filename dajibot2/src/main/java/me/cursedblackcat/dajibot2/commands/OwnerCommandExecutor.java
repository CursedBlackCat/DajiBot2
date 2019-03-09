package me.cursedblackcat.dajibot2.commands;

import org.javacord.api.entity.user.User;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import me.cursedblackcat.dajibot2.DajiBot;

public class OwnerCommandExecutor implements CommandExecutor {
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
	
	
	@Command(aliases = "$resetdaily",
			description = "Manually resets everyone's daily reward status. Can only be run by the bot owner.",
			usage = "$resetdaily",
			privateMessages = false,
			async = true)
	public String onResetDailyCommand(String[] args, User user) {
		if (user.isBotOwner()) {
			DajiBot.accountDBHandler.resetDailyRewards();
			return user.getMentionTag() + " Daily rewards reset.";
		} else {
			return user.getMentionTag() + " You do not have permissions to run this command!";
		}
	}
	
}

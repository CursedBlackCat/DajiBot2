package me.cursedblackcat.dajibot2.commands;

import java.awt.Color;
import java.util.List;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.CommandHandler.SimpleCommand;

public class HelpCommand implements CommandExecutor {
	private final CommandHandler diamondSealCommandHandler;
	private final CommandHandler accountCommandHandler;
	private final CommandHandler shopCommandHandler;
	private final CommandHandler adminCommandHandler;
	private final CommandHandler ownerCommandHandler;

	public HelpCommand(CommandHandler ds, CommandHandler acc, CommandHandler s, CommandHandler a, CommandHandler o) {
		diamondSealCommandHandler = ds;
		accountCommandHandler = acc;
		shopCommandHandler = s;
		adminCommandHandler = a;
		ownerCommandHandler = o;
	}

	@Command(aliases = {"$help", "$commands"},
			description = "Shows this page",
			privateMessages = false)
	public void onHelpCommand(String[] args, User user, ServerTextChannel  channel) {
		try {
			EmbedBuilder builder = new EmbedBuilder();
			List<SimpleCommand> commands = null;
			builder.setAuthor(user)
			.setColor(Color.MAGENTA)
			.setFooter("DajiBot v2", "https://cdn.discordapp.com/app-icons/293148175013773312/9ec4cdaabd88f0902a7ea2eddab5a827.png");
			switch(args[0].toLowerCase()) {
			case "diamondseal":
				commands = diamondSealCommandHandler.getCommands();
				builder.setTitle("DajiBot v2 - Help - Diamond Seal");
				break;
			case "account":
				commands = accountCommandHandler.getCommands();
				builder.setTitle("DajiBot v2 - Help - Account");
				break;
			case "shop":
				commands = shopCommandHandler.getCommands();
				builder.setTitle("DajiBot v2 - Help - Shop");
				break;
			case "admin":
				commands = adminCommandHandler.getCommands();
				builder.setTitle("DajiBot v2 - Help - Admin");
				break;
			case "owner":
				commands = ownerCommandHandler.getCommands();
				builder.setTitle("DajiBot v2 - Help - Owner");
				break;
			default:
				throw new Exception();
			}
			
			for (SimpleCommand command : commands) {
				Command annotation = command.getCommandAnnotation();
				builder.addField(annotation.aliases()[0], annotation.description() + "\nUsage: `" + annotation.usage() + "`");
			}
			
			channel.sendMessage(builder);
			
		} catch (Exception e) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setAuthor(user)
			.setTitle("DajiBot v2 - Help")
			.setColor(Color.MAGENTA)
			.addField("General info", "DajiBot v2 is a gacha simulation bot based off of Tower of Saviors, written by TheCatGod.")
			.addField("Help", "The help command is split into the following subsections: `diamondseal account shop admin owner`\nTo see the commands in a subsection, type `$help <subsection>`")
			.setFooter("DajiBot v2", "https://cdn.discordapp.com/app-icons/293148175013773312/9ec4cdaabd88f0902a7ea2eddab5a827.png");
			if(channel != null) {
				channel.sendMessage(builder);
			} else {
				System.out.println("[WARNING] Attempted to send help to private message channel!");
			}
		}
	}
}

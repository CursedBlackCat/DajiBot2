package me.cursedblackcat.dajibot2.commands;

import org.javacord.api.entity.user.User;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import me.cursedblackcat.dajibot2.DajiBot;
import me.cursedblackcat.dajibot2.accounts.InsufficientCurrencyException;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSeal;
import me.cursedblackcat.dajibot2.diamondseal.DiamondSealCard;
import me.cursedblackcat.dajibot2.rewards.ItemType;

public class DiamondSealCommandExecutor implements CommandExecutor{
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

	@Command(aliases = "$diamondseal",
			description = "Use 5 diamonds from your account to pull a card from the diamond seal simulator.",
			usage = "$diamondseal <sealname>",
			privateMessages = false,
			async = true)
	public String onDiamondSealCommand(String[] args, User user) {
		if (!DajiBot.accountDBHandler.userAlreadyExists(user)) {
			return user.getMentionTag() + " Please register first by running  the `register` command.";
		}

		try {
			DajiBot.accountDBHandler.deductCurrencyFromAccount(user, ItemType.DIAMOND, 5);
			DiamondSeal seal = DajiBot.getDiamondSealByCommandName(args[0]);
			DiamondSealCard result = seal.drawFromMachine();
			DajiBot.accountDBHandler.addCardToAccount(user, result);
			return user.getMentionTag() + " You pulled `" + result.getName() + "` from " + seal.getName() +  "! The card has been added to your inventory.";
		} catch (NullPointerException e) {
			return user.getMentionTag() + " No such diamond seal `" + args[0] + "`";
		} catch (ArrayIndexOutOfBoundsException e) {
			return user.getMentionTag() + " Please specify a diamond seal banner. Run `" + "listseals" + "` for a list of diamond seal banners.";
		} catch (InsufficientCurrencyException e) {
			return user.getMentionTag() + " You have insufficient diamonds to perform a diamond seal.";
		}
	}	

	@Command(aliases = "$simulateseal",
			description = "Simulates pulling a card from the diamond seal simulator. Doesn't cost you diamonds, but doesn't give you the card you pull.",
			usage = "$simulateseal <sealname>",
			privateMessages = false,
			async = true)
	public String onSimulateSealCommand(String[] args, User user) {
		try {
			DiamondSeal seal = DajiBot.getDiamondSealByCommandName(args[0]);
			return user.getMentionTag() + " You pulled `" + seal.drawFromMachine().getName() + "` from " + seal.getName() +  "! As this was a free simulated pull, the card was not added to your inventory.";

		} catch (NullPointerException e) {
			return user.getMentionTag() + " No such diamond seal `" + args[0] + "`";
		} catch (ArrayIndexOutOfBoundsException e) {
			return user.getMentionTag() + " Please specify a diamond seal banner. Run `" + "listseals" + "` for a list of diamond seal banners.";
		}
	}
	
	@Command(aliases = "$sealinfo",
			description = "Check available cards/series and their rates in a seal banner.",
			usage = "$sealinfo <sealname>",
			privateMessages = false,
			async = true)
	public String onSealInfoCommand(String[] args, User user) {
		try {
			DiamondSeal sealBanner = DajiBot.getDiamondSealByCommandName(args[0]);
			return user.getMentionTag() + "\n" + sealBanner.getInfo();

		} catch (NullPointerException e) {
			return user.getMentionTag() + " No such diamond seal `" + args[0] + "`";
		} catch (ArrayIndexOutOfBoundsException e) {
			return user.getMentionTag() + " Please specify a diamond seal banner. Run `" + "listseals" + "` for a list of diamond seal banners.";
		}
	}

	@Command(aliases = "$listseals",
			description = "List all diamond seal banners in the diamond seal simulator.",
			usage = "$listseals",
			privateMessages = false,
			async = true)
	public String onListSealsCommand(String[] args, User user) {
		try {
			String response = "";
			for (DiamondSeal s : DajiBot.getDiamondSeals()) {
				response += s.getCommandName();
				response += ", ";
			}
			response = response.substring(0, response.length() - 2);
			return user.getMentionTag() + ", here are all the diamond seal banners available:\n\n" + response;

		} catch (StringIndexOutOfBoundsException e) {
			return user.getMentionTag() + " There are currently no diamond seal banners available.";
		}
	}
}

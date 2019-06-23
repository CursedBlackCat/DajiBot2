package me.cursedblackcat.dajibot.rewards;

import java.util.Date;

import me.cursedblackcat.dajibot.DajiBot;
/**
 * Cron task for resetting the daily login reward flag.
 * @author Darren Yip
 *
 */
public class DailyRewardsCron implements Runnable{


	private String command;

	public DailyRewardsCron(String s){
		command=s;
	}

	public void run() {
		DajiBot.accountDBHandler.resetDailyRewards();

		System.out.println("Resetting everyone's daily rewards flag for " + new Date());
		
		DajiBot.botChannel.sendMessage("**A new day has dawned for DajiBot!** You can now collect another daily reward by running the `daily` command.");
	}

	@Override
	public String toString(){
		return command;
	}
}

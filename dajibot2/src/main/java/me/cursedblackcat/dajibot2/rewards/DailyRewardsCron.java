package me.cursedblackcat.dajibot2.rewards;

import java.util.Date;
/**
 * Cron task for one week's worth of reward diamonds.
 * @author Darren Yip
 *
 */
public class DailyRewardsCron implements Runnable{


	private String command;

	public DailyRewardsCron(String s){
		command=s;
	}

	public void run() {
		
		System.out.println("Resetting everyone's daily rewards flag for " + new Date());
	}

	@Override
	public String toString(){
		return command;
	}
}

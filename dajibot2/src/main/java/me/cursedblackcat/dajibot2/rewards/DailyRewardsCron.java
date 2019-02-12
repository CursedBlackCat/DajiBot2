package me.cursedblackcat.dajibot2.rewards;

import java.sql.SQLException;
import java.util.Date;

import me.cursedblackcat.dajibot2.accounts.AccountDatabaseHandler;
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
		
		try {
			new AccountDatabaseHandler().resetDailyRewards();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Resetting everyone's daily rewards flag for " + new Date());
	}

	@Override
	public String toString(){
		return command;
	}
}

package me.cursedblackcat.dajibot.accounts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.javacord.api.entity.user.User;

import me.cursedblackcat.dajibot.rewards.Reward;
import me.cursedblackcat.dajibot.rewards.RewardsDatabaseHandler;

public class Account {
	private User user;
	private int coins;
	private int diamonds;
	private int friendPoints;
	private int souls;
	private ArrayList<Reward> rewardsInbox = new ArrayList<Reward>();
	private ArrayList<Integer> inventory = new ArrayList<Integer>(); //IDs of all the cards in this account's inventory

	/**
	 * Register a new user with default currency values
	 * @param u
	 */
	public Account(User u) {
		user = u;
		coins = 10000;
		diamonds = 5;
		friendPoints = 2000;
		souls = 0;
		inventory.add(1);
	}

	public Account(User u, int c, int d, int fp, int s, int[] inv) throws ClassNotFoundException, SQLException {
		user = u;
		coins = c;
		diamonds = d;
		friendPoints = fp;
		souls = s;
		
		rewardsInbox = new RewardsDatabaseHandler().getRewardsForUser(u);
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i : inv) {
			temp.add(i);
		}
		inventory = temp;
		Collections.sort(inventory);
	}

	public User getUser() {
		return user;
	}

	public int getCoins() {
		return coins;
	}

	public int getDiamonds() {
		return diamonds;
	}

	public int getFriendPoints() {
		return friendPoints;
	}
	
	public int getSouls() {
		return souls;
	}
	
	public ArrayList<Reward> getRewards(){
		return rewardsInbox;
	}

	public ArrayList<Integer> getInventory(){
		return inventory;
	}

	public int[] getInventoryAsArray() {
		int[] ret = new int[inventory.size()];
		for (int i=0; i < ret.length; i++){
			ret[i] = inventory.get(i).intValue();
		}
		return ret;
	}
}

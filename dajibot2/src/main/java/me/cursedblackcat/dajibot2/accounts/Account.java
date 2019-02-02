package me.cursedblackcat.dajibot2.accounts;

import java.util.ArrayList;

import org.javacord.api.entity.user.User;

import me.cursedblackcat.dajibot2.rewards.Reward;

public class Account {
	private User user;
	private int coins;
	private int diamonds;
	private int friendPoints;
	private int souls;
	private ArrayList<Reward> rewardsInbox = new ArrayList<Reward>();
	private ArrayList<Integer> inventory = new ArrayList<Integer>(); //IDs of all the cards in this account's inventory

	public Account(User u) {
		user = u;
		coins = 10000;
		diamonds = 5;
		friendPoints = 2000;
		souls = 0;
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

package me.cursedblackcat.dajibot2.inventory;

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
	}
}

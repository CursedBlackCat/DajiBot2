package me.cursedblackcat.dajibot2.rewards;

import java.util.Date;

import org.javacord.api.entity.user.User;

public class Reward {
	private User user;
	private ItemType itemType;
	private int amount;
	private Date expiryDate;
	private int cardID;
	private String text;
	
	public Reward(User u, ItemType t, int a, Date e, int c, String txt) {
		user = u;
		itemType = t;
		amount = a;
		expiryDate = e;
		cardID = c;
		text = txt;
	}
	
	public User getUser() {
		return user;
	}
	
	public ItemType getItemType() {
		return itemType;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public Date getExpiryDate() {
		return expiryDate;
	}
	
	public int getCardID() {
		return cardID;
	}
	
	public String getText() {
		return text;
	}
}

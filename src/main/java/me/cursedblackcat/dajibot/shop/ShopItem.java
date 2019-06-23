package me.cursedblackcat.dajibot.shop;

import me.cursedblackcat.dajibot.rewards.ItemType;

/**
 * Represents one item for sale in an item shop.
 * @author Darren
 */
public class ShopItem {
	private ItemType itemType;
	private int itemAmount;
	private ItemType costType;
	private int costAmount;
	private int cardID;
	
	public ShopItem(ItemType type, int id, int amt, ItemType cost, int costAmt) {
		itemType = type;
		itemAmount = amt;
		costType = cost;
		costAmount = costAmt;
		cardID = id;
	}
	
	public int getCardID() {
		return cardID;
	}
	
	public ItemType getItemType() {
		return itemType;
	}
	
	public int getItemAmount() {
		return itemAmount;
	}
	
	public ItemType getCostType() {
		return costType;
	}
	
	public int getCostAmount() {
		return costAmount;
	}
}

package me.cursedblackcat.dajibot2.shop;

import java.util.ArrayList;

/**
 * Represents an item shop.
 * @author Darren
 */
public class Shop {
	private String name;
	private ArrayList<ShopItem> shopItems = new ArrayList<ShopItem>();
	
	public Shop(String shopName, ArrayList<ShopItem> items) {
		name = shopName;
		shopItems = items;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<ShopItem> getShopItems(){
		return shopItems;
	}
}

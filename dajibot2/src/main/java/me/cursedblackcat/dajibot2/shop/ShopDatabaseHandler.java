package me.cursedblackcat.dajibot2.shop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import me.cursedblackcat.dajibot2.rewards.ItemType;

/**
 * Class for handling database operations in the Shop database.
 * @author Darren
 */
public class ShopDatabaseHandler {
	Connection conn = null;
	Statement stmt = null;

	/**
	 * Initializes the connection with the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public ShopDatabaseHandler() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:shops.db");
		stmt = conn.createStatement();
		String sql = "CREATE TABLE ShopItems" +
				" (ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" ItemType        STRING     NOT NULL, " + 
				" CardID          INTEGER    NOT NULL, " + 
				" ItemAmount      INTEGER    NOT NULL, " + 
				" Cost            INTEGER    NOT NULL, " + 
				" CostType        STRING     NOT NULL, " +
				" ShopName        STRING     NOT NULL)";
		stmt.executeUpdate(sql);
		
		sql = "CREATE TABLE ShopNames" +
				" (ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" ShopName        STRING     NOT NULL)";
		stmt.executeUpdate(sql);
		stmt.close();
	}
	
	public boolean createShop(String shopName) {
		try {
			stmt = conn.createStatement();
			String sql = "INSERT INTO ShopNames (ShopName) " +
					"VALUES ('" + shopName +"');";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	public boolean shopAlreadyExists(String name) {
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM ShopNames WHERE ShopName='" + name + "';");
			
			return rs.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addItemToShop(String shopName, ShopItem item) throws NoSuchShopException {
		if(!shopAlreadyExists(shopName)) {
			throw new NoSuchShopException();
		}
		
		try {
			stmt = conn.createStatement();
			String sql = "INSERT INTO ShopItems (ItemType, CardID, ItemAmount, Cost, CostType) " +
					"VALUES ('"+ item.getItemType().toString() +"'," + item.getCardID() + ", " + item.getItemAmount() + ", " + item.getCostAmount() + ", '"+ item.getCostType().toString() + "', '"+ shopName +"');";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	/**
	 * Returns all the items for a shop in the database.
	 * @param shopName The shop to look up items for.
	 * @return An ArrayList of all the items in the shop.
	 * @throws SQLException 
	 */
	public ArrayList<ShopItem> getAllItemsInShop(String shopName) throws SQLException{
		
		ArrayList<ShopItem> items = new ArrayList<ShopItem>();
		
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM ShopItems WHERE ShopName='" + shopName + "';");
		
		while (rs.next()) {
			items.add(new ShopItem(Enum.valueOf(ItemType.class, rs.getString("ItemType")),rs.getInt("CardID"),rs.getInt("ItemAmount"), ItemType.valueOf(ItemType.class, rs.getString("CostType")), rs.getInt("ItemCost")));
		}
		
		return items;
	}
	
	public ArrayList<Shop> getAllShops(){
		ArrayList<Shop> shops = new ArrayList<Shop>();
		
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM ShopNames;");
			
			while(rs.next()) {
				String shopName = rs.getString("ShopName");
				ArrayList<ShopItem> shopItems = getAllItemsInShop(shopName);
				shops.add(new Shop(shopName, shopItems));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return shops;
	}
}

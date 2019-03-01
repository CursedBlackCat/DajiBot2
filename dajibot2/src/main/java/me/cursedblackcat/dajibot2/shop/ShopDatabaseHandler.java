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
		conn = DriverManager.getConnection("jdbc:sqlite:shop.db");
		stmt = conn.createStatement();
		String sql = "CREATE TABLE ShopItems" +
				" (ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" ItemType        STRING     NOT NULL, " + 
				" CardID          INTEGER    NOT NULL, " + 
				" ItemAmount      INTEGER    NOT NULL, " + 
				" Cost            INTEGER    NOT NULL, " + 
				" CostType        STRING     NOT NULL)";
		stmt.executeUpdate(sql);

		stmt.close();
	}

	/**
	 * Adds a new item for sale to the shop.
	 * @param item The ShopItem to add to the shop.
	 * @return True if the operation completed successfully, or false otehrwise.
	 * @throws NoSuchShopException
	 */
	public boolean addItemToShop(ShopItem item) {
		try {
			stmt = conn.createStatement();
			String sql = "INSERT INTO ShopItems (ItemType, CardID, ItemAmount, Cost, CostType) " +
					"VALUES ('"+ item.getItemType().toString() +"'," + item.getCardID() + ", " + item.getItemAmount() + ", " + item.getCostAmount() + ", '"+ item.getCostType().toString() + ");";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	/**
	 * Returns all the items for a shop in the database.
	 * @return An ArrayList of all the items in the shop.
	 * @throws SQLException 
	 */
	public ArrayList<ShopItem> getAllItemsInShop() throws SQLException{
		
		ArrayList<ShopItem> items = new ArrayList<ShopItem>();
		
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM ShopItems;");
		
		while (rs.next()) {
			items.add(new ShopItem(Enum.valueOf(ItemType.class, rs.getString("ItemType")),rs.getInt("CardID"),rs.getInt("ItemAmount"), ItemType.valueOf(ItemType.class, rs.getString("CostType")), rs.getInt("ItemCost")));
		}
		
		return items;
	}
}

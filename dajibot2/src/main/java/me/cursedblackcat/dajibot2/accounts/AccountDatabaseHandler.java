package me.cursedblackcat.dajibot2.accounts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.javacord.api.entity.user.User;

import me.cursedblackcat.dajibot2.DajiBot;
import me.cursedblackcat.dajibot2.rewards.ItemType;
import me.cursedblackcat.dajibot2.rewards.Reward;

/**
 * Class for handling database operations in the Diamond Seal table.
 * @author Darren
 *
 */
public class AccountDatabaseHandler {
	Connection conn = null;
	Statement stmt = null;

	/**
	 * Initializes the connection with the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public AccountDatabaseHandler() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:accounts.db");
		stmt = conn.createStatement();
		String sql = "CREATE TABLE IF NOT EXISTS Accounts " +
				"(ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" UserID          INTEGER    NOT NULL, " + //Discord ID of the user that the account belongs to
				" Coins           INTEGER    NOT NULL, " + //Amount of coins
				" Diamonds        INTEGER    NOT NULL, " + //Amount of diamonds
				" FriendPoints    INTEGER    NOT NULL, " + //Amount of friend points
				" Souls           INTEGER    NOT NULL, " + //Amount of souls
				" Inventory       STRING     NOT NULL)"; //Arrays.toString format of all card IDs in this user's inventory
		stmt.executeUpdate(sql);
		
		sql = "CREATE TABLE IF NOT EXISTS RegisteredUsers " +
				"(ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" UserID          INTEGER    NOT NULL)"; //User's Discord ID
		stmt.executeUpdate(sql);
		stmt.close();
	}

	/**
	 * Register a new user.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean registerNewUser(Account account) {
		try {	
			String sql = "INSERT INTO Accounts (UserID, Coins, Diamonds, FriendPoints, Souls, Inventory) " +
					"VALUES (" + account.getUser().getId() + ", " + account.getCoins() + ", " + account.getDiamonds() + ", " + account.getFriendPoints() + ", " + account.getSouls() + ", '" +  Arrays.toString(account.getInventoryAsArray()) + "');";
			stmt.executeUpdate(sql);
			
			sql = "INSERT INTO RegisteredUsers (UserID) " +
					"VALUES (" + account.getUser().getId() + ");";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Checks if a user is already registered.
	 * @return
	 */
	public boolean userAlreadyExists(User user) {
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM RegisteredUsers WHERE UserID=" + user.getId() + ";");

			while (rs.next()) {			
				return true;
			}

			return false;
		} catch (Exception e) {
			return true;
		}
	}
	
	/**
	 * Claim a reward.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean claimReward(User user, Reward reward) {
		try {	
			//TODO claim rewards
			String sql = "";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}

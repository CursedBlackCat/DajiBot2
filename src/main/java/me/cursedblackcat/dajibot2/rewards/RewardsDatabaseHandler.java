package me.cursedblackcat.dajibot2.rewards;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.javacord.api.entity.user.User;

import me.cursedblackcat.dajibot2.DajiBot;

/**
 * Class for handling database operations in the Diamond Seal table.
 * @author Darren
 *
 */
public class RewardsDatabaseHandler {
	Connection conn = null;
	Statement stmt = null;

	/**
	 * Initializes the connection with the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public RewardsDatabaseHandler() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:rewards.db");
		stmt = conn.createStatement();
		String sql = "CREATE TABLE IF NOT EXISTS Rewards " +
				"(ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" UserID          INTEGER    NOT NULL, " + //Discord ID of the user that the reward belongs to
				" ItemType        TEXT       NOT NULL, " + //What item type the reward is (diamonds, coins, etc)
				" Amount          INTEGER    NOT NULL, " + //How many of the item to give.
				" ExpiryDate      INTEGER    NOT NULL, " + //Unix timestamp of the time when this reward expires
				" CardID          INTEGER    NOT NULL, " + //Card ID, if reward is of type Card, or -1 if reward is not a card
				" Text            TEXT       NOT NULL)"; //Reward description text
		stmt.executeUpdate(sql);
		stmt.close();
	}

	/**
	 * Create a reward.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean addReward(Reward reward) {
		try {	
			String sql = "INSERT INTO Rewards (UserID, ItemType, Amount, ExpiryDate, CardID, Text) " +
					"VALUES (" + reward.getUser().getId() + ", '" + reward.getItemType() + "', '" + reward.getAmount() + "', '" + reward.getExpiryDate().getTime() + "', " + reward.getCardID() + ", '" + reward.getText() + "');";
			//System.out.println(sql);
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Remove a reward (due to it being claimed, it expiring, or otherwise)
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean removeReward(Reward reward) {
		try {
			String sql = "DELETE FROM Rewards WHERE ID = (SELECT ID from Rewards WHERE UserID=" + reward.getUser().getId() + " AND ItemType= '" + reward.getItemType() + "' AND Amount=" + reward.getAmount() + " AND ExpiryDate=" + reward.getExpiryDate().getTime() + " AND CardID=" + reward.getCardID() + " LIMIT 1);";
			//System.out.println(sql);
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Get all the rewards for a user.
	 * @return An ArrayList of all the rewards that a user has, or null if the rewards inbox could not be loaded.
	 * @throws SQLException
	 */
	public ArrayList<Reward> getRewardsForUser(User user) throws SQLException{
		try {
			long currentTime = new Date().getTime();
			ArrayList<Reward> rewards = new ArrayList<Reward>();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Rewards WHERE UserID=" + user.getId() + " AND ExpiryDate>=" + currentTime + ";");

			while (rs.next()) {			
				Reward reward = new Reward(DajiBot.getUserById(rs.getLong("UserID")), ItemType.valueOf(rs.getString("ItemType")), rs.getInt("Amount"), new Date(rs.getLong("ExpiryDate")), rs.getInt("CardID"), rs.getString("Text"));
				rewards.add(reward);
			}

			return rewards;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Deletes all the uncollected reward entries that have expired.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean clearExpiredRewards() {
		try {
			long currentTime = new Date().getTime();
			String sql = "DELETE FROM Rewards WHERE ExpiryDate<" + currentTime + ";";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}

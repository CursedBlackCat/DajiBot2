package me.cursedblackcat.dajibot2.accounts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import org.javacord.api.entity.user.User;

import me.cursedblackcat.dajibot2.diamondseal.DiamondSealCard;
import me.cursedblackcat.dajibot2.rewards.ItemType;
import me.cursedblackcat.dajibot2.rewards.Reward;
import me.cursedblackcat.dajibot2.rewards.RewardsDatabaseHandler;

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
				" UserID          INTEGER    NOT NULL," + // User's Discord ID
				" DailyRewardCollected INTEGER    NOT NULL)"; //0 if user hasn't collected daily diamond yet, 1 if they have. Resets to 0 at midnight
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

			sql = "INSERT INTO RegisteredUsers (UserID, DailyRewardCollected) " +
					"VALUES (" + account.getUser().getId() + ", 0);";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Get an Account for a given Discord user.
	 * @param user The Discord user.
	 * @return An Account with that Discord user's game account info.
	 */
	public Account getUserAccount(User user) {
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Accounts WHERE UserID=" + user.getId() + ";");

			rs.next();

			return new Account(user, rs.getInt("Coins"), rs.getInt("Diamonds"), rs.getInt("FriendPoints"), rs.getInt("Souls"), deserializeIntArray(rs.getString("Inventory")));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
			
			return rs.next();
		} catch (Exception e) {
			return true;
		}
	}
	
	/**
	 * Returns whether a user has collected their daily reward.
	 * @param user The Discord user to check.
	 * @return Whether the user has collected their daily reward or not.
	 */
	public boolean dailyRewardAlreadyCollected(User user) {
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM RegisteredUsers WHERE UserID=" + user.getId() + ";");
			return rs.getBoolean("DailyRewardCollected");
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	/**
	 * Marks a user has having collected their daily reward.
	 * @param user The Discord user to mark.
	 * @return True if the operation completed successfully, or false otherwise.
	 */
	public boolean collectDailyReward(User user) {
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE RegisteredUsers SET DailyRewardCollected = 1 WHERE UserID=" + user.getId() + ";");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Reset everyone's daily reward collection flag.
	 */
	public void resetDailyRewards() {
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE RegisteredUsers SET DailyRewardCollected = 0;");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a card to a user's inventory.
	 * @param user The user to give the card to.
	 * @param card The card to be added.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean addCardToAccount(User user, DiamondSealCard card) {
		Account userAccount = getUserAccount(user);
		ArrayList<Integer> inventory = userAccount.getInventory();
		try {
			inventory.add(DiamondSealCard.getCardIDFromName(card.getName()));
			String sql = "UPDATE Accounts SET Inventory = '" + Arrays.toString(inventory.toArray(new Integer[inventory.size()])) + "' WHERE UserID = " + user.getIdAsString();
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param user The user whose account the currency should be added to.
	 * @param currency The type of currency to add.
	 * @param amount The amount to add.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean addCurrencyToAccount(User user, ItemType currency, int amount) {
		try {
			String sql = "";
			switch (currency) {
			case DIAMOND:
				sql = "UPDATE Accounts SET Diamonds = Diamonds + " + amount + " WHERE UserID = " + user.getIdAsString() + ";";
				break;
			case COIN:
				sql = "UPDATE Accounts SET Coins = Coins + " + amount + " WHERE UserID = " + user.getIdAsString() + ";";
				break;
			case FRIEND_POINT:
				sql = "UPDATE Accounts SET FriendPoints = FriendPoints + " + amount + " WHERE UserID = " + user.getIdAsString() + ";";
				break;
			case SOUL:
				sql = "UPDATE Accounts SET Souls = Souls + " + amount + " WHERE UserID = " + user.getIdAsString() + ";";
				break;
			case CARD:
				throw new IllegalArgumentException("Cannot call method addCurrencyToAccount on an ItemType of CARD");
			}
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param user The user whose account the currency should be deducted from.
	 * @param currency The type of currency to deduct.
	 * @param amount The amount to deduct.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 * @throws InsufficientCurrencyException
	 */
	public boolean deductCurrencyFromAccount(User user, ItemType currency, int amount) throws InsufficientCurrencyException {
		Account userAccount = getUserAccount(user);

		try {
			String sql = "";
			switch (currency) {
			case DIAMOND:
				if (userAccount.getDiamonds() < amount) {
					throw new InsufficientCurrencyException();
				}
				sql = "UPDATE Accounts SET Diamonds = Diamonds - " + amount + " WHERE UserID = " + user.getIdAsString() + " AND Diamonds >= " + amount;
				break;
			case COIN:
				if (userAccount.getCoins() < amount) {
					throw new InsufficientCurrencyException();
				}
				sql = "UPDATE Accounts SET Coins = Coins - " + amount + " WHERE UserID = " + user.getIdAsString() + " AND Coins >= " + amount;
				break;
			case FRIEND_POINT:
				if (userAccount.getFriendPoints() < amount) {
					throw new InsufficientCurrencyException();
				}
				sql = "UPDATE Accounts SET FriendPoints = FriendPoints - " + amount + " WHERE UserID = " + user.getIdAsString() + " AND FriendPoints >= " + amount;
				break;
			case SOUL:
				if (userAccount.getSouls() < amount) {
					throw new InsufficientCurrencyException();
				}
				sql = "UPDATE Accounts SET Souls = Souls - " + amount + " WHERE UserID = " + user.getIdAsString() + " AND Souls >= " + amount;;
				break;
			case CARD:
				throw new IllegalArgumentException("Cannot call method deductCurrencyFromAccount on an ItemType of CARD");
			}
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Claim a reward.
	 * @param user The user whose account the reward should be added to
	 * @param reward Information about the reward to be added.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean claimReward(User user, Reward reward) {
		try {
			new RewardsDatabaseHandler().removeReward(reward);
			String sql = "";
			switch (reward.getItemType()) {
			case DIAMOND:
				sql = "UPDATE Accounts SET Diamonds = Diamonds + " + reward.getAmount() + " WHERE UserID = " + reward.getUser().getIdAsString();
				break;
			case COIN:
				sql = "UPDATE Accounts SET Coins = Coins + " + reward.getAmount() + " WHERE UserID = " + reward.getUser().getIdAsString();
				break;
			case FRIEND_POINT:
				sql = "UPDATE Accounts SET FriendPoints = FriendPoints + " + reward.getAmount() + " WHERE UserID = " + reward.getUser().getIdAsString();
				break;
			case SOUL:
				sql = "UPDATE Accounts SET Souls = Souls + " + reward.getAmount() + " WHERE UserID = " + reward.getUser().getIdAsString();
				break;
			case CARD:
				Account userAccount = getUserAccount(user);
				ArrayList<Integer> inventory = userAccount.getInventory();
				inventory.add(reward.getCardID());
				sql = "UPDATE Accounts SET Inventory = '" + Arrays.toString(inventory.toArray(new Integer[inventory.size()])) + "' WHERE UserID = " + user.getIdAsString();
				break;
			}
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static int[] deserializeIntArray(String string) {
		String[] strings = string.replace("[", "").replace("]", "").split(", ");
		int result[] = new int[strings.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(strings[i]);
		}
		return result;
	}

	/**
	 * Returns the user IDs of all registered users.
	 * @return An ArrayList of user IDs of all registered users.
	 * @throws SQLException 
	 */
	public ArrayList<Long> getAllUsers() throws SQLException {
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM RegisteredUsers;");
		
		ArrayList<Long> userIDs = new ArrayList<Long>();
		while (rs.next()) {
			userIDs.add(rs.getLong("UserID"));
		}
		
		return userIDs;
	}
}

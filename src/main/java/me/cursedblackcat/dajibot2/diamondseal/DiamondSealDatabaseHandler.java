package me.cursedblackcat.dajibot2.diamondseal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for handling database operations in the Diamond Seal table.
 * @author Darren
 *
 */
public class DiamondSealDatabaseHandler {
	Connection conn = null;
	Statement stmt = null;

	/**
	 * Initializes the connection with the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DiamondSealDatabaseHandler() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:diamondseal.db");
		stmt = conn.createStatement();
		String sql = "CREATE TABLE IF NOT EXISTS DiamondSeals " +
				"(ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" Name            TEXT    NOT NULL, " + //Name to be displayed. Example: "Fairy's Light"
				" CommandName     TEXT    NOT NULL, " + //Name to be used in the command. Example: "fairytail"
				" Entities        TEXT    NOT NULL, " + //Cards or series in the seal, in Arrays.toString format. Example: "["Salamander of Fire - Natsu", "Celestial Wizard - Lucy", "Fairy Queen Titania - Erza"]"
				" EntityType      TEXT    NOT NULL, " + //Whether the entities are "Card" or "Series".
				" Rates           TEXT    NOT NULL)"; //Card pull rates in Arrays.toString format, each number being the percent chance multiplied by 10. Should add up to 1000. Example: "[10, 45, 45, 180, 180, 180, 180, 180]" 
		stmt.executeUpdate(sql);
		stmt.close();
	}

	/**
	 * Add a new seal to the list of diamond seal boxes.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean addSeal(String name, String commandName, String[] entities, String entityType, int[] rates) {
		if (!(entityType.equalsIgnoreCase("Card") || entityType.equalsIgnoreCase("Series"))) {
			throw new IllegalArgumentException("entityType must be either \"Card\" or \"Series\".");
		}

		try {
			name = name.replaceAll("'", "''");
			String sql = "INSERT INTO DiamondSeals (Name, CommandName, Entities, EntityType, Rates) " +
					"VALUES ('" + name + "', '" + commandName + "', '" + Arrays.toString(entities) + "', '" + entityType + "', '" + Arrays.toString(rates) + "');";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Remove a seal from the list of diamond seal boxes.
	 * @return True if the operation completed successfully, or false if an exception occurred.
	 */
	public boolean removeSeal(String commandName) {
		try {
			String sql = "DELETE FROM DiamondSeals WHERE CommandName='" + commandName + "';";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Get all the saved diamond seal boxes.
	 * @return An ArrayList of all the diamond seal boxes saved in the database.
	 * @throws SQLException
	 */
	public ArrayList<DiamondSeal> getAllDiamondSeals() throws SQLException{
		ArrayList<DiamondSeal> seals = new ArrayList<DiamondSeal>();
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM DiamondSeals;");

		while (rs.next()) {			
			DiamondSealBuilder builder = new DiamondSealBuilder();
			builder.withName(rs.getString("Name"));
			builder.withCommandName(rs.getString("CommandName"));

			switch(rs.getString("EntityType")) {
			case "Card":
				String[] cardNames = toStringArray(rs.getString("Entities"));
				for (String name : cardNames) {
					builder.withCard(new DiamondSealCard(name));
				}
				break;
			case "Series":
				//TODO series implementation
				break;
			default:
				throw new IllegalArgumentException("EntityType in database must be either Card or Series");
			}

			int[] cardRates = toIntArray(rs.getString("Rates"));
			for (int rate : cardRates) {
				builder.withRate(rate);
			}

			DiamondSeal seal = builder.build();
			seals.add(seal);
		}

		return seals;
	}

	private static String[] toStringArray(String string) {
		String[] strings = string.replace("[", "").replace("]", "").split("\\s*,\\s*");
		String result[] = new String[strings.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = strings[i];
		}
		return result;
	}

	private static int[] toIntArray(String string) {
		String[] strings = string.replace("[", "").replace("]", "").split(", ");
		int result[] = new int[strings.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(strings[i]);
		}
		return result;
	}
}

package me.cursedblackcat.dajibot2.diamondseal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Class for handling database operations.
 * @author Darren
 *
 */
public class DiamondSealHandler {
	Connection conn = null;
	Statement stmt = null;

	/**
	 * Initializes the connection with the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DiamondSealHandler() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:diamondseal.db");
		stmt = conn.createStatement();
		String sql = "CREATE TABLE IF NOT EXISTS DiamondSeals " +
				"(ID INTEGER PRIMARY KEY AUTOINCREMENT     NOT NULL," +
				" Name            TEXT    NOT NULL, " + //Name to be displayed. Example: "Fairy's Light"
				" Command         TEXT    NOT NULL, " + //Name to be used in the command. Example: "fairytail"
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
			String sql = "INSERT INTO DiamondSeals (Name, Command, Entities, EntityType, Rates) " +
					"VALUES ('" + name + "', '" + commandName + "', '" + Arrays.toString(entities) + "', '" + entityType + "', '" + Arrays.toString(rates) + "');";
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}

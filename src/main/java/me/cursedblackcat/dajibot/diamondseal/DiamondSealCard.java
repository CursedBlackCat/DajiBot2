package me.cursedblackcat.dajibot.diamondseal;

import org.json.JSONObject;
import org.json.JSONTokener;

import me.cursedblackcat.dajibot.DajiBot;

public class DiamondSealCard extends DiamondSealEntity{
	public static String getCardNameFromID(int id) {
		JSONTokener tokener = null;
		try {
			tokener = new JSONTokener(DajiBot.class.getResourceAsStream("CardsIDToName.json"));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occurred when trying to load card name for card ID " + padZeroes(id) + ". Using card ID instead.");
			return "Card ID #" + padZeroes(id);
		}
		JSONObject cards = new JSONObject(tokener);
		return cards.getString(padZeroes(id));
	}
	
	public static int getCardIDFromName(String name) throws Exception {
		JSONTokener tokener = null;
		try {
			tokener = new JSONTokener(DajiBot.class.getResourceAsStream("CardsNameToID.json"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error occurred when trying to load card ID for card  " + name + ".");
		}
		JSONObject cards = new JSONObject(tokener);
		return cards.getInt(name);
	}
	
	private static String padZeroes(int id) {
		if (id < 0) {
			return String.valueOf(id);
		}
		
		if (id < 10) {
			return "00" + id;
		} else if (id >= 10 && id < 100) {
			return "0" + id;
		} else {
			return Integer.toString(id);
		}
	}
	
	public DiamondSealCard(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}

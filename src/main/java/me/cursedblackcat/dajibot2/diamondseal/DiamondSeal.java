package me.cursedblackcat.dajibot2.diamondseal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Represents a diamond seal machine/banner.
 * @author Darren Yip
 *
 */
public class DiamondSeal {
	private String name;
	private String commandName;
	private ArrayList<DiamondSealEntity> entities;
	private int[] rates; //Represents the percent draw rates of the entities of the corresponding index, multiplied by 10. For example, if rates[0] is 10, entities.get(0) has a pull chance of 1 percent. Should add up to 1000.
	private int[] rateRanges; //Used in random number selection. Each index is the sum of itself and all the previous indices. For example, if rates is {10, 45, 45, 145} then rateRanges is {10, 55, 100, 245}
	
	public DiamondSeal(String n, String c, ArrayList<DiamondSealEntity> ent, int[] r) {
		
		/*Check if rates add up to 100%*/
		int sum = 0;
		for(int i : r) {
		    sum += i;
		}
		
		if (sum != 1000) {
			throw new IllegalArgumentException("Diamond seal rate does not add up to 100%. Adds up to " + (sum / 10) + "%");
		}
		
		/*Assign the variables*/
		name = n;
		commandName = c;
		entities = ent;
		rates = r;
		
		/*Calculate the rate ranges to be used for gacha pulls*/
		rateRanges = new int[rates.length];
		rateRanges[0] = rates[0];
		
		for (int i = 1; i < rates.length; i++) {
			rateRanges[i] = rateRanges[i - 1] + rates[i];
		}
		
		if (rateRanges[rateRanges.length - 1] != 1000) {
			throw new IllegalArgumentException("Upper bound of rate range is not 1000 (100.0%). rateRanges was " + Arrays.toString(rateRanges));
		}
	}
	
	public String getCommandName() {
		return commandName;
	}
	
	public DiamondSealCard drawFromMachine() {
		Random r = new Random();
		int randNum = r.nextInt(1000); //random int from 0 to 999, inclusive
		
		for (int i = 0; i < rateRanges.length; i++) {
			if (randNum < rateRanges[i]) {
				DiamondSealEntity result = entities.get(i);
				if (result instanceof DiamondSealCard) {
					return (DiamondSealCard) result;
				} else {
					return ((DiamondSealSeries) result).getRandomCard();
				}
			}
		}
		
		return new DiamondSealCard("An error has occurred.");
	}
	
	public ArrayList<DiamondSealEntity> getEntities(){
		return entities;
	}
	
	public String[] getEntityNames() {
		String[] names = new String[entities.size()];
		for (int i = 0; i < entities.size(); i++) {
			names[i] = entities.get(i).getName();
		}
		return names;
	}
	
	public String getInfo() {
		String response = "**" + name + "**\n\n";
		
		for (int i = 0; i < entities.size(); i++) {
			response += entities.get(i).getName() + " - " + (double) rates[i] / 10 + "%\n";
		}
		
		return response;
	}
	
	public int[] getRates() {
		return rates;
	}

	public String getName() {
		return name;
	}
}

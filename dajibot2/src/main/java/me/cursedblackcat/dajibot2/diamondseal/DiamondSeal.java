package me.cursedblackcat.dajibot2.diamondseal;

import java.util.ArrayList;
import java.util.Random;

public class DiamondSeal {
	private ArrayList<DiamondSealEntity> entities;
	private int[] rates; //Represents the percent draw rates of the entities of the corresponding index, multiplied by 10. For example, if rates[0] is 10, entities.get(0) has a pull chance of 1 percent. Should add up to 1000.
	private int[] rateRanges; //Used in random number selection. Each index is the sum of itself and all the previous indices. For example, if rates is {10, 45, 45, 145} then rateRanges is {10, 55, 100, 245}
	
	public DiamondSeal(ArrayList<DiamondSealEntity> ent, int[] r) {
		entities = ent;
		rates = r;
	}
	
	public Card drawFromMachine() {
		Random r = new Random();
		int randNum = r.nextInt(1000); //random int from 0 to 999, inclusive
		
		for (int i = 0; i < rateRanges.length; i++) {
			if (randNum < rateRanges[i]) {
				DiamondSealEntity result = entities.get(i);
				if (result instanceof Card) {
					return (Card) result;
				} else {
					return ((Series) result).getRandomCard();
				}
			}
		}
		
		return new Card("An error has occurred.");
	}
}

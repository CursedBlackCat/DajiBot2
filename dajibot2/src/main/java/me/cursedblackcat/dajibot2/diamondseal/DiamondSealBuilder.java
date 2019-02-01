package me.cursedblackcat.dajibot2.diamondseal;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility class to construct DiamondSeal objects.
 * @author Darren Yip
 *
 */
public class DiamondSealBuilder {
	private ArrayList<DiamondSealEntity> entities;
	private ArrayList<Integer> rates; //Represents the percent draw rates of the entities of the corresponding index, multiplied by 10. For example, if rates[0] is 10, entities.get(0) has a pull chance of 1 percent. Should add up to 1000.
	
	public void withSeries(Series series, int rate) {
		if (hasCard()) {
			throw new IllegalStateException("Diamond seal already has cards; cannot add series.");
		}
		entities.add(series);
		rates.add(rate);
	}
	
	public void withCard(Card card, int rate) {
		if (hasSeries()) {
			throw new IllegalStateException("Diamond seal already has series; cannot add card.");
		}
		entities.add(card);
		rates.add(rate);
	}
	
	public DiamondSeal build() {
		return new DiamondSeal(entities, toPrimitive(rates));
	}
	
	public boolean hasSeries() {
		for (DiamondSealEntity ent : entities) {
			if (ent instanceof Series) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasCard() {
		for (DiamondSealEntity ent : entities) {
			if (ent instanceof Card) {
				return true;
			}
		}
		
		return false;
	}
	
	private int[] toPrimitive(ArrayList<Integer> integers) {
	    int[] ret = new int[integers.size()];
	    Iterator<Integer> iterator = integers.iterator();
	    for (int i = 0; i < ret.length; i++) {
	        ret[i] = iterator.next().intValue();
	    }
	    return ret;
	}
}

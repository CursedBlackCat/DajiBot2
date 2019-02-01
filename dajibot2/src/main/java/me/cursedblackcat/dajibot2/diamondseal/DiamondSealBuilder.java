package me.cursedblackcat.dajibot2.diamondseal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Utility class to construct DiamondSeal objects.
 * @author Darren Yip
 *
 */
public class DiamondSealBuilder {
	String name;
	String commandName;
	private ArrayList<DiamondSealEntity> entities = new ArrayList<DiamondSealEntity>();;
	private ArrayList<Integer> rates = new ArrayList<Integer>(); //Represents the percent draw rates of the entities of the corresponding index, multiplied by 10. For example, if rates[0] is 10, entities.get(0) has a pull chance of 1 percent. Should add up to 1000.
	
	public void withName(String n) {
		name = n;
	}
	
	public void withCommandName(String c) {
		commandName = c;
	}
	
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
	
	/*The below two methods should be used in such a way that cards and rates of any given index match!*/
	public void withCard(Card card) {
		if (hasSeries()) {
			throw new IllegalStateException("Diamond seal already has series; cannot add card.");
		}
		entities.add(card);
	}
	public void withRate(int rate) {
		rates.add(rate);
	}
	/*The above two methods should be used in such a way that cards and rates of any given index match!*/
	
	public DiamondSeal build() {
		if (entities.size() != rates.size()) {
			throw new IllegalStateException("Entity list length does not match rate list length");
		}
		return new DiamondSeal(name, commandName, entities, toPrimitive(rates));
	}
	
	public boolean hasSeries() {
		if (entities.size() == 0) {
			return false;
		}
		
		for (DiamondSealEntity ent : entities) {
			if (ent instanceof Series) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasCard() {
		if (entities.size() == 0) {
			return false;
		}
		
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

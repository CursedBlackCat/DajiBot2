package me.cursedblackcat.dajibot2.diamondseal;

import java.util.ArrayList;
import java.util.Random;

public class Series extends DiamondSealEntity {
	private ArrayList<Card> cards;

	public Series(String name, ArrayList<Card> cards) {
		this.name = name;
		this.cards = cards;
	}

	public Card getRandomCard() {
		Random r = new Random();
		return cards.get(r.nextInt(5));
	}
}

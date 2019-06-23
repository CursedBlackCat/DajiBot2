package me.cursedblackcat.dajibot.diamondseal;

import java.util.ArrayList;
import java.util.Random;

public class DiamondSealSeries extends DiamondSealEntity {
	private ArrayList<DiamondSealCard> cards;

	public DiamondSealSeries(String name, ArrayList<DiamondSealCard> cards) {
		this.name = name;
		this.cards = cards;
	}

	public DiamondSealCard getRandomCard() {
		Random r = new Random();
		return cards.get(r.nextInt(5));
	}
}

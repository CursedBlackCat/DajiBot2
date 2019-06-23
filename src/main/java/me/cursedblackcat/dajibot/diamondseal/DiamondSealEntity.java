package me.cursedblackcat.dajibot.diamondseal;

/**
 * Represents a result of a diamond seal pull. Can be either a card (for collab seals) or a series (for engraver and carver seals)
 * @author Darren Yip
 *
 */
public abstract class DiamondSealEntity {
	protected String name;
	
	public String getName() {
		return name;
	}
}

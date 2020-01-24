package seng202.team4.Model;

/**
 * Class to store data about a single retailer.
 */
public class Retailer extends Location {

	private String primaryType, secondaryType, street, state;
	private int block, lot, zipCode;

	/**
	 * Constructs a new retailer.
	 *
	 * @param name The name of the retailer
	 * @param city The city the retailer is in
	 * @param id The primary key id of this retailer
	 * @param latitude The latitude coordinate of this retailer
	 * @param longitude The longitude coordinate of this retailer
	 * @param street The street address of the retailer
	 * @param state The state the retailer is in
	 * @param zipCode The zip code of the area the retailer is in
	 * @param primaryType The general type of retailer
	 * @param secondaryType A more specific type specification for this retailer
	 * @param block The block this retailer is in
	 * @param lot The lot this retailer is in
	 */
	public Retailer(String name, String city, int id, double latitude, double longitude,
		String street, String state, int zipCode, String primaryType, String secondaryType,
		int block, int lot, boolean favourite) {
		super(name, city, id, latitude, longitude, favourite);
		this.street = street;
		this.state = state;
		this.zipCode = zipCode;
		this.primaryType = primaryType;
		this.secondaryType = secondaryType;
		this.block = block;
		this.lot = lot;
	}

	public String getName() {
		return name;
	}

	public String getStreet() {
		return street;
	}

	public String getState() {
		return state;
	}

	public int getZipCode() {
		return zipCode;
	}

	public String getPrimaryType() {
		return primaryType;
	}

	public String getSecondaryType() {
		return secondaryType;
	}

	public int getBlock() {
		return block;
	}

	public int getLot() {
		return lot;
	}

	public void setName(String newName) {
		name = newName;
	}

	public void setPrimaryType(String newPrmType) {
		primaryType = newPrmType;
	}

	public void setSecondaryType(String newSecType) {
		secondaryType = newSecType;
	}

	public void setBlock(int newBlock) {
		block = newBlock;
	}

	public void setLot(int newLot) {
		lot = newLot;
	}

	public String toString() {
		String infoString = String.format("%s at %s (block: %d, lot: %d), %s, %s%s. Type: %s %s",
			name, street, block, lot, city, state, "%s", primaryType, secondaryType);
		if (zipCode != 0) {
			infoString = String.format(infoString, " " + zipCode);
		} else {
			infoString = String.format(infoString, "");
		}
		return infoString;
	}

	/**
	 * Returns a string which can be used for geocoding queries.
	 *
	 * @return The geocoding-usable string
	 */
	@Override
	public String locationString() {
		return String.format("%s, %s, %s %d", street, city, state, zipCode);
	}

}
package seng202.team4.Model;

/**
 * Class to store data about a specific wifi hot spot.
 */
public class HotSpot extends Location {

	private String borough, locationInfo, locationType, policy, policyDescription, provider;

	/**
	 * Construct a new hot spot.
	 *
	 * @param name The name (SSID) of the hot spot
	 * @param city The city the hot spot is in
	 * @param id The primary key id of this hot spot
	 * @param latitude The latitude coordinate of this hot spot
	 * @param longitude The longitude coordinate of this hot spot
	 * @param borough The borough this hot spot is in
	 * @param locationInfo Info about this hot spot that would be useful for google searching
	 * @param locationType The type of location (eg. Outdoor)
	 * @param policy The policy this hot spot uses (eg. Free)
	 * @param policyDescription A more in-depth description of the policy
	 * @param provider The provider of this hot spot
	 */
	public HotSpot(String name, String city, int id, double latitude, double longitude,
		String borough, String locationInfo, String locationType, String policy,
		String policyDescription, String provider, boolean favourite) {
		super(name, city, id, latitude, longitude, favourite);
		this.borough = borough;
		this.locationInfo = locationInfo;
		this.locationType = locationType;
		this.policy = policy;
		this.policyDescription = policyDescription;
		this.provider = provider;
	}

	public String getBorough() {
		return borough;
	}

	public String getLocationInfo() {
		return locationInfo;
	}

	public String getLocationType() {
		return locationType;
	}

	public String getPolicy() {
		return policy;
	}

	public String getPolicyDescription() {
		return policyDescription;
	}

	public String getProvider() {
		return provider;
	}

	public void setName(String newName) {
		name = newName;
	}

	public void setBorough(String newBorough) {
		borough = newBorough;
	}

	public void setLocationType(String newType) {
		locationType = newType;
	}

	public void setPolicy(String newPolicy) {
		policy = newPolicy;
	}

	public void setPolicyDescription(String newDescription) {
		policyDescription = newDescription;
	}

	public void setProvider(String newProvider) {
		provider = newProvider;
	}

	public String toString() {
		return String.format("%s at %s (borough: %s) in %s. Provided by %s with policy %s %s",
			name, locationInfo, borough, city, provider, policy, policyDescription);
	}

	/**
	 * Returns a string which can be used for geocoding queries.
	 *
	 * @return The geocoding-usable string
	 */
	@Override
	public String locationString() {
		return String.format("%s, %s, %s", locationType, borough, city);
	}
}

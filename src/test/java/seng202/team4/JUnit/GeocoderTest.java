package seng202.team4.JUnit;

import org.junit.Test;
import seng202.team4.Model.Retailer;
import seng202.team4.Services.Geocoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GeocoderTest {

	//Test to test if the latLongGetter returns the right latitude and longitude.
	@Test
	public void testLatLongGetter() {
		Retailer googleBuilding = new Retailer("Google", "Mountain View", 0, 0, 0,
			"1600 Amphitheatre Parkway", "CA", 94043, "", "", 0, 0, false);
		System.out.println(googleBuilding.locationString());
		double[] latLong = Geocoder.getLatitudeLongitude(googleBuilding.locationString());
		System.out.println(latLong[2]);

		assertEquals(37.421200, latLong[0], 0.01);
		assertEquals(-122.077264, latLong[1], 0.01);
	}

	//Test to test if the locationGetter returns a location string containing the same zip code as the expected one
	@Test
	public void testLocationGetter() {
		Retailer googleBuilding = new Retailer("Google", "Mountain View", 0, 0, 0,
			"1600 Amphitheatre Parkway", "CA", 94043, "", "", 0, 0, false);
		System.out.println(googleBuilding.locationString());
		double[] latLong = Geocoder.getLatitudeLongitude(googleBuilding.locationString());
		String location = Geocoder.getLocation(latLong[0], latLong[1]);
		if (location == null) {
			//Google failed to return route, API key delay or connection issue?
			fail();
		}
		assertEquals(true,
			location.toLowerCase().contains(("" + googleBuilding.getZipCode()).toLowerCase()));

	}

	//Test to test if the distanceGetter returns the right distance in metres.
	@Test
	public void testDistanceGetter() {
		assertEquals(10423,
			Geocoder.getDistance(40.6655101, -73.89188969999998, 40.6905615, -73.9976592), 0);
	}
}
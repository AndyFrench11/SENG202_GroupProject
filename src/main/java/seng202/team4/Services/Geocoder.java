package seng202.team4.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class containing static methods offering geocoding functionality.
 */
public class Geocoder {

	private static final String[] apiKeys = { "AIzaSyBf7eOzw50Nwh7c_bDe6bhAyEdPY5-49q0" };
	private static int currentKey = 0;

	/**
	 * Increment api key to use
	 */
	private static void incrementKey() {
		currentKey++;
		if (currentKey >= apiKeys.length) {
			currentKey = 0;
		}
	}

	/**
	 * Get latitude and longitude coordinates based on a string location.
	 *
	 * @param location The string to query by
	 * @return The latitude and longitude in a double array
	 */
	public static double[] getLatitudeLongitude(String location) {
		int count = 0;
		double zipcode = 0;
		double longitude = 0, latitude = 0;
		try {
			URL url = new URL(
				String.format(
					"https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
					location.replaceAll(" ", "+"), apiKeys[currentKey]));
			BufferedReader br = new BufferedReader(
				new InputStreamReader(url.openConnection().getInputStream()));
			incrementKey();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.toLowerCase().contains("lat".toLowerCase())) {
					Pattern p = Pattern.compile("\\d+\\.\\d+");
					Matcher m = p.matcher(line);
					while (m.find()) {
						latitude = Double.parseDouble(m.group());
					}
					if (line.contains("-")) {
						latitude = 0 - latitude;
					}
				} else if (line.toLowerCase().contains("lng".toLowerCase())) {
					Pattern p = Pattern.compile("\\d+\\.\\d+");
					Matcher m = p.matcher(line);
					while (m.find()) {
						longitude = Double.parseDouble(m.group());
					}
					if (line.contains("-")) {
						longitude = 0 - longitude;
					}
				} else if (count == 41) {
					Pattern p = Pattern.compile("\\d+\\d+");
					Matcher m = p.matcher(line);
					while (m.find()) {
						zipcode = Double.parseDouble(m.group());
					}
				}
				count++;
			}
		} catch (IOException e) {
			System.out.println("Error getting lat and long, setting to 0.");
			latitude = 0;
			longitude = 0;
		}
		double[] latLong = new double[3];
		latLong[0] = latitude + 0.0013170000000002346;
		latLong[1] = longitude + 0.001360000000005357;
		latLong[2] = zipcode;
		return latLong;
	}

	/**
	 * Get distance between two locations using their latitudes and longitudes
	 *
	 * @param originLat The latitude of the origin point
	 * @param originLng The longitude of the origin point
	 * @param destLat The latitude of the destination point
	 * @param destLng the longitude of the destination point
	 * @return The distance in metres as an integer
	 */
	public static int getDistance(double originLat, double originLng, double destLat,
		double destLng) {
		int distance = 0;
		try {
			URL url = new URL(
				"https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="
					+ originLat + "," + originLng + "&destinations=" + destLat + "," + destLng
					+ "&key=" + apiKeys[currentKey]);
			BufferedReader br = new BufferedReader(
				new InputStreamReader(url.openConnection().getInputStream()));
			incrementKey();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.toLowerCase().contains("value".toLowerCase())) {
					Pattern p = Pattern.compile("\\d+\\d+");
					Matcher m = p.matcher(line);
					while (m.find()) {
						distance = Integer.parseInt(m.group());
					}
				} else if (line.toLowerCase().contains("duration".toLowerCase())) {
					break;
				}

			}
		} catch (IOException e) {
			System.out.println("Error getting distance, setting to 0.");
			distance = 0;
		}
		return distance;
	}

	/**
	 * Get string detailing location using the latitude and longitude.
	 *
	 * @param lat the latitude of the location
	 * @param lng the longitude of the location
	 * @return A string detailing the location i.e street address and city
	 */
	public static String getLocation(double lat, double lng) {
		String location = null;
		try {
			URL url = new URL(
				"https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng
					+ "&key=" + apiKeys[currentKey]);
			BufferedReader br = new BufferedReader(
				new InputStreamReader(url.openConnection().getInputStream()));
			//incrementKey();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.toLowerCase().contains("formatted_address".toLowerCase())) {
					location = line.substring(line.indexOf(":") + 1, line.lastIndexOf(","));
					break;
				}
			}
			System.out.println(location);
			if (location == null) {
				return null;
			}
			location = location.substring(location.indexOf("\"") + 1, location.lastIndexOf("\""));
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
		return location;
	}
}



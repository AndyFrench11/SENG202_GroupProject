package seng202.team4.Model;

import seng202.team4.Services.Geocoder;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Class to store data about a specific bike trip.
 */
public class BikeTrip {

	private double startLatitude, startLongitude, endLatitude, endLongitude;
	private LocalDateTime startTime, endTime;
	private int bikeId, id;
	private long duration;
	private String gender;
	private double distance, avgSpeed, caloriesBurnt, avgRating;
	private ArrayList<Integer> ratings;
	private ArrayList<double[]> otherCoords;
	private boolean favourite;

	/**
	 * Constructs a new bike trip
	 *
	 * @param bikeId The id of the bike used
	 * @param id The primary key id of the bike trip
	 * @param startLatitude The latitude of the start location
	 * @param startLongitude The longitude of the end location
	 * @param endLatitude The latitude of the end location
	 * @param endLongitude The longitude of the end location
	 * @param otherCoords Other coordinates along the bike trip
	 * @param startTime The start date and time of the bike trip
	 * @param endTime The end date and time of the bike trip
	 * @param duration The duration of the trip
	 * @param gender The gender of the cyclist
	 * @param distance The distance of the bike trip in kilometres
	 * @param avgSpeed The average speed the cyclist travelled at on the bike trip
	 * @param caloriesBurnt The approximate number of calories burnt travelling this bike trip
	 * @param avgRating The average rating of this bike trip
	 * @param ratings All of the ratings for this bike trip
	 * @param favourite Whether this bike trip is a favourite
	 */
	public BikeTrip(int bikeId, int id, double startLatitude, double startLongitude,
		double endLatitude, double endLongitude, ArrayList<double[]> otherCoords,
		LocalDateTime startTime, LocalDateTime endTime, long duration, String gender,
		double distance, double avgSpeed, double caloriesBurnt, double avgRating,
		ArrayList<Integer> ratings, boolean favourite) {
		this.bikeId = bikeId;
		this.id = id;
		this.startLatitude = startLatitude;
		this.startLongitude = startLongitude;
		this.endLatitude = endLatitude;
		this.endLongitude = endLongitude;
		this.otherCoords = otherCoords;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.gender = gender;
		this.distance = distance;
		this.avgSpeed = avgSpeed;
		this.caloriesBurnt = caloriesBurnt;
		this.avgRating = avgRating;
		this.ratings = ratings;
		this.favourite = favourite;
	}

	public int getBikeId() {
		return bikeId;
	}

	public int getId() {
		return id;
	}

	public double getStartLatitude() {
		return startLatitude;
	}

	public double getStartLongitude() {
		return startLongitude;
	}

	public double getEndLatitude() {
		return endLatitude;
	}

	public double getEndLongitude() {
		return endLongitude;
	}

	public ArrayList<double[]> getOtherCoords() {
		return otherCoords;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public long getDuration() {
		return duration;
	}

	public String getGender() {
		return gender;
	}

	public double getDistance() {
		return distance;
	}

	public double getAvgSpeed() {
		return avgSpeed;
	}

	public double getCaloriesBurnt() {
		return caloriesBurnt;
	}

	public double getAvgRating() {
		return avgRating;
	}

	public ArrayList<Integer> getRatings() {
		return ratings;
	}

	public boolean isFavourite() {
		return favourite;
	}

	/**
	 * Recalculate the distance and update avgSpeed and caloriesBurnt accordingly.
	 *
	 * @return A string usable to update the sql database stored entry corresponding to this bike
	 * trip
	 */
	public String calcStats() {
		switch (otherCoords.size()) {
			case 0:
				distance = Geocoder.getDistance(startLatitude, startLongitude, endLatitude,
					endLongitude);
				break;
			case 1:
				distance = Geocoder.getDistance(startLatitude, startLongitude,
					otherCoords.get(0)[0], otherCoords.get(0)[1]) + Geocoder.getDistance(
					otherCoords.get(0)[0], otherCoords.get(0)[1], endLatitude, endLongitude);
				break;
			default:
				int size = otherCoords.size() - 1;
				distance = Geocoder.getDistance(startLatitude, startLongitude,
					otherCoords.get(0)[0], otherCoords.get(0)[1]) + Geocoder.getDistance(
					otherCoords.get(size)[0], otherCoords.get(size)[1], endLatitude, endLongitude);
				for (int i = 0; i < size; i++) {
					distance += Geocoder.getDistance(otherCoords.get(i)[0], otherCoords.get(i)[1],
						otherCoords.get(i + 1)[0], otherCoords.get(i + 1)[1]);
				}
		}
		distance /= 1000; //Convert to kilometres

		avgSpeed = distance / duration * 60; //Km/h

		if (gender.equals('F')) {
			caloriesBurnt = 1089.9 * 7.5/24 * (((double)duration)/60);
		} else {
			caloriesBurnt = 1272.65 * 7.5 / 24 * (((double)duration) /60);
		}
		return String.format("distance = %.2f, avgSpeed = %.2f, caloriesBurnt = %.2f",
			distance, avgSpeed, caloriesBurnt);
	}


	public void setStartTime(LocalDateTime newStartTime) {
		startTime = newStartTime;
		duration = ChronoUnit.MINUTES.between(startTime, endTime);
	}

	public void setEndTime(LocalDateTime newEndTime) {
		endTime = newEndTime;
		duration = ChronoUnit.MINUTES.between(startTime, endTime);
	}

	public void setGender(String newGender) {
		gender = newGender;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}


	public void addRating(int newRating) {
		ratings.add(newRating);
		double sum = 0;
		for (int rating : ratings) {
			sum += rating;
		}
		DecimalFormat df = new DecimalFormat("#.##");
		String avg = df.format(sum / ratings.size());
		avgRating = Double.parseDouble(avg);
	}

	public void addOtherCoord(double[] newCoord) {
		otherCoords.add(newCoord);
	}

	public String toString() {
		return String
			.format("Bike ID: %d, start (%.3f, %.3f), end (%.3f, %.3f), duration %d minutes, "
					+ "gender %s, distance %f", bikeId, startLatitude, startLongitude, endLatitude,
				endLongitude, duration, gender, distance);
	}

}

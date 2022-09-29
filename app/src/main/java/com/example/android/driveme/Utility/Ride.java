package com.example.android.driveme.Utility;

public class Ride {

    private double startRideLatitude;
    private double endRideLatitude;
    private double startRideLongitude;
    private double endRideLongitude;
    private User rider;
    private String hour;
    private String minute;
    private String vehicle;
    private int numPassengers;

    public Ride(){

    }

    public Ride(double startRideLatitude, double startRideLongitude, double endRideLatitude, double endRideLongitude, User rider, String hour, String minute, String vehicle, int numPassengers) {
        this.startRideLatitude = startRideLatitude;
        this.endRideLatitude = endRideLatitude;
        this.startRideLongitude = startRideLongitude;
        this.endRideLongitude = endRideLongitude;
        this.rider = rider;
        this.hour = hour;
        this.minute = minute;
        this.vehicle = vehicle;
        this.numPassengers = numPassengers;
    }

    public double getStartRideLatitude() {
        return startRideLatitude;
    }

    public void setStartRideLatitude(double startRideLatitude) {
        this.startRideLatitude = startRideLatitude;
    }

    public double getEndRideLatitude() {
        return endRideLatitude;
    }

    public void setEndRideLatitude(double endRideLatitude) {
        this.endRideLatitude = endRideLatitude;
    }

    public double getStartRideLongitude() {
        return startRideLongitude;
    }

    public void setStartRideLongitude(double startRideLongitude) {
        this.startRideLongitude = startRideLongitude;
    }

    public double getEndRideLongitude() {
        return endRideLongitude;
    }

    public void setEndRideLongitude(double endRideLongitude) {
        this.endRideLongitude = endRideLongitude;
    }


    public User getRider() {
        return rider;
    }

    public void setRider(User rider) {
        this.rider = rider;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public int getNumPassengers() {
        return numPassengers;
    }

    public void setNumPassengers(int numPassengers) {
        this.numPassengers = numPassengers;
    }

    public void printRide(){
        System.out.println("startRideLatitude: " + getStartRideLatitude());
        System.out.println("startRideLongitude: " + getStartRideLongitude());
        System.out.println("endRideLatitude: " + getEndRideLatitude());
        System.out.println("endRideLongitude: " + getEndRideLongitude());
        System.out.println("riderUID: " + rider.getUID());
    }
}

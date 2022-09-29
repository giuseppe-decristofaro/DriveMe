package com.example.android.driveme.Utility;

public class RequestRide {

    private User passenger;
    private Ride ride;

    public RequestRide(){
    }

    public RequestRide(User passenger, Ride ride) {
        this.passenger = passenger;
        this.ride = ride;
    }

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }
}

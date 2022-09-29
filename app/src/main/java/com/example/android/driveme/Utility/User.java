package com.example.android.driveme.Utility;

import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class User {

    private String UID;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private double points;
    private String photoUrl;

    public User() {
    }

    public User(String UID, String name, String surname, String email, String phone, double points) {
        this.UID = UID;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.points = points;
        this.photoUrl = "";
    }

    public User(String UID, String name, String surname, String email, String phone, double points, String photoUrl) {
        this.UID = UID;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.points = points;
        this.photoUrl = photoUrl;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getPoints() { return points; }

    public void setPoints(double points) { this.points = points; }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

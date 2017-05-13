package com.nicefontaine.matcha.data;


import android.location.Location;


public class OnLocationEvent {

    private Location location;

    public OnLocationEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}

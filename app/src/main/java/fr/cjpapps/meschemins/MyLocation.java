package fr.cjpapps.meschemins;

import android.location.Location;

public class MyLocation extends Location {
    private double latitude;
    private double longitude;
    private float accuracy;
    private double altitude;

    public MyLocation() {
        super("");
    }

    MyLocation addLocationFraction (Location location, int filterLenght){
        this.latitude = this.latitude + location.getLatitude()/filterLenght;
        this.longitude = this.longitude + location.getLongitude()/filterLenght;
        this.accuracy = this.accuracy + location.getAccuracy()/filterLenght;
        this.altitude = this.altitude + location.getAltitude()/filterLenght;
        return this;
    }

    MyLocation substractLocationFraction (Location location, int filterLenght){
        this.latitude = this.latitude - location.getLatitude()/filterLenght;
        this.longitude = this.longitude - location.getLongitude()/filterLenght;
        this.accuracy = this.accuracy - location.getAccuracy()/filterLenght;
        this.altitude = this.altitude - location.getAltitude()/filterLenght;
        return this;
    }
}

package com.example.shishir.locationhack.Model_Class;

/**
 * Created by Shishir on 11/30/2017.
 */

public class LatLongClass {
    private String lat;
    private String Long;

    public String getLat() {
        return lat;
    }

    public String getLong() {
        return Long;
    }

    public LatLongClass(String lat, String aLong) {

        this.lat = lat;
        Long = aLong;
    }
}

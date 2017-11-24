package com.example.shishir.locationhack.Model_Class;

/**
 * Created by Shishir on 11/24/2017.
 */

public class MyLocation {
    private String day;
    private String time;
    private String lat;
    private String lng;

    public MyLocation(String day, String time, String lat, String lng) {
        this.day = day;
        this.time = time;
        this.lat = lat;
        this.lng = lng;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}

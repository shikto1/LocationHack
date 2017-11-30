package com.example.shishir.locationhack.Model_Class;

import java.io.Serializable;

/**
 * Created by Shishir on 11/24/2017.
 */

public class MyLocation implements Serializable {
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
    public MyLocation(String day, String time) {
        this.day = day;
        this.time = time;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLng(String lng) {
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

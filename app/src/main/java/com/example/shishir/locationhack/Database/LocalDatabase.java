package com.example.shishir.locationhack.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

/**
 * Created by Shishir on 11/23/2017.
 */

public class LocalDatabase {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    public LocalDatabase(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("LOCATION_HACKER", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLoggedIn(boolean b) {
        editor.putBoolean("log", b).commit();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean("log", false);
    }

}

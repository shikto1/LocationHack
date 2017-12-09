package com.example.shishir.locationhack.Location;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.shishir.locationhack.Database.DatabaseManager;
import com.example.shishir.locationhack.Database.LocalDatabase;
import com.example.shishir.locationhack.ExtraClass.Network;
import com.example.shishir.locationhack.Model_Class.MyLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Shishir on 11/24/2017.
 */

public class LatLongFinder extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    private static Location lastLocation = null;
    FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;
    LocalDatabase localDatabase;
    Calendar calendar = Calendar.getInstance();
    FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private static int INTERVAL = 10 * 60 * 1000;// This is the interval for checking Location.
    private static int R = 6371000;
    SimpleDateFormat dayFormatter = new SimpleDateFormat("dd MMMM"); //The format of Day
    SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");// the format of Time
    private static boolean networkRecReg = false;
    private DatabaseManager databaseManager;


    @Override
    public void onCreate() {
        toast("Started");
        localDatabase = new LocalDatabase(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        databaseManager = new DatabaseManager(this);
        setUpGoogleAPIClient();
        if (!networkRecReg) {
            registerReceiver(networkStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            networkRecReg = true;
        }
        super.onCreate();
    }

    protected synchronized void setUpGoogleAPIClient() {

        //Making Google API Client for getting Location...............................

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location newLocation) {
        Toast.makeText(getApplicationContext(), "called ", Toast.LENGTH_SHORT).show();
        if (newLocation != null) {
            if (lastLocation != null) {
                double lat1 = lastLocation.getLatitude();
                double lng1 = lastLocation.getLongitude();
                double lat2 = newLocation.getLatitude();
                double lng2 = newLocation.getLongitude();
                double distance = meterDistanceBetweenPoints(lat1, lng1, lat2, lng2);
                double distanceInM = SphericalUtil.computeDistanceBetween(new LatLng(lat1, lng1), new LatLng(lat2, lng2));
                Toast.makeText(this, "" + distanceInM, Toast.LENGTH_SHORT).show();
                if (distance > 3000) {
                    if (Network.isNetAvailable(getApplicationContext())) {

                        //If There is Network Connection I will save location in Firebase Database........................................
                        lastLocation = newLocation;
                        Toast.makeText(this, "I am saving", Toast.LENGTH_SHORT).show();
                        addLocationToFireBase(mAuth.getCurrentUser(), lat2, lng2);
                    } else {

                        //If there is no Internet Connection I Will save location in SQLite Database.....................................
                        Date date = new Date();
                        MyLocation location = new MyLocation(dayFormatter.format(date), timeFormatter.format(date), String.valueOf(lat2), String.valueOf(lng2));
                        databaseManager.addLocation(location);
                    }
                } else
                    Toast.makeText(this, "Distance is less than 3Km", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Last Location is Empty !", Toast.LENGTH_SHORT).show();
                lastLocation = newLocation;
            }
        }
    }

    @Override
    public void onDestroy() {
        toast("Destroyed");
        if (googleApiClient.isConnected()) {
            locationProviderApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
            if (networkRecReg) {
                networkRecReg = false;
                unregisterReceiver(networkStateReceiver);
            }
        }
        super.onDestroy();
    }

    private void addLocationToFireBase(FirebaseUser user, Double lat, Double lng) {
        if (user != null) {
            String dayMonth = dayFormatter.format(new Date());
            String time = timeFormatter.format(new Date());
            databaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(dayMonth).child(time);
            //For Single Value Input.............
            // databaseReference.child('name').setValue(name).addOnCompletionListener.............................

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Lat", String.valueOf(lat));
            map.put("Long", String.valueOf(lng));
            databaseReference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LatLongFinder.this, "Saved", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


    private double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = (float) (180.f / Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    public static double distanceInMeter(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();


    }

    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                //Connected....................
                Toast.makeText(LatLongFinder.this, "Connected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LatLongFinder.this, "Disconnected", Toast.LENGTH_SHORT).show();
                //No connection................
            }
        }
    };

}

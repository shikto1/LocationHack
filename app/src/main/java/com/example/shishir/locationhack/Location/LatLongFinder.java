package com.example.shishir.locationhack.Location;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.example.shishir.locationhack.Database.DatabaseManager;
import com.example.shishir.locationhack.Database.LocalDatabase;
import com.example.shishir.locationhack.ExtraClass.Network;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    Location currentLocation = null;
    FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;
    LocalDatabase localDatabase;
    Calendar calendar = Calendar.getInstance();
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    private static int R = 6371000;
    SimpleDateFormat dayFormatter = new SimpleDateFormat("dd MMM");
    SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");// the format of your date


    @Override
    public void onCreate() {
        toast("Started");
        localDatabase = new LocalDatabase(getApplicationContext());
        setUpGoogleAPIClient();
        mAuth = FirebaseAuth.getInstance();
        super.onCreate();
    }

    private void setUpGoogleAPIClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(2 * 60 * 1000);
        locationRequest.setFastestInterval(2 * 60 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        googleApiClient.connect();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUp();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//        if (currentLocation != null) {
//            sendLocation(currentLocation);
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void sendLocation(Location currentLocation) {
        double lat = currentLocation.getLatitude();
        double lng = currentLocation.getLongitude();
        if (Network.isNetAvailable(this)) {
            addLocationToFireBase(mAuth.getCurrentUser(), lat, lng);
        }

        // Here I have to send location to database. If net is on. Then i wll put it to fireBase otherWise i will send it to DatabaseHelper....
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        Toast.makeText(getApplicationContext(), "called ", Toast.LENGTH_SHORT).show();
        if (newLocation != null) {
            if (currentLocation != null) {
                double lat1 = currentLocation.getLatitude();
                double lng1 = currentLocation.getLongitude();
                double lat2 = newLocation.getLatitude();
                double lng2 = newLocation.getLongitude();
                double distance1 = meterDistanceBetweenPoints(lat1, lng1, lat2, lng2);
                double distance2 = distanceInMeter(lat1, lng1, lat2, lng2);
                if(Network.isNetAvailable(getApplicationContext())){
                    addLocationToFireBase(mAuth.getCurrentUser(),distance1,distance2);
                }
                Toast.makeText(getApplicationContext(), "Distance 1: " + distance1 + "\n" + "Distance 2: " + distance2, Toast.LENGTH_SHORT).show();

                currentLocation = newLocation;
            } else {
                currentLocation = newLocation;
            }
            //          sendLocation(currentLocation);
        }
    }

    private void requestLocationUp() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onDestroy() {
        toast("Destroyed");
        if (googleApiClient.isConnected()) {
            locationProviderApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onDestroy();
    }

    private void addLocationToFireBase(FirebaseUser user, Double lat, Double lng) {
        if (user != null) {
            String dayMonth = dayFormatter.format(new Date());
            String time = timeFormatter.format(new Date());
            databaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(dayMonth).child(time);
            Toast.makeText(this, dayMonth + "\n" + "Dis 1: " + lat + "\nDis 2: " + lng, Toast.LENGTH_LONG).show();
            //For Single Value Input.............
            // databaseReference.child('name').setValue(name).addOnCompletionListener.............................

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Dis 1", String.valueOf(lat));
            map.put("Dis 2", String.valueOf(lng));
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

    private void toast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
}

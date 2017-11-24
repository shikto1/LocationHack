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
    SimpleDateFormat dayFormatter = new SimpleDateFormat("dd MMM");
    SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");// the format of your date


    @Override
    public void onCreate() {
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
        locationRequest.setFastestInterval(3 * 60 * 1000);
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
        if (currentLocation != null) {
            sendLocation(currentLocation);
        }
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
        if (newLocation != null) {
            currentLocation = newLocation;
            sendLocation(currentLocation);
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
            Toast.makeText(this, dayMonth + "\n" + "Lat: " + lat + "\nLang: " + lng, Toast.LENGTH_LONG).show();
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


}

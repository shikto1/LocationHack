package com.example.shishir.locationhack;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.example.shishir.locationhack.Database.LocalDatabase;
import com.example.shishir.locationhack.Location.LatLongFinder;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button startStopButton;
    private ImageButton settingBtn;


    private LocationManager locationManager;
    private static boolean gpsIsEnabled = false;
    private static boolean receiverIsRegistered = false;
    private TextView serviceTv;
    private LocalDatabase localDatabase;
    public static final int REQUEST_LOCATION_CODE = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localDatabase = new LocalDatabase(this);
        setContentView(R.layout.activity_main);
        findViewById();

    }


    private void findViewById() {
        startStopButton = (Button) findViewById(R.id.startStopButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        serviceTv = (TextView) findViewById(R.id.service);

        if (isMyServiceRunning(LatLongFinder.class)) {
            // startStopButton.setBackgroundResource(R.drawable.circle_button_stop);
            startStopButton.setText("STOP");
            serviceTv.setVisibility(View.VISIBLE);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsIsEnabled = true;
        }

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals("START")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkLocationPermission();

                    } else {
                        startStopButton.setText("STOP");
                        // startStopButton.setBackgroundResource(R.drawable.circle_button_stop);
                        startMyService();
                        if (!gpsIsEnabled) {
                            showSettingsAlert();
                        }
                    }

                } else {
                    startStopButton.setText("START");
                    //  startStopButton.setBackgroundResource(R.drawable.circle_button_start);
                    stopMyService();
                }

            }
        });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;

        } else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        startStopButton.setText("STOP");
                        // startStopButton.setBackgroundResource(R.drawable.circle_button_stop);
                        startMyService();
                        if (!gpsIsEnabled) {
                            showSettingsAlert();
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void stopMyService() {
        stopService(new Intent(this, LatLongFinder.class));
        serviceTv.setVisibility(View.INVISIBLE);
    }


    private void startMyService() {
        startService(new Intent(this, LatLongFinder.class));
        serviceTv.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = alertDialog.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        dialog.show();
    }


    @Override
    protected void onStart() {
        if (!receiverIsRegistered) {
            registerReceiver(GpsTracker, new IntentFilter("android.location.PROVIDERS_CHANGED"));
            receiverIsRegistered = true;
        }
        super.onStart();
    }


    //Broadcast Receiver for checking weather GPS is turned On or Off..............................................................

    BroadcastReceiver GpsTracker = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsIsEnabled = true;
            } else {
                gpsIsEnabled = false;
            }

        }
    };


    @Override
    protected void onPause() {
        if (receiverIsRegistered) {
            unregisterReceiver(GpsTracker);
            receiverIsRegistered = false;
        }
        super.onPause();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
